package pk.ajneb97.versions;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

public interface NMS {

    public ItemStack setSkullSinID(ItemStack item, String texture) ;

    public ItemStack setUnbreakable(ItemStack item);

    public boolean getUnbreakable(ItemStack item);

    public ItemStack setSkull(ItemStack item, String path, FileConfiguration config);

    public void saveSkull(ItemStack item, String path, FileConfiguration config, String playerName);

    public void saveSkullDisplay(ItemStack item, String path, FileConfiguration config);

    public ItemStack setAttributes(ItemStack item, String path, FileConfiguration config);

    public void saveAttributes(ItemStack item, String path, FileConfiguration config);

    public void saveNBT(ItemStack item, String path, FileConfiguration config);

    public ItemStack setNBT(ItemStack item, String path, FileConfiguration config);

    ItemStack getBukkitCopy(net.minecraft.world.item.ItemStack item);

    net.minecraft.world.item.ItemStack getNmsCopy(ItemStack item);
}
