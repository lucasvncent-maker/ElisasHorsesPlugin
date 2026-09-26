package fr.loual.myplugin.horses;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public class SaddlebagHolder implements InventoryHolder {

    private final UUID horseUuid;

    public SaddlebagHolder(UUID horseUuid) {
        this.horseUuid = horseUuid;
    }

    public UUID getHorseUuid() {
        return horseUuid;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
