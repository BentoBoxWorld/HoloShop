package world.bentobox.holoshop.listeners;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.holoshop.HoloShop;
import world.bentobox.holoshop.objects.ChestTransaction;
import world.bentobox.holoshop.objects.HoloChestShop;

/**
 * @author tastybento
 *
 */
public class ChestShopListener implements Listener {

    private HoloShop addon;
    private Map<String, Integer> chestInventory = new HashMap<>();

    public ChestShopListener(HoloShop addon) {
        super();
        this.addon = addon;
    }

    /**
     * Prevents chest shop from being broken
     *
     * @param e - event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent e) {
        Material m = e.getBlock().getType();
        if (!m.equals(Material.CHEST)) {
            return;
        }
        User u = User.getInstance(e.getPlayer());
        Location l = e.getBlock().getLocation();
        addon.getManager().getShop(l).ifPresent(hcs -> {
            if (hcs.getChest().owner().equals(u.getUniqueId())) {
                // Owner breaking
                addon.getManager().remove(hcs.getUniqueId());
                u.sendMessage("holoshop.shop-removed");
            } else {
                e.setCancelled(true);
                u.sendMessage("holoshop.protected");
            }
        });
    }


    

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=false)
    public void onInventoryOpen(InventoryOpenEvent e)
    {
        BentoBox.getInstance().logDebug(e.getEventName());
        if (!e.getInventory().getType().equals(InventoryType.CHEST)) {
            return;
        }
        InventoryHolder inventoryHolder = e.getInventory().getHolder();

        if (inventoryHolder == null || !(e.getPlayer() instanceof Player player))
        {
            return;
        }
        User u = User.getInstance(e.getPlayer());
        Location l = inventoryHolder.getInventory().getLocation();
        addon.getManager().getShop(l).ifPresent(hcs -> {
            // Allow access to this chest
            e.setCancelled(false);
            if (chestInventory.containsKey(hcs.getUniqueId())) {
                if (!hcs.getChest().owner().equals(u.getUniqueId())) {
                    e.setCancelled(true);
                }
                u.sendMessage("holoshop.error.in-use");
            } else {
                Material t = hcs.getChest().type();
                int beforeCount = Arrays.stream(inventoryHolder.getInventory().getContents()).filter(Objects::nonNull).filter(m -> m.getType().equals(t)).mapToInt(ItemStack::getAmount).sum();
                chestInventory.put(hcs.getUniqueId(), beforeCount);
            }
        });
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=true)
    public void onInventoryClose(InventoryCloseEvent e)
    {
        BentoBox.getInstance().logDebug(e.getEventName());
        if (!e.getInventory().getType().equals(InventoryType.CHEST)) {
            return;
        }
        InventoryHolder inventoryHolder = e.getInventory().getHolder();

        if (inventoryHolder == null || !(e.getPlayer() instanceof Player player))
        {
            return;
        }
        User u = User.getInstance(e.getPlayer());
        Location l = inventoryHolder.getInventory().getLocation();
        addon.getManager().getShop(l).ifPresent(hcs -> {
            if (!hcs.getChest().owner().equals(u.getUniqueId())) {
                // This is a vistor, check the before and after
                if (chestInventory.containsKey(hcs.getUniqueId())) {
                    Material t = hcs.getChest().type();
                    int beforeCount = chestInventory.get(hcs.getUniqueId());
                    ItemStack[] after = inventoryHolder.getInventory().getContents();
                    int afterCount = Arrays.stream(after).filter(Objects::nonNull).filter(m -> m.getType().equals(t)).mapToInt(ItemStack::getAmount).sum();
                    BentoBox.getInstance().logDebug("Before " + beforeCount + " After " + afterCount);
                    buySell(u, hcs, beforeCount, afterCount);
                } else {
                    addon.logWarning("Chest shop was closed but didn't register as being opened " + hcs);

                }
            }
            chestInventory.remove(hcs.getUniqueId());
        });
    }

    private void buySell(User u, HoloChestShop hcs, int beforeCount, int afterCount) {
        int diff = beforeCount - afterCount;
        if (beforeCount == afterCount) {
            return; // No sale
        }
        if (beforeCount > afterCount) {
            // Sale
            double cost = diff * hcs.getChest().sell();
            String costString = addon.getPlugin().getVault().map(vh -> vh.format(cost)).orElse(String.valueOf(cost));
            u.sendMessage("holochest-you-bought", TextVariables.NUMBER, "[quantity]", "[cost]", costString);
            addon.getPlugin().logDebug("Sold " + hcs.getChest().type() + " x " + diff + " for " + costString);
            ChestTransaction tx = new ChestTransaction(System.currentTimeMillis(), u.getUniqueId(), hcs.getChest().type(), cost, diff);
            hcs.getTransactions().add(tx);

        } else {
            // Buy back
            double cost = diff * hcs.getChest().buy();
            String costString = addon.getPlugin().getVault().map(vh -> vh.format(cost)).orElse(String.valueOf(cost));
            u.sendMessage("holochest-you-sold", TextVariables.NUMBER, "[quantity]", "[cost]", costString);
            addon.getPlugin().logDebug("Sold " + hcs.getChest().type() + " x " + diff + " for " + costString);
            ChestTransaction tx = new ChestTransaction(System.currentTimeMillis(), u.getUniqueId(), hcs.getChest().type(), cost, diff);
            hcs.getTransactions().add(tx);
        }

    }

    private boolean checkTypes(User u, HoloChestShop hcs, ItemStack[] before, ItemStack[] after) {
        Material t = hcs.getChest().type();
        if (Arrays.stream(before).filter(Objects::nonNull).map(ItemStack::getType).allMatch(m -> !m.equals(t))) {
            addon.logWarning("Chest shop before items don't match type " + hcs);
            return false;
        }
        if (Arrays.stream(after).filter(Objects::nonNull).map(ItemStack::getType).allMatch(m -> !m.equals(t))) {
            addon.logWarning("Chest shop after items don't match type " + hcs);
            return false;
        }
        // TODO, check for durability of items
        Arrays.stream(before).filter(Objects::nonNull).map(ItemStack::getType).filter(m -> !m.isAir()).distinct().forEach(m -> BentoBox.getInstance().logDebug("Before type " + m));
        Arrays.stream(after).filter(Objects::nonNull).map(ItemStack::getType).filter(m -> !m.isAir()).distinct().forEach(m -> BentoBox.getInstance().logDebug("After type " + m));

        return Arrays.stream(before).filter(Objects::nonNull).map(ItemStack::getType).filter(m -> !m.isAir()).distinct().count() 
                == Arrays.stream(after).filter(Objects::nonNull).map(ItemStack::getType).filter(m -> !m.isAir()).distinct().count();
    }


}
