package net.myteria.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import org.bukkit.inventory.ItemStack;

import net.myteria.HousingAPI.Action;
import net.myteria.HousingAPI;
import net.myteria.PlayerHousing;
import net.myteria.menus.PlayerManagerMenu;
import net.myteria.utils.Scheduler;

public class PlayerManagerEvent implements Listener{
	HousingAPI api = PlayerHousing.getAPI();
	@EventHandler
	public void onInventoryClicked(InventoryClickEvent event) {
		if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
		if (event.getClickedInventory().getHolder() instanceof PlayerManagerMenu) {
			event.setCancelled(true);
			ItemStack clickedItem = event.getCurrentItem();
			Player player = (Player)event.getWhoClicked();

			switch(clickedItem.getType()) {
				case WRITABLE_BOOK: {
					player.closeInventory();
					openPlayersMenu(player, Action.Whitelist);
					break;
				}
				case BUCKET: {
					player.closeInventory();
					openPlayersMenu(player, Action.Manage);
					break;
				}
				
				case BARRIER: {
					player.closeInventory();
					openPlayersMenu(player, Action.Banned);
					break;
				}
				
				default: {
					break;
				}
			}
		}
		
	}
	public void openPlayersMenu(Player player, Action action) {
		Scheduler.runTaskLater(player, PlayerHousing.getInstance(), () -> {
			api.getOnlinePlayersMenu().setupMenu(player, action);
			api.playersInv.put(player, Bukkit.createInventory(api.getOnlinePlayersMenu(), 5*9, "Players"));
			api.getOnlinePlayersMenu().setInventory(api.playersInv.get(player), api.playersPage.get(player));
			player.openInventory(api.getOnlinePlayersMenu().getInventory());
		}, null, 1L);
		
	}
}
