package fr.loual.myplugin.items;

import java.util.List;

import fr.loual.myplugin.MyPlugin;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

public class RacePass {

    private static final Material baseItem = Material.PAPER;
    private static final String ITEM_ID = "race_pass";

    public static ItemStack create(JavaPlugin plugin, int raceId, String raceName) {
        ItemStack item = new ItemStack(baseItem);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Inscription à la course: " + raceName, NamedTextColor.GOLD));
        CustomModelDataComponent customModelData = meta.getCustomModelDataComponent();
        customModelData.setStrings( List.of("race_pass"));
        meta.setCustomModelDataComponent(customModelData);
        NamespacedKey itemKey = new NamespacedKey(plugin, ITEM_ID);
        meta.getPersistentDataContainer().set(itemKey, PersistentDataType.BYTE, (byte) 1);
        NamespacedKey raceKey = new NamespacedKey(plugin, "race_id");
        meta.getPersistentDataContainer().set(raceKey, PersistentDataType.INTEGER, raceId);
        item.setItemMeta(meta);

        return item;
    }

    public static boolean isRacePass(JavaPlugin plugin, ItemStack item) {
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

        NamespacedKey raceKey = new NamespacedKey(plugin, "race_id");
        NamespacedKey legacyRaceKey = new NamespacedKey("myplugin", "race_id");
        return meta.getPersistentDataContainer().has(raceKey, PersistentDataType.INTEGER)
                || meta.getPersistentDataContainer().has(legacyRaceKey, PersistentDataType.INTEGER);
    }

    public static int getRaceId(MyPlugin plugin, ItemStack item) {
        if (!isRacePass(plugin, item)) {
            return -1;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return -1;

        NamespacedKey raceKey = new NamespacedKey(plugin, "race_id");
        Integer raceId = meta.getPersistentDataContainer().get(raceKey, PersistentDataType.INTEGER);

        if (raceId == null) {
            NamespacedKey legacyRaceKey = new NamespacedKey("myplugin", "race_id");
            raceId = meta.getPersistentDataContainer().get(legacyRaceKey, PersistentDataType.INTEGER);
        }

        if (raceId == null) {
            return -1;
        }

        return raceId;
    }
}