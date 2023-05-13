package world.bentobox.holoshop.objects;

import com.google.gson.annotations.Expose;

import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.database.objects.Table;
import world.bentobox.bentobox.util.Util;

/**
 * Stores all the holo chest shops
 *
 * @author tastybento
 *
 */
@Table(name = "HoloChestShops")
public class HoloChestShop implements DataObject {
    
    /**
     * uniqueId
     */
    @Expose
    private String uniqueId = "";
    
    @Expose
    private ChestData chest;
    

    public HoloChestShop(ChestData chest) {
        super();
        this.uniqueId = Util.getStringLocation(chest.location());
        this.chest = chest;
    }

    @Override
    public String getUniqueId() {
        return uniqueId;
    }

    @Override
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
        
    }

    /**
     * @return the chest
     */
    public ChestData getChest() {
        return chest;
    }

    /**
     * @param chest the chest to set
     */
    public void setChest(ChestData chest) {
        this.chest = chest;
    }


}
