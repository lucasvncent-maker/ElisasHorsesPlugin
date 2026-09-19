package fr.loual.myplugin.listeners;
import java.util.concurrent.ThreadLocalRandom;

import fr.loual.myplugin.MyPlugin;

import fr.loual.myplugin.items.VigorApple;
import fr.loual.myplugin.items.HasteBamboo;
import fr.loual.myplugin.items.HealthyGrass;
import fr.loual.myplugin.items.HorseAnalyzer;
import fr.loual.myplugin.items.RacePass;
import fr.loual.myplugin.items.DivineArmor;

import fr.loual.myplugin.advancements.AdvancementManager;
import org.bukkit.event.inventory.CraftItemEvent;

import fr.loual.myplugin.horses.HorseData;
import org.bukkit.Sound;
import org.bukkit.entity.Horse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.event.player.PlayerInteractEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.Bukkit;
import org.bukkit.util.RayTraceResult;

import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.event.EventPriority;

public class HorseItemListener implements Listener {

    private final MyPlugin plugin;
    private AdvancementManager advancementManager;
    
    private final java.util.Map<java.util.UUID, Long> lastInteractTick = new java.util.HashMap<>();

    public HorseItemListener(MyPlugin plugin) {
        this.plugin = plugin;
        this.advancementManager = plugin.getAdvancementManager();
    }

