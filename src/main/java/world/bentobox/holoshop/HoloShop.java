package world.bentobox.holoshop;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.holoshop.commands.MakeShopCommand;
import world.bentobox.holoshop.listeners.ChestShopListener;

/**
 * Main HoloShop class - provides chest shops with holograms
 * @author tastybento
 */
public class HoloShop extends Addon {

    private ShopManager manager;
    // Settings

    @Override
    public void onEnable(){
        manager = new ShopManager(this);
        // Register commands
        this.getPlugin().getAddonsManager().getGameModeAddons().forEach(gameModeAddon -> {
            if (gameModeAddon.getPlayerCommand().isPresent())
            {
                new MakeShopCommand(this, gameModeAddon.getPlayerCommand().get());
            }
        });
        // Register listeners
        this.registerListener(new ChestShopListener(this));
    }

    @Override
    public void onDisable() {
        // Nothing to do here
    }

    /**
     * @return the manager
     */
    public ShopManager getManager() {
        return manager;
    }

}
