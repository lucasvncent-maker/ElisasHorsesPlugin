package fr.loual.myplugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Coords implements CommandExecutor {

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        // Vérifie que la commande est exécutée par un joueur
        if (!(sender instanceof Player player)) {
            sender.sendMessage(
                    ChatColor.RED + "Cette commande doit être exécutée par un joueur."
            );
            return true;
        }

        Location location = player.getLocation();

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        // Argument optionnel
        String messageSupplementaire = "";

        if (args.length > 0) {
            messageSupplementaire = " " + String.join(" ", args);
        }

        // Message envoyé à tout le serveur
        String message =
                ChatColor.YELLOW + player.getName()
                + ChatColor.WHITE + " se trouve aux coordonnées "
                + ChatColor.RED + "X: " + x
                + ChatColor.WHITE + " "
                + ChatColor.GREEN + "Y: " + y
                + ChatColor.WHITE + " "
                + ChatColor.BLUE + "Z: " + z
                + ChatColor.WHITE + " !"
                + ChatColor.GRAY + messageSupplementaire;

        // Envoie le message à tous les joueurs connectés
        for (Player onlinePlayer : player.getServer().getOnlinePlayers()) {
            onlinePlayer.sendMessage(message);
        }

        return true;
    }
}
