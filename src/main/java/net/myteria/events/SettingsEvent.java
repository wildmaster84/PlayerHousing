package net.myteria.events;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.ItemStack;

import net.myteria.HousingAPI;
import net.myteria.PlayerHousing;
import net.myteria.HousingAPI.Status;
import net.myteria.menus.SettingsMenu;
import net.myteria.objects.PlayerWorld;
import net.myteria.utils.Scheduler;

public class SettingsEvent implements Listener{
	private static final HashMap<UUID, String> pendingInputs = new HashMap<>();
	
	@EventHandler
	public void onInventoryClicked(InventoryClickEvent event) {
		if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
		if (event.getClickedInventory().getHolder() instanceof SettingsMenu) {
			HousingAPI api = PlayerHousing.getAPI();
			event.setCancelled(true);
			ItemStack clickedItem = event.getCurrentItem();
			Player player = (Player)event.getWhoClicked();
			OfflinePlayer owner = api.getWorldOwner(player.getWorld());
			PlayerWorld world = api.getWorldInstance(owner.getUniqueId());
			

			// Subtract
			switch(clickedItem.getType()) {
				case SPAWNER: {
					switch(world.getDifficulty()) {
					case EASY: {
						world.setDifficulty(Difficulty.NORMAL);
						openSettingsMenu(player);
						break;
					}
					case NORMAL: {
						world.setDifficulty(Difficulty.HARD);
						openSettingsMenu(player);
						break;
					}
					case HARD: {
						world.setDifficulty(Difficulty.PEACEFUL);
						openSettingsMenu(player);
						break;
					}
					case PEACEFUL: {
						world.setDifficulty(Difficulty.EASY);
						openSettingsMenu(player);
						break;
					}
				}
					break;
				}
				case CLOCK: {
					Scheduler.runTask(PlayerHousing.getInstance(), () -> {
						if (world.getWorld().isDayTime()) {
							world.getWorld().setTime(13000);
						} 
						else if (!world.getWorld().isDayTime()) {
							world.getWorld().setTime(1000);
						}
					});
					openSettingsMenu(player);
					break;
				}
				case DIAMOND_SWORD: {
					world.setPVP(!world.getPVP());
					openSettingsMenu(player);
					break;
				}
				case CRAFTING_TABLE: {
					switch(world.getGamemode()) {
						case ADVENTURE: {
							world.setGamemode(GameMode.CREATIVE);
							openSettingsMenu(player);
							break;
						}
						case CREATIVE: {
							world.setGamemode(GameMode.SURVIVAL);
							openSettingsMenu(player);
							break;
						}
						case SURVIVAL: {
							world.setGamemode(GameMode.ADVENTURE);
							openSettingsMenu(player);
							break;
						}
					}
					break;
				}
				case BOOK: {
					player.closeInventory();
					api.getGameRulesMenu().setupMenu(player.getUniqueId(), true);
					api.gameRulesInv.put(player, Bukkit.createInventory(api.getGameRulesMenu(), 5*9, "GameRules"));
					
					api.getGameRulesMenu().setInventory(api.gameRulesInv.get(player), api.gameRulesPage.get(player));
					
					player.openInventory(api.getGameRulesMenu().getInventory());
					break;
					
				}
				case OAK_DOOR: {
					world.setStatus((world.getStatus() == Status.PUBLIC ? Status.PRIVATE : Status.PUBLIC));
					openSettingsMenu(player);
					break;
				}
				case TNT: {
					break;
				}
				case OAK_SIGN: {
					player.closeInventory();
	                player.sendMessage("§aType your new description in chat:");

	                // Store the player's pending input type
	                pendingInputs.put(player.getUniqueId(), "");
				}
				default:
					break;
				}
		}
		
	}

	private void openSettingsMenu(Player player) {
		HousingAPI api = PlayerHousing.getAPI();
		Scheduler.runTaskLater(player, PlayerHousing.getInstance(), () -> {
			api.getSettingsMenu().setupInventory(player);
			player.openInventory(api.getSettingsMenu().getInventory());
		}, null, 1L);
	}
	
	@EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (pendingInputs.containsKey(playerId)) {
            event.setCancelled(true); // Cancel normal chat message
            String inputType = pendingInputs.get(playerId);

            if (inputType.isEmpty()) {
            	HousingAPI api = PlayerHousing.getAPI();
                String message = event.getMessage();
                OfflinePlayer owner = api.getWorldOwner(player.getWorld());
                if (player.getUniqueId() != owner.getUniqueId()) {
                	player.sendMessage("§aYou can not set a motd for " + owner.getName() + "'s world!");
                }
                api.getWorldInstance(owner.getUniqueId()).setDescription(message);
                player.sendMessage("§aYou have set the description to: §e" + message);
                
            }
            pendingInputs.remove(playerId);
        }
    }
	
	@EventHandler
	public void onWorldChangeEvent(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();
		HousingAPI api = PlayerHousing.getAPI();
		OfflinePlayer owner = api.getWorldOwner(player.getWorld());
		if (player.getUniqueId() != owner.getUniqueId()) {
			if (pendingInputs.containsKey(player.getUniqueId())) pendingInputs.remove(player.getUniqueId());
		}
	}
}
