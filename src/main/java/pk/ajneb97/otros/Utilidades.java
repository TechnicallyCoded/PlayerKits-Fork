package pk.ajneb97.otros;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import pk.ajneb97.managers.JugadorManager;
import pk.ajneb97.versions.NMS;
import pk.ajneb97.versions.V1_19_R3;
import pk.ajneb97.versions.V1_20_R1;

public class Utilidades {

	static NMS nms;
	private static int serverVersion = -1;

	static {
		String packageName = Bukkit.getServer().getClass().getPackage().getName();
		if (packageName.contains("1_19_R3")) {
			nms = new V1_19_R3();
		}
		else if (packageName.contains("1_20_R1")) {
			nms = new V1_20_R1();
		}
	}
	
	
	public static boolean isLegacy() {
		try {
			int version = getVersion();
			if (version < 13) {
				return true;
			}
		} catch (NumberFormatException ex) {}

		return false;
	}

	private static int getVersion() {
		if (serverVersion == -1) {
			serverVersion = Integer.parseInt(Bukkit.getVersion().split("\\.")[1]);
		}
		return serverVersion;
	}

	public static boolean isNew() {
		try {
			int version = getVersion();
			if (version >= 16) {
				return true;
			}
		} catch (NumberFormatException ex) {}

		return false;
	}
	
	public static String getCooldown(String kit,Player jugador,FileConfiguration kitConfig,FileConfiguration config,JugadorManager jManager){
		//1000millis claimea un kit de 5 segundos
		//6000millis puede claimearlo otra vez (timecooldown)
		
    	long timecooldown = jManager.getCooldown(jugador, kit);
		
    	long millis = System.currentTimeMillis();
    	if(!kitConfig.contains("Kits."+kit+".cooldown")) {
    		return "no_existe";
    	}
    	String cooldownconfig = kitConfig.getString("Kits."+kit+".cooldown");
 	    long cooldown = Long.valueOf(cooldownconfig); 
 	    long cooldownmil = cooldown*1000;
 	    
 	    long espera = millis - timecooldown;
 	    long esperaDiv = espera/1000;
 	    long esperatotalseg = cooldown - esperaDiv;
 	    long esperatotalmin = esperatotalseg/60;
 	    long esperatotalhour = esperatotalmin/60;
 	    long esperatotalday = esperatotalhour/24;
 	    if(((timecooldown + cooldownmil) > millis) && (timecooldown != 0)){		    		
 		   if(esperatotalseg > 59){
 			   esperatotalseg = esperatotalseg - 60*esperatotalmin;
 		   }
 		   String time = esperatotalseg+config.getString("Messages.seconds");		    		
 		   if(esperatotalmin > 59){
 			   esperatotalmin = esperatotalmin - 60*esperatotalhour;
 		   }	
 		   if(esperatotalmin > 0){
 			   time = esperatotalmin+config.getString("Messages.minutes")+" "+time;
 		   }
 		   if(esperatotalhour > 24) {
 			  esperatotalhour = esperatotalhour - 24*esperatotalday;
 		   }
 		   if(esperatotalhour > 0){
 			   time = esperatotalhour+ config.getString("Messages.hours")+" " + time;
 		   }
 		   if(esperatotalday > 0) {
 			  time = esperatotalday+ config.getString("Messages.days")+" " + time;
 		   }
 		   
 		   return time;
 	    }else{
 	    	return "ready";
 	    }
	}
	
	@SuppressWarnings("deprecation")
	public static ItemStack getItem(String id,int amount,String skulldata){
		  String[] idsplit = new String[2];
		  int DataValue = 0;
		  ItemStack stack = null;
		  if(id.contains(":")){
			  idsplit = id.split(":");
			  String stringDataValue = idsplit[1];
			  DataValue = Integer.valueOf(stringDataValue);
			  Material mat = Material.getMaterial(idsplit[0].toUpperCase()); 
			  stack = new ItemStack(mat,amount,(short)DataValue);             	  
		  }else{
			  Material mat = Material.getMaterial(id.toUpperCase());
			  stack = new ItemStack(mat,amount);  			  
		  }
		  if(!skulldata.isEmpty()) {
			  String[] sep = skulldata.split(";");
			  stack = Utilidades.setSkull(stack, sep[0], sep[1]);
		  }
		  
		  
		  return stack;
	}
	
