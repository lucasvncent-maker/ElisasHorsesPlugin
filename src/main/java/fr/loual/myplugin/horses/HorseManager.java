package fr.loual.myplugin.horses;

import fr.loual.myplugin.items.DivineArmor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Horse;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.Particle;

public class HorseManager {

    private final Map<UUID, HorseData> horses = new HashMap<>();

    private static final double MAX_JUMP_MULTIPLIER = 1.3673;
    private static final double MAX_JUMP_LEVEL = 9.0;
    private static final double MAX_SAFE_FALL_DISTANCE = 15.0;

    public HorseData getData(Horse horse) {
        return horses.computeIfAbsent(
                horse.getUniqueId(),
                uuid -> new HorseData(horse)
        );
    }

    public void remove(Horse horse) {
        horses.remove(horse.getUniqueId());
    }

    public void applyStats(Horse horse) {
        applyJumpStats(horse);
        applySpeedStats(horse);
        applyHealthStats(horse);
        applyHorseshoeStats(horse);
    }

    public void applyHealthStats(Horse horse) {
        AttributeInstance healthAttribute = horse.getAttribute(Attribute.MAX_HEALTH);
        HorseData horseData = getData(horse);

        if (healthAttribute == null) { return; }

        double healthLevel = horseData.getHealthLevel();
        double bonusHealth = 2.0 * healthLevel;
        double baseHealth = horseData.getBaseHealth();
        double newHealth = baseHealth + bonusHealth;
        healthAttribute.setBaseValue(newHealth);
    }

    public void applyJumpStats(Horse horse) {
        AttributeInstance jumpAttribute = horse.getAttribute(Attribute.JUMP_STRENGTH);
        HorseData horseData = getData(horse);

        if (jumpAttribute == null) { return; }

        double jumpLevel = horseData.getJumpLevel();
        double multiplier = 1.0 + (0.041 * jumpLevel);
        double baseJump = horseData.getBaseJumpStrength();
        double newJump = baseJump * multiplier;
        jumpAttribute.setBaseValue(newJump);
        applySafeFallDistance(horse, jumpLevel);
    }

    public void applySpeedStats(Horse horse) {
        AttributeInstance speedAttribute = horse.getAttribute(Attribute.MOVEMENT_SPEED);
        HorseData horseData = getData(horse);

        if (speedAttribute == null) { return; }

        double speedLevel = horseData.getSpeedLevel();
        double multiplier = 1.0 + (0.3 * speedLevel);
        double baseSpeed = horseData.getBaseSpeed();
        double newSpeed = baseSpeed * multiplier;
        speedAttribute.setBaseValue(newSpeed);
    }

    public void applyHorseshoeStats(Horse horse) {
        HorseData data = getData(horse);
        AttributeInstance stepAttr = horse.getAttribute(Attribute.STEP_HEIGHT);
        if (stepAttr != null) {
            if ("ROCK".equalsIgnoreCase(data.getHorseshoeType())) {
                stepAttr.setBaseValue(1.05);
            } else {
                stepAttr.setBaseValue(0.6);
            }
        }
    }

    private void applySafeFallDistance(Horse horse, double jumpLevel) { 
        AttributeInstance safeFallAttribute = horse.getAttribute(Attribute.SAFE_FALL_DISTANCE); 

        if (safeFallAttribute == null) { return; } 

        if (jumpLevel >= 5) { 
            safeFallAttribute.setBaseValue(MAX_SAFE_FALL_DISTANCE); 
        } 
    }

