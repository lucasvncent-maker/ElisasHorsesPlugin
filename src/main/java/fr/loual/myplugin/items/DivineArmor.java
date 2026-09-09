package fr.loual.myplugin.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import java.util.List;

public class DivineArmor {

    private static final String ITEM_ID = "divine_armor";
    private static final NamedTextColor color = NamedTextColor.GOLD;
    private static final Boolean isEnchanted = true;
    private static final String displayName = "Armure divine";
    private static final Material baseItem = Material.DIAMOND_HORSE_ARMOR;

    public static ItemStack create(JavaPlugin plugin) {
        ItemStack item = new ItemStack(baseItem);
        ItemMeta meta = item.getItemMeta();

        CustomModelDataComponent customModelData = meta.getCustomModelDataComponent();
        customModelData.setStrings(List.of("divine_armor"));
        meta.setCustomModelDataComponent(customModelData);

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

    public static boolean isDivineArmor(JavaPlugin plugin, ItemStack item) {
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