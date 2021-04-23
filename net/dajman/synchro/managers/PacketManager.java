package net.dajman.synchro.managers;

import net.dajman.synchro.SynchroAPI;
import net.dajman.synchro.basic.Packet;

import java.util.HashMap;
import java.util.Map;

public class PacketManager {

    private final SynchroAPI plugin;
    private Map<Integer, Class<? extends Packet>> packets;

    public PacketManager(final SynchroAPI plugin){
        this.plugin = plugin;
        this.packets = new HashMap<>();
    }

    public boolean addPacket(final int id, final Class<? extends Packet> packetClass){
        if (this.packets.containsKey(id)){
            return false;
        }
        this.packets.put(id, packetClass);
        return true;
    }

    public Packet getPacket(final int id){
        if (this.packets.containsKey(id)){
            try{
                return this.packets.get(id).newInstance();
            }catch (IllegalAccessException | InstantiationException e){
                e.printStackTrace();
            }
        }
        return null;
    }

}
