package net.dajman.synchro;

import net.dajman.synchro.basic.Packet;
import net.dajman.synchro.basic.Synchro;
import net.dajman.synchro.basic.SynchroListener;
import net.dajman.synchro.configuration.Settings;
import net.dajman.synchro.connection.SynchroClient;
import net.dajman.synchro.connection.SynchroServer;
import net.dajman.synchro.listeners.PacketListener;
import net.dajman.synchro.managers.ListenerManager;
import net.dajman.synchro.managers.PacketManager;
import net.dajman.synchro.packets.StatusPingPacket;
import net.dajman.synchro.packets.StatusPongPacket;
import net.dajman.synchro.runnable.StatusRunnable;
import net.dajman.synchro.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;

public class SynchroAPI extends JavaPlugin {


    private static transient SynchroAPI plugin;
    private transient ListenerManager listenerManager;
    private transient PacketManager packetManager;
    private transient Synchro synchro;
    private transient Settings settings;
    private transient StatusRunnable statusRunnable;
    private static transient boolean serverReady = false;


    public ListenerManager getListenerManager() {
        return listenerManager;
    }

    public PacketManager getPacketManager() {
        return packetManager;
    }

    public Settings getSettings() {
        return settings;
    }

    public StatusRunnable getStatusRunnable() {
        return statusRunnable;
    }

    public static boolean isServerReady() {
        return serverReady;
    }

    @Override
    public void onEnable() {
        SynchroAPI.plugin = this;
        this.saveDefaultConfig();
        this.settings = new Settings(this).loadConfiguration();
        this.listenerManager = new ListenerManager(this);
        this.packetManager = new PacketManager(this);
        this.statusRunnable = new StatusRunnable(this);
        SynchroAPI.registerPacket(this, new StatusPingPacket());
        SynchroAPI.registerPacket(this, new StatusPongPacket());
        SynchroAPI.registerListener(this, new PacketListener(this));
        this.synchro = this.settings.server ? new SynchroServer(this) : new SynchroClient(this);
        Bukkit.getScheduler().runTask(this, () -> SynchroAPI.serverReady = true);
    }


    @Override
    public void onDisable() {
        this.synchro.cancel();
        this.synchro.closeConnection();
        this.statusRunnable.cancel();
    }

    public static boolean isServer(){
        return SynchroAPI.plugin.settings.server;
    }

    public static boolean registerPacket(final Plugin plugin, final Packet packet){
        if (packet.getId() < 0){
            Util.error("The Packet id must be greater than or equal to 0");
            return false;
        }
        if (!SynchroAPI.plugin.packetManager.addPacket(packet.getId(), packet.getClass())){
            Util.error("The packet with the given id already exists. (" + plugin.getName() + ")");
            return false;
        }
        return true;
    }

    public static boolean registerListener(final Plugin plugin, final SynchroListener listener){
        if (!SynchroAPI.plugin.listenerManager.addListener(listener)){
            Util.error("The listener \"" + listener.getClass().getName() + " is already registered (" + plugin.getName() + ")");
            return false;
        }
        return true;
    }


    public static void sendPacket1(final String toServerName, final Packet packet) throws IOException {
        packet.setFrom(SynchroAPI.plugin.settings.serverName);
        SynchroAPI.plugin.synchro.sendPacket(toServerName, packet);
    }

    public static void sendBroadcastPacket1(final Packet packet) throws IOException{
        packet.setFrom(SynchroAPI.plugin.settings.serverName);
        SynchroAPI.plugin.synchro.sendPacket("ALL", packet);
    }

    public static void sendBroadcastPacket(final Packet packet){
        try{
            SynchroAPI.sendBroadcastPacket1(packet);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void sendPacket(final String toServerName, final Packet packet){
        try{
            SynchroAPI.sendPacket1(toServerName, packet);
        }catch (IOException e){
            e.printStackTrace();
        }
    }


}
