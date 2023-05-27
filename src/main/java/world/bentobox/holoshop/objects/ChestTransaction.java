package world.bentobox.holoshop.objects;

import java.util.UUID;

import org.bukkit.Material;

import com.google.gson.annotations.Expose;

/**
 * @param timestamp of the transaction
 * @param patron UUID
 * @param type the material type being sold or bought
 * @param price of the sale or buy back
 * @param quantity sold (positive) or bought back (negative)
 *
 */
public record ChestTransaction(@Expose long timestamp, @Expose UUID patron, @Expose Material type, @Expose double price, @Expose long quantity) {};

