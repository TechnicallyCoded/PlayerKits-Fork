package pk.ajneb97.versions;

import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;
import pk.ajneb97.PlayerKits;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class V1_20_OrHigher extends V1_19_R3 {

    static MethodHandle asBukkitCopy;
    static MethodHandle asNMSCopy;
    static MethodHandle getNBTTagKeys;
    static MethodHandle hasNBTTagCompound;
    static MethodHandle getNBTTagCompound;

    static {
        try {
            Class<? extends Server> craftServerClass = Bukkit.getServer().getClass();
            String packageName = craftServerClass.getPackage().getName();

            String craftItemStackClassPath = packageName + ".inventory.CraftItemStack";
            Class<?> craftItemStackClass = Class.forName(craftItemStackClassPath);
            Class<net.minecraft.world.item.ItemStack> nmsItemStackClass = net.minecraft.world.item.ItemStack.class;

            MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();

            MethodType asBukkitCopyMethodType = MethodType.methodType(ItemStack.class, nmsItemStackClass);
            asBukkitCopy = publicLookup.findStatic(craftItemStackClass, "asBukkitCopy", asBukkitCopyMethodType);

            MethodType asNMSCopyMethodType = MethodType.methodType(nmsItemStackClass, ItemStack.class);
            asNMSCopy = publicLookup.findStatic(craftItemStackClass, "asNMSCopy", asNMSCopyMethodType);

            // Find the NBT methods
            MethodType getNBTTagKeysMethodType = MethodType.methodType(Set.class);
            Method getNBTTagKeysMethod = null;
            for (Method method : NBTTagCompound.class.getMethods()) {
                if (method.getReturnType() == Set.class) {
                    getNBTTagKeysMethod = method;
                    break;
                }
            }
            if (getNBTTagKeysMethod == null) {
                throw new IllegalStateException("Could not find getNBTTagKeys method in NBTTagCompound");
            }
            getNBTTagKeys = publicLookup.findVirtual(NBTTagCompound.class, getNBTTagKeysMethod.getName(), getNBTTagKeysMethodType);

            MethodType hasNBTTagCompoundMethodType = MethodType.methodType(boolean.class);
            MethodType getNBTTagCompoundMethodType = MethodType.methodType(NBTTagCompound.class);
            Method[] methods = nmsItemStackClass.getMethods();

            List<Method> potentialHasNBTTagCompoundMethods = new ArrayList<>(methods.length);
            Method getNBTTagCompoundMethod = null;
            for (Method method : methods) {
//                System.out.println("> Found: public " + method.getReturnType().getName() + " " + method.getName() +
//                        "(" + String.join(", ", Arrays.stream(method.getParameterTypes())
//                            .map(Class::getName)
//                            .collect(Collectors.toList())) +
//                        ")");
                if (method.getReturnType() == boolean.class && method.getParameterCount() == 0) {
                    potentialHasNBTTagCompoundMethods.add(method);
//                    System.out.println("<< Found potential hasNBTTagCompound method");
                }
                else if (method.getReturnType() == NBTTagCompound.class && method.getParameterCount() == 0 && method.getAnnotations().length > 0) {
//                    System.out.println("!!!! Found getNBTTagCompound method");
                    getNBTTagCompoundMethod = method;
                }
            }
            if (potentialHasNBTTagCompoundMethods.isEmpty()) {
                throw new IllegalStateException("Could not find hasNBTTagCompound method in CraftItemStack");
            }
            if (getNBTTagCompoundMethod == null) {
                throw new IllegalStateException("Could not find getNBTTagCompound method in CraftItemStack");
            }
            Collections.sort(potentialHasNBTTagCompoundMethods, Comparator.comparing(Method::getName));

//            System.out.println("Filtering potential matches...");
            String lowestNameBeforeGetTagMethod = null;
            for (Method potentialHasNBT : potentialHasNBTTagCompoundMethods) {
                String name = potentialHasNBT.getName();
                if (name.compareTo(getNBTTagCompoundMethod.getName()) < 0) {
                    lowestNameBeforeGetTagMethod = name;
                }
            }
            PlayerKits.getInstance().getLogger().info("Auto NMS Finder: Picked " + getNBTTagKeysMethod.getName() + "() as getNBTTagKeys method");
            PlayerKits.getInstance().getLogger().info("Auto NMS Finder: Picked " + lowestNameBeforeGetTagMethod + "() as hasNBTTagCompound method");
            PlayerKits.getInstance().getLogger().info("Auto NMS Finder: Picked " + getNBTTagCompoundMethod.getName() + "() as getNBTTagCompound method");
            hasNBTTagCompound = publicLookup.findVirtual(nmsItemStackClass, lowestNameBeforeGetTagMethod, hasNBTTagCompoundMethodType);
            getNBTTagCompound = publicLookup.findVirtual(nmsItemStackClass, getNBTTagCompoundMethod.getName(), getNBTTagCompoundMethodType);
        }
        catch (Throwable t) {
            t.printStackTrace();
            throw new IllegalStateException("Failed to initialize V1_20_OrHigher", t);
        }
    }

    @Override
    public ItemStack getBukkitCopy(net.minecraft.world.item.ItemStack nuevoItem) {
        try {
            return (ItemStack) asBukkitCopy.invoke(nuevoItem);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public net.minecraft.world.item.ItemStack getNmsCopy(ItemStack item) {
        try {
            return (net.minecraft.world.item.ItemStack) asNMSCopy.invoke(item);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Set<String> getNBTTagKeys(NBTTagCompound nbtTagCompound) {
        try {
            return (Set<String>) getNBTTagKeys.invoke(nbtTagCompound);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean hasNBTTagCompound(net.minecraft.world.item.ItemStack item) {
        try {
            return (boolean) hasNBTTagCompound.invoke(item);
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public NBTTagCompound getNBTTagCompound(net.minecraft.world.item.ItemStack item) {
        try {
            return (NBTTagCompound) getNBTTagCompound.invoke(item);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
}