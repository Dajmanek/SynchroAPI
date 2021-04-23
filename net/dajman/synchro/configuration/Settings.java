package net.dajman.synchro.configuration;

import net.dajman.synchro.SynchroAPI;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Optional;

public class Settings {

    private SynchroAPI plugin;

    public Settings(final SynchroAPI plugin){
        this.plugin = plugin;
    }

    public boolean server;
    public String hostname;
    public int port;
    public String serverName;


    public Settings loadConfiguration(){
        final FileConfiguration configuration = this.plugin.getConfig();
        this.server = configuration.getBoolean("SERVER");
        this.hostname = configuration.getString("HOSTNAME");
        this.port = configuration.getInt("PORT");
        this.serverName = Optional.ofNullable(configuration.getString("SERVER_NAME")).orElse("");
        if (this.server && this.serverName.equals("")){
            this.serverName = "MAIN";
        }
        return this;
    }

}
