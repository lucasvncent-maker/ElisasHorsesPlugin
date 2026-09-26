package fr.loual.myplugin;
import org.bukkit.plugin.java.JavaPlugin;

import fr.loual.myplugin.commands.Coords;
import fr.loual.myplugin.commands.Elisa;
import fr.loual.myplugin.commands.ElisaVersion;
import fr.loual.myplugin.commands.StartRace;
import fr.loual.myplugin.commands.StopRace;

import fr.loual.myplugin.recipes.HorseRecipeManager;
import org.bukkit.Bukkit;

import fr.loual.myplugin.horses.HorseManager;
import fr.loual.myplugin.races.HorseRaceManager;

import fr.loual.myplugin.listeners.HorseItemListener;
import fr.loual.myplugin.listeners.HorseRaceListener;
import fr.loual.myplugin.listeners.HorseCareListener;

import org.bukkit.World;
import org.bukkit.entity.Horse;
import org.bukkit.WorldCreator;

import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import fr.loual.myplugin.advancements.AdvancementManager;

import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;

public class MyPlugin extends JavaPlugin {
    private final HorseManager horseManager = new HorseManager();
    private HorseRaceManager horseRaceManager;
    private AdvancementManager advancementManager;
    private HorseRecipeManager horseRecipeManager;

    @Override
    public void onEnable() {
        advancementManager = new AdvancementManager(this);
        advancementManager.installDatapack();
        horseRaceManager = new HorseRaceManager(this);

        getCommand("coords").setExecutor(new Coords());
        getCommand("elisa").setExecutor(new Elisa(this));
        getCommand("start_race").setExecutor(new StartRace(this));
        getCommand("stop_race").setExecutor(new StopRace(this));
        getCommand("elisa_version").setExecutor(new ElisaVersion(this));

        this.horseRecipeManager = new HorseRecipeManager(this);
        this.horseRecipeManager.registerRecipes();
        getServer().getPluginManager().registerEvents(this.horseRecipeManager, this);

        getServer().getPluginManager().registerEvents(new HorseItemListener(this), this);
        getServer().getPluginManager().registerEvents(new HorseRaceListener(this), this);
        getServer().getPluginManager().registerEvents(new HorseCareListener(this), this);

        for (Player p : Bukkit.getOnlinePlayers()) {
            this.horseRecipeManager.discoverAll(p);
        }

        // Sécurité supplémentaire : réenregistrer et débloquer au tick suivant
        // au cas où un rechargement de ressources (datapack) a eu lieu durant le chargement
        Bukkit.getScheduler().runTask(this, () -> {
            this.horseRecipeManager.registerRecipes();
            for (Player p : Bukkit.getOnlinePlayers()) {
                this.horseRecipeManager.discoverAll(p);
            }
        });

        getLogger().info("ElisasHorses est activé !");

        Bukkit.getScheduler().runTaskTimer(
            this,
            () -> {
                long currentTick = Bukkit.getCurrentTick();
                for (World world : getServer().getWorlds()) {
                    for (Horse horse : world.getEntitiesByClass(Horse.class)) {
                        horseManager.tickFlyingHorse(this, horse);
                        horseManager.tickCareAndComfort(this, horse, currentTick);
                    }
                }
            },
            0L,
            1L
        );

        Bukkit.getScheduler().runTaskTimer(
                this,
                () -> horseRaceManager.tick(),
                0L,
                1L
        );

        WorldCreator creator = new WorldCreator("horse_races");
        World raceWorld = creator.createWorld();

        if (raceWorld == null) {
            getLogger().severe("Impossible de charger horse_races !");
        } else {
            getLogger().info("Monde horse_races chargé !");
        }

        Bukkit.getScheduler().runTaskLater(this, () -> {
            NamespacedKey key = new NamespacedKey(this, "root");
            Advancement adv = Bukkit.getAdvancement(key);
            if (adv != null) {
                getLogger().info("🚀 Succès ! elisashorses:root est reconnu.");
            } else {
                getLogger().warning("❌ Toujours introuvable malgré le reload.");
            }
        }, 20L);
    }

    public HorseManager getHorseManager() {
        return horseManager;
    }

    public HorseRaceManager getHorseRaceManager() { return horseRaceManager; }

    public void awardAdvancement(Player player, String advancementId) {
        NamespacedKey key = new NamespacedKey(this, advancementId);
        Advancement advancement = Bukkit.getAdvancement(key);

        if (advancement == null) {
            getLogger().warning("Advancement introuvable : " + key);
            return;
        }

        AdvancementProgress progress =player.getAdvancementProgress(advancement);

        for (String criterion : progress.getRemainingCriteria()) {
            progress.awardCriteria(criterion);
        }
    }

    public AdvancementManager getAdvancementManager() {
        return advancementManager;
    }

    public HorseRecipeManager getHorseRecipeManager() {
        return horseRecipeManager;
    }

}