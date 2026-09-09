package fr.loual.myplugin.races;

import org.bukkit.entity.Horse;
import org.bukkit.Location;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.loual.myplugin.MyPlugin;
import fr.loual.myplugin.races.HorseRaceConfig;
import fr.loual.myplugin.items.RacePass;
import fr.loual.myplugin.items.DivineArmor;
import org.bukkit.attribute.Attribute;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class HorseRaceManager {

    private final MyPlugin plugin;
    private final Map<UUID, HorseRace> activeRaces = new HashMap<>();
    private final Map<Integer, HorseRaceConfig> raceConfigs = new HashMap<>();

    public HorseRaceManager(MyPlugin plugin) {
        this.plugin = plugin;

        saveRaceResources();
        loadRaceConfigs();
    }

    public boolean startRace(Player player,  int raceId) {

        if (activeRaces.containsKey(player.getUniqueId())) {
            player.sendMessage("§cVous êtes déjà dans une course !");
            return false;
        }

        World world = Bukkit.getWorld("horse_races");

        if (world == null) {
            player.sendMessage("§cLe monde de la course n'est pas chargé.");
            return false;
        }

        Location returnLocation = player.getLocation().clone();
        Horse horse = findPlayerHorse(player);

        if (horse == null) {
            player.sendMessage("§cVous devez être à cheval.");
            return false;
        }

        HorseRaceConfig config = raceConfigs.get(raceId);

        if (config == null) {
            player.sendMessage("§cCette course n'existe pas.");
            return false;
        }

        if (horse.getAttribute(Attribute.MOVEMENT_SPEED).getValue() < config.getMinSpeed() || horse.getAttribute(Attribute.JUMP_STRENGTH).getValue() < config.getMinJumpStrength()) {
            player.sendMessage("§cVotre cheval n'a pas les capacités suffisantes pour concourir.\n" + 
            "§7Prérequis : Vitesse §f%.2f m/s §7| Saut : §f%.2f m".formatted(config.getMinSpeed(), config.getMinJumpStrength()));

            return false;
        }

        ItemStack armor = horse.getInventory().getArmor();

        if (DivineArmor.isDivineArmor(plugin, armor) && !config.getAcceptsDivineArmor()) {
            player.sendMessage("§cVous ne pouvez pas rejoindre cette course avec une Armure Divine sur votre cheval");
            return false;
        }

        Location start = config.getStartLocation(world);
        
        horse.removePassenger(player);
        player.teleport(start);
        horse.teleport(start);
        horse.addPassenger(player);

        HorseRace race = new HorseRace(player, horse, returnLocation, config,  raceId);

        activeRaces.put(player.getUniqueId(), race);

        player.sendMessage("§eAtteignez la ligne d'arrivée le plus rapidement possible !");
        return true;
    }

    public void stopRace(Player player) {
        HorseRace race = activeRaces.get(player.getUniqueId());

        if (race == null) {
            player.sendMessage("§cVous n'êtes pas dans une course.");
            return;
        }

        finishRace(race, "§cVous avez abandonné la course.", 0);
    }

    private void finishRace(HorseRace race, String message, double raceTime) {
        Player player = race.getPlayer();
        Horse horse = race.getHorse();
        
        HorseRaceConfig config = race.getConfig();
        int raceId = race.getRaceId();

        if (raceTime != 0) {
            if (raceTime <= config.getMaxTime()) {
                HorseRaceConfig nextConfig = raceConfigs.get(raceId + 1);
                if (nextConfig != null) {
                    ItemStack nextPass = RacePass.create(plugin, raceId + 1, nextConfig.getName());
                    nextPass.setAmount(5);
                    player.getInventory().addItem(nextPass);
                    player.sendMessage("§6§lFélicitations ! §eVous avez terminé la course §6" + config.getName() + " §een §6" + String.format("%.2f", raceTime) + " §esecondes !");
                    player.sendMessage("§aVous obtenez des invitations à d'autres courses.");
                }
                if (raceId == 2) {
                    plugin.getAdvancementManager().award(player, "win_race_2");
                }
            } else {
                player.sendMessage("§cVous terminez la course en §f" + String.format("%.2f", raceTime)+ " §csecondes.");
                player.sendMessage("§7C'est bien, mais il faut au maximum §f" + String.format("%.2f", config.getMaxTime()) + " §7secondes afin d'obtenir des récompenses !");
                player.sendMessage("§7Essayez de vous améliorer ou améliorez les statistiques " + "de votre cheval pour progresser.");
            }
        }

        activeRaces.remove(player.getUniqueId());

        horse.removePassenger(player);
        horse.teleport(race.getReturnLocation());
        player.teleport(race.getReturnLocation());
        horse.addPassenger(player);

        if (message != null) {
            player.sendMessage(message);
        }
    }

    private Horse findPlayerHorse(Player player) {

        if (player.getVehicle() instanceof Horse horse) {
            return horse;
        }

        return null;
    }

    public void tick() {

        for (HorseRace race : new ArrayList<>(activeRaces.values())) {

            Player player = race.getPlayer();
            Horse horse = race.getHorse();

            HorseRaceConfig config = race.getConfig();

            if (!player.isOnline()) {
                finishRace(race, "Player disconnected, end of the race !", 0);
                continue;
            }

            if (player.getLocation().getY() < 0) {
                finishRace(race,"§cVous êtes tombé du parcours !", 0);
                continue;
            }
            
            long elapsed = race.getElapsedTime();
            double seconds = elapsed / 1000.0;

            if (horse.getLocation().getZ() >= config.getFinishZ()) {
                finishRace(race, "§6§lARRIVÉE ! §eTemps : " + String.format("%.2f", seconds) + " secondes", seconds);
                continue;
            }
        }
    }

    public void handlePlayerQuit(Player player) {

        HorseRace race = activeRaces.get(player.getUniqueId());

        if (race == null) {
            return;
        }
        Horse horse = race.getHorse();
        horse.removePassenger(player);
        horse.teleport(race.getReturnLocation());
        player.teleport(race.getReturnLocation());
        horse.addPassenger(player);

        activeRaces.remove(player.getUniqueId());
    }

    private void loadRaceConfigs() {

        File racesFolder = new File(plugin.getDataFolder(), "races");

        if (!racesFolder.exists()) {
            racesFolder.mkdirs();
        }

        File[] files = racesFolder.listFiles((dir, name) -> name.endsWith(".yml"));

        if (files == null) {
            return;
        }

        for (File file : files) {

            try {
                String fileName = file.getName();

                int raceId = Integer.parseInt(fileName.substring(0, fileName.length() - 4));

                FileConfiguration config = YamlConfiguration.loadConfiguration(file);

                String name = config.getString("name", "Course");

                double startX = config.getDouble("start_x");
                double startY = config.getDouble("start_y");
                double startZ = config.getDouble("start_z");

                double finishZ = config.getDouble("finish_z");
                double maxTime = config.getDouble("max_time", 30.0);

                boolean accepts_divine_armor = config.getBoolean("accepts_divine_armor", false);
                double min_jump_strength = config.getDouble("min_jump_strength");
                double max_jump_strength = config.getDouble("max_jump_strength");
                double min_speed = config.getDouble("min_speed");
                double max_speed = config.getDouble("max_speed");

                HorseRaceConfig raceConfig = new HorseRaceConfig(name, startX, startY, startZ, finishZ, maxTime, accepts_divine_armor, min_jump_strength, max_jump_strength, min_speed, max_speed);

                raceConfigs.put(raceId, raceConfig);

                plugin.getLogger().info("[ElisasHorses] Course " + raceId + " chargée : " + name);

            } catch (NumberFormatException e) {
                plugin.getLogger().warning("[ElisasHorses] Fichier de course ignoré : " + file.getName()  + " (le nom doit être un nombre)");
            }
        }
    }

    private void saveRaceResources() {

        for (int i = 1; ; i++) {

            String resourcePath = "races/" + i + ".yml";

            if (plugin.getResource(resourcePath) == null) {
                break;
            }
            
            // TODO Set to false when all well configured
            plugin.saveResource(resourcePath, true);

            plugin.getLogger().info("[ElisasHorses] Course copiée : " + resourcePath);
        }
    }
}