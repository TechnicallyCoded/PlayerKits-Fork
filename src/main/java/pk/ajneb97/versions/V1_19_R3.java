package pk.ajneb97.versions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;
import pk.ajneb97.api.PlayerKitsAPI;


public class V1_19_R3 implements NMS {
	
	private String sepChar;

	public V1_19_R3() {
		sepChar = PlayerKitsAPI.getNBTSeparationCharacter();
	}
	
	public ItemStack setSkullSinID(ItemStack item, String textura) {
		if (textura.isEmpty()) return item;

        SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);

        profile.getProperties().put("textures", new Property("textures", textura));

        try {
            Method mtd = skullMeta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
            mtd.setAccessible(true);
            mtd.invoke(skullMeta, profile);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
            ex.printStackTrace();
        }

        item.setItemMeta(skullMeta);
        return item;
	}

	public ItemStack setUnbreakable(ItemStack item){
		if(item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta();
			meta.setUnbreakable(true);
			item.setItemMeta(meta);
		}
		return item;
	}
	
	public boolean getUnbreakable(ItemStack item){
		if(item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta();
			return meta.isUnbreakable();
		}
		return false;
	}

	public ItemStack setSkull(ItemStack crafteos, String path, FileConfiguration config) {
		String pathTextura = path+".skull-texture";

		if(config.contains(pathTextura)){
			String textura = config.getString(pathTextura);
			if (textura.isEmpty()) return crafteos;

	        SkullMeta skullMeta = (SkullMeta) crafteos.getItemMeta();
	        GameProfile profile = new GameProfile(UUID.randomUUID(), null);

	        profile.getProperties().put("textures", new Property("textures", textura));

	        try {
	            Method mtd = skullMeta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
	            mtd.setAccessible(true);
	            mtd.invoke(skullMeta, profile);
	        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
	            ex.printStackTrace();
	        }

	        crafteos.setItemMeta(skullMeta);
		}
		return crafteos;
	}

	public void saveSkull(ItemStack item, String path, FileConfiguration config, String nombreJugador) {
		net.minecraft.world.item.ItemStack cabeza = getNmsCopy(item);
		net.minecraft.nbt.NBTTagCompound cabezaTag =  cabeza.u();
		if(cabeza.t()){
			if(cabezaTag.e("SkullOwner")){
				net.minecraft.nbt.NBTTagCompound skullOwner = cabezaTag.p("SkullOwner");
				if(skullOwner.e("Properties")){
					net.minecraft.nbt.NBTTagCompound propiedades = skullOwner.p("Properties");
					if(propiedades.e("textures")){
						net.minecraft.nbt.NBTTagList texturas = propiedades.c("textures", 10);
						config.set(path+".skull-texture", texturas.a(0).l("Value"));
					}
					
				}
			}
		}	
		
	}
	
	public void saveSkullDisplay(ItemStack item, String path, FileConfiguration config) {
		net.minecraft.world.item.ItemStack cabeza = getNmsCopy(item);
		net.minecraft.nbt.NBTTagCompound cabezaTag =  cabeza.u();
		if(cabeza.t()){
			if(cabezaTag.e("SkullOwner")){
				net.minecraft.nbt.NBTTagCompound skullOwner = cabezaTag.p("SkullOwner");
				String skullmeta = "";
				if(skullOwner.e("Id")){
					skullmeta = skullOwner.l("Id");
					
				}
				if(skullOwner.e("Properties")){
					net.minecraft.nbt.NBTTagCompound propiedades = skullOwner.p("Properties");
					if(propiedades.e("textures")){
						net.minecraft.nbt.NBTTagList texturas = propiedades.c("textures", 10);
						skullmeta = skullmeta+";"+texturas.a(0).l("Value");
					}
					
				}
				if(skullmeta.contains(";")) {
					config.set(path+".display_item_skulldata", skullmeta);
				}
			}
		}	
		
	}
	
	public ItemStack setAttributes(ItemStack item, String path, FileConfiguration config){
		path = path+".attributes";
		ItemMeta meta = item.getItemMeta();
		if(config.contains(path)){
			for(String attribute : config.getConfigurationSection(path).getKeys(false)) {
				List<String> modifiers = config.getStringList(path+"."+attribute+".modifiers");
				for(String linea : modifiers) {
					String[] sep = linea.split(";");
					String m = sep[0];
					double amount = Double.valueOf(sep[1]);
					AttributeModifier.Operation op = AttributeModifier.Operation.valueOf(sep[2]);
					UUID uuid = UUID.fromString(sep[3]);
					AttributeModifier modifier = null;
					if(sep.length >= 5) {
						EquipmentSlot slot = EquipmentSlot.valueOf(sep[4]);
						modifier = new AttributeModifier(uuid,m,amount,op,slot);
					}else {
						modifier = new AttributeModifier(uuid,m,amount,op);
					}
	
					meta.addAttributeModifier(Attribute.valueOf(attribute), modifier);
				}
			}
			
			item.setItemMeta(meta);
		}
		
		return item;
	}

	public void saveAttributes(ItemStack item, String path, FileConfiguration config) {
		ItemMeta meta = item.getItemMeta();
		if(meta != null && meta.hasAttributeModifiers()) {
			Multimap<Attribute,AttributeModifier> atributos = meta.getAttributeModifiers();
			Set<Attribute> set = atributos.keySet();
			for(Attribute a : set) {
				Collection<AttributeModifier> listaModifiers = atributos.get(a);
				List<String> lista = new ArrayList<String>();
				for(AttributeModifier m : listaModifiers) {
					String linea = m.getName()+";"+m.getAmount()+";"+m.getOperation().name()+";"+m.getUniqueId().toString();
					if(m.getSlot() != null) {
						linea=linea+";"+m.getSlot().name();
					}
					lista.add(linea);
				}
				config.set(path+".attributes."+a.name()+".modifiers", lista);
			}
		}
	}
	
	public void saveNBT(ItemStack item, String path, FileConfiguration config) {
		net.minecraft.world.item.ItemStack itemModificado = getNmsCopy(item);
		net.minecraft.nbt.NBTTagCompound itemTag =  this.getNBTTagCompound(itemModificado);
		if(this.hasNBTTagCompound(itemModificado)){
			Set<String> tags = this.getNBTTagKeys(itemTag);
			List<String> listaNBT = new ArrayList<String>();
			for(String t : tags) {
				if(!t.equals("ench") && !t.equals("HideFlags") && !t.equals("display")
						&& !t.equals("SkullOwner") && !t.equals("AttributeModifiers") 
						&& !t.equals("Enchantments") && !t.equals("Damage") && !t.equals("CustomModelData") && !t.equals("Potion")
						&& !t.equals("StoredEnchantments") && !t.equals("CustomPotionColor") && !t.equals("CustomPotionEffects") && !t.equals("Fireworks")
						&& !t.equals("Explosion")&& !t.equals("pages") && !t.equals("title") && !t.equals("author") && !t.equals("resolved")
						&& !t.equals("generation")) {
					if(itemTag.b(t, 1)) {
						//boolean
						listaNBT.add(t+sepChar+itemTag.q(t)+sepChar+"boolean");
					}else if(itemTag.b(t, 3)) {
						//int
						listaNBT.add(t+sepChar+itemTag.h(t)+sepChar+"int");
					}else if(itemTag.b(t, 6)) {
						//double
						listaNBT.add(t+sepChar+itemTag.k(t)+sepChar+"double");
					}else if(itemTag.b(t, 10)){
						//Compound
						listaNBT.add(t+sepChar+itemTag.p(t)+sepChar+"compound");
					}else if(itemTag.b(t, 8)) {
						//String
						listaNBT.add(t+sepChar+itemTag.l(t));
					}else {
						//compound
						listaNBT.add(t+sepChar+itemTag.c(t));
					}
				}	
			}
			if(!listaNBT.isEmpty()) {
				config.set(path+".nbt", listaNBT);
			}
		}
		
	}

	public Set<String> getNBTTagKeys(net.minecraft.nbt.NBTTagCompound nbtTagCompound) {
		return nbtTagCompound.e();
	}

	public boolean hasNBTTagCompound(net.minecraft.world.item.ItemStack item) {
		return item.t();
	}

	public NBTTagCompound getNBTTagCompound(net.minecraft.world.item.ItemStack item) {
		return item.u();
	}

	public ItemStack setNBT(ItemStack item, String path, FileConfiguration config) {
		net.minecraft.world.item.ItemStack nuevoItem = getNmsCopy(item);
		net.minecraft.nbt.NBTTagCompound tag = nuevoItem.t() ? nuevoItem.u() : new net.minecraft.nbt.NBTTagCompound(); 
		List<String> listaNBT = config.getStringList(path+".nbt");
		for(int i=0;i<listaNBT.size();i++) {
			
			String nbt = listaNBT.get(i);
			String[] sep = nbt.split("\\" + sepChar);
			String id = sep[0];
			String type = sep[sep.length-1];
			if(type.equals("boolean")) {
				tag.a(sep[0], Boolean.valueOf(sep[1]));
			}else if(type.equals("double")) {
				tag.a(sep[0], Double.valueOf(sep[1]));
			}else if(type.equals("int")) {
				tag.a(sep[0], Integer.valueOf(sep[1]));
			}else if(type.equals("compound")) {
				try {
					String finalNBT = nbt.replace(id+sepChar, "").replace(sepChar+"compound", "");
					NBTTagCompound tagNew = MojangsonParser.a(finalNBT);
					tag.a(sep[0], tagNew);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else {
				tag.a(sep[0], nbt.replace(id+sepChar, ""));
			}
			
		}
		nuevoItem.c(tag);
		return getBukkitCopy(nuevoItem);
	}

	public ItemStack getBukkitCopy(net.minecraft.world.item.ItemStack nuevoItem) {
		return org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack.asBukkitCopy(nuevoItem);
	}

	public net.minecraft.world.item.ItemStack getNmsCopy(ItemStack item) {
		return org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack.asNMSCopy(item);
	}
}
