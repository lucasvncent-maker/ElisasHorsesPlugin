package fr.loual.myplugin.commands;

import fr.loual.myplugin.MyPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Elisa implements CommandExecutor {

    private final MyPlugin plugin;

    public Elisa(MyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (args.length > 0 && args[0].equalsIgnoreCase("version")) {
            sender.sendMessage(
                    ChatColor.GOLD + "[ElisasHorses] "
                    + ChatColor.WHITE + "Dernière version installée : "
                    + ChatColor.GREEN + "v" + plugin.getDescription().getVersion()
            );
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Cette commande doit être exécutée par un joueur.");
            return true;
        }

        player.sendMessage(
                player.getName() + " dit qu'Elisa est la plus belle des petites mimi !"
        );

        return true;
    }
}