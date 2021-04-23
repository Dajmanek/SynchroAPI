package net.dajman.synchro.basic;


import java.io.*;

public abstract class Packet {

    private String fromServerName;

    public String getFrom(){
        return this.fromServerName;
    }

    public Packet setFrom(final String serverName){
        this.fromServerName = serverName;
        return this;
    }


    public abstract int getId();

    public abstract void write(DataOutputStream outputStream) throws IOException;

    public abstract void read(DataInputStream inputStream) throws IOException;

}
