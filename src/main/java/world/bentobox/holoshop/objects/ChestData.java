package world.bentobox.holoshop.objects;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;

/**
 * @param location location of the chest
 * @param type the material type being sold or bought
 * @param sell how much this shop sells things for
 * @param buy how much this shop buys things for
 * @param owner uuid of who made the shop
 * @param islandID island id where the chest is (optional)
 *
 */
public record ChestData(Location location, Material type, double sell, double buy, UUID owner, String islandID) {};

