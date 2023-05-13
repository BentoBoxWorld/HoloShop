package world.bentobox.holoshop;


import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.Pladdon;


public class HoloShopPladdon extends Pladdon {

    @Override
    public Addon getAddon() {
        return new HoloShop();
    }
}
