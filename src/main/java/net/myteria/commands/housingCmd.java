package net.myteria.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import net.myteria.HousingAPI;
import net.myteria.PlayerHousing;

public class housingCmd implements CommandExecutor {
	HousingAPI api = PlayerHousing.getAPI();

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
			@NotNull String[] args) {
		Player player = (Player) sender;
		
		if (api.getWorldInstance(player.getUniqueId()) != null &&player.getWorld() == api.getWorldInstance(player.getUniqueId()).getWorld()) {
			player.openInventory(api.getHousingMenu().getInventory());
			
		} else {
			if (api.getConfigManager().hasWorld(player.getUniqueId())) {
				api.loadWorld(player.getUniqueId());
				api.joinWorld(player, player.getUniqueId());
				return true;
			}
			api.templatesMenuInv.put(player, Bukkit.createInventory(api.getTemplatesMenu(), 5*9, "Templates Menu"));
			api.getTemplatesMenu().setInventory(api.templatesMenuInv.get(player), api.templatesMenuPage.get(player));
			player.openInventory(api.getTemplatesMenu().getInventory());
		}
				
		return true;
	}

}