	public static ItemStack getDisplayItem(FileConfiguration kits,String path) {
		ItemStack item = getItem(kits.getString(path+".display_item"),1,"");
		ItemMeta meta = item.getItemMeta();
		if(kits.contains(path+".display_name")) {
			meta.setDisplayName(MensajesUtils.getMensajeColor(kits.getString(path+".display_name")));
		}
		if(kits.contains(path+".display_lore")) {
			List<String> lore = kits.getStringList(path+".display_lore");
			for(int i=0;i<lore.size();i++) {
				lore.set(i, MensajesUtils.getMensajeColor(lore.get(i)));
			}
			meta.setLore(lore);
		}
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES,ItemFlag.HIDE_ENCHANTS);
		item.setItemMeta(meta);
		if(kits.contains(path+".display_item_leathercolor")) {
			LeatherArmorMeta meta2 = (LeatherArmorMeta) meta;
			int color = Integer.valueOf(kits.getString(path+".display_item_leathercolor"));
			meta2.setColor(Color.fromRGB(color));
			item.setItemMeta(meta2);
		}
		if(kits.contains(path+".display_item_custom_model_data")) {
			int customModelData = kits.getInt(path+".display_item_custom_model_data");
			meta = item.getItemMeta();
			meta.setCustomModelData(customModelData);
			item.setItemMeta(meta);
		}
		item = Utilidades.setUnbreakable(item);
		if(kits.contains(path+".display_item_skulldata")) {
			String[] skulldata = kits.getString(path+".display_item_skulldata").split(";");
			item = Utilidades.setSkull(item, skulldata[0], skulldata[1]);
		}
		
		return item;
	}
	
	public static int getSlotDisponible(FileConfiguration kitConfig,FileConfiguration config) {
		ArrayList<Integer> slotsOcupados = new ArrayList<Integer>();
		if(kitConfig.contains("Kits")) {
			for(String path : kitConfig.getConfigurationSection("Kits").getKeys(false)) {
				if(kitConfig.contains("Kits."+path+".slot")) {
					int slotOcupado = Integer.valueOf(kitConfig.getString("Kits."+path+".slot"));
					slotsOcupados.add(slotOcupado);
				}
			}
		}
		if(config.contains("Config.Inventory")) {
			for(String key : config.getConfigurationSection("Config.Inventory").getKeys(false)) {
				int slotOcupado = Integer.valueOf(key);
				slotsOcupados.add(slotOcupado);
			}
		}
		
		int slotsMaximos = Integer.valueOf(config.getString("Config.inventorySize"));
		for(int i=0;i<slotsMaximos;i++) {
			if(!slotsOcupados.contains(i)) {
				return i;
			}
		}
		return -1;
	}
	
	public static DyeColor getBannerColor(String mainColor) {
		String fixed = mainColor.replace("_BANNER", "");
		return DyeColor.valueOf(fixed);
	}
	
	public static void guardarSkullDisplay(ItemStack item, FileConfiguration config, String path) {
		nms.saveSkullDisplay(item,path,config);
	}

	public static void guardarSkull(ItemStack item, FileConfiguration config, String path, String nombreJugador) {
		nms.saveSkull(item,path,config,nombreJugador);
	}

	public static void guardarAttributes(ItemStack item, FileConfiguration config, String path) {
		nms.saveAttributes(item,path,config);
	}
	
	public static void guardarNBT(ItemStack item, FileConfiguration config, String path) {
		nms.saveNBT(item,path,config);
	}
	
	public static  ItemStack setUnbreakable(ItemStack item){
		return nms.setUnbreakable(item);
	}
	
	public static boolean getUnbreakable(ItemStack item){
		return nms.getUnbreakable(item);
	}
	
	public static ItemStack setSkull(ItemStack crafteos, String path, FileConfiguration config) {
		return nms.setSkull(crafteos,path,config);
	}
	
	public static ItemStack setNBT(ItemStack item, FileConfiguration config, String key) {
		return nms.setNBT(item,key,config);
	}
	
	public static ItemStack setAttributes(ItemStack item, FileConfiguration config, String key) {
		return nms.setAttributes(item,key,config);
	}
	
	public static ItemStack setSkull(ItemStack item, String id, String textura) {
		return nms.setSkullSinID(item,textura);
	}
}
