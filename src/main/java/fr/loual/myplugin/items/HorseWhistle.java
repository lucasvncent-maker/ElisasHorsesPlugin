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

public class HorseWhistle {

    public static final String ITEM_ID = "horse_whistle";
    private static final Material BASE_MATERIAL = Material.GOAT_HORN;
    private static final String DISPLAY_NAME = "Sifflet équin";

    public static ItemStack create(JavaPlugin plugin) {
        ItemStack item = new ItemStack(BASE_MATERIAL);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(DISPLAY_NAME, NamedTextColor.AQUA));
        meta.lore(List.of(
                Component.text("Émet un son doux que seul votre destrier reconnaît.", NamedTextColor.GRAY),
                Component.text("Appelle votre cheval lié à vos côtés.", NamedTextColor.YELLOW),
                Component.text("Requiert un niveau d'affection Niveau 2 (50+).", NamedTextColor.DARK_GRAY)
        ));
        meta.setEnchantmentGlintOverride(true);

        NamespacedKey key = new NamespacedKey(plugin, ITEM_ID);
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);

        item.setItemMeta(meta);
        return item;
    }

    public static boolean isHorseWhistle(JavaPlugin plugin, ItemStack item) {
        if (item == null || item.getType() != BASE_MATERIAL) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        NamespacedKey key = new NamespacedKey(plugin, ITEM_ID);
        return meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }
}
