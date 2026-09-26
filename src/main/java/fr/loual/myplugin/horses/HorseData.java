package fr.loual.myplugin.horses;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Monster;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;

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

    // Nouveaux champs pour le lien, les soins et l'équipement
    private static final NamespacedKey KEY_AFFECTION = new NamespacedKey("elisashorses", "affection");
    private static final NamespacedKey KEY_LAST_PET = new NamespacedKey("elisashorses", "last_pet");
    private static final NamespacedKey KEY_LAST_BRUSH = new NamespacedKey("elisashorses", "last_brush");
    private static final NamespacedKey KEY_LAST_RIDDEN = new NamespacedKey("elisashorses", "last_ridden");
    private static final NamespacedKey KEY_HORSESHOE = new NamespacedKey("elisashorses", "horseshoe");
    private static final NamespacedKey KEY_HAS_SADDLEBAG = new NamespacedKey("elisashorses", "has_saddlebag");
    private static final NamespacedKey KEY_SADDLEBAG_DATA = new NamespacedKey("elisashorses", "saddlebag_data");
    private static final NamespacedKey KEY_RESTED_UNTIL = new NamespacedKey("elisashorses", "rested_until");
    private static final NamespacedKey KEY_OWNER_UUID = new NamespacedKey("elisashorses", "owner_uuid");
    private static final NamespacedKey KEY_OWNER_NAME = new NamespacedKey("elisashorses", "owner_name");

    private double jumpLevel;
    private double speedLevel;
    private double healthLevel;
    private double scaleLevel;
    private boolean canFly;
    private double baseJumpStrength;
    private double baseSpeed;
    private double baseHealth;

    private double affection;
    private long lastPetTime;
    private long lastBrushTime;
    private long lastRiddenTime;
    private String horseshoeType;
    private boolean hasSaddlebag;
    private long restedUntil;
    private java.util.UUID ownerUuid;
    private String ownerName;

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

        // Affection & Soins
        if (pdc.has(KEY_AFFECTION, PersistentDataType.DOUBLE)) {
            this.affection = pdc.get(KEY_AFFECTION, PersistentDataType.DOUBLE);
        } else {
            this.affection = 0.0;
        }

        this.lastPetTime = pdc.getOrDefault(KEY_LAST_PET, PersistentDataType.LONG, 0L);
        this.lastBrushTime = pdc.getOrDefault(KEY_LAST_BRUSH, PersistentDataType.LONG, 0L);
        this.lastRiddenTime = pdc.getOrDefault(KEY_LAST_RIDDEN, PersistentDataType.LONG, System.currentTimeMillis());
        this.horseshoeType = pdc.getOrDefault(KEY_HORSESHOE, PersistentDataType.STRING, "NONE");
        this.hasSaddlebag = pdc.getOrDefault(KEY_HAS_SADDLEBAG, PersistentDataType.BYTE, (byte) 0) == (byte) 1;
        this.restedUntil = pdc.getOrDefault(KEY_RESTED_UNTIL, PersistentDataType.LONG, 0L);

        if (pdc.has(KEY_OWNER_UUID, PersistentDataType.STRING)) {
            try {
                this.ownerUuid = java.util.UUID.fromString(pdc.get(KEY_OWNER_UUID, PersistentDataType.STRING));
                this.ownerName = pdc.getOrDefault(KEY_OWNER_NAME, PersistentDataType.STRING, "Inconnu");
            } catch (Exception ignored) {}
        } else if (horse.getOwner() != null) {
            this.ownerUuid = horse.getOwner().getUniqueId();
            this.ownerName = (horse.getOwner().getName() != null) ? horse.getOwner().getName() : "Inconnu";
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

    // Gestion de l'affection
    public double getAffection() {
        return affection;
    }

    public void setAffection(double affection) {
        this.affection = Math.max(0.0, Math.min(100.0, affection));
        horse.getPersistentDataContainer().set(KEY_AFFECTION, PersistentDataType.DOUBLE, this.affection);
    }

    public void addAffection(double amount) {
        setAffection(this.affection + amount);
    }

    public int getBondLevel() {
        if (affection >= 100) return 4;
        if (affection >= 75) return 3;
        if (affection >= 50) return 2;
        if (affection >= 25) return 1;
        return 0;
    }

    public boolean canPet() {
        return (System.currentTimeMillis() - lastPetTime) >= 180_000L; // 3 minutes
    }

    public void pet() {
        this.lastPetTime = System.currentTimeMillis();
        horse.getPersistentDataContainer().set(KEY_LAST_PET, PersistentDataType.LONG, lastPetTime);
        addAffection(5.0);
    }

    public boolean canBrush() {
        return (System.currentTimeMillis() - lastBrushTime) >= 300_000L; // 5 minutes
    }

    public void brush() {
        this.lastBrushTime = System.currentTimeMillis();
        horse.getPersistentDataContainer().set(KEY_LAST_BRUSH, PersistentDataType.LONG, lastBrushTime);
        addAffection(10.0);
    }

    public void updateLastRidden() {
        this.lastRiddenTime = System.currentTimeMillis();
        horse.getPersistentDataContainer().set(KEY_LAST_RIDDEN, PersistentDataType.LONG, lastRiddenTime);
    }

    public long getLastRiddenTime() {
        return lastRiddenTime;
    }

    // Gestion du propriétaire unique
    public java.util.UUID getOwnerUniqueId() {
        return ownerUuid;
    }

    public String getOwnerName() {
        return (ownerName != null && !ownerName.isEmpty()) ? ownerName : "Aucun (Sauvage)";
    }

    public boolean hasOwner() {
        return ownerUuid != null;
    }

    public void setOwner(org.bukkit.entity.Player player) {
        if (player == null) {
            this.ownerUuid = null;
            this.ownerName = null;
            horse.getPersistentDataContainer().remove(KEY_OWNER_UUID);
            horse.getPersistentDataContainer().remove(KEY_OWNER_NAME);
            horse.setOwner(null);
            return;
        }
        this.ownerUuid = player.getUniqueId();
        this.ownerName = player.getName();
        horse.getPersistentDataContainer().set(KEY_OWNER_UUID, PersistentDataType.STRING, ownerUuid.toString());
        horse.getPersistentDataContainer().set(KEY_OWNER_NAME, PersistentDataType.STRING, ownerName);
        horse.setTamed(true);
        horse.setOwner(player);
    }

    public boolean isOwner(org.bukkit.entity.Player player) {
        if (player == null) return false;
        if (player.isOp()) return true;
        if (ownerUuid == null) return false;
        return ownerUuid.equals(player.getUniqueId());
    }

    // Fers à cheval
    public String getHorseshoeType() {
        return (horseshoeType != null) ? horseshoeType : "NONE";
    }

    public void setHorseshoeType(String type) {
        this.horseshoeType = (type != null) ? type : "NONE";
        horse.getPersistentDataContainer().set(KEY_HORSESHOE, PersistentDataType.STRING, this.horseshoeType);
    }

    // Sacoche de selle
    public boolean hasSaddlebag() {
        return hasSaddlebag;
    }

    public void setHasSaddlebag(boolean hasSaddlebag) {
        this.hasSaddlebag = hasSaddlebag;
        horse.getPersistentDataContainer().set(KEY_HAS_SADDLEBAG, PersistentDataType.BYTE, (byte) (hasSaddlebag ? 1 : 0));
    }

    public ItemStack[] getSaddlebagItems() {
        String data = horse.getPersistentDataContainer().get(KEY_SADDLEBAG_DATA, PersistentDataType.STRING);
        return itemStackArrayFromBase64(data);
    }

    public void saveSaddlebagItems(ItemStack[] items) {
        String data = itemStackArrayToBase64(items);
        horse.getPersistentDataContainer().set(KEY_SADDLEBAG_DATA, PersistentDataType.STRING, data);
    }

    // Confort et repos
    public boolean isRested() {
        return System.currentTimeMillis() < restedUntil;
    }

    public void setRestedUntil(long timestamp) {
        this.restedUntil = timestamp;
        horse.getPersistentDataContainer().set(KEY_RESTED_UNTIL, PersistentDataType.LONG, timestamp);
    }

    public boolean isAfraidOfMonsters() {
        long timeSinceRidden = System.currentTimeMillis() - lastRiddenTime;
        if (timeSinceRidden < 60_000L) {
            return false; // Cavalier actif
        }
        if (affection >= 75) {
            return false; // Trop brave et fidèle pour paniquer
        }
        java.util.Collection<Monster> nearbyMonsters = horse.getWorld().getNearbyEntitiesByType(Monster.class, horse.getLocation(), 8.0);
        return !nearbyMonsters.isEmpty();
    }

    public String getMood() {
        if (isAfraidOfMonsters()) {
            return "Apeuré (Monstres proches !)";
        }
        if (isRested()) {
            return "En pleine forme (Reposé au box)";
        }
        AttributeInstance healthAttr = horse.getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = healthAttr != null ? healthAttr.getValue() : 20.0;
        if (horse.getHealth() < maxHealth * 0.45) {
            return "Affamé / Blessé";
        }
        if (affection >= 85) {
            return "Comblé d'affection";
        }
        if (affection >= 50) {
            return "Joyeux et loyal";
        }
        if (System.currentTimeMillis() - lastPetTime < 120_000L) {
            return "Détendu (Caresse récente)";
        }
        return "Paisible";
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

        int bondLevel = getBondLevel();
        String bondLevelName = switch (bondLevel) {
            case 4 -> "Niv 4 - Fidélité Protectrice";
            case 3 -> "Niv 3 - Lien Vital";
            case 2 -> "Niv 2 - Appel au Sifflet";
            case 1 -> "Niv 1 - Confiance Sereine";
            default -> "Niv 0 - Méfiant";
        };

        String horseshoeDisplay = switch (getHorseshoeType()) {
            case "WINTER" -> "Fers d'hiver (Anti-glisse & Poudreuse)";
            case "FEATHER" -> "Fers légers (-50% Chute)";
            case "ROCK" -> "Fers de roche (Auto-step 1 bloc)";
            default -> "Aucun";
        };

        return Component.text()
                .append(Component.text("._.-. " + getHorseName() + " .-._.\n", NamedTextColor.GOLD))
                .append(Component.text("Propriétaire : ", NamedTextColor.GRAY))
                .append(Component.text(getOwnerName() + "\n", hasOwner() ? NamedTextColor.YELLOW : NamedTextColor.DARK_GRAY))
                .append(Component.text("Humeur : ", NamedTextColor.GRAY))
                .append(Component.text(getMood() + "\n", isAfraidOfMonsters() ? NamedTextColor.RED : NamedTextColor.GREEN))
                .append(Component.text("Lien d'amitié : ", NamedTextColor.GRAY))
                .append(Component.text("%.0f/100 ".formatted(affection), NamedTextColor.LIGHT_PURPLE))
                .append(Component.text("(%s)\n".formatted(bondLevelName), NamedTextColor.YELLOW))
                .append(Component.text("Fers équipés : ", NamedTextColor.GRAY))
                .append(Component.text(horseshoeDisplay + "\n", NamedTextColor.AQUA))
                .append(Component.text("Sacoche de selle : ", NamedTextColor.GRAY))
                .append(Component.text(hasSaddlebag ? "Installée (9 slots)\n\n" : "Aucune\n\n", hasSaddlebag ? NamedTextColor.GREEN : NamedTextColor.DARK_GRAY))
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

    // Sérialisation sécurisée de l'inventaire en Base64
    public static String itemStackArrayToBase64(ItemStack[] items) {
        if (items == null) return "";
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeInt(items.length);
            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            return "";
        }
    }

    public static ItemStack[] itemStackArrayFromBase64(String data) {
        if (data == null || data.isEmpty()) return new ItemStack[9];
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            int size = dataInput.readInt();
            ItemStack[] items = new ItemStack[size];
            for (int i = 0; i < size; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }
            dataInput.close();
            return items;
        } catch (Exception e) {
            return new ItemStack[9];
        }
    }
}