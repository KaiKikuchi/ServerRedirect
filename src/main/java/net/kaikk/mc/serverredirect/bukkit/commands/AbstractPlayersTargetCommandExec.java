package net.kaikk.mc.serverredirect.bukkit.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public abstract class AbstractPlayersTargetCommandExec implements CommandExecutor {
	protected final String permission;
	
	protected AbstractPlayersTargetCommandExec(String permission) {
		this.permission = permission;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!argumentsCheck(sender, cmd, label, args)) {
			return false;
		}

		if (!sender.hasPermission(permission)) {
			sender.sendMessage(ChatColor.RED + "Permission denied.");
			return false;
		}

		if (args[0].equals("*")) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				handler(p, sender, cmd, label, args);			
			}
		} else if (args[0].length() >= 3 && args[0].startsWith("r=")) {
			double distance = Double.valueOf(args[0].substring(2));
			distance *= distance;
			
			Location l;
			Entity e = null;
			if (sender instanceof Entity) {
				e = (Entity) sender;
				l = e.getLocation();
			} else if (sender instanceof BlockCommandSender) {
				BlockCommandSender csender = (BlockCommandSender) sender;
				l = csender.getBlock().getLocation();
			} else {
				l = Bukkit.getWorlds().get(0).getSpawnLocation();
			}

			for (Player p : l.getWorld().getPlayers()) {
				if (p.getLocation().distanceSquared(l) <= distance) {
					handler(p, sender, cmd, label, args);	
				}
			}
		} else if (args[0].length() < 2 || args[0].charAt(0) != '@') {
			Player player;
			if (args[0].length() == 36) {
				UUID playerId = UUID.fromString(args[0]);
				player = Bukkit.getPlayer(playerId);
				if (player == null) {
					player = Bukkit.getPlayer(args[0]);
				}
			} else {
				player = Bukkit.getPlayer(args[0]);
			}
			
			if (player == null) {
				return true;
			}
			
			handler(player, sender, cmd, label, args);	
		} else {
			EntitySelector.selectEntities(sender, args[0]).forEach(e -> {
				if (e instanceof Player) {
					handler((Player) e, sender, cmd, label, args);	
				}
			});
		}
		
		return true;
	}
	
	public boolean argumentsCheck(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1) {
			sender.sendMessage(getUsage(sender, cmd, label, args));
			return false;
		}
		return true;
	}
	
	public String getUsage(CommandSender sender, Command cmd, String label, String[] args) {
		return "Usage: /" + label + " <Player>";
	}
	
	public abstract void handler(Player p, CommandSender sender, Command cmd, String label, String[] args);
}
