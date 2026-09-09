package fr.loual.myplugin.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class HorseAnalyzer {

    private static final String ITEM_ID = "horse_analyzer";
    private static final Material baseItem = Material.SPYGLASS;
    private static final String displayName = "Analyseur Equin";
    private static final NamedTextColor color = NamedTextColor.GOLD;
    private static final Boolean isEnchanted = true;

    public static ItemStack create(JavaPlugin plugin) {
        ItemStack item = new ItemStack(baseItem);
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

    public static boolean isHorseAnalyzer(JavaPlugin plugin, ItemStack item) {
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
            return plain.toLowerCase().contains("analyseur");
        }

        return false;
    }
}