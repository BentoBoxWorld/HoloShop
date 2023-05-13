package world.bentobox.holoshop.commands;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.ItemStack;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.holoshop.HoloShop;
import world.bentobox.holoshop.objects.ChestData;

/**
 * @author tastybento
 *
 */
public class MakeShopCommand extends CompositeCommand {

    private HoloShop addon;
    private Chest chest;
    private Material type;
    private double sellPrice;

    public MakeShopCommand(Addon addon, CompositeCommand parent) {
        super(addon, parent, "makeshop");
    }

    @Override
    public void setup() {
        this.setPermission("holoshop.makeshop");
        this.setOnlyPlayer(true);
        this.setDescription("makeshop.help.description");
        this.setParametersHelp("makeshop.help.parameters");
        addon = (HoloShop)getAddon();
    }
    
    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        // Command is makechest sellprice
        if (args.size() != 1) {
            showHelp(this, user);
            return false;
        }
        // Check that player is looking at a chest
        Block target = user.getPlayer().getTargetBlock(null, 5);
        if (!target.getType().equals(Material.CHEST)) {
            user.sendMessage("holoshop.commands.makechest.look-at-chest");
            return false;
        }
        chest = (Chest)target.getState();
        // Not sure if this is needed
        if (chest.getInventory().getHolder() instanceof DoubleChest) {
            user.sendMessage("holoshop.commands.makechest.only-single-chest");
            return false;
        }
        type = null;
        for (ItemStack item : chest.getInventory().getContents()) {
            if (item != null) {
                if (type != null && !type.equals(item.getType())) {
                    user.sendMessage("holoshop.commands.makechest.not-all-same-type");
                    return false;
                }
                type = item.getType();
            }
        }
        if (type == null) {
            user.sendMessage("holoshop.commands.makechest.empty-chest");
            return false;
        }
        // Check price
        try {
            sellPrice = Double.parseDouble(args.get(0));
        } catch (Exception e) {
            user.sendMessage("holoshop.commands.makechest.not-a-number");
            return false;
        }
        
        return true;
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        String id = addon.getIslandsManager().getIslandAt(chest.getLocation()).map(Island::getUniqueId).orElse("");
        ChestData chestData = new ChestData(chest.getLocation(), type, sellPrice, 0, user.getUniqueId(), id);
        addon.getManager().setShop(chestData);
        user.sendMessage("general.success");
        return true;
    }

}
