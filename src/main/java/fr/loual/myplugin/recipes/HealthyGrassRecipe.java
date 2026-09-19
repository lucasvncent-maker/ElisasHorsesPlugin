package fr.loual.myplugin.recipes;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import fr.loual.myplugin.items.HealthyGrass;

public class HealthyGrassRecipe {

    public static void register(JavaPlugin plugin) {
        NamespacedKey key = new NamespacedKey(plugin, "healthy_grass");

        ItemStack result = HealthyGrass.create(plugin);

        ShapedRecipe recipe = new ShapedRecipe(key, result);

        recipe.shape(
                "BGB",
                "GHG",
                "BGB"
        );

        recipe.setIngredient('B', Material.GLOW_BERRIES);
        recipe.setIngredient('G', Material.GLOWSTONE_DUST);
        recipe.setIngredient('H', Material.SHORT_GRASS);

        try {
            Bukkit.removeRecipe(key);
        } catch (Exception ignored) {}

        Bukkit.addRecipe(recipe);
    }
}