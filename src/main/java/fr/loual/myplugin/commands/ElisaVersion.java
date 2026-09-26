package fr.loual.myplugin.commands;

import fr.loual.myplugin.MyPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ElisaVersion implements CommandExecutor {

    private final MyPlugin plugin;

    public ElisaVersion(MyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        String version = plugin.getDescription().getVersion();
        sender.sendMessage(
                ChatColor.GOLD + "[ElisasHorses] "
                + ChatColor.WHITE + "Dernière version installée : "
                + ChatColor.GREEN + "v" + version
        );
        return true;
    }
}
