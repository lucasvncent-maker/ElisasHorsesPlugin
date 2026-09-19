package fr.loual.myplugin.recipes;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import fr.loual.myplugin.items.HasteBamboo;

public class HasteBambooRecipe {

    public static void register(JavaPlugin plugin) {
        NamespacedKey key = new NamespacedKey(plugin, "haste_bamboo");

        ItemStack result = HasteBamboo.create(plugin);

        ShapedRecipe recipe = new ShapedRecipe(key, result);

        recipe.shape(
                "SAS",
                "RBR",
                "SAS"
        );

        recipe.setIngredient('R', Material.BLAZE_ROD);
        recipe.setIngredient('S', Material.SUGAR);
        recipe.setIngredient('A', Material.AMETHYST_SHARD);
        recipe.setIngredient('B', Material.BAMBOO);

        try {
            Bukkit.removeRecipe(key);
        } catch (Exception ignored) {}

        Bukkit.addRecipe(recipe);
    }
}