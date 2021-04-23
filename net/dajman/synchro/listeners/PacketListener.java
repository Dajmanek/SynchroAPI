package net.dajman.synchro.listeners;

import net.dajman.synchro.SynchroAPI;
import net.dajman.synchro.basic.PacketHandler;
import net.dajman.synchro.basic.SynchroListener;
import net.dajman.synchro.packets.StatusPingPacket;
import net.dajman.synchro.packets.StatusPongPacket;

public class PacketListener implements SynchroListener {

    private final SynchroAPI plugin;

    public PacketListener(final SynchroAPI plugin){
        this.plugin = plugin;
    }

    @PacketHandler
    public void handle(final StatusPingPacket packet){
        if (!SynchroAPI.isServerReady()){
            return;
        }
        SynchroAPI.sendPacket(packet.getFrom(), new StatusPongPacket());
    }

    @PacketHandler
    public void handle(final StatusPongPacket packet){
        if (this.plugin.getStatusRunnable().isConnected()){
            return;
        }
        this.plugin.getStatusRunnable().connect();
        this.plugin.getListenerManager().callConnectEvent();
    }

    @Override
    public void onDisconnect() {
        this.plugin.getStatusRunnable().disconnect();
    }
}