    @EventHandler
    public void onHorseDamage(EntityDamageEvent event) {

        if (!(event.getEntity() instanceof Horse horse)) {
            return;
        }

        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        ItemStack armor = horse.getInventory().getArmor();

        if (DivineArmor.isDivineArmor(plugin, armor)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {

        if (!(event.getRightClicked() instanceof Horse horse)) {
            return;
        }

        Player player = event.getPlayer();
        EquipmentSlot hand = event.getHand();
        ItemStack item = (hand == EquipmentSlot.HAND)
                ? player.getInventory().getItemInMainHand()
                : player.getInventory().getItemInOffHand();

        if (item == null || item.isEmpty()) {
            return;
        }

        boolean isVigor = VigorApple.isVigorApple(plugin, item);
        boolean isHaste = HasteBamboo.isHasteBamboo(plugin, item);
        boolean isHealthy = HealthyGrass.isHealthyGrass(plugin, item);
        boolean isAnalyzer = HorseAnalyzer.isHorseAnalyzer(plugin, item);

        if (!isVigor && !isHaste && !isHealthy && !isAnalyzer) {
            return;
        }

        event.setCancelled(true);

        long currentTick = Bukkit.getCurrentTick();
        Long lastTick = lastInteractTick.get(player.getUniqueId());
        if (lastTick != null && lastTick == currentTick) {
            return;
        }
        lastInteractTick.put(player.getUniqueId(), currentTick);

        if (isVigor) {
            onVigorApple(plugin, horse, player, item);
        } else if (isHaste) {
            onHasteBamboo(plugin, horse, player, item);
        } else if (isHealthy) {
            onHealthyGrass(plugin, horse, player, item);
        } else if (isAnalyzer) {
            onHorseFound(plugin, horse, player, hand);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Player player = event.getPlayer();
        Action action = event.getAction();

        if (item == null || item.isEmpty()) {
            return;
        }

        if (event.getHand() == EquipmentSlot.HAND && HorseAnalyzer.isHorseAnalyzer(plugin, item) && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
            onHorseAnalyzer(plugin, event, item); 
        }

        if (event.getAction().isRightClick() && RacePass.isRacePass(plugin, item)) {
            onRacePass(plugin, item, player, event);
        }

        if (event.getAction().isRightClick() && player.getVehicle() instanceof Horse horse) {
            boolean isVigor = VigorApple.isVigorApple(plugin, item);
            boolean isHaste = HasteBamboo.isHasteBamboo(plugin, item);
            boolean isHealthy = HealthyGrass.isHealthyGrass(plugin, item);

            if (isVigor || isHaste || isHealthy) {
                event.setCancelled(true);

                long currentTick = Bukkit.getCurrentTick();
                Long lastTick = lastInteractTick.get(player.getUniqueId());
                if (lastTick != null && lastTick == currentTick) {
                    return;
                }
                lastInteractTick.put(player.getUniqueId(), currentTick);

                if (isVigor) {
                    onVigorApple(plugin, horse, player, item);
                } else if (isHaste) {
                    onHasteBamboo(plugin, horse, player, item);
                } else if (isHealthy) {
                    onHealthyGrass(plugin, horse, player, item);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        Player player = event.getPlayer();
        this.advancementManager.discoverRecipe(player, "horse_analyzer");
    }

    @EventHandler
    public void onHorseTame(EntityTameEvent event) {
        if (!(event.getEntity() instanceof Horse)) {
            return;
        }

        if (!(event.getOwner() instanceof Player player)) {
            return;
        }

        plugin.awardAdvancement(player, "adopt_horse");
        player.sendMessage("Félicitations !");
        this.advancementManager.discoverRecipe(player, "horse_analyzer");
    }
 
    @EventHandler
    public void onCraftItem(CraftItemEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack res = (event.getRecipe() != null) ? event.getRecipe().getResult() : null;
        if (res == null) return;

        if (VigorApple.isVigorApple(plugin, res) || res.isSimilar(VigorApple.create(plugin))) {
            this.advancementManager.award(player, "craft_vigor_apple");
            this.advancementManager.discoverRecipe(player, "haste_bamboo");
            return;
        }

        if (HorseAnalyzer.isHorseAnalyzer(plugin, res) || res.isSimilar(HorseAnalyzer.create(plugin))) {
            this.advancementManager.award(player, "craft_horse_analyzer");
            this.advancementManager.discoverRecipe(player, "vigor_apple");
            this.advancementManager.discoverRecipe(player, "race_pass");
            return;
        }

        if (HasteBamboo.isHasteBamboo(plugin, res) || res.isSimilar(HasteBamboo.create(plugin))) {
            this.advancementManager.award(player, "craft_haste_bamboo");
            return;
        }
    }

    public void onRacePass(MyPlugin plugin, ItemStack item, Player player, PlayerInteractEvent event) {
        event.setCancelled(true);
        int raceId = RacePass.getRaceId(plugin, item);
        boolean raceStarted = plugin.getHorseRaceManager().startRace(player, raceId);
        if (raceStarted) {
            this.advancementManager.award(player, "participate_to_race");
            if (player.getGameMode() != GameMode.CREATIVE) {
                item.subtract(1);
            }
        }
    }

    public void onHasteBamboo(MyPlugin plugin, Horse horse, Player player, ItemStack item) {
        HorseData data = plugin.getHorseManager().getData(horse);

        if (data.getSpeedLevel() >= HorseData.MAX_SPEED_LEVEL) {
            player.sendMessage("§cVotre cheval a déjà atteint le niveau de vitesse maximal !");
            return;
        }

        if (player.getGameMode() != GameMode.CREATIVE) {
            item.subtract(1);
        }

        data.addSpeedLevel(1);
        plugin.getHorseManager().applyStats(horse);

        horse.getWorld().spawnParticle(Particle.HEART, horse.getLocation().add(0, 1.2, 0), 7, 0.3, 0.3, 0.3, 0.1);
        horse.getWorld().playSound(horse.getLocation(), Sound.ENTITY_HORSE_EAT, 1.0f, 1.0f);

        player.sendMessage("§6Votre cheval a gagné +1 niveau de vitesse ! (§e" + (int) data.getSpeedLevel() + "§6/§e9§6)");
    }

    public void onHealthyGrass(MyPlugin plugin, Horse horse, Player player, ItemStack item) {
        HorseData data = plugin.getHorseManager().getData(horse);

        if (data.getHealthLevel() >= HorseData.MAX_HEALTH_LEVEL) {
            player.sendMessage("§cVotre cheval a déjà atteint le niveau de santé maximal !");
            return;
        }

        if (player.getGameMode() != GameMode.CREATIVE) {
            item.subtract(1);
        }

        data.addHealthLevel(1);
        plugin.getHorseManager().applyStats(horse);

        AttributeInstance healthAttribute = horse.getAttribute(Attribute.MAX_HEALTH);
        if (healthAttribute != null) {
            horse.setHealth(Math.min(horse.getHealth() + 2.0, healthAttribute.getValue()));
        }

        horse.getWorld().spawnParticle(Particle.HEART, horse.getLocation().add(0, 1.2, 0), 7, 0.3, 0.3, 0.3, 0.1);
        horse.getWorld().playSound(horse.getLocation(), Sound.ENTITY_HORSE_EAT, 1.0f, 1.0f);

        player.sendMessage("§6Votre cheval a gagné +1 niveau de santé ! (§e" + (int) data.getHealthLevel() + "§6/§e9§6)");
    }

    public void onVigorApple(MyPlugin plugin, Horse horse, Player player, ItemStack item) {
        HorseData data = plugin.getHorseManager().getData(horse);

        if (data.getJumpLevel() >= HorseData.MAX_JUMP_LEVEL) {
            player.sendMessage("§cVotre cheval a déjà atteint le niveau de saut maximal !");
            return;
        }

        if (player.getGameMode() != GameMode.CREATIVE) {
            item.subtract(1);
        }

        data.addJumpLevel(1);
        plugin.getHorseManager().applyStats(horse);

        horse.getWorld().spawnParticle(Particle.HEART, horse.getLocation().add(0, 1.2, 0), 7, 0.3, 0.3, 0.3, 0.1);
        horse.getWorld().playSound(horse.getLocation(), Sound.ENTITY_HORSE_EAT, 1.0f, 1.0f);

        player.sendMessage("§6Votre cheval a gagné +1 niveau de saut ! (§e" + (int) data.getJumpLevel() + "§6/§e9§6)");
    }

    public void onHorseAnalyzer(MyPlugin plugin, PlayerInteractEvent event, ItemStack item) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isHandRaised()) {
                return;
            }

            RayTraceResult result = player.getWorld().rayTraceEntities(
                    player.getEyeLocation(),
                    player.getEyeLocation().getDirection(),
                    100,
                    entity -> entity instanceof Horse
            );

            if (result == null || !(result.getHitEntity() instanceof Horse horse)) {
                return;
            }
            
            onHorseFound(plugin, horse, player, event.getHand());
        }, 1L);
    }

    public void onHorseFound(MyPlugin plugin, Horse horse, Player player, EquipmentSlot hand) {
        HorseData data = plugin.getHorseManager().getData(horse);
        Component horseResume = data.getResume();

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        player.sendMessage(horseResume);

        AttributeInstance jumpAttr = horse.getAttribute(Attribute.JUMP_STRENGTH);
        if (jumpAttr != null && jumpAttr.getValue() >= 0.7) {
            this.advancementManager.award(player, "observe_good_jumper");
        }

        if (ThreadLocalRandom.current().nextDouble() < 0.03) {
            ItemStack handItem = (hand != null) ? player.getInventory().getItem(hand) : player.getInventory().getItemInMainHand();

            if (handItem != null && !handItem.isEmpty()) {
                Component itemName = (handItem.hasItemMeta() && handItem.getItemMeta().hasDisplayName())
                        ? handItem.getItemMeta().displayName()
                        : Component.text("Analyseur Équin");

                handItem.subtract(1);

                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                player.sendMessage(Component.text("Pas de chance, votre ")
                        .append(itemName != null ? itemName : Component.text("Analyseur"))
                        .append(Component.text(" s'est cassé...")));
            }
        }  
    }
}