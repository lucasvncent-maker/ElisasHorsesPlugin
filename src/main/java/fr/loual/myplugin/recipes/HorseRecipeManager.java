package fr.loual.myplugin.recipes;

import fr.loual.myplugin.MyPlugin;
import fr.loual.myplugin.items.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HorseRecipeManager implements Listener {

    private final MyPlugin plugin;
    private final List<NamespacedKey> recipeKeys = new ArrayList<>();

    public final NamespacedKey horseAnalyzerKey;
    public final NamespacedKey vigorAppleKey;
    public final NamespacedKey hasteBambooKey;
    public final NamespacedKey healthyGrassKey;
    public final NamespacedKey divineArmorKey;
    public final NamespacedKey racePassKey;

    // Nouvelles recettes pour soins, lien et équipement
    public final NamespacedKey groomingBrushKey;
    public final NamespacedKey horseWhistleKey;
    public final NamespacedKey saddlebagKey;
    public final NamespacedKey horseshoeWinterKey;
    public final NamespacedKey horseshoeFeatherKey;
    public final NamespacedKey horseshoeRockKey;

    public HorseRecipeManager(MyPlugin plugin) {
        this.plugin = plugin;
        this.horseAnalyzerKey = new NamespacedKey(plugin, "horse_analyzer");
        this.vigorAppleKey = new NamespacedKey(plugin, "vigor_apple");
        this.hasteBambooKey = new NamespacedKey(plugin, "haste_bamboo");
        this.healthyGrassKey = new NamespacedKey(plugin, "healthy_grass");
        this.divineArmorKey = new NamespacedKey(plugin, "divine_armor");
        this.racePassKey = new NamespacedKey(plugin, "race_pass");

        this.groomingBrushKey = new NamespacedKey(plugin, "grooming_brush");
        this.horseWhistleKey = new NamespacedKey(plugin, "horse_whistle");
        this.saddlebagKey = new NamespacedKey(plugin, "saddlebag");
        this.horseshoeWinterKey = new NamespacedKey(plugin, "horseshoe_winter");
        this.horseshoeFeatherKey = new NamespacedKey(plugin, "horseshoe_feather");
        this.horseshoeRockKey = new NamespacedKey(plugin, "horseshoe_rock");

        recipeKeys.add(horseAnalyzerKey);
        recipeKeys.add(vigorAppleKey);
        recipeKeys.add(hasteBambooKey);
        recipeKeys.add(healthyGrassKey);
        recipeKeys.add(divineArmorKey);
        recipeKeys.add(racePassKey);

        recipeKeys.add(groomingBrushKey);
        recipeKeys.add(horseWhistleKey);
        recipeKeys.add(saddlebagKey);
        recipeKeys.add(horseshoeWinterKey);
        recipeKeys.add(horseshoeFeatherKey);
        recipeKeys.add(horseshoeRockKey);
    }

    public void registerRecipes() {
        int registered = 0;

        // 1. Analyseur Equin
        ShapedRecipe horseAnalyzer = new ShapedRecipe(horseAnalyzerKey, HorseAnalyzer.create(plugin));
        horseAnalyzer.shape(" H ", "ASA", " H ");
        horseAnalyzer.setIngredient('H', Material.HAY_BLOCK);
        horseAnalyzer.setIngredient('S', Material.SPYGLASS);
        horseAnalyzer.setIngredient('A', Material.APPLE);
        if (registerOrReplace(horseAnalyzer)) registered++;

        // 2. Pomme de Vigueur
        ShapedRecipe vigorApple = new ShapedRecipe(vigorAppleKey, VigorApple.create(plugin));
        vigorApple.shape("DGD", "GAG", "DGD");
        vigorApple.setIngredient('D', Material.DIAMOND);
        vigorApple.setIngredient('G', Material.GOLD_INGOT);
        vigorApple.setIngredient('A', Material.APPLE);
        if (registerOrReplace(vigorApple)) registered++;

        // 3. Bambou de Célérité
        ShapedRecipe hasteBamboo = new ShapedRecipe(hasteBambooKey, HasteBamboo.create(plugin));
        hasteBamboo.shape("SAS", "RBR", "SAS");
        hasteBamboo.setIngredient('R', Material.BLAZE_ROD);
        hasteBamboo.setIngredient('S', Material.SUGAR);
        hasteBamboo.setIngredient('A', Material.AMETHYST_SHARD);
        hasteBamboo.setIngredient('B', Material.BAMBOO);
        if (registerOrReplace(hasteBamboo)) registered++;

        // 4. Herbe Fortifiante
        ShapedRecipe healthyGrass = new ShapedRecipe(healthyGrassKey, HealthyGrass.create(plugin));
        healthyGrass.shape("BGB", "GHG", "BGB");
        healthyGrass.setIngredient('B', Material.GLOW_BERRIES);
        healthyGrass.setIngredient('G', Material.GLOWSTONE_DUST);
        healthyGrass.setIngredient('H', Material.SHORT_GRASS);
        if (registerOrReplace(healthyGrass)) registered++;

        // 5. Armure Divine
        ShapedRecipe divineArmor = new ShapedRecipe(divineArmorKey, DivineArmor.create(plugin));
        divineArmor.shape(" E ", "SAU", "DDD");
        divineArmor.setIngredient('E', Material.ELYTRA);
        divineArmor.setIngredient('S', Material.NETHER_STAR);
        divineArmor.setIngredient('A', Material.DIAMOND_HORSE_ARMOR);
        divineArmor.setIngredient('U', Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
        divineArmor.setIngredient('D', Material.DIAMOND);
        if (registerOrReplace(divineArmor)) registered++;

        // 6. Pass de Course
        ShapedRecipe racePass = new ShapedRecipe(racePassKey, RacePass.create(plugin, 1, "Première course"));
        racePass.shape("HPA");
        racePass.setIngredient('H', Material.HAY_BLOCK);
        racePass.setIngredient('P', Material.PAPER);
        racePass.setIngredient('A', Material.APPLE);
        if (registerOrReplace(racePass)) registered++;

        // 7. Brosse de Pansage
        ShapedRecipe groomingBrush = new ShapedRecipe(groomingBrushKey, GroomingBrush.create(plugin));
        groomingBrush.shape(" F ", " C ", " W ");
        groomingBrush.setIngredient('F', Material.FEATHER);
        groomingBrush.setIngredient('C', Material.COPPER_INGOT);
        groomingBrush.setIngredient('W', Material.WHITE_WOOL);
        if (registerOrReplace(groomingBrush)) registered++;

        // 8. Sifflet Équin
        ShapedRecipe horseWhistle = new ShapedRecipe(horseWhistleKey, HorseWhistle.create(plugin));
        horseWhistle.shape(" I ", " G ", " C ");
        horseWhistle.setIngredient('I', Material.IRON_INGOT);
        horseWhistle.setIngredient('G', Material.GOLD_INGOT);
        horseWhistle.setIngredient('C', Material.COPPER_INGOT);
        if (registerOrReplace(horseWhistle)) registered++;

        // 9. Sacoche de Selle
        ShapedRecipe saddlebag = new ShapedRecipe(saddlebagKey, SaddlebagItem.create(plugin));
        saddlebag.shape("LLL", "LCL", "LLL");
        saddlebag.setIngredient('L', Material.LEATHER);
        saddlebag.setIngredient('C', Material.CHEST);
        if (registerOrReplace(saddlebag)) registered++;

        // 10. Fers d'Hiver
        ShapedRecipe horseshoeWinter = new ShapedRecipe(horseshoeWinterKey, HorseshoeItem.create(plugin, HorseshoeItem.Type.WINTER));
        horseshoeWinter.shape("I I", " A ", "I I");
        horseshoeWinter.setIngredient('I', Material.IRON_INGOT);
        horseshoeWinter.setIngredient('A', Material.AMETHYST_SHARD);
        if (registerOrReplace(horseshoeWinter)) registered++;

        // 11. Fers Légers
        ShapedRecipe horseshoeFeather = new ShapedRecipe(horseshoeFeatherKey, HorseshoeItem.create(plugin, HorseshoeItem.Type.FEATHER));
        horseshoeFeather.shape("G G", " F ", "G G");
        horseshoeFeather.setIngredient('G', Material.GOLD_INGOT);
        horseshoeFeather.setIngredient('F', Material.FEATHER);
        if (registerOrReplace(horseshoeFeather)) registered++;

        // 12. Fers de Roche
        ShapedRecipe horseshoeRock = new ShapedRecipe(horseshoeRockKey, HorseshoeItem.create(plugin, HorseshoeItem.Type.ROCK));
        horseshoeRock.shape("C C", " S ", "C C");
        horseshoeRock.setIngredient('C', Material.COPPER_INGOT);
        horseshoeRock.setIngredient('S', Material.SMOOTH_STONE);
        if (registerOrReplace(horseshoeRock)) registered++;

        plugin.getLogger().info("Recettes personnalisées enregistrées : " + registered + "/" + recipeKeys.size());
    }

    private boolean registerOrReplace(Recipe recipe) {
        if (recipe instanceof org.bukkit.Keyed keyed) {
            try {
                Bukkit.removeRecipe(keyed.getKey());
            } catch (Exception ignored) {}
        }
        boolean added = Bukkit.addRecipe(recipe);
        if (!added && recipe instanceof org.bukkit.Keyed keyed) {
            plugin.getLogger().warning("Échec de l'enregistrement de la recette : " + keyed.getKey());
        }
        return added;
    }

    public void discoverAll(Player player) {
        if (player == null || !player.isOnline()) return;
        for (NamespacedKey key : recipeKeys) {
            if (!player.hasDiscoveredRecipe(key)) {
                player.discoverRecipe(key);
            }
        }
    }

    public List<NamespacedKey> getRecipeKeys() {
        return Collections.unmodifiableList(recipeKeys);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        discoverAll(event.getPlayer());
    }
}
