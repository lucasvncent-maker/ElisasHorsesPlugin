package fr.loual.myplugin.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class SaddlebagItem {

    public static final String ITEM_ID = "saddlebag";
    private static final Material BASE_MATERIAL = Material.CHEST;
    private static final String DISPLAY_NAME = "Sacoche de selle";

    public static ItemStack create(JavaPlugin plugin) {
        ItemStack item = new ItemStack(BASE_MATERIAL);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(DISPLAY_NAME, NamedTextColor.GOLD));
        meta.lore(List.of(
                Component.text("Se fixe sur votre cheval pour lui ajouter un inventaire.", NamedTextColor.GRAY),
                Component.text("Clic droit sur le cheval pour équiper.", NamedTextColor.YELLOW),
                Component.text("Shift + Clic droit à main nue pour ouvrir.", NamedTextColor.AQUA)
        ));
        meta.setEnchantmentGlintOverride(true);

        NamespacedKey key = new NamespacedKey(plugin, ITEM_ID);
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);

        item.setItemMeta(meta);
        return item;
    }

    public static boolean isSaddlebag(JavaPlugin plugin, ItemStack item) {
        if (item == null || item.getType() != BASE_MATERIAL) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        NamespacedKey key = new NamespacedKey(plugin, ITEM_ID);
        return meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }
}
