package fr.loual.myplugin.recipes;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import fr.loual.myplugin.items.RacePass;

public class RacePassRecipe {

    public static void register(JavaPlugin plugin) {
        NamespacedKey key = new NamespacedKey(plugin, "race_pass");
        ItemStack result = RacePass.create(plugin, 1, "Première course");
        ShapedRecipe recipe = new ShapedRecipe(key, result);

        recipe.shape(
                "   ",
                "HPA",
                "   "
        );

        recipe.setIngredient('H', Material.HAY_BLOCK);
        recipe.setIngredient('P', Material.PAPER);
        recipe.setIngredient('A', Material.APPLE);

        try {
            Bukkit.removeRecipe(key);
        } catch (Exception ignored) {}

        Bukkit.addRecipe(recipe);
    }
}