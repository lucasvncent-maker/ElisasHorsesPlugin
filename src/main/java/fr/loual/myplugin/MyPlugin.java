package fr.loual.myplugin;
import org.bukkit.plugin.java.JavaPlugin;

import fr.loual.myplugin.commands.Coords;
import fr.loual.myplugin.commands.Elisa;
import fr.loual.myplugin.commands.ElisaVersion;
import fr.loual.myplugin.commands.StartRace;
import fr.loual.myplugin.commands.StopRace;

import fr.loual.myplugin.recipes.VigorAppleRecipe;
import fr.loual.myplugin.recipes.HorseAnalyzerRecipe;
import fr.loual.myplugin.recipes.HasteBambooRecipe;
import fr.loual.myplugin.recipes.HealthyGrassRecipe;
import fr.loual.myplugin.recipes.DivineArmorRecipe;
import fr.loual.myplugin.recipes.RacePassRecipe;
import org.bukkit.Bukkit;

import fr.loual.myplugin.horses.HorseManager;
import fr.loual.myplugin.races.HorseRaceManager;

import fr.loual.myplugin.listeners.HorseItemListener;
import fr.loual.myplugin.listeners.HorseRaceListener;

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

        VigorAppleRecipe.register(this);
        HorseAnalyzerRecipe.register(this);
        HasteBambooRecipe.register(this);
        HealthyGrassRecipe.register(this);
        DivineArmorRecipe.register(this);
        RacePassRecipe.register(this);

        getServer().getPluginManager().registerEvents(new HorseItemListener(this), this);
        getServer().getPluginManager().registerEvents(new HorseRaceListener(this), this);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.discoverRecipe(new NamespacedKey(this, "horse_analyzer"));
        }

        getLogger().info("ElisasHorses est activé !");

        Bukkit.getScheduler().runTaskTimer(
            this,
            () -> {
                for (World world : getServer().getWorlds()) {
                    for (Horse horse : world.getEntitiesByClass(Horse.class)) {
                        horseManager.tickFlyingHorse(this, horse);
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

}