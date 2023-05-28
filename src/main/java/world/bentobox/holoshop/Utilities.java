package world.bentobox.holoshop;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author tastybento
 *static utils
 */
public class Utilities {
    /**
     * This method checks if 2 given item stacks are similar without durability check.
     * @param input First item.
     * @param stack Second item.
     * @return {@code true} if items are equal, {@code false} otherwise.
     */
    public static boolean isSimilarNoDurability(@Nullable ItemStack input, @Nullable ItemStack stack)
    {
        if (stack == null || input == null)
        {
            return false;
        }
        else if (stack == input)
        {
            return true;
        }
        else
        {
            return input.getType() == stack.getType() &&
                    input.hasItemMeta() == stack.hasItemMeta() &&
                    (!input.hasItemMeta() || Bukkit.getItemFactory().equals(input.getItemMeta(), stack.getItemMeta()));
        }
    }

    public static boolean isDamaged(ItemStack item) {
        if (item.getItemMeta() instanceof Damageable meta) {
            return meta.hasDamage();
        }
        return false;
    }
}
