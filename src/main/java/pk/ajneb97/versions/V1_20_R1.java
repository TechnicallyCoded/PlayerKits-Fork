package pk.ajneb97.versions;

import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import pk.ajneb97.api.PlayerKitsAPI;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;


public class V1_20_R1 extends V1_19_R3 {

    @Override
    public @NotNull ItemStack getBukkitCopy(net.minecraft.world.item.ItemStack nuevoItem) {
        return org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asBukkitCopy(nuevoItem);
    }

    @Override
    public net.minecraft.world.item.ItemStack getNmsCopy(ItemStack item) {
        return org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asNMSCopy(item);
    }

}