package net.myteria.menus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import net.myteria.HousingAPI;
import net.myteria.PlayerHousing;

public class GameRulesMenu implements InventoryHolder {
	private List<ItemStack> items = new ArrayList<>();
	private Inventory inv;
	
	public void setupMenu(UUID uuid, boolean clear) {
		if (clear) {
			items.clear();
		}
		HousingAPI api = PlayerHousing.getAPI();
		YamlConfiguration config = api.getWorldInstance(uuid).getConfig();
		String world = config.getString("default-world");
		
		if (items.isEmpty()) {
			config.getConfigurationSection(world + ".gamerules").getKeys(false).forEach(item -> {
				ItemStack gamerule;
				switch(config.getString(world + ".gamerules." + item.toString())) {
					case "true":{
						gamerule = new ItemStack(Material.GREEN_WOOL);
						break;
					}
					case "false": {
						gamerule = new ItemStack(Material.RED_WOOL);
						break;
					}
					default: {
						if (config.getInt(world + ".gamerules." + item.toString()) == 0) {
							gamerule = new ItemStack(Material.RED_WOOL);
							break;
						} else {
							gamerule = new ItemStack(Material.GREEN_WOOL, config.getInt(world + ".gamerules." + item.toString()));
							break;
						}
					}
				}
				
				ItemMeta meta = gamerule.getItemMeta();
				meta.setDisplayName(item.toString());
				meta.getPersistentDataContainer().set(new NamespacedKey(PlayerHousing.getInstance(), "gamerule"), PersistentDataType.STRING, item.toString());								
				gamerule.setItemMeta(meta);
				items.add(gamerule);
			}); 
		}
		
	}

	private void setPageItems(int page) {
		inv.clear();
		
		ItemStack back = new ItemStack(Material.ARROW);
		ItemMeta meta = back.getItemMeta();
		meta.setDisplayName("Back");
		back.setItemMeta(meta);
		
		ItemStack next = new ItemStack(Material.ARROW);
		ItemMeta meta2 = next.getItemMeta();
		meta2.setDisplayName("Next");
		next.setItemMeta(meta2);
		
		int pages = PlayerHousing.getAPI().listToPages(items, 36).size();
		if (pages >= 2 && page != pages - 1) {
			inv.setItem(44, next);
		}
		if (page >= 1) {
			inv.setItem(36, back);
		}
		
		PlayerHousing.getAPI().listToPages(items, 36).get(page).forEach(item -> {
			inv.addItem(item);
		});
		
		//44 next page
		//36 last page
		
	}

	@Override
	public Inventory getInventory() {
		// TODO Auto-generated method stub
		return inv;
	}
	
	public void setInventory(Inventory inv, int page) {
		// TODO Auto-generated method stub
		this.inv = inv;
		setPageItems(page);
	}
	
	public List<ItemStack> getItems() {
		// TODO Auto-generated method stub
		return items;
	}

}
