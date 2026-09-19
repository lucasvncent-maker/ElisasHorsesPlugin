package fr.loual.myplugin.recipes;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import fr.loual.myplugin.items.DivineArmor;

public class DivineArmorRecipe {

    public static void register(JavaPlugin plugin) {
        NamespacedKey key = new NamespacedKey(plugin, "divine_armor");

        ItemStack result = DivineArmor.create(plugin);

        ShapedRecipe recipe = new ShapedRecipe(key, result);

        recipe.shape(
                " E ",
                "SAU",
                "DDD"
        );

        recipe.setIngredient('E', Material.ELYTRA);
        recipe.setIngredient('S', Material.NETHER_STAR);
        recipe.setIngredient('A', Material.DIAMOND_HORSE_ARMOR);
        recipe.setIngredient('U', Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
        recipe.setIngredient('D', Material.DIAMOND);

        try {
            Bukkit.removeRecipe(key);
        } catch (Exception ignored) {}

        Bukkit.addRecipe(recipe);
    }
}