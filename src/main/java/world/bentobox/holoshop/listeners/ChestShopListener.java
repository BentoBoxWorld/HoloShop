package world.bentobox.holoshop.listeners;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
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
import world.bentobox.bentobox.util.Util;
import world.bentobox.holoshop.HoloShop;
import world.bentobox.holoshop.Utilities;
import world.bentobox.holoshop.objects.ChestTransaction;
import world.bentobox.holoshop.objects.HoloChestShop;

/**
 * @author tastybento
 *
 */
public class ChestShopListener implements Listener {

    private static final DecimalFormat df = new DecimalFormat("0.00");

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
                u.sendMessage("holoshop.error.protected");
            }
        });
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=false)
    public void onInventoryOpen(InventoryOpenEvent e)
    {
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
            // Clear
            clearChest(u, hcs,inventoryHolder);
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

    private void clearChest(User u, HoloChestShop hcs, InventoryHolder inventoryHolder) {
        List<ItemStack> toBeRemoved = new ArrayList<>();
        // Remove wrong typed items
        inventoryHolder.getInventory().forEach(item -> {
            if (item != null && !item.getType().isAir() && !item.getType().equals(hcs.getChest().type())) {
                u.sendMessage("holoshop.error.wrong-item-type", "[type]", Util.prettifyText(item.getType().name()));
                toBeRemoved.add(item);
            }
        });
        // Remove items that are not 100%
        inventoryHolder.getInventory().forEach(item -> {
            if (item != null && Utilities.isDamaged(item)) {
                u.sendMessage("holoshop.error.worn-item", "[type]", Util.prettifyText(item.getType().name()));
                toBeRemoved.add(item);
            }
        });
        // Remove items that are not the same
        ItemStack firstItem = null;
        for (ItemStack item : inventoryHolder.getInventory()) {
            if (item == null || !item.getType().isAir()) continue;
            if (firstItem != null) {
                if (!Utilities.isSimilarNoDurability(firstItem, item)) {
                    u.sendMessage("holoshop.error.not-identical", "[type]", Util.prettifyText(item.getType().name()));
                    toBeRemoved.add(item);
                }
            } else {
                firstItem = item;
            }
        }
        // Remove items
        toBeRemoved.forEach(inventoryHolder.getInventory()::remove);
        toBeRemoved.forEach(i -> {
            World w = hcs.getChest().location().getWorld();
            w.dropItem(hcs.getChest().location(), i);
            w.playSound(hcs.getChest().location(), Sound.ITEM_BUNDLE_DROP_CONTENTS, 1F, 1F);
        });

    }

    private void buySell(User u, HoloChestShop hcs, int beforeCount, int afterCount) {
        int diff = beforeCount - afterCount;
        if (beforeCount == afterCount) {
            return; // No sale
        }
        if (beforeCount > afterCount) {
            // Sale
            double cost = cost(diff, hcs.getChest().sell());
            String costString = addon.getPlugin().getVault().map(vh -> vh.format(cost)).orElse(df.format(cost));
            u.sendMessage("holoshop.you-bought", TextVariables.NUMBER, String.valueOf(diff), "[type]", Util.prettifyText(hcs.getChest().type().name()), "[cost]", costString);
            ChestTransaction tx = new ChestTransaction(System.currentTimeMillis(), u.getUniqueId(), hcs.getChest().type(), cost, diff);
            hcs.getTransactions().add(tx);
            addon.getManager().saveShop(hcs);
        } else {
            // Buy back
            double cost = cost(diff, hcs.getChest().buy());
            String costString = addon.getPlugin().getVault().map(vh -> vh.format(cost)).orElse(df.format(cost));
            u.sendMessage("holoshop.you-sold", TextVariables.NUMBER, String.valueOf(diff), "[type]", Util.prettifyText(hcs.getChest().type().name()), "[cost]", costString);
            addon.getPlugin().logDebug("Sold " + hcs.getChest().type() + " x " + diff + " for " + costString);
            ChestTransaction tx = new ChestTransaction(System.currentTimeMillis(), u.getUniqueId(), hcs.getChest().type(), cost, diff);
            hcs.getTransactions().add(tx);
            addon.getManager().saveShop(hcs);
        }

    }

    private double cost(int diff, double price) {
        double cost = diff * price;
        if (Math.abs(cost) < 0.001d) cost = 0d;
        return cost;
    }

}
