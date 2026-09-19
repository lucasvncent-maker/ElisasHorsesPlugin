package fr.loual.myplugin.recipes;

import fr.loual.myplugin.items.VigorApple;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class VigorAppleRecipe {

    public static void register(JavaPlugin plugin) {
        NamespacedKey key = new NamespacedKey(plugin, "vigor_apple");

        ItemStack result = VigorApple.create(plugin);

        ShapedRecipe recipe = new ShapedRecipe(key, result);

        recipe.shape(
                "DGD",
                "GAG",
                "DGD"
        );

        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('A', Material.APPLE);

        try {
            Bukkit.removeRecipe(key);
        } catch (Exception ignored) {}

        Bukkit.addRecipe(recipe);
    }
}