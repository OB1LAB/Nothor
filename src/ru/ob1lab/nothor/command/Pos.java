package ru.ob1lab.nothor.command;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import ru.ob1lab.nothor.Main;
import ru.ob1lab.nothor.Message;
import ru.ob1lab.nothor.areas.LocationItem;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class Pos {
    private static final Map<String, Map<String, Location>> positionsSet = Main.getPositionsSet();
    private static final int limitZone = Main.getPluginConfig().getInt("settings.limitZone", 10000);
    private static final Map<String, Boolean> isZoneShow = new HashMap<>();
    public static void setPos(Player player, String posNumber) {
        Block blockLooked = player.getTargetBlock(null, 25);
        if (blockLooked.getType() != Material.AIR) {
            if (!positionsSet.containsKey(player.getDisplayName())) {
                positionsSet.put(player.getDisplayName(), new HashMap<>());
            }
            Map<String, Location> posSet = positionsSet.get(player.getDisplayName());
            if (isZoneShow.containsKey(player.getDisplayName()) && isZoneShow.get(player.getDisplayName())) {
                asyncHidePerimeter(player,  getPerimeter(posSet.get("pos1"), posSet.get("pos2")));
                isZoneShow.put(player.getDisplayName(), false);
            }
            posSet.put(posNumber, blockLooked.getLocation());
            Message.selectPos.replace("{number}", posNumber).send(player);
            String oppositePosNumber;
            if (Objects.equals(posNumber, "pos1")) {
                oppositePosNumber = "pos2";
            } else {
                oppositePosNumber = "pos1";
            }
            if (posSet.containsKey(oppositePosNumber)) {
                if (posSet.get(posNumber).getWorld() != posSet.get(oppositePosNumber).getWorld()) {
                    Message.differentWorlds.send(player);
                    return;
                }
                LocationItem perimeter = getPerimeter(posSet.get(posNumber), posSet.get(oppositePosNumber));
                int countBlocks = (perimeter.x2 - perimeter.x1 + 1) * (perimeter.y2 - perimeter.y1 + 1) * (perimeter.z2 - perimeter.z1 + 1);
                Message.countSelectedPos.replace("{count}", String.valueOf(countBlocks)).send(player);
                if (countBlocks > limitZone) {
                    Message.limitOverflow.replace("{limit}", String.valueOf(limitZone)).send(player);
                }
            }
        } else {
            Message.posNotFound.send(player);
        }
    }
    public static void actionSelectPos(Player player, String action) {
        if (!positionsSet.containsKey(player.getDisplayName()) ||
                !positionsSet.get(player.getDisplayName()).containsKey("pos1") ||
                !positionsSet.get(player.getDisplayName()).containsKey("pos2")) {
            Message.mustTwoPos.send(player);
            return;
        }
        if (!isZoneShow.containsKey(player.getDisplayName())) {
            isZoneShow.put(player.getDisplayName(), false);
        }
        Location pos1 = positionsSet.get(player.getDisplayName()).get("pos1");
        Location pos2 = positionsSet.get(player.getDisplayName()).get("pos2");
        LocationItem perimeter = getPerimeter(pos1, pos2);
        int countBlocks = (perimeter.x2 - perimeter.x1 + 1) * (perimeter.y2 - perimeter.y1 + 1) * (perimeter.z2 - perimeter.z1 + 1);
        if (countBlocks > limitZone) {
            Message.limitOverflow.replace("{limit}", String.valueOf(limitZone)).send(player);
            return;
        }
        if (Objects.equals(action, "show")) {
            if (isZoneShow.get(player.getDisplayName())) {
                Message.isZoneIsShow.send(player);
                return;
            }
            asyncShowPerimeter(player, perimeter);
            Message.zoneShow.send(player);
            isZoneShow.put(player.getDisplayName(), true);
        } else {
            if (!isZoneShow.get(player.getDisplayName())) {
                Message.isZoneIsHide.send(player);
                return;
            }
            asyncHidePerimeter(player, perimeter);
            Message.zoneHide.send(player);
            isZoneShow.put(player.getDisplayName(), false);
        }
    }
    public static void setPerimeter(Player player, LocationItem perimeter, Consumer<Location> function) {
        World world = player.getWorld();
        for (int x = perimeter.x1; x <= perimeter.x2; x++) {
            for (int y = perimeter.y1; y <= perimeter.y2; y++) {
                for (int z = perimeter.z1; z <= perimeter.z2; z++) {
                    if (x == perimeter.x1 || x == perimeter.x2 || y == perimeter.y1 ||
                            y == perimeter.y2 || z == perimeter.z1 || z == perimeter.z2) {
                        Location loc = new Location(world, x, y, z);
                        function.accept(loc);
                    }
                }
            }
        }
    }
    public static void setZone(LocationItem perimeter, Material material) {
        World world = Bukkit.getServer().getWorld(perimeter.world);
        for (int x = perimeter.x1; x <= perimeter.x2; x++) {
            for (int y = perimeter.y1; y <= perimeter.y2; y++) {
                for (int z = perimeter.z1; z <= perimeter.z2; z++) {
                    if (x == perimeter.x1 || x == perimeter.x2 || y == perimeter.y1 ||
                            y == perimeter.y2 || z == perimeter.z1 || z == perimeter.z2) {
                        Location loc = new Location(world, x, y, z);
                        Block block = world.getBlockAt(loc);
                        block.setType(material);
                    }
                }
            }
        }
    }
    public static LocationItem getPerimeter(Location pos1, Location pos2) {
        int x1 = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int y1 = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int z1 = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int x2 = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int y2 = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int z2 = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        return new LocationItem(pos1.getWorld().getName(), x1, y1, z1, x2, y2, z2);
    }
    public static void asyncShowPerimeter(Player player, LocationItem perimeter) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            setPerimeter(player, perimeter, (Location loc) -> {
                player.sendBlockChange(loc, Material.GLOWSTONE, (byte) 0);
            });
        });
    }
    public static void asyncHidePerimeter(Player player, LocationItem perimeter) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> setPerimeter(player, perimeter, (Location loc) -> {
            player.sendBlockChange(loc, loc.getBlock().getType(), loc.getBlock().getData());
        }));
    }
}
