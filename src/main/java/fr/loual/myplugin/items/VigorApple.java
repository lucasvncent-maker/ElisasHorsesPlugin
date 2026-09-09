package fr.loual.myplugin.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class VigorApple {

    private static final String ITEM_ID = "vigor_apple";
    private static final NamedTextColor color = NamedTextColor.GOLD;
    private static final Boolean isEnchanted = true;
    private static final String displayName = "Pomme de vigueur";
    private static final Material baseItem = Material.APPLE;

    public static ItemStack create(JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.APPLE);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(
            Component.text(displayName, color)
        );

        meta.setEnchantmentGlintOverride(isEnchanted);

        NamespacedKey key = new NamespacedKey(plugin, ITEM_ID);
        meta.getPersistentDataContainer().set(
                key,
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);

        return item;
    }

    public static boolean isVigorApple(JavaPlugin plugin, ItemStack item) {
        if (item == null || item.getType() != baseItem) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return false;
        }

        NamespacedKey key = new NamespacedKey(plugin, ITEM_ID);
        NamespacedKey legacyKey = new NamespacedKey("myplugin", ITEM_ID);

        if (meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE)
                || meta.getPersistentDataContainer().has(legacyKey, PersistentDataType.BYTE)) {
            return true;
        }

        if (meta.hasDisplayName()) {
            String plain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(meta.displayName());
            return plain.equalsIgnoreCase(displayName);
        }

        return false;
    }
}