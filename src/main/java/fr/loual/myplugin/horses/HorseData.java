package fr.loual.myplugin.horses;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Horse;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class HorseData {

    private static final double MAX_BASE_SPEED = 0.3375;
    private static final double MAX_BASE_JUMP = 1.0;
    private static final double MAX_BASE_HEALTH = 60.0;

    public static final double MAX_JUMP_LEVEL = 9;
    public static final double MAX_SPEED_LEVEL = 9;
    public static final double MAX_HEALTH_LEVEL = 9;
    public static final double MAX_SCALE_LEVEL = 9;

    private static final NamespacedKey KEY_JUMP_LEVEL = new NamespacedKey("elisashorses", "jump_level");
    private static final NamespacedKey LEGACY_KEY_JUMP_LEVEL = new NamespacedKey("myplugin", "jump_level");

    private static final NamespacedKey KEY_SPEED_LEVEL = new NamespacedKey("elisashorses", "speed_level");
    private static final NamespacedKey LEGACY_KEY_SPEED_LEVEL = new NamespacedKey("myplugin", "speed_level");

    private static final NamespacedKey KEY_HEALTH_LEVEL = new NamespacedKey("elisashorses", "health_level");
    private static final NamespacedKey LEGACY_KEY_HEALTH_LEVEL = new NamespacedKey("myplugin", "health_level");

    private static final NamespacedKey KEY_BASE_JUMP = new NamespacedKey("elisashorses", "base_jump");
    private static final NamespacedKey LEGACY_KEY_BASE_JUMP = new NamespacedKey("myplugin", "base_jump");

    private static final NamespacedKey KEY_BASE_SPEED = new NamespacedKey("elisashorses", "base_speed");
    private static final NamespacedKey LEGACY_KEY_BASE_SPEED = new NamespacedKey("myplugin", "base_speed");

    private static final NamespacedKey KEY_BASE_HEALTH = new NamespacedKey("elisashorses", "base_health");
    private static final NamespacedKey LEGACY_KEY_BASE_HEALTH = new NamespacedKey("myplugin", "base_health");

    private double jumpLevel;
    private double speedLevel;
    private double healthLevel;
    private double scaleLevel;
    private boolean canFly;
    private double baseJumpStrength;
    private double baseSpeed;
    private double baseHealth;
    private final Horse horse;

    public HorseData(Horse horse) {
        this.horse = horse;
        loadFromHorse();
    }

    private double getDoubleFromPdc(PersistentDataContainer pdc, NamespacedKey key, NamespacedKey legacyKey) {
        if (pdc.has(key, PersistentDataType.DOUBLE)) {
            Double val = pdc.get(key, PersistentDataType.DOUBLE);
            if (val != null) return val;
        }
        if (pdc.has(key, PersistentDataType.INTEGER)) {
            Integer val = pdc.get(key, PersistentDataType.INTEGER);
            if (val != null) return val.doubleValue();
        }
        if (legacyKey != null) {
            if (pdc.has(legacyKey, PersistentDataType.DOUBLE)) {
                Double val = pdc.get(legacyKey, PersistentDataType.DOUBLE);
                if (val != null) return val;
            }
            if (pdc.has(legacyKey, PersistentDataType.INTEGER)) {
                Integer val = pdc.get(legacyKey, PersistentDataType.INTEGER);
                if (val != null) return val.doubleValue();
            }
        }
        return -1.0;
    }

    private void loadFromHorse() {
        PersistentDataContainer pdc = horse.getPersistentDataContainer();

        double loadedJump = getDoubleFromPdc(pdc, KEY_JUMP_LEVEL, LEGACY_KEY_JUMP_LEVEL);
        this.jumpLevel = (loadedJump >= 0) ? loadedJump : 0;

        double loadedSpeed = getDoubleFromPdc(pdc, KEY_SPEED_LEVEL, LEGACY_KEY_SPEED_LEVEL);
        this.speedLevel = (loadedSpeed >= 0) ? loadedSpeed : 0;

        double loadedHealth = getDoubleFromPdc(pdc, KEY_HEALTH_LEVEL, LEGACY_KEY_HEALTH_LEVEL);
        this.healthLevel = (loadedHealth >= 0) ? loadedHealth : 0;

        this.scaleLevel = 0;
        this.canFly = false;

        // Base jump strength
        double savedBaseJump = getDoubleFromPdc(pdc, KEY_BASE_JUMP, LEGACY_KEY_BASE_JUMP);
        if (savedBaseJump > 0) {
            this.baseJumpStrength = savedBaseJump;
        } else {
            AttributeInstance jumpAttr = horse.getAttribute(Attribute.JUMP_STRENGTH);
            double currentJump = jumpAttr != null ? jumpAttr.getBaseValue() : 0.7;
            this.baseJumpStrength = (this.jumpLevel > 0) ? (currentJump / (1.0 + (0.041 * this.jumpLevel))) : currentJump;
            pdc.set(KEY_BASE_JUMP, PersistentDataType.DOUBLE, this.baseJumpStrength);
        }

        // Base speed
        double savedBaseSpeed = getDoubleFromPdc(pdc, KEY_BASE_SPEED, LEGACY_KEY_BASE_SPEED);
        if (savedBaseSpeed > 0) {
            this.baseSpeed = savedBaseSpeed;
        } else {
            AttributeInstance speedAttr = horse.getAttribute(Attribute.MOVEMENT_SPEED);
            double currentSpeed = speedAttr != null ? speedAttr.getBaseValue() : 0.225;
            this.baseSpeed = (this.speedLevel > 0) ? (currentSpeed / (1.0 + (0.3 * this.speedLevel))) : currentSpeed;
            pdc.set(KEY_BASE_SPEED, PersistentDataType.DOUBLE, this.baseSpeed);
        }

        // Base health
        double savedBaseHealth = getDoubleFromPdc(pdc, KEY_BASE_HEALTH, LEGACY_KEY_BASE_HEALTH);
        if (savedBaseHealth > 0) {
            this.baseHealth = savedBaseHealth;
        } else {
            AttributeInstance healthAttr = horse.getAttribute(Attribute.MAX_HEALTH);
            double currentHealth = healthAttr != null ? healthAttr.getBaseValue() : 20.0;
            this.baseHealth = (this.healthLevel > 0) ? Math.max(1.0, currentHealth - (2.0 * this.healthLevel)) : currentHealth;
            pdc.set(KEY_BASE_HEALTH, PersistentDataType.DOUBLE, this.baseHealth);
        }
    }

    public double getHealthLevel() {
        return healthLevel;
    }

    public double getJumpLevel() {
        return jumpLevel;
    }

    public double getScaleLevel() {
        return scaleLevel;
    }

    public double getSpeedLevel() {
        return speedLevel;
    }

    public void addHealthLevel(double amount) {
        healthLevel = Math.min(MAX_HEALTH_LEVEL, healthLevel + amount);
        horse.getPersistentDataContainer().set(KEY_HEALTH_LEVEL, PersistentDataType.DOUBLE, healthLevel);
    }

    public void addJumpLevel(double amount) {
        jumpLevel = Math.min(MAX_JUMP_LEVEL, jumpLevel + amount);
        horse.getPersistentDataContainer().set(KEY_JUMP_LEVEL, PersistentDataType.DOUBLE, jumpLevel);
    }

    public void addSpeedLevel(double amount) {
        speedLevel = Math.min(MAX_SPEED_LEVEL, speedLevel + amount);
        horse.getPersistentDataContainer().set(KEY_SPEED_LEVEL, PersistentDataType.DOUBLE, speedLevel);
    }

    public void addScaleLevel(double amount) {
        scaleLevel = Math.min(MAX_SCALE_LEVEL, scaleLevel + amount);
    }

    public boolean canFly() {
        return canFly;
    }

    public void setCanFly(boolean canFly) {
        this.canFly = canFly;
    }

    public double getBaseJumpStrength() {
        return baseJumpStrength;
    }

    public double getBaseSpeed() {
        return baseSpeed;
    }

    public double getBaseHealth() {
        return baseHealth;
    }

    public String getHorseName() {
        Component customName = this.horse.customName();
        if (customName == null) {
            return "Cheval";
        }
        return PlainTextComponentSerializer.plainText().serialize(customName);
    }

    public Component getResume() {
        AttributeInstance speedAttr = this.horse.getAttribute(Attribute.MOVEMENT_SPEED);
        double totalSpeed = speedAttr != null ? speedAttr.getValue() : getBaseSpeed();
        AttributeInstance jumpAttr = this.horse.getAttribute(Attribute.JUMP_STRENGTH);
        double totalJump = jumpAttr != null ? jumpAttr.getValue() : getBaseJumpStrength();
        AttributeInstance healthAttr = this.horse.getAttribute(Attribute.MAX_HEALTH);
        double totalHealth = healthAttr != null ? healthAttr.getValue() : getBaseHealth();

        return Component.text()
                .append(Component.text("._.-. " + getHorseName() + " .-._.\n", NamedTextColor.GOLD))
                .append(Component.text("- Vitesse de base: ", NamedTextColor.GRAY))
                .append(Component.text("%.1f%%\n".formatted(getBaseSpeed() / MAX_BASE_SPEED * 100), NamedTextColor.WHITE))
                .append(Component.text("- Saut de base: ", NamedTextColor.GRAY))
                .append(Component.text("%.1f%%\n".formatted(getBaseJumpStrength() / MAX_BASE_JUMP * 100), NamedTextColor.WHITE))
                .append(Component.text("- Points de vie de base: ", NamedTextColor.GRAY))
                .append(Component.text("%.1f%%\n\n".formatted(getBaseHealth() / MAX_BASE_HEALTH * 100), NamedTextColor.WHITE))
                .append(Component.text("Bonus:\n", NamedTextColor.GREEN))
                .append(Component.text("- Vitesse: ", NamedTextColor.GRAY))
                .append(Component.text("%d/9 ".formatted((int) getSpeedLevel()), NamedTextColor.AQUA))
                .append(Component.text("([Total] %.2f m/s)\n".formatted(totalSpeed), NamedTextColor.WHITE))
                .append(Component.text("- Saut: ", NamedTextColor.GRAY))
                .append(Component.text("%d/9 ".formatted((int) getJumpLevel()), NamedTextColor.AQUA))
                .append(Component.text("([Total] %.2f m)\n".formatted(totalJump), NamedTextColor.WHITE))
                .append(Component.text("- PV: ", NamedTextColor.GRAY))
                .append(Component.text("%d/9 ".formatted((int) getHealthLevel()), NamedTextColor.AQUA))
                .append(Component.text("([Total] %.1f PV)".formatted(totalHealth), NamedTextColor.WHITE))
                .build();
    }
}