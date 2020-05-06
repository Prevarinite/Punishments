package com.prevarinite.punishments;

import com.prevarinite.punishments.commands.*;
import com.prevarinite.punishments.database.DatabaseWrapper;
import com.prevarinite.punishments.listeners.PlayerListeners;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Punishments extends JavaPlugin {

    private @Getter DatabaseWrapper database;
    private @Getter YamlConfiguration messagesConfig;

    @Override
    public void onEnable() {
        try {
            database = new DatabaseWrapper(this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        saveResource("messages.yml", false);
        messagesConfig = new YamlConfiguration();
        try {
            messagesConfig.load(new File("plugins/BardyPunishments/messages.yml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        getDataFolder().mkdir();
        saveDefaultConfig();
        getCommand("ban").setExecutor(new BanCommand(this));
        getCommand("kick").setExecutor(new KickCommand(this));
        getCommand("unban").setExecutor(new UnbanCommand(this));
        getCommand("mute").setExecutor(new MuteCommand(this));
        getCommand("unmute").setExecutor(new UnmuteCommand(this));
        getServer().getPluginManager().registerEvents(new PlayerListeners(this), this);
    }
}
