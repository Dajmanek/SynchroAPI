package net.dajman.synchro.managers;

import net.dajman.synchro.SynchroAPI;
import net.dajman.synchro.basic.Packet;
import net.dajman.synchro.basic.PacketHandler;
import net.dajman.synchro.basic.SynchroListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListenerManager {

    private SynchroAPI plugin;
    private List<SynchroListener> listeners;

    public ListenerManager(final SynchroAPI plugin){
        this.plugin = plugin;
        this.listeners = Collections.synchronizedList(new ArrayList<>());
    }

    public boolean addListener(final SynchroListener listener){
        if (this.listeners.contains(listener)){
            return false;
        }
        this.listeners.add(listener);
        return true;
    }

    private List<SynchroListener> getListeners(){
        return new ArrayList<>(listeners);
    }

    public void receive(final Packet packet){
        for (SynchroListener listener : this.getListeners()) {
            for (Method method : listener.getClass().getDeclaredMethods()) {
                try{
                    method.setAccessible(true);
                    if (method.getParameterTypes().length != 1){
                        continue;
                    }
                    if (method.getAnnotationsByType(PacketHandler.class).length == 0){
                        continue;
                    }
                    if (method.isSynthetic()){
                        continue;
                    }
                    if (!method.getParameterTypes()[0].equals(packet.getClass())){
                        continue;
                    }
                    method.invoke(listener, packet);
                }catch (IllegalAccessException |  InvocationTargetException e){
                    e.printStackTrace();
                }

            }
        }
    }

    public void callDisconnectEvent(){
        for (SynchroListener listener : this.getListeners()) {
            listener.onDisconnect();
        }
    }

    public void callConnectEvent(){
        for (SynchroListener listener : this.getListeners()) {
            listener.onConnect();
        }
    }



}
