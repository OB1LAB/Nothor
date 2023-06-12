package ru.ob1lab.nothor.gui;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.ob1lab.nothor.Main;
import ru.ob1lab.nothor.Message;
import ru.ob1lab.nothor.areas.Area;
import ru.ob1lab.nothor.areas.LocationItem;
import ru.ob1lab.nothor.areas.TeleportationItem;
import ru.ob1lab.nothor.command.GameData;
import ru.ob1lab.nothor.command.Pos;

import java.util.*;

public class GUI implements Listener {
    private final List<String> inventoryNames = Arrays.asList("Выбор игры", "Настройка игры");
    private static final String permissionName = Main.getPluginConfig().getString("settings.permission", "OP");
    private static final Boolean sendGlobalMsgOnStartRegister = Main.getPluginConfig().getBoolean("settings.sendGlobalMessageOnStartRegister", false);
    private static Map<String, Object> games = Area.get();
    private static Set<String> gameNames = games.keySet();
    private static final Map<String, Map<String, Map<Integer, Boolean>>> showFloors = new HashMap<>();
    public static void openSelectGame(Player player) {
        updateGamesData();
        Inventory inv = Bukkit.createInventory(null, 27, "Выбор игры");
        int offset = 0;
        for (String gameName: gameNames) {
            Map<String, Object> game = (Map<String, Object>) games.get(gameName);
            TeleportationItem spawnTp = (TeleportationItem) game.get("spawnTp");
            TeleportationItem gameArenaTp = (TeleportationItem) game.get("gameArenaTp");
            List<LocationItem> floors = (List<LocationItem>) game.get("floorsLocation");
            if (!GameData.isGameRun && !GameData.isGameRegister && floors.size() > 0
                    && !Objects.equals(spawnTp.world, "None")  && !Objects.equals(gameArenaTp.world, "None")) {
                inv.setItem(offset, ItemUtil.createHead("&2Начать регистрацию на игру", gameName, "startRegister"));
            } else if (Objects.equals(gameName, GameData.gameRunName)) {
                inv.setItem(offset, ItemUtil.create("&4Заверишь игру",  Material.WOOL, DyeColor.RED.getData(), gameName, "stopGame"));
            }
            inv.setItem(offset + 9, ItemUtil.create("&5" + gameName, Material.GOLD_BLOCK));
            if (GameData.isGameRegister && (Objects.equals(gameName, GameData.gameRunName))) {
                inv.setItem(offset+18, ItemUtil.create("&2Начать игру",  Material.WOOL, DyeColor.LIME.getData(), gameName, "startGame"));
            }
            offset++;
        }
        inv.setItem(26, ItemUtil.create("&4Выход", Material.WOOD_DOOR));
        player.openInventory(inv);
    }
    public static void openGame(Player player, String gameName) {
        updateGamesData();
        String playerName = player.getDisplayName();
        if (!showFloors.containsKey(playerName)) {
            showFloors.put(playerName, new HashMap<>());
        }
        if (!showFloors.get(playerName).containsKey(gameName)) {
            showFloors.get(playerName).put(gameName, new HashMap<>());
        }
        Map<Integer, Boolean> playerFloors = showFloors.get(playerName).get(gameName);
        Inventory inv = Bukkit.createInventory(null, 27, "Настройка игры");
        Map<String, Object> game = (Map<String, Object>) games.get(gameName);
        List<LocationItem> floors = (List<LocationItem>) game.get("floorsLocation");
        int offset = 0;
        for (LocationItem ignored : floors) {
            inv.setItem(offset, ItemUtil.create("&2Телепорт", Material.COMPASS, gameName, "TeleportArena", String.valueOf(offset)));
            if (!playerFloors.containsKey(offset)) {
                playerFloors.put(offset, false);
            }
            if (!playerFloors.get(offset)) {
                inv.setItem(offset + 9, ItemUtil.create("&2Показать " + (offset + 1) + " слой", Material.WOOL, DyeColor.LIME.getData(), gameName, "showFloor", String.valueOf(offset)));
            } else {
                inv.setItem(offset + 9, ItemUtil.create("&4Скрыть " + (offset + 1) + " слой", Material.WOOL, DyeColor.RED.getData(), gameName, "hideFloor", String.valueOf(offset)));
            }
            inv.setItem(offset + 18, ItemUtil.create("&4Удалить", Material.SHEARS, gameName, "removeFloor", String.valueOf(offset)));
            offset++;
        }
        if (offset < 7) {
            inv.setItem(offset + 9, ItemUtil.create("&2Установить арену", Material.ENDER_PORTAL, gameName, "setFloor"));
        }
        TeleportationItem spawnTp = (TeleportationItem) game.get("spawnTp");
        TeleportationItem gameArenaTp = (TeleportationItem) game.get("gameArenaTp");
        if (!Objects.equals(spawnTp.world, "None")) {
            inv.setItem(7, ItemUtil.create("&5Телепорт на спавн", Material.COMPASS, gameName, "spawnTp"));
        }
        if (!Objects.equals(gameArenaTp.world, "None")) {
            inv.setItem(8, ItemUtil.create("&5Телепорт на арену", Material.COMPASS, gameName, "gameArenaTp"));
        }
        inv.setItem(16, ItemUtil.create("&2Установить телелепорт на спавн", Material.ENDER_PORTAL, gameName, "setSpawnTp"));
        inv.setItem(17, ItemUtil.create("&2Установить телелепорт на арену", Material.ENDER_PORTAL, gameName, "setGameArenaTp"));
        inv.setItem(26, ItemUtil.create("&4Назад", Material.WOOD_DOOR));
        player.openInventory(inv);
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (inventoryNames.contains(event.getInventory().getName()) && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
            event.setCancelled(true);
            updateGamesData();
            String invName = event.getInventory().getName();
            Player player = (Player) event.getWhoClicked();
            if (!player.hasPermission(permissionName)) {
                Message.noPermission.send(player);
                return;
            }
            if (Objects.equals(invName, "Выбор игры")) {
                if (gameNames.contains(event.getCurrentItem().getItemMeta().getDisplayName().substring(2))) {
                    String gameName = event.getCurrentItem().getItemMeta().getDisplayName().substring(2);
                    openGame(player, gameName);
                    return;
                }
                ItemStack item = event.getCurrentItem();
                if (item.getType() == Material.WOOD_DOOR) {
                    player.closeInventory();
                    return;
                }
                List<String> lore = item.getItemMeta().getLore();
                String gameName = lore.get(0);
                String action = lore.get(1);
                Map<String, Object> game = (Map<String, Object>) games.get(gameName);
                if (Objects.equals(action, "startRegister")) {
                    if (GameData.isGameRun || GameData.isGameRegister) {
                        Message.isGameRunOrRegister.send(player);
                        return;
                    }
                    GameData.isGameRegister = true;
                    GameData.gameRunName = gameName;
                    GameData.playerGameRun = player;
                    Message.startRegister.replace("{game}", gameName).send(player);
                    openSelectGame(player);
                    List<LocationItem> floors = (List<LocationItem>) game.get("floorsLocation");
                    for (LocationItem floor: floors) {
                        Pos.setZone(floor, Material.BEDROCK);
                    }
                    if (sendGlobalMsgOnStartRegister) {
                        for (Player playerMsg: Bukkit.getServer().getOnlinePlayers()) {
                            Message.globalMessageOnStartRegister.send(playerMsg);
                        }
                    }
                } else if (Objects.equals(action, "startGame")) {
                    if (!(GameData.isGameRegister && Objects.equals(GameData.gameRunName, gameName))) {
                        Message.isGameRunOrRegister.send(player);
                        return;
                    }
                    GameData.gameRunName = gameName;
                    openSelectGame(player);
                    GameData.startGame();
                } else if (Objects.equals(action, "stopGame")) {
                    if (!GameData.isGameRun && !GameData.isGameRegister) {
                        Message.gameNotStart.send(player);
                        return;
                    }
                    GameData.playersGameAlert(Message.creatorStopGame.replace("{player}", player.getDisplayName())::send);
                    GameData.stopGame();
                    openSelectGame(player);
                }
            } else if (Objects.equals(invName, "Настройка игры")) {
                ItemStack item = event.getCurrentItem();
                if (item.getType() == Material.WOOD_DOOR) {
                    openSelectGame(player);
                    return;
                }
                List<String> lore = item.getItemMeta().getLore();
                String gameName = lore.get(0);
                String action = lore.get(1);
                Map<String, Object> game = (Map<String, Object>) games.get(gameName);
                if (Objects.equals(action, "spawnTp") || Objects.equals(action, "gameArenaTp")) {
                    TeleportationItem teleport = (TeleportationItem) game.get(action);
                    World world = Bukkit.getServer().getWorld(teleport.world);
                    Location loc = new Location(world, teleport.x, teleport.y, teleport.z);
                    loc.setYaw(teleport.yaw);
                    loc.setPitch(teleport.pitch);
                    player.teleport(loc);
                } else if (Objects.equals(action, "setSpawnTp") || Objects.equals(action, "setGameArenaTp")) {
                    String teleportType = Character.toLowerCase(action.charAt(3)) + action.substring(4);
                    GameData.setTeleport(player, gameName, teleportType);
                    openGame(player, gameName);
                } else if (Objects.equals(action, "hideFloor") || Objects.equals(action, "showFloor")) {
                    List<LocationItem> floors = (List<LocationItem>) game.get("floorsLocation");
                    int floorNum = Integer.parseInt(lore.get(2));
                    LocationItem floor = floors.get(floorNum);
                    Map<Integer, Boolean> playerFloors = showFloors.get(player.getDisplayName()).get(gameName);
                    if (Objects.equals(action, "hideFloor")) {
                        Pos.asyncHidePerimeter(player, floor);
                        playerFloors.put(floorNum, false);
                    } else {
                        Pos.asyncShowPerimeter(player, floor);
                        playerFloors.put(floorNum, true);
                    }
                    openGame(player, gameName);
                } else if (Objects.equals(action, "TeleportArena")) {
                    List<LocationItem> floors = (List<LocationItem>) game.get("floorsLocation");
                    int floorNum = Integer.parseInt(lore.get(2));
                    LocationItem floor = floors.get(floorNum);
                    int x = floor.x1 + ((floor.x2-floor.x1)/2);
                    int z = floor.z1 + ((floor.z2-floor.z1)/2);
                    World world = Bukkit.getServer().getWorld(floor.world);
                    Location loc = new Location(world, x, floor.y1 + 1, z);
                    player.teleport(loc);
                } else if (Objects.equals(action, "setFloor")) {
                    GameData.setLocation(player, gameName);
                    openGame(player, gameName);
                } else if (Objects.equals(action, "removeFloor")) {
                    int floorNum = Integer.parseInt(lore.get(2));
                    GameData.removeLocation(player, gameName, floorNum);
                    openGame(player, gameName);
                }
            }
        }
    }
    private static void updateGamesData() {
        games = Area.get();
        gameNames = games.keySet();
    }
}
