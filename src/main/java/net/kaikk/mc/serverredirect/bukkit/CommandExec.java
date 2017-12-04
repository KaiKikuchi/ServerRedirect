package net.kaikk.mc.serverredirect.bukkit;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class CommandExec implements CommandExecutor {
	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length<2) {
			sender.sendMessage("Usage: /redirect <address> <PlayerName|PlayerUUID|\"*\">");
			return false;
		}
		
		if (!sender.hasPermission("serverredirect.command.redirect")) {
			sender.sendMessage(ChatColor.RED + "Permission denied.");
			return false;
		}

		if (args[1].equals("*")) {
			ServerRedirect.sendToAll(args[0]);
		} else {
			Player playerToSend;
			try {
				playerToSend = Bukkit.getPlayer(UUID.fromString(args[1]));
			} catch (IllegalArgumentException e) {
				playerToSend = Bukkit.getPlayer(args[1]);
			}
			
			if (playerToSend == null) {
				sender.sendMessage("Player "+args[1]+" not found");
				return false;
			}
			
			ServerRedirect.sendTo(playerToSend, args[0]);
		}
		return true;
	}
}
