package world.bentobox.holoshop;

import java.util.Objects;
import java.util.Optional;

import org.bukkit.Location;

import world.bentobox.bentobox.database.Database;
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
    
    

}
