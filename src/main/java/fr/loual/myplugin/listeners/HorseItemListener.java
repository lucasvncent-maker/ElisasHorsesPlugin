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

public class HorseItemListener implements Listener {

    private final MyPlugin plugin;
    private AdvancementManager advancementManager;
    
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

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {

        if (!(event.getRightClicked() instanceof Horse horse)) {
            return;
        }

        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();

        if (VigorApple.isVigorApple(plugin, item)) {
            onVigorApple(plugin, horse, event, item);
        }

        if (HasteBamboo.isHasteBamboo(plugin, item)) {
            onHasteBamboo(plugin, horse, event, item);
        }

        if (HealthyGrass.isHealthyGrass(plugin, item)) {
            onHealthyGrass(plugin, horse, event, item);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Player player = event.getPlayer();
        Action action = event.getAction();

        if (event.getHand() == EquipmentSlot.HAND && HorseAnalyzer.isHorseAnalyzer(plugin, item) && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
            onHorseAnalyzer(plugin, event, item); 
        }

        if (event.getAction().isRightClick() && RacePass.isRacePass(plugin, item)) {
            onRacePass(plugin, item, player, event);
        }

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

        if (event.getRecipe().getResult().isSimilar(VigorApple.create(plugin))) {
            this.advancementManager.award(player, "craft_vigor_apple");
            this.advancementManager.discoverRecipe(player, "haste_bamboo");
            return;
        }

        if (event.getRecipe().getResult().isSimilar(HorseAnalyzer.create(plugin))) {
            this.advancementManager.award(player, "craft_horse_analyzer");
            this.advancementManager.discoverRecipe(player, "vigor_apple");
            this.advancementManager.discoverRecipe(player, "race_pass");
            return;
        }

        if (event.getRecipe().getResult().isSimilar(HasteBamboo.create(plugin))) {
            this.advancementManager.award(player, "craft_haste_bamboo");
            return;
        }
    }

    public void onRacePass(MyPlugin plugin, ItemStack item, Player player, PlayerInteractEvent event) {
        int raceId = RacePass.getRaceId(plugin, item);
        boolean raceStarted = plugin.getHorseRaceManager().startRace(player, raceId);
        event.setCancelled(true);
        if (raceStarted) {
            this.advancementManager.award(player, "participate_to_race");
            item.setAmount(item.getAmount() - 1);
        }
    }

    public void onHasteBamboo(MyPlugin plugin, Horse horse, PlayerInteractEntityEvent event, ItemStack item) {
        HorseData data = plugin.getHorseManager().getData(horse);

        item.setAmount(item.getAmount() - 1);
        if (data.getSpeedLevel() == data.MAX_SPEED_LEVEL) {
            event.getPlayer().sendMessage("§6Votre cheval a déjà atteint le niveau de vitesse maximal !");
            return;
        }
        
        data.addSpeedLevel(1);
        plugin.getHorseManager().applyStats(horse);

        event.getPlayer().sendMessage("§6Votre cheval a gagné +1 niveau de vitesse !");
        event.setCancelled(true);
    }

    public void onHealthyGrass(MyPlugin plugin, Horse horse, PlayerInteractEntityEvent event, ItemStack item) {
        HorseData data = plugin.getHorseManager().getData(horse);

        item.setAmount(item.getAmount() - 1);
        if (data.getHealthLevel() == data.MAX_HEALTH_LEVEL) {
            event.getPlayer().sendMessage("§6Votre cheval a déjà atteint le niveau de santé maximal !");
            return;
        }
        
        data.addHealthLevel(1);
        plugin.getHorseManager().applyStats(horse);

        event.getPlayer().sendMessage("§6Votre cheval a gagné +1 niveau de santé !");
        event.setCancelled(true);
    }

    public void onVigorApple(MyPlugin plugin, Horse horse, PlayerInteractEntityEvent event, ItemStack item) {
        HorseData data = plugin.getHorseManager().getData(horse);

        item.setAmount(item.getAmount() - 1);
        if (data.getJumpLevel() == data.MAX_JUMP_LEVEL) {
            event.getPlayer().sendMessage("§6Votre cheval a déjà atteint le niveau de saut maximal !");
            return;
        }
        
        data.addJumpLevel(1);
        plugin.getHorseManager().applyStats(horse);

        event.getPlayer().sendMessage("§6Votre cheval a gagné +1 niveau de saut !");
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
            
            Horse observedHorse = (Horse) result.getHitEntity();
            onHorseFound(plugin, observedHorse, event, item);

    

        }, 1L);
    }

    public void onHorseFound(MyPlugin plugin, Horse horse, PlayerInteractEvent event, ItemStack item) {
        HorseData data = plugin.getHorseManager().getData(horse);
        Component horseResume = data.getResume();

        event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        event.getPlayer().sendMessage(horseResume);

        AttributeInstance jumpAttr = horse.getAttribute(Attribute.JUMP_STRENGTH);
        if (jumpAttr != null && jumpAttr.getValue() >= 0.7) {
            this.advancementManager.award(event.getPlayer(), "observe_good_jumper");
        }

        if (ThreadLocalRandom.current().nextDouble() < 0.03) {
            EquipmentSlot hand = event.getHand();
            ItemStack handItem = event.getPlayer().getInventory().getItem(hand);

            if (handItem != null) {
                Component itemName = (handItem.hasItemMeta() && handItem.getItemMeta().hasDisplayName())
                        ? handItem.getItemMeta().displayName()
                        : Component.text("Analyseur Équin");

                if (handItem.getAmount() <= 1) {
                    event.getPlayer().getInventory().setItem(hand, null);
                } else {
                    handItem.setAmount(handItem.getAmount() - 1); 
                }

                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                event.getPlayer().sendMessage(Component.text("Pas de chance, votre ")
                        .append(itemName != null ? itemName : Component.text("Analyseur"))
                        .append(Component.text(" s'est cassé...")));
            }
        }  
    }
}