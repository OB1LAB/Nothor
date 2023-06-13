package ru.ob1lab.nothor.command;

import com.google.common.collect.Lists;
import org.bukkit.*;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.ob1lab.nothor.Main;
import ru.ob1lab.nothor.Message;
import ru.ob1lab.nothor.areas.Area;
import ru.ob1lab.nothor.areas.LocationItem;
import ru.ob1lab.nothor.areas.TeleportationItem;

import java.util.Objects;
import java.util.function.Consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameData implements Listener {
    public static boolean isGameRun = false;
    public static boolean isGameRegister = false;
    public static String gameRunName = "";
    public static Player playerGameRun;
    private static final FileConfiguration areaConfig = Main.getData().getConfig();
    private static final Map<String, Map<String, Location>> positionsSet = Main.getPositionsSet();
    private static final ArrayList<Player> Players = Lists.newArrayList();
    private static final ArrayList<Player> PlayersInGame = Lists.newArrayList();
    private static final ArrayList<Player> HistoryLeave = Lists.newArrayList();
    public static void setTeleport(Player player, String gameName, String areaName) {
        String path = "areas." + gameName;
        if (!areaConfig.contains(path)) {
            Message.gameNotFound.replace("{game}", gameName).send(player);
            return;
        }
        Location playerLocation = player.getLocation();
        int x = playerLocation.getBlockX();
        int y = playerLocation.getBlockY();
        int z = playerLocation.getBlockZ();
        float yaw = playerLocation.getYaw();
        float pitch = playerLocation.getPitch();
        Area.setGameArenaTeleport(gameName, areaName, playerLocation.getWorld().getName(), x, y, z, yaw, pitch);
        Message.teleportSuccessfullySet.replace("{game}", gameName).send(player);
    }
    public static void setLocation(Player player, String gameName) {
        String path = "areas." + gameName;
        if (!areaConfig.contains(path)) {
            Message.gameNotFound.replace("{game}", gameName).send(player);
            return;
        }
        if (!positionsSet.containsKey(player.getDisplayName()) ||
                !positionsSet.get(player.getDisplayName()).containsKey("pos1") ||
                !positionsSet.get(player.getDisplayName()).containsKey("pos2")) {
            Message.mustTwoPos.send(player);
            return;
        }
        Location pos1 = positionsSet.get(player.getDisplayName()).get("pos1");
        Location pos2 = positionsSet.get(player.getDisplayName()).get("pos2");
        LocationItem perimeter = Pos.getPerimeter(pos1, pos2);
        if (Area.setGameArenaLocation(gameName, perimeter)) {
            Message.locationSuccessfullySet.replace("{game}", gameName).send(player);
        } else {
            Message.floorsInGame.replace("{game}", gameName).send(player);
        }
    }
    public static void removeLocation(Player player, String gameName, int index) {
        String path = "areas." + gameName;
        if (!areaConfig.contains(path)) {
            Message.gameNotFound.replace("{game}", gameName).send(player);
            return;
        }
        Area.removeGameArenaLocation(gameName, index);
        Message.locationSuccessfullyRemove.replace("{game}", gameName).send(player);
    }
    public static void createGame(CommandSender player, String gameName) {
        String path = "areas." + gameName;
        if (areaConfig.contains(path)) {
            Message.gameIsCreate.replace("{game}", gameName).send(player);
            return;
        }
        if (Area.get().keySet().size() > 7) {
            Message.gameAmountLimit.send(player);
            return;
        }
        Area.createGameArea(gameName);
        Message.gameSuccessfullyCreate.replace("{game}", gameName).send(player);
    }
    public static void removeGame(CommandSender player, String gameName) {
        String path = "areas." + gameName;
        if (!areaConfig.contains(path)) {
            Message.gameNotFound.replace("{game}", gameName).send(player);
            return;
        }
        Area.removeGameArea(gameName);
        Message.gameSuccessfullyRemove.replace("{game}", gameName).send(player);
    }
    public static void joinGame(Player player) {
        Map<String, Object> games = Area.get();
        if (!isGameRegister) {
            Message.notRegisterGame.send(player);
            return;
        }
        if (Players.contains(player)) {
            Message.userInRegisterGame.send(player);
            return;
        }
        for(ItemStack item: player.getInventory().getContents()) {
            if (item != null) {
                Message.inventoryIsNotEmpty.send(player);
                return;
            }
        }
        for(ItemStack item: player.getInventory().getArmorContents()) {
            if (item.getType() != Material.AIR) {
                Message.inventoryIsNotEmpty.send(player);
                return;
            }
        }
        Map<String, Object> game = (Map<String, Object>) games.get(gameRunName);
        TeleportationItem teleport = (TeleportationItem) game.get("spawnTp");
        World world = Bukkit.getServer().getWorld(teleport.world);
        Location loc = new Location(world, teleport.x, teleport.y, teleport.z);
        loc.setYaw(teleport.yaw);
        loc.setPitch(teleport.pitch);
        player.teleport(loc);
        Message.joinGame.send(player);
        Players.add(player);
        player.setFoodLevel(20);
        player.setHealth(20);
        playersGameAlert(Message.playerJoinGame.replace("{player}", player.getDisplayName()).replace("{count}", String.valueOf(Players.size()))::send);
    }
    public static void exitGame(Player player) {
        if (!Players.contains(player)) {
            Message.userNotRegisterGame.send(player);
            return;
        }
        if (PlayersInGame.contains(player)) {
            Map<String, Object> game = getActivityGame();
            Location locationSpawn = getActivityGameLocation(game, "spawnTp");
            Inventory playerInv = player.getInventory();
            playerInv.clear();
            player.teleport(locationSpawn);
            PlayersInGame.remove(player);
            HistoryLeave.add(player);
            playersGameAlert(Message.playerLossGame.replace("{player}", player.getDisplayName()).replace("{count}", String.valueOf(PlayersInGame.size()))::send);
            return;
        }
        Players.remove(player);
        Message.userUnRegisterGame.send(player);
        if (!isGameRun) {
            playersGameAlert(Message.playerExitGame.replace("{player}", player.getDisplayName()).replace("{count}", String.valueOf(Players.size()))::send);
        }
    }
    public static void startGame() {
        if (Players.size() < 4) {
            Message.notEnoughPlayersToStartGame.send(playerGameRun);
            return;
        }
        isGameRegister = false;
        isGameRun = true;
        PlayersInGame.addAll(Players);
        Map<String, Object> game = getActivityGame();
        List<LocationItem> floors = getActivityGameFloors(game);
        LocationItem lastFloor = floors.get(0);
        Location locationArena = getActivityGameLocation(game, "gameArenaTp");
        Location locationSpawn = getActivityGameLocation(game, "spawnTp");
        for (Player player: PlayersInGame) {
            player.teleport(locationArena);
        }
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            gameStartAlert();
            for (LocationItem floor: floors) {
                Pos.setZone(floor, Material.SNOW_BLOCK);
            }
            ItemStack spade = new ItemStack(Material.DIAMOND_SPADE);
            spade.addEnchantment(Enchantment.DIG_SPEED, 5);
            for (Player player: PlayersInGame) {
                Inventory playerInv = player.getInventory();
                playerInv.clear();
                playerInv.addItem(spade);
            }
            while (isGameRun) {
                try {
                    for (Player player: PlayersInGame) {
                        Location playerLocation = player.getLocation();
                        if (!Objects.equals(playerLocation.getWorld().getName(), lastFloor.world)
                                || !(lastFloor.x1 <= playerLocation.getBlockX() && playerLocation.getBlockX() <= lastFloor.x2)
                                || !(lastFloor.y1 <= playerLocation.getY())
                                || !(lastFloor.z1 <= playerLocation.getBlockZ() && playerLocation.getBlockZ() <= lastFloor.z2)
                                || player.isDead()) {
                            PlayersInGame.remove(player);
                            HistoryLeave.add(player);
                            Inventory playerInv = player.getInventory();
                            playerInv.clear();
                            player.teleport(locationSpawn);
                            playersGameAlert(Message.playerLossGame.replace("{player}", player.getDisplayName()).replace("{count}", String.valueOf(PlayersInGame.size()))::send);
                            break;
                        }
                        if (player.getFoodLevel() != 20) {
                            player.setFoodLevel(20);
                        }
                        if (player.isFlying()) {
                            player.setFlying(false);
                        }
                    }
                    if (PlayersInGame.size() <= 1) {
                        if (PlayersInGame.size() == 1) {
                            HistoryLeave.add(PlayersInGame.get(0));
                        }
                        playersGameAlert(
                                Message.winMsg.replace("{player1}", HistoryLeave.get(HistoryLeave.size() - 1).getDisplayName())
                                        .replace("{player2}", HistoryLeave.get(HistoryLeave.size() - 2).getDisplayName())
                                        .replace("{player3}", HistoryLeave.get(HistoryLeave.size() - 3).getDisplayName())
                                        ::send);
                        stopGame();
                    }
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
    public static void stopGame() {
        isGameRun = false;
        isGameRegister = false;
        Map<String, Object> game = getActivityGame();
        List<LocationItem> floors = getActivityGameFloors(game);
        Location locationSpawn = getActivityGameLocation(game, "spawnTp");
        for (Player player: PlayersInGame) {
            player.teleport(locationSpawn);
            Inventory playerInv = player.getInventory();
            playerInv.clear();
        }
        for (LocationItem floor: floors) {
            Pos.setZone(floor, Material.BEDROCK);
        }
        PlayersInGame.clear();
        Players.clear();
    }
    public static void playersGameAlert(Consumer<Player> function) {
        if (!Players.contains(playerGameRun)) {
            function.accept(playerGameRun);
        }
        for (Player player: Players) {
            function.accept(player);
        }
    }
    private static Map<String, Object> getActivityGame () {
        Map<String, Object> games = Area.get();
        return (Map<String, Object>) games.get(gameRunName);
    }
    private static List<LocationItem> getActivityGameFloors(Map<String, Object> game) {
        return (List<LocationItem>) game.get("floorsLocation");
    }
    private static Location getActivityGameLocation(Map<String, Object> game, String locationName) {
        TeleportationItem teleport = (TeleportationItem) game.get(locationName);
        World worldArena = Bukkit.getServer().getWorld(teleport.world);
        Location gameLocation = new Location(worldArena, teleport.x, teleport.y, teleport.z);
        gameLocation.setYaw(teleport.yaw);
        gameLocation.setPitch(teleport.pitch);
        return gameLocation;
    }
    private static void gameStartAlert() {
        try {
            playersGameAlert(Message.firstAlertStartGame::send);
            Thread.sleep(10000);
            playersGameAlert(Message.alertStartGame.replace("{second}", "5")::send);
            Thread.sleep(1000);
            playersGameAlert(Message.alertStartGame.replace("{second}", "4")::send);
            Thread.sleep(1000);
            playersGameAlert(Message.alertStartGame.replace("{second}", "3")::send);
            Thread.sleep(1000);
            playersGameAlert(Message.alertStartGame.replace("{second}", "2")::send);
            Thread.sleep(1000);
            playersGameAlert(Message.alertStartGame.replace("{second}", "1")::send);
            Thread.sleep(1000);
            playersGameAlert(Message.alertGameIsStart::send);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    @EventHandler
    public void onPlayerExit(PlayerQuitEvent event) {
        if (isGameRun && PlayersInGame.contains(event.getPlayer())) {
            Map<String, Object> game = getActivityGame();
            Location locationSpawn = getActivityGameLocation(game, "spawnTp");
            Player player = event.getPlayer();
            Inventory playerInv = player.getInventory();
            playerInv.clear();
            player.teleport(locationSpawn);
            PlayersInGame.remove(player);
            HistoryLeave.add(player);
            playersGameAlert(Message.playerLossGame.replace("{player}", player.getDisplayName()).replace("{count}", String.valueOf(PlayersInGame.size()))::send);
        }
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (isGameRun && PlayersInGame.contains((Player) event.getWhoClicked())) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onPlayerPickUpItems(PlayerPickupItemEvent event) {
        if (isGameRun && PlayersInGame.contains(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onPlayerDropItems(PlayerDropItemEvent event) {
        if (isGameRun && PlayersInGame.contains(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}
