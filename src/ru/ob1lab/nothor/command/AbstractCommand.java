package ru.ob1lab.nothor.command;

import org.bukkit.command.*;
import ru.ob1lab.nothor.Main;

public abstract class AbstractCommand implements CommandExecutor {
    public AbstractCommand(String command) {
        PluginCommand pluginCommand =  Main.getInstance().getCommand(command);
        if (pluginCommand != null) {
            pluginCommand.setExecutor(this);
            pluginCommand.setTabCompleter(new onTabComplete());
        }
    }
    public abstract void execute(CommandSender sender, String label, String[] args);
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        execute(sender, label, args);
        return true;
    }
}

