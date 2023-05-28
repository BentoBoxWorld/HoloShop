package world.bentobox.holoshop;

import java.util.Objects;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.Display.Billboard;

import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;
import world.bentobox.holoshop.objects.ChestData;
import world.bentobox.holoshop.objects.HoloChestShop;

/**
 * @author tastybento
 *
 */
public class ShopManager {
    
    private HoloShop addon;
    private final Database<HoloChestShop> handler;


    public ShopManager(HoloShop addon) {
        super();
        this.addon = addon;
        handler = new Database<>(addon, HoloChestShop.class);
    }
    

    public void setShop(ChestData chestData) {
        HoloChestShop hcs = new HoloChestShop(chestData);
        handler.saveObject(hcs);
        // Display hologram
        displayHologram(chestData);
    }
    
    public void saveShop(HoloChestShop hcs) {
        handler.saveObject(hcs);
    }
    
    private void displayHologram(ChestData chestData) {
        // TODO Auto-generated method stub
        
    }
    
    private void createHologram(ChestData chestData) {
        Location pos = chestData.location().clone().add(0.5, 1.1, 0.5);
        World world = pos.getWorld();
        assert world != null;

        TextDisplay newDisplay = world.spawn(pos, TextDisplay.class);
        newDisplay.setAlignment(TextDisplay.TextAlignment.CENTER);
        newDisplay.setBillboard(Billboard.CENTER);
        
    }


    /**
     * Try to get a holoshop for location
     * @param location location of the chest
     * @return HoloChestShop chest object
     */
    public Optional<HoloChestShop> getShop(Location location) {
        String id = Util.getStringLocation(location);
        if (handler.objectExists(id)) {
            HoloChestShop hcs = Objects.requireNonNull(handler.loadObject(id));
            return Optional.of(hcs);
        }
        return Optional.empty();
    }


    /**
     * Remove a shop with ID
     * @param id unique ID of shop
     */
    public void remove(String id) {
        if (handler.objectExists(id)) {
            handler.deleteID(id);
        }
    }
    
    

}
