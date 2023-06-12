package ru.ob1lab.nothor.command;

import com.google.common.collect.Lists;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.ob1lab.nothor.Main;
import ru.ob1lab.nothor.areas.Area;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class onTabComplete implements TabCompleter {
    private static final String permissionName = Main.getPluginConfig().getString("settings.permission", "OP");
    private static final List<String> zeroConsole = Lists.newArrayList("info", "createGame", "removeGame");
    private static final List<String> zeroWithoutPerm = Lists.newArrayList("join", "exit", "info");
    private static final List<String> zeroWithPerm = Lists.newArrayList("join", "exit", "info", "open", "pos1", "pos2", "show", "hide", "createGame", "removeGame");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) {
            if (sender.hasPermission(permissionName)) {
                return zeroWithPerm;
            }
            if (!(sender instanceof Player)) {
                return zeroConsole;
            }
            return zeroWithoutPerm;
        }
        List<String> result = Lists.newArrayList();
        String lastArg = args[args.length -1].toLowerCase();
        if (args.length == 1) {
            List<String> currentCommands = Lists.newArrayList();
            if (sender.hasPermission(permissionName)) {
                if (!(sender instanceof Player)) {
                    currentCommands.addAll(zeroConsole);
                } else {
                    currentCommands.addAll(zeroWithPerm);
                }
            } else {
                currentCommands.addAll(zeroWithoutPerm);
            }
            for (String arg: currentCommands) {
                if (arg.toLowerCase().startsWith(lastArg)) {
                    result.add(arg);
                }
            }
        } else if (args.length == 2 && sender.hasPermission(permissionName) && args[args.length - 2].toLowerCase().equalsIgnoreCase("removeGame")) {
            Map<String, Object> games = Area.get();
            Set<String> gameNames = games.keySet();
            for (String arg: gameNames) {
                if (arg.toLowerCase().startsWith(lastArg)) {
                    result.add(arg);
                }
            }
        }
        return result;
    }
}
