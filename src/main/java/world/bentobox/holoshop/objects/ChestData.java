package world.bentobox.holoshop.objects;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;

import com.google.gson.annotations.Expose;

/**
 * @param location location of the chest
 * @param type the material type being sold or bought
 * @param sell how much this shop sells things for
 * @param buy how much this shop buys things for
 * @param owner uuid of who made the shop
 * @param islandID island id where the chest is (optional)
 *
 */
public record ChestData(@Expose Location location, @Expose Material type, @Expose double sell, @Expose double buy, @Expose UUID owner, @Expose String islandID) {};

