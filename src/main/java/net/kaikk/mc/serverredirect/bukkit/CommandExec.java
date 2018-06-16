package net.kaikk.mc.serverredirect.bukkit;

import java.util.StringJoiner;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class CommandExec implements CommandExecutor {
	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length<2) {
			sender.sendMessage("Usage: /redirect <address> <PlayerName|PlayerUUID|\"r=[radius]\"|\"*\">");
			sender.sendMessage("Usage: /redirect \"playerslist\"");
			return false;
		}
		
		if (args[0].equalsIgnoreCase("playerslist")) {
			if (!sender.hasPermission("serverredirect.command.list")) {
				sender.sendMessage(ChatColor.RED + "Permission denied.");
				return false;
			}
			
			StringJoiner players = new StringJoiner(", ");
			for (Player player : ServerRedirect.instance.playersWithMod) {
				players.add(player.getName());
			}
			sender.sendMessage(ChatColor.GOLD + "Players with the mod: " + players.toString());
			return true;
		}
		
		if (args[1].equals("*")) {
			if (!sender.hasPermission("serverredirect.command.redirect.all")) {
				sender.sendMessage(ChatColor.RED + "Permission denied.");
				return false;
			}
			ServerRedirect.sendToAll(args[0]);
		} else if (args[1].startsWith("r=")) {
			if (!sender.hasPermission("serverredirect.command.redirect.radius")) {
				sender.sendMessage(ChatColor.RED + "Permission denied.");
				return false;
			}
			final Location loc;
			if (sender instanceof Player) {
				loc = ((Player) sender).getLocation();
			} else if (sender instanceof BlockCommandSender) {
				loc = ((BlockCommandSender) sender).getBlock().getLocation();
			} else {
				sender.sendMessage("Only players and command blocks can use the \"r=\" parameter!");
				return false;
			}
			
			final int radiusSquared = (int) Math.pow(Integer.valueOf(args[1].substring(2)), 2);
			
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.getLocation().getWorld() == loc.getWorld() && p.getLocation().distanceSquared(loc) < radiusSquared) {
					ServerRedirect.sendTo(p, args[0]);
				}
			}
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
			
			if (!sender.hasPermission("serverredirect.command.redirect."+(playerToSend == sender ? "self" : "others"))) {
				sender.sendMessage(ChatColor.RED + "Permission denied.");
				return false;
			}
			
			ServerRedirect.sendTo(playerToSend, args[0]);
		}
		return true;
	}
}
