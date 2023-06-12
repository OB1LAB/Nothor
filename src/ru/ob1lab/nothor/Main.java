package ru.ob1lab.nothor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import ru.ob1lab.nothor.command.GameData;
import ru.ob1lab.nothor.command.MainCommand;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ob1lab.nothor.gui.GUI;

import java.util.HashMap;
import java.util.Map;

public class Main extends JavaPlugin {
    private static Main instance;
    private Map<String, Map<String, Location>> positionsSet;
    private FileConfiguration config;
    private Storage dataAreas;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        instance = this;
        config = getConfig();
        Message.load(config);
        dataAreas = new Storage("areas.yml");
        positionsSet = new HashMap<>();
        new MainCommand();
        Bukkit.getPluginManager().registerEvents(new GUI(), this);
        Bukkit.getPluginManager().registerEvents(new GameData(), this);
    }
    public static Main getInstance() {
        return instance;
    }
    public static Map<String, Map<String, Location>> getPositionsSet() {
        return instance.positionsSet;
    }
    public static Storage getData() {
        return instance.dataAreas;
    }
    public static FileConfiguration getPluginConfig() {
        return instance.config;
    }
}
