package net.dajman.synchro.connection;

import net.dajman.synchro.SynchroAPI;
import net.dajman.synchro.basic.Packet;
import net.dajman.synchro.basic.Synchro;
import net.dajman.synchro.utils.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SynchroServer extends Thread implements Synchro {

    private final SynchroAPI plugin;
    private ServerSocket serverSocket;
    private Set<SynchroServer.Client> users;
    private int freeId = 0;
    private boolean cancel = false;

    public SynchroServer(final SynchroAPI plugin){
        this.plugin = plugin;
        this.users = Collections.synchronizedSet(new HashSet<>());
        SynchroServer.Client.setSynchroServer(this);
        this.start();
    }

    public SynchroAPI getPlugin() {
        return plugin;
    }

    public int getFreeId(){
        return freeId++;
    }

    public String getFreeName(){
        String name;
        while(this.getClient((name = "SERVER_" + this.getFreeId())) != null){
        }
        return name;
    }

    public void cancel(){
        this.cancel = true;
    }

    public Client getClient(final String serverName){
        for (Client user : this.users) {
            if (user.getServerName() != null && user.getServerName().equalsIgnoreCase(serverName)){
                return user;
            }
        }
        return null;
    }

    public void removeUser(final Client client){
        this.users.remove(client);
    }

    @Override
    public void run() {
        try{
            this.serverSocket = new ServerSocket(this.plugin.getSettings().port);
            Util.info("Synchronization server successfully started on port: " + this.plugin.getSettings().port);
            while(!this.serverSocket.isClosed() && !this.cancel){
                final Socket socket = this.serverSocket.accept();
                final int id = this.getFreeId();
                Util.info("(ID: #" + id + ") Successfully connected new client");
                this.users.add(new SynchroServer.Client(id, socket));
            }
        }catch (IOException e){
            //e.printStackTrace();
        }
        finally {
            for (Client user : this.users) {
                user.closeConnection();
            }
            this.users.clear();
            if (!cancel){
                try {
                    sleep(2000L);
                } catch (InterruptedException ee) {
                    ee.printStackTrace();
                }
                run();
            }
        }
    }

    @Override
    public void closeConnection() {
        try{
            this.serverSocket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void sendPacket(final String toServerName, final Packet packet) throws IOException{
        for (Client client : this.users) {
            if (!toServerName.equalsIgnoreCase("ALL") && !toServerName.equalsIgnoreCase(client.getServerName())){
                continue;
            }
            client.sendPacket(packet);
        }
    }

    public void sendPacket(final int exceptId, final Packet packet) throws IOException{
        for (Client client : this.users) {
            if (client.getClientId() == exceptId){
                continue;
            }
            client.sendPacket(packet);
        }
    }



    // Client class
    private static class Client extends Thread{

        private static SynchroServer synchroServer;

        private int cliendId;
        private String serverName;
        private Socket socket;
        private DataOutputStream outputStream;
        private final Object locker = new Object();

        public static void setSynchroServer(SynchroServer synchroServer) {
            Client.synchroServer = synchroServer;
        }

        public Client(final int cliendId, final Socket socket){
            this.cliendId = cliendId;
            this.socket = socket;
            this.start();
        }

        public int getClientId() {
            return cliendId;
        }

        public String getServerName() {
            return serverName;
        }

        public void closeConnection(){
            try{
                this.outputStream.close();
                if (this.socket != null && this.socket.isConnected()) {
                    this.socket.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
            Client.synchroServer.removeUser(this);
        }

        @Override
        public void run() {
            try{
                final DataInputStream input = new DataInputStream(this.socket.getInputStream());
                synchronized (this.locker){
                    this.outputStream = new DataOutputStream(this.socket.getOutputStream());
                    final String nameRequest = input.readUTF();

                    if (nameRequest.equals("") || nameRequest.equalsIgnoreCase("ALL") || nameRequest.equalsIgnoreCase("MAIN") || nameRequest.equalsIgnoreCase(Client.synchroServer.plugin.getSettings().serverName) || Client.synchroServer.getClient(nameRequest) != null){
                        this.serverName = Client.synchroServer.getFreeName();
                        this.outputStream.writeUTF(this.serverName);
                        Util.info("(ID: #" + this.cliendId + ") The client name was assigned automatically - \"" + this.serverName + "\"");
                    }else{
                        this.serverName = nameRequest;
                        this.outputStream.writeUTF(nameRequest);
                        Util.info("(ID: #" + this.cliendId + ") Successfully assigned client name " + "- \"" + this.serverName + "\"");
                    }
                }
                while(true){
                    final int packetId = input.read();
                    if (packetId == -1){
                        break;
                    }
                    final String toServerName = input.readUTF();
                    final SynchroAPI plugin = Client.synchroServer.getPlugin();
                    final Packet packet = plugin.getPacketManager().getPacket(packetId);
                    if (packet == null){
                        continue;
                    }
                    packet.setFrom(this.serverName);
                    packet.read(input);
                    // BROADCAST
                    if (toServerName.equals("ALL")){
                        Client.synchroServer.sendPacket(this.cliendId, packet);
                        plugin.getListenerManager().receive(packet);
                        continue;
                    }
                    // SEND TO ME
                    if (toServerName.equals(Client.synchroServer.plugin.getSettings().serverName) || toServerName.equalsIgnoreCase("MAIN")){
                        plugin.getListenerManager().receive(packet);
                        continue;
                    }
                    // SEND TO OTHER
                    final Client client = Client.synchroServer.getClient(toServerName);
                    if (client == null){
                        continue;
                    }
                    try{
                        client.sendPacket(packet);
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    continue;
                }
            }catch (IOException e){
                //e.printStackTrace();
            }
            finally {
               closeConnection();
               Util.info("(ID: #" + this.cliendId + ") The client disconnected -\"" + this.serverName + "\"");
            }
        }

        public void sendPacket(final Packet packet) throws IOException{
            synchronized (this.locker){
                this.outputStream.write(packet.getId());
                this.outputStream.writeUTF(packet.getFrom());
                packet.write(this.outputStream);
                this.outputStream.flush();
            }
        }
    }

}
