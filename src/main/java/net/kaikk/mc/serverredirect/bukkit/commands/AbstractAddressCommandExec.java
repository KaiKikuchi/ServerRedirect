package net.kaikk.mc.serverredirect.bukkit.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
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
			double selectorDistance = Double.valueOf(args[0].substring(2));
			
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
				if (p.getLocation().distanceSquared(l) <= selectorDistance) {
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
			targetSelectPlayers(sender, args[0]).forEach(p -> handler(p, args[1]));
		}
		
		return true;
	}

	// TODO add target selectors arguments support
	public static Collection<? extends Player> targetSelectPlayers(CommandSender sender, String selector) {
		if (selector.length() < 2 || selector.charAt(0) != '@') {
			throw new IllegalArgumentException(selector + " is not a selector");
		}
		
		if (selector.equals("@a") || selector.equals("@e")) {
			return Bukkit.getOnlinePlayers();
		} else if (selector.equals("@p")) {
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

			Player nearestPlayer = null;
			double distance = Double.MAX_VALUE;
			for (Player p : l.getWorld().getPlayers()) {
				if (p != e) {
					if (nearestPlayer == null) {
						nearestPlayer = p;
					} else {
						double d = p.getLocation().distanceSquared(l);
						if (d < distance) {
							distance = d;
							nearestPlayer = p;
						}
					}
				}
			}

			if (nearestPlayer == null) {
				return Collections.emptyList();
			}

			return Arrays.asList(nearestPlayer);
		} else if (selector.equals("@r")) {
			if (Bukkit.getOnlinePlayers().isEmpty()) {
				return Collections.emptyList();
			}

			int n = (int) Math.ceil(Math.random() * (Bukkit.getOnlinePlayers().size() - 1));
			Iterator<? extends Player> it = Bukkit.getOnlinePlayers().iterator();
			while (--n >= 0) {
				it.next();
			}

			return Arrays.asList(it.next());
		} else if (selector.equals("@s")) {
			if (sender instanceof Player) {
				return Arrays.asList((Player) sender);
			} else {
				return Collections.emptyList();
			}
		} else {
			throw new UnsupportedOperationException(selector + " is not a currently supported selector");
		}
	}
}
