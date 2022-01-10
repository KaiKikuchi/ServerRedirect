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

import net.kaikk.mc.serverredirect.Utils;

public abstract class AbstractAddressCommandExec implements CommandExecutor {
	protected final String permission;
	
	protected AbstractAddressCommandExec(String permission) {
		this.permission = permission;
	}

	public abstract void handler(Player p, String addr);
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length<2) {
			sender.sendMessage("Usage: /" + label + " <Player> <Server Address>");
			return false;
		}

		if (!sender.hasPermission(permission)) {
			sender.sendMessage(ChatColor.RED + "Permission denied.");
			return false;
		}
		
		if (!Utils.ADDRESS_PREVALIDATOR.matcher(args[1]).matches()) {
			sender.sendMessage(ChatColor.RED + "The server address contains forbidden characters.");
			return false;
		}

		if (args[0].equals("*")) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				handler(p, args[1]);				
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
					handler(p, args[1]);
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
			
			handler(player, args[1]);
		} else {
			EntitySelector.selectEntities(sender, args[0]).forEach(e -> {
				if (e instanceof Player) {
					handler((Player) e, args[1]);
				}
			});
		}
		
		return true;
	}
}
