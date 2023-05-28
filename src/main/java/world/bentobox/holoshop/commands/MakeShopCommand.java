package world.bentobox.holoshop.commands;

import java.text.DecimalFormat;
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
import world.bentobox.bentobox.util.Util;
import world.bentobox.holoshop.HoloShop;
import world.bentobox.holoshop.Utilities;
import world.bentobox.holoshop.objects.ChestData;

/**
 * @author tastybento
 *
 */
public class MakeShopCommand extends CompositeCommand {

    private static final DecimalFormat df = new DecimalFormat("0.00");
    private HoloShop addon;
    private Chest chest;
    private Material type;
    private double sellPrice;
    private double buyPrice;

    public MakeShopCommand(Addon addon, CompositeCommand parent) {
        super(addon, parent, "makeshop");
    }

    @Override
    public void setup() {
        this.setPermission("holoshop.makeshop");
        this.setOnlyPlayer(true);
        this.setDescription("holoshop.commands.makechest.description");
        this.setParametersHelp("holoshop.commands.makechest.parameters");
        addon = (HoloShop)getAddon();
    }
    
    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        // Command is makechest sellprice
        if (args.isEmpty() || args.size() > 2) {
            showHelp(this, user);
            return false;
        }
        // Check price
        try {
            sellPrice = Double.parseDouble(args.get(0));
        } catch (Exception e) {
            user.sendMessage("holoshop.commands.makechest.not-a-number");
            return false;
        }
        if (args.size() == 2) {
            try {
                buyPrice = Double.parseDouble(args.get(1));
            } catch (Exception e) {
                user.sendMessage("holoshop.commands.makechest.not-a-number");
                return false;
            }
        }
        if (sellPrice < 0D || buyPrice < 0) {
            user.sendMessage("holoshop.commands.makechest.cannot-be-negative");
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
        boolean pass = true;
        // Stop items that are not 100%
        for (ItemStack item : chest.getInventory()) {
            if (item != null && Utilities.isDamaged(item)) {
                user.sendMessage("holoshop.error.worn-item", "[type]", Util.prettifyText(item.getType().name()));
                pass = false;
            }
        }
        // Stop items that are not the same
        ItemStack firstItem = null;
        for (ItemStack item : chest.getInventory()) {
            if (item == null || item.getType().isAir()) continue;
            if (firstItem != null) {
                if (!Utilities.isSimilarNoDurability(firstItem, item)) {
                    user.sendMessage("holoshop.error.not-identical", "[type]", Util.prettifyText(item.getType().name()));
                    pass = false;
                }
            } else {
                firstItem = item;
            }
        }
        
        if (firstItem == null) {
            user.sendMessage("holoshop.commands.makechest.empty-chest");
            return false;
        }
        type = firstItem.getType();
        return pass;
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        String id = addon.getIslandsManager().getIslandAt(chest.getLocation()).map(Island::getUniqueId).orElse("");
        ChestData chestData = new ChestData(chest.getLocation(), type, sellPrice, buyPrice, user.getUniqueId(), id);
        addon.getManager().setShop(chestData);
        String sp = addon.getPlugin().getVault().map(vh -> vh.format(sellPrice)).orElse(df.format(sellPrice));
        String bp = addon.getPlugin().getVault().map(vh -> vh.format(buyPrice)).orElse(df.format(buyPrice));
        user.sendMessage("holoshop.commands.makechest.success", "[type]", Util.prettifyText(type.name()), "[sell-price]", sp, "[buy-price]", bp);
        return true;
    }

}
