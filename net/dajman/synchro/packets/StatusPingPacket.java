package net.dajman.synchro.packets;

import net.dajman.synchro.basic.Packet;

import java.io.*;

public class StatusPingPacket extends Packet {


    public StatusPingPacket() {
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public void write(DataOutputStream outputStream) throws IOException {
    }

    @Override
    public void read(DataInputStream inputStream) throws IOException {
    }

}
