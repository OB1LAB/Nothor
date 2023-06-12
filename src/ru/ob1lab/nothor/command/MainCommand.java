package ru.ob1lab.nothor.command;

import org.bukkit.entity.Player;
import ru.ob1lab.nothor.Main;
import ru.ob1lab.nothor.Message;
import org.bukkit.command.CommandSender;
import ru.ob1lab.nothor.gui.GUI;

public class MainCommand extends AbstractCommand {
    private static final String permissionName = Main.getPluginConfig().getString("settings.permission", "OP");
    public MainCommand() {
        super("nothor");
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            if (args.length == 0) {
                Message.infoConsole.send(sender);
            } else if (args[0].equalsIgnoreCase("createGame")) {
                GameData.createGame(sender, args[1].toLowerCase());
            } else if (args[0].equalsIgnoreCase("removeGame")) {
                GameData.removeGame(sender, args[1].toLowerCase());
            } else if (args[0].equalsIgnoreCase("info")) {
                Message.infoConsole.send(sender);
            } else {
                Message.commandAvailableOnlyPlayer.send(sender);
            }
            return;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            if (sender.hasPermission(permissionName)) {
                Message.infoWithPermission.send(player);
            } else {
                Message.infoWithoutPermission.send(player);
            }
            return;
        }
        if (args[0].equalsIgnoreCase("join")) {
            GameData.joinGame(player);
            return;
        }
        if (args[0].equalsIgnoreCase("exit")) {
            GameData.exitGame(player);
            return;
        }
        if (args[0].equalsIgnoreCase("info")) {
            if (sender.hasPermission(permissionName)) {
                Message.infoWithPermission.send(sender);
            } else {
                Message.infoWithoutPermission.send(sender);
            }
            return;
        }
        if (!sender.hasPermission(permissionName)) {
            Message.noPermission.send(sender);
            return;
        }
        if (args[0].equalsIgnoreCase("pos1") || args[0].equalsIgnoreCase("pos2")) {
            Pos.setPos(player, args[0]);
            return;
        }
        if (args[0].equalsIgnoreCase("show") || args[0].equalsIgnoreCase("hide")) {
            Pos.actionSelectPos(player, args[0]);
            return;
        }
        if (args[0].equalsIgnoreCase("open")) {
            GUI.openSelectGame(player);
            return;
        }
        if (args.length < 2) {
            Message.notEnoughArguments.send(sender);
            return;
        }
        if (args[0].equalsIgnoreCase("createGame")) {
            GameData.createGame(player, args[1].toLowerCase());
            return;
        }
        if (args[0].equalsIgnoreCase("removeGame")) {
            GameData.removeGame(player, args[1].toLowerCase());
            return;
        }
        Message.argumentNotFound.send(sender);
    }
}
