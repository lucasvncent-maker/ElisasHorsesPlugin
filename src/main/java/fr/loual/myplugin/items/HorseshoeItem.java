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

public class HorseshoeItem {

    public enum Type {
        WINTER("horseshoe_winter", "Fers d'hiver", NamedTextColor.AQUA, Material.IRON_NUGGET,
                List.of(
                        Component.text("Sabots renforcés pour les climats glacés.", NamedTextColor.GRAY),
                        Component.text("Ne glisse pas sur la glace et marche sur la poudreuse.", NamedTextColor.AQUA),
                        Component.text("Clic droit sur votre cheval pour équiper.", NamedTextColor.YELLOW)
                )),
        FEATHER("horseshoe_feather", "Fers légers", NamedTextColor.YELLOW, Material.GOLD_NUGGET,
                List.of(
                        Component.text("Forgés avec légèreté pour amortir les réceptions.", NamedTextColor.GRAY),
                        Component.text("Réduit de 50% les dégâts de chute subis.", NamedTextColor.YELLOW),
                        Component.text("Clic droit sur votre cheval pour équiper.", NamedTextColor.GOLD)
                )),
        ROCK("horseshoe_rock", "Fers de roche", NamedTextColor.GOLD, Material.COPPER_INGOT,
                List.of(
                        Component.text("Sabots tout-terrain à forte adhérence.", NamedTextColor.GRAY),
                        Component.text("Franchit les dénivelés d'1 bloc sans effort (auto-step).", NamedTextColor.GOLD),
                        Component.text("Clic droit sur votre cheval pour équiper.", NamedTextColor.YELLOW)
                ));

        public final String id;
        public final String name;
        public final NamedTextColor color;
        public final Material material;
        public final List<Component> lore;

        Type(String id, String name, NamedTextColor color, Material material, List<Component> lore) {
            this.id = id;
            this.name = name;
            this.color = color;
            this.material = material;
            this.lore = lore;
        }

        public static Type fromId(String id) {
            if (id == null) return null;
            for (Type t : values()) {
                if (t.id.equalsIgnoreCase(id) || t.name().equalsIgnoreCase(id)) return t;
            }
            return null;
        }
    }

    public static final String KEY_TYPE = "horseshoe_type";

    public static ItemStack create(JavaPlugin plugin, Type type) {
        ItemStack item = new ItemStack(type.material);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(type.name, type.color));
        meta.lore(type.lore);
        meta.setEnchantmentGlintOverride(true);

        NamespacedKey key = new NamespacedKey(plugin, KEY_TYPE);
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, type.name());

        item.setItemMeta(meta);
        return item;
    }

    public static Type getHorseshoeType(JavaPlugin plugin, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, KEY_TYPE);
        String typeStr = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        return Type.fromId(typeStr);
    }
}
