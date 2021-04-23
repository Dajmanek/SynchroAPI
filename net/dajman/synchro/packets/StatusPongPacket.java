package net.dajman.synchro.packets;

import net.dajman.synchro.basic.Packet;

import java.io.*;

public class StatusPongPacket extends Packet {


    public StatusPongPacket() {
    }

    @Override
    public int getId() {
        return 1;
    }

    @Override
    public void write(DataOutputStream outputStream) throws IOException {
    }

    @Override
    public void read(DataInputStream inputStream) throws IOException {
    }

}
