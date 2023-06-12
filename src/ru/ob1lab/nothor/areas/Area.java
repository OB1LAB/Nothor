package ru.ob1lab.nothor.areas;

import com.google.common.collect.Lists;
import net.minecraft.util.com.google.gson.Gson;
import org.bukkit.configuration.file.FileConfiguration;
import ru.ob1lab.nothor.Main;
import ru.ob1lab.nothor.Storage;

import java.util.*;

public class Area {
    private static final Gson gson = new Gson();
    private static final Storage data = Main.getData();
    private static final FileConfiguration config = data.getConfig();
    public static void createGameArea(String gameName) {
        String path = "areas." + gameName;
        if (!config.contains(path)) {
            config.set(path + ".spawnTp", gson.toJson(new TeleportationItem("None", 0, 0, 0, 0, 0), TeleportationItem.class));
            config.set(path + ".gameArenaTp", gson.toJson(new TeleportationItem("None", 0, 0, 0, 0, 0), TeleportationItem.class));
            config.set(path + ".floorsLocation", gson.toJson(Lists.newArrayList()));
        }
        data.save();
    }
    public static void removeGameArea(String gameName) {
        String path = "areas." + gameName;
        if (config.contains(path)) {
            config.set(path, null);
        }
        data.save();
    }
    public static void setGameArenaTeleport(String gameName, String areaName, String world, int x, int y, int z, float yaw, float pitch) {
        String path = "areas." + gameName;
        TeleportationItem teleport = new TeleportationItem(world, x, y, z, yaw, pitch);
        config.set(path + "." + areaName, gson.toJson(teleport, TeleportationItem.class));
        data.save();
    }
    public static boolean setGameArenaLocation(String gameName, LocationItem perimeter) {
        String path = "areas." + gameName;
        List<String> floors = config.getStringList(path + ".floorsLocation");
        String locationJson = gson.toJson(perimeter, LocationItem.class);
        if (floors.contains(locationJson)) {
            return false;
        }
        if (floors.size() >= 7) {
            return false;
        }
        floors.add(locationJson);
        floors.sort(valueComparator);
        config.set(path + ".floorsLocation", floors);
        data.save();
        return true;
    }
    public static void removeGameArenaLocation(String gameName, int index) {
        String path = "areas." + gameName;
        List<String> floors = config.getStringList(path + ".floorsLocation");
        floors.remove(index);
        config.set(path + ".floorsLocation", floors);
        data.save();
    }
    public static Map<String, Object> get() {
        Map<String, Object> gamesData = new HashMap<>();
        Set<String> gamesNames = config.getConfigurationSection("areas").getKeys(false);
        for (String gameName: gamesNames) {
            String path = "areas." + gameName;
            Map<String, Object> gameData = new HashMap<>();
            gameData.put("spawnTp", gson.fromJson(config.getString(path + ".spawnTp"), TeleportationItem.class));
            gameData.put("gameArenaTp", gson.fromJson(config.getString(path + ".gameArenaTp"), TeleportationItem.class));
            List<LocationItem> floors = Lists.newArrayList();
            List<String> floorsString = config.getStringList(path + ".floorsLocation");
            for (String floor: floorsString) {
                floors.add(gson.fromJson(floor, LocationItem.class));
            }
            gameData.put("floorsLocation", floors);
            gamesData.put(gameName, gameData);
        }
        return gamesData;
    }
    static Comparator<String> valueComparator = (value1, value2) -> {
        int y1 = gson.fromJson(value1, LocationItem.class).y1;
        int y2 = gson.fromJson(value2, LocationItem.class).y2;
        return Integer.compare(y1, y2);
    };
}
