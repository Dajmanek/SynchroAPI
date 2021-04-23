package net.dajman.synchro.basic;


import java.io.IOException;

public interface Synchro {

    void closeConnection();
    void sendPacket(final String toServerName, final Packet packet) throws IOException;
    void cancel();

}
