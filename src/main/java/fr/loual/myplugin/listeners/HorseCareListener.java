package fr.loual.myplugin.listeners;

import fr.loual.myplugin.MyPlugin;
import fr.loual.myplugin.horses.HorseData;
import fr.loual.myplugin.horses.SaddlebagHolder;
import fr.loual.myplugin.items.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HorseCareListener implements Listener {

    private final MyPlugin plugin;
    private final Map<UUID, Long> lastInteractTick = new HashMap<>();

    public HorseCareListener(MyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onHorseInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Horse horse)) {
            return;
        }

        Player player = event.getPlayer();
        EquipmentSlot hand = event.getHand();
        ItemStack item = (hand == EquipmentSlot.HAND)
                ? player.getInventory().getItemInMainHand()
                : player.getInventory().getItemInOffHand();

        long currentTick = Bukkit.getCurrentTick();
        Long lastTick = lastInteractTick.get(player.getUniqueId());
        if (lastTick != null && lastTick == currentTick) {
            return;
        }

        HorseData data = plugin.getHorseManager().getData(horse);

        // 0. Attribution du propriétaire unique via l'étiquette (Name Tag)
        if (item != null && item.getType() == Material.NAME_TAG && item.hasItemMeta()
                && (item.getItemMeta().hasDisplayName() || item.getItemMeta().hasCustomName())) {
            if (data.hasOwner() && !data.isOwner(player)) {
                event.setCancelled(true);
                player.sendMessage(Component.text("Ce cheval appartient déjà à " + data.getOwnerName() + " !", NamedTextColor.RED));
                horse.getWorld().playSound(horse.getLocation(), Sound.ENTITY_HORSE_ANGRY, 1.0f, 1.2f);
                return;
            }

            data.setOwner(player);
            plugin.awardAdvancement(player, "bind_owner");
            player.sendMessage(Component.text("Vous êtes désormais l'unique propriétaire de ce cheval !", NamedTextColor.GOLD));
            horse.getWorld().playSound(horse.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.3f);
            horse.getWorld().spawnParticle(Particle.HEART, horse.getLocation().add(0, 1.4, 0), 10, 0.4, 0.4, 0.4, 0.1);
            return;
        }

        // 1. Ouvrir la sacoche de selle (Shift + Clic droit à main nue ou quand le cheval a une sacoche)
        if (player.isSneaking() && (item == null || item.isEmpty()) && data.hasSaddlebag()) {
            event.setCancelled(true);
            lastInteractTick.put(player.getUniqueId(), currentTick);
            openSaddlebag(player, horse, data);
            return;
        }

        // 2. Caresser le cheval (Shift + Clic droit à main nue)
        if (player.isSneaking() && (item == null || item.isEmpty())) {
            event.setCancelled(true);
            lastInteractTick.put(player.getUniqueId(), currentTick);
            handlePetting(player, horse, data);
            return;
        }

        if (item == null || item.isEmpty()) {
            return;
        }

        // 3. Brossage avec la Brosse de pansage
        if (GroomingBrush.isGroomingBrush(plugin, item)) {
            event.setCancelled(true);
            lastInteractTick.put(player.getUniqueId(), currentTick);
            handleBrushing(player, horse, data, item);
            return;
        }

        // 4. Équiper des Fers à cheval
        HorseshoeItem.Type horseshoeType = HorseshoeItem.getHorseshoeType(plugin, item);
        if (horseshoeType != null) {
            event.setCancelled(true);
            lastInteractTick.put(player.getUniqueId(), currentTick);
            handleEquipHorseshoe(player, horse, data, horseshoeType, item);
            return;
        }

        // 5. Installer la Sacoche de selle
        if (SaddlebagItem.isSaddlebag(plugin, item)) {
            event.setCancelled(true);
            lastInteractTick.put(player.getUniqueId(), currentTick);
            handleEquipSaddlebag(player, horse, data, item);
            return;
        }
    }

    private void handlePetting(Player player, Horse horse, HorseData data) {
        if (data.hasOwner() && !data.isOwner(player)) {
            player.sendMessage(Component.text("Ce cheval appartient à " + data.getOwnerName() + " !", NamedTextColor.RED));
            return;
        }

        if (!data.canPet()) {
            player.sendMessage(Component.text("Votre cheval apprécie déjà votre présence pour le moment.", NamedTextColor.GRAY));
            horse.getWorld().playSound(horse.getLocation(), Sound.ENTITY_HORSE_AMBIENT, 0.7f, 1.4f);
            return;
        }

        data.pet();
        plugin.awardAdvancement(player, "pet_horse");
        if (data.getAffection() >= 50.0) {
            plugin.awardAdvancement(player, "affection_tier2");
        }
        if (data.getAffection() >= 100.0) {
            plugin.awardAdvancement(player, "affection_tier4");
        }
        horse.getWorld().spawnParticle(Particle.HEART, horse.getLocation().add(0, 1.2, 0), 5, 0.3, 0.3, 0.3, 0.1);
        horse.getWorld().playSound(horse.getLocation(), Sound.ENTITY_HORSE_AMBIENT, 1.0f, 1.3f);
        player.sendMessage(Component.text("Vous caressez tendrement votre cheval (+5 affection).", NamedTextColor.LIGHT_PURPLE));
    }

    private void handleBrushing(Player player, Horse horse, HorseData data, ItemStack brushItem) {
        if (data.hasOwner() && !data.isOwner(player)) {
            player.sendMessage(Component.text("Ce cheval appartient à " + data.getOwnerName() + " !", NamedTextColor.RED));
            return;
        }

        if (!data.canBrush()) {
            player.sendMessage(Component.text("Le pelage de votre cheval est déjà impeccablement propre !", NamedTextColor.GRAY));
            return;
        }

        data.brush();
        plugin.awardAdvancement(player, "groom_horse");
        if (data.getAffection() >= 50.0) {
            plugin.awardAdvancement(player, "affection_tier2");
        }
        if (data.getAffection() >= 100.0) {
            plugin.awardAdvancement(player, "affection_tier4");
        }
        horse.setHealth(Math.min(horse.getHealth() + 2.0, horse.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue()));
        horse.getWorld().spawnParticle(Particle.FIREWORK, horse.getLocation().add(0, 1.2, 0), 12, 0.4, 0.3, 0.4, 0.05);
        horse.getWorld().playSound(horse.getLocation(), Sound.ITEM_BRUSH_BRUSHING_GENERIC, 1.0f, 1.0f);
        player.sendMessage(Component.text("Vous avez brossé et lustré le pelage de votre cheval (+10 affection) !", NamedTextColor.GOLD));
    }

    private void handleEquipHorseshoe(Player player, Horse horse, HorseData data, HorseshoeItem.Type type, ItemStack item) {
        if (data.hasOwner() && !data.isOwner(player)) {
            player.sendMessage(Component.text("Seul le propriétaire (" + data.getOwnerName() + ") peut changer les fers !", NamedTextColor.RED));
            return;
        }

        String currentType = data.getHorseshoeType();
        if (currentType.equalsIgnoreCase(type.name())) {
            player.sendMessage(Component.text("Ce cheval est déjà équipé de ces fers !", NamedTextColor.YELLOW));
            return;
        }

        // Rendre les anciens fers au joueur si existants
        HorseshoeItem.Type oldType = HorseshoeItem.Type.fromId(currentType);
        if (oldType != null) {
            ItemStack oldItem = HorseshoeItem.create(plugin, oldType);
            player.getInventory().addItem(oldItem);
        }

        if (player.getGameMode() != GameMode.CREATIVE) {
            item.subtract(1);
        }

        data.setHorseshoeType(type.name());
        plugin.getHorseManager().applyStats(horse);
        plugin.awardAdvancement(player, "equip_horseshoe");

        horse.getWorld().playSound(horse.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.2f);
        horse.getWorld().spawnParticle(Particle.CRIT, horse.getLocation().add(0, 0.5, 0), 10, 0.3, 0.2, 0.3, 0.1);
        player.sendMessage(Component.text("Vous avez ferré votre cheval avec : " + type.name + " !", NamedTextColor.GREEN));
    }

    private void handleEquipSaddlebag(Player player, Horse horse, HorseData data, ItemStack item) {
        if (data.hasOwner() && !data.isOwner(player)) {
            player.sendMessage(Component.text("Seul le propriétaire (" + data.getOwnerName() + ") peut équiper une sacoche !", NamedTextColor.RED));
            return;
        }

        if (data.hasSaddlebag()) {
            player.sendMessage(Component.text("Ce cheval dispose déjà d'une sacoche de selle !", NamedTextColor.YELLOW));
            return;
        }

        if (player.getGameMode() != GameMode.CREATIVE) {
            item.subtract(1);
        }

        data.setHasSaddlebag(true);
        horse.getWorld().playSound(horse.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1.0f, 1.0f);
        horse.getWorld().spawnParticle(Particle.CLOUD, horse.getLocation().add(0, 0.8, 0), 8, 0.3, 0.2, 0.3, 0.05);
        player.sendMessage(Component.text("Sacoche de selle installée ! Faites Shift + Clic Droit à main nue pour l'ouvrir.", NamedTextColor.GREEN));
    }

    private void openSaddlebag(Player player, Horse horse, HorseData data) {
        if (data.hasOwner() && !data.isOwner(player)) {
            player.sendMessage(Component.text("Cette sacoche appartient au cheval de " + data.getOwnerName() + " !", NamedTextColor.RED));
            return;
        }

        Inventory inv = Bukkit.createInventory(new SaddlebagHolder(horse.getUniqueId()), 9, Component.text("Sacoche - " + data.getHorseName()));
        ItemStack[] items = data.getSaddlebagItems();
        if (items != null) {
            for (int i = 0; i < Math.min(items.length, 9); i++) {
                if (items[i] != null) {
                    inv.setItem(i, items[i]);
                }
            }
        }
        horse.getWorld().playSound(horse.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.8f, 1.2f);
        player.openInventory(inv);
        plugin.awardAdvancement(player, "open_saddlebag");
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof SaddlebagHolder holder) {
            Entity entity = Bukkit.getEntity(holder.getHorseUuid());
            if (entity instanceof Horse horse) {
                HorseData data = plugin.getHorseManager().getData(horse);
                data.saveSaddlebagItems(event.getInventory().getContents());
                horse.getWorld().playSound(horse.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.8f, 1.2f);
            }
        }
    }

    @EventHandler
    public void onHorseDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Horse horse) {
            HorseData data = plugin.getHorseManager().getData(horse);
            if (data.hasSaddlebag()) {
                ItemStack[] items = data.getSaddlebagItems();
                if (items != null) {
                    for (ItemStack it : items) {
                        if (it != null && !it.isEmpty()) {
                            horse.getWorld().dropItemNaturally(horse.getLocation(), it);
                        }
                    }
                }
                horse.getWorld().dropItemNaturally(horse.getLocation(), SaddlebagItem.create(plugin));
            }
        }
    }

    @EventHandler
    public void onPlayerUseWhistle(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || !HorseWhistle.isHorseWhistle(plugin, item)) {
            return;
        }

        if (!event.getAction().isRightClick()) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();

        // Chercher le cheval le plus proche appartenant AU JOUEUR
        Horse nearestHorse = null;
        double nearestDistSq = Double.MAX_VALUE;

        for (Entity ent : player.getWorld().getNearbyEntities(player.getLocation(), 60, 30, 60)) {
            if (ent instanceof Horse h) {
                HorseData hData = plugin.getHorseManager().getData(h);
                if (hData.isOwner(player)) {
                    double dist = ent.getLocation().distanceSquared(player.getLocation());
                    if (dist < nearestDistSq) {
                        nearestDistSq = dist;
                        nearestHorse = h;
                    }
                }
            }
        }

        player.getWorld().playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_0, 1.0f, 1.2f);

        if (nearestHorse == null) {
            player.sendMessage(Component.text("Aucun cheval vous appartenant n'a entendu votre sifflement à portée (60 blocs).", NamedTextColor.RED));
            return;
        }

        HorseData data = plugin.getHorseManager().getData(nearestHorse);
        if (data.getBondLevel() < 2) {
            player.sendMessage(Component.text("Votre lien d'amitié avec ce cheval n'est pas suffisant pour qu'il obéisse au sifflet (Niveau 2 requis : 50/100).", NamedTextColor.YELLOW));
            return;
        }

        double distance = Math.sqrt(nearestDistSq);
        if (distance <= 20) {
            nearestHorse.getPathfinder().moveTo(player.getLocation());
            player.sendMessage(Component.text("Votre fidèle destrier accourt vers vous !", NamedTextColor.GREEN));
        } else {
            Location tpLoc = player.getLocation().clone().add(player.getLocation().getDirection().multiply(-1.5));
            nearestHorse.teleport(tpLoc);
            nearestHorse.getWorld().spawnParticle(Particle.CLOUD, nearestHorse.getLocation().add(0, 1, 0), 15, 0.4, 0.4, 0.4, 0.1);
            nearestHorse.getWorld().playSound(nearestHorse.getLocation(), Sound.ENTITY_HORSE_AMBIENT, 1.0f, 1.1f);
            player.sendMessage(Component.text("Votre fidèle destrier a rejoint vos côtés !", NamedTextColor.GREEN));
        }
        plugin.awardAdvancement(player, "use_horse_whistle");
    }

    // Réduction des dégâts de chute avec les Fers Légers (FEATHER)
    @EventHandler
    public void onHorseFallDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Horse horse && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            HorseData data = plugin.getHorseManager().getData(horse);
            if ("FEATHER".equalsIgnoreCase(data.getHorseshoeType())) {
                event.setDamage(event.getDamage() * 0.5);
                horse.getWorld().spawnParticle(Particle.CLOUD, horse.getLocation(), 6, 0.3, 0.1, 0.3, 0.05);
            }
        }
    }

    // Fidélité Protectrice (Palier 4 : >= 100) : Ruade contre les monstres agresseurs du propriétaire
    @EventHandler
    public void onPlayerAttacked(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!(event.getDamager() instanceof Monster monster)) {
            return;
        }

        if (player.getVehicle() instanceof Horse) {
            return; // Déjà monté
        }

        // Trouver un cheval protecteur du joueur proche à pied
        for (Entity ent : player.getWorld().getNearbyEntities(player.getLocation(), 7, 4, 7)) {
            if (ent instanceof Horse horse && horse.getPassengers().isEmpty()) {
                HorseData data = plugin.getHorseManager().getData(horse);
                if (data.isOwner(player) && data.getBondLevel() >= 4) {
                    // Ruade protectrice !
                    horse.getWorld().playSound(horse.getLocation(), Sound.ENTITY_HORSE_ANGRY, 1.0f, 1.2f);
                    horse.getWorld().spawnParticle(Particle.CRIT, monster.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.1);

                    Vector knockback = monster.getLocation().toVector().subtract(horse.getLocation().toVector()).normalize().multiply(1.3).setY(0.4);
                    monster.setVelocity(knockback);
                    monster.damage(6.0, horse);

                    player.sendMessage(Component.text("Votre cheval repousse violemment le monstre d'un coup de sabot protecteur !", NamedTextColor.GOLD));
                    break;
                }
            }
        }
    }
}