    public void tickCareAndComfort(JavaPlugin plugin, Horse horse, long currentTick) {
        HorseData data = getData(horse);

        if (!horse.getPassengers().isEmpty() && horse.getPassengers().getFirst() instanceof Player player) {
            data.updateLastRidden();

            // Voyage ensemble : gain d'affection selon distance (tous les 200 ticks si en mouvement)
            if (currentTick % 200 == 0 && horse.getVelocity().lengthSquared() > 0.01) {
                data.addAffection(0.5);
            }

            // Palier 3 (Lien Vital >= 75) : Régénération lente partagée
            if (data.getBondLevel() >= 3 && currentTick % 100 == 0) {
                AttributeInstance hAttr = horse.getAttribute(Attribute.MAX_HEALTH);
                double maxH = hAttr != null ? hAttr.getValue() : 20.0;
                if (horse.getHealth() < maxH) {
                    horse.setHealth(Math.min(maxH, horse.getHealth() + 1.0));
                }
                if (player.getHealth() < 20.0) {
                    player.setHealth(Math.min(20.0, player.getHealth() + 1.0));
                }
                horse.getWorld().spawnParticle(Particle.HEART, horse.getLocation().add(0, 1.3, 0), 1, 0.2, 0.2, 0.2, 0.05);
            }

            // Fers d'hiver : marche sur la poudreuse
            if ("WINTER".equalsIgnoreCase(data.getHorseshoeType())) {
                Block below = horse.getLocation().getBlock();
                Block under = horse.getLocation().clone().add(0, -0.5, 0).getBlock();
                if (below.getType() == Material.POWDER_SNOW || under.getType() == Material.POWDER_SNOW) {
                    horse.setFreezeTicks(0);
                    Vector v = horse.getVelocity();
                    if (v.getY() < 0) {
                        horse.setVelocity(new Vector(v.getX(), 0.0, v.getZ()));
                    }
                }
            }
        } else {
            // À pied : Vérification du stress face aux monstres (toutes les 40 ticks = 2s)
            if (currentTick % 40 == 0 && data.isAfraidOfMonsters()) {
                horse.getWorld().spawnParticle(Particle.SMOKE, horse.getLocation().add(0, 1.2, 0), 3, 0.2, 0.2, 0.2, 0.02);
                if (currentTick % 120 == 0) {
                    horse.getWorld().playSound(horse.getLocation(), Sound.ENTITY_HORSE_ANGRY, 0.8f, 1.3f);
                }
            }

            // Vérification du confort de l'écurie / box (toutes les 60 ticks = 3s)
            if (currentTick % 60 == 0 && !data.isRested()) {
                Location loc = horse.getLocation();
                Block blockBelow = loc.clone().add(0, -0.2, 0).getBlock();
                boolean onHay = blockBelow.getType() == Material.HAY_BLOCK;

                // Vérifier si abrité (toit au-dessus)
                boolean hasRoof = false;
                for (int y = 2; y <= 5; y++) {
                    if (loc.clone().add(0, y, 0).getBlock().getType().isSolid()) {
                        hasRoof = true;
                        break;
                    }
                }

                // Vérifier s'il y a de l'eau à proximité (chaudron ou eau)
                boolean hasWater = false;
                for (int dx = -2; dx <= 2 && !hasWater; dx++) {
                    for (int dz = -2; dz <= 2 && !hasWater; dz++) {
                        Material mat = loc.clone().add(dx, 0, dz).getBlock().getType();
                        if (mat == Material.WATER || mat == Material.WATER_CAULDRON) {
                            hasWater = true;
                        }
                    }
                }

                if (onHay && hasRoof && hasWater) {
                    // Accorde le statut Bien Reposé pour 15 minutes
                    data.setRestedUntil(System.currentTimeMillis() + 900_000L);
                    data.addAffection(15.0);
                    horse.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc.add(0, 1.2, 0), 8, 0.4, 0.4, 0.4, 0.1);
                    horse.getWorld().playSound(loc, Sound.ENTITY_HORSE_AMBIENT, 1.0f, 1.2f);

                    UUID ownerUuid = data.getOwnerUniqueId();
                    if (ownerUuid != null) {
                        Player owner = org.bukkit.Bukkit.getPlayer(ownerUuid);
                        if (owner != null && owner.isOnline() && plugin instanceof fr.loual.myplugin.MyPlugin mp) {
                            mp.awardAdvancement(owner, "stable_comfort");
                        }
                    }
                }
            }
        }
    }

    public void tickFlyingHorse(JavaPlugin plugin, Horse horse) {
        ItemStack armor = horse.getInventory().getArmor();

        if (!DivineArmor.isDivineArmor(plugin, armor)) {
            return;
        }

        if (horse.getPassengers().isEmpty()) {
            return;
        }

        if (!(horse.getPassengers().getFirst() instanceof Player player)) {
            return;
        }

        if (!player.getCurrentInput().isForward()) {
            horse.setVelocity(new Vector(0, 0, 0));
            return;
        }

        Vector direction = player.getLocation()
                .getDirection()
                .normalize();

        horse.setVelocity(direction.multiply(0.8));

        Location particleLocation = horse.getLocation().clone().add(0, -0.8, 0);

        horse.getWorld().spawnParticle(
                Particle.CLOUD,
                particleLocation,
                5,
                0.3, 0.05, 0.3,
                0.02
        );
    }
}