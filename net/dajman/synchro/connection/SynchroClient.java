package net.dajman.synchro.connection;
import net.dajman.synchro.SynchroAPI;
import net.dajman.synchro.basic.Packet;
import net.dajman.synchro.basic.Synchro;
import net.dajman.synchro.utils.Util;
import org.bukkit.Bukkit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SynchroClient extends Thread implements Synchro {

    private static SynchroAPI plugin;

    private Socket socket;
    private DataOutputStream outputStream;
    private final transient Object locker = new Object();
    private boolean cancel = false;

    public void cancel(){
        this.cancel = true;
    }

    public SynchroClient(final SynchroAPI plugin){
        SynchroClient.plugin = plugin;
        this.start();
    }

    private boolean isConnected(){
        return this.socket != null && !this.socket.isClosed();
    }

    private void openConnection(){
        if (this.isConnected()){
            return;
        }
        boolean failMessage = true;
        while(!this.cancel){
            try{
                this.socket = new Socket(SynchroClient.plugin.getSettings().hostname, SynchroClient.plugin.getSettings().port);
                Util.info("Succesfull connected to " + SynchroClient.plugin.getSettings().hostname + ":" + SynchroClient.plugin.getSettings().port);
                break;
            }catch (IOException e){
                if (failMessage){
                    Util.warning("Failed to connect to " + SynchroClient.plugin.getSettings().hostname + ":" + SynchroClient.plugin.getSettings().port + ", try to connect again...");
                    failMessage = false;
                }
            }
            try{
                sleep(2000L);
            }catch (InterruptedException e){
                e.printStackTrace();
                return;
            }
        }
    }

    @Override
    public void  run() {
        while(true){
            openConnection();
            try{
                final DataInputStream inputStream = new DataInputStream(this.socket.getInputStream());

                synchronized (this.locker){
                    this.outputStream = new DataOutputStream(this.socket.getOutputStream());
                    this.outputStream.writeUTF(SynchroClient.plugin.getSettings().serverName); // REQUEST SETTING NAME
                    final String responseName = inputStream.readUTF(); // RESPONSE OF NAME
                    if (!SynchroClient.plugin.getSettings().serverName.equals(responseName)){
                        Util.info("The server name was assigned automatically - \"" + responseName + "\"");
                    }
                    else{
                        Util.info("Successfully assigned server name - \"" + responseName + "\"");
                    }
                    SynchroClient.plugin.getSettings().serverName = responseName;

                }
                Bukkit.getScheduler().runTask(SynchroClient.plugin, () -> SynchroClient.plugin.getStatusRunnable().start());
                while(this.socket.isConnected() && !this.cancel){
                    final int packetId = inputStream.read();
                    if (packetId == -1){
                        break;
                    }
                    final String fromServerName = inputStream.readUTF();
                    final Packet packet = SynchroClient.plugin.getPacketManager().getPacket(packetId);
                    if (packet == null){
                        continue;
                    }
                    packet.setFrom(fromServerName);
                    packet.read(inputStream);
                    SynchroClient.plugin.getListenerManager().receive(packet);
                    continue;
                }
            }catch (IOException e){
                //e.printStackTrace();
            }
            finally {
                Util.info("Disconnected from the server.");
                SynchroClient.plugin.getListenerManager().callDisconnectEvent();
                this.closeConnection();
                try{
                    sleep(2000L);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                if (this.cancel){
                    return;
                }
            }
        }
    }

    @Override
    public void closeConnection() {
        try{
           if (this.socket != null && !this.socket.isClosed()){
               this.outputStream.close();
               this.socket.close();
           }
       }catch (IOException e){
            e.printStackTrace();
       }
    }

    @Override
    public void sendPacket(final String toServerName, final Packet packet) throws IOException{
        synchronized (this.locker){
            this.outputStream.write(packet.getId());
            this.outputStream.writeUTF(toServerName);
            packet.write(this.outputStream);
            this.outputStream.flush();
        }
    }
}
