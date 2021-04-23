package net.dajman.synchro.runnable;

import net.dajman.synchro.SynchroAPI;
import net.dajman.synchro.packets.StatusPingPacket;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class StatusRunnable implements Runnable{

    private final SynchroAPI plugin;
    private BukkitTask bukkitTask;
    private boolean connected = false;

    public StatusRunnable(final SynchroAPI plugin){
        this.plugin = plugin;
    }

    public StatusRunnable start(){
        this.bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, this, 0L, 20L);
        return this;
    }

    public void connect(){
        this.connected = true;
        if (this.bukkitTask == null){
            return;
        }
        this.bukkitTask.cancel();
    }

    public void cancel(){
        if (this.bukkitTask == null){
            return;
        }
        this.bukkitTask.cancel();
        this.bukkitTask = null;
    }

    public void disconnect() {
        this.connected = false;
    }

    public boolean isConnected() {
        return connected;
    }

    @Override
    public void run() {
        if (this.bukkitTask == null){
            return;
        }
        SynchroAPI.sendPacket("MAIN", new StatusPingPacket());
    }


}
