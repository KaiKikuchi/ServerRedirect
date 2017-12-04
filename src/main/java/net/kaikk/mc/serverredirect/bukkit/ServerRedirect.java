package net.kaikk.mc.serverredirect.bukkit;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import net.kaikk.mc.serverredirect.bukkit.event.PlayerRedirectEvent;
import net.kaikk.mc.serverredirect.bukkit.event.PlayerWithRedirectJoinEvent;

public class ServerRedirect extends JavaPlugin implements PluginMessageListener {
	protected static ServerRedirect instance;
	protected Set<Player> playersWithMod = Collections.newSetFromMap(new WeakHashMap<Player, Boolean>());

	@Override
	public void onEnable() {
		instance = this;

		// commands executor
		CommandExec ce = new CommandExec();
		this.getDescription().getCommands().keySet().forEach((cmd) -> this.getCommand(cmd).setExecutor(ce));
		
		Bukkit.getMessenger().registerOutgoingPluginChannel(this, "ServerRedirect");
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "ServerRedirect", this);
	}
	
	/**
	 * Checks if the player has this mod installed.
	 * 
	 * @param player the player
	 * @return whether the player has the mod installed
	 */
	public static boolean doesPlayerHaveThisMod(Player player) {
		return instance.playersWithMod.contains(player);
	}
	
	/**
	 * @return the set with all the Player having this mod.
	 */
	public static Set<Player> getPlayersWithMod() {
		return instance.playersWithMod;
	}
	
	/**
	 * Connects the specified player to the specified server address.<br>
	 * The client must have this mod in order for this to work.
	 * 
	 * @param serverAddress the new server address the player should connect to
	 * @param player the player's instance
	 * @return true if the redirect message was sent to the specified player
	 */
	public static boolean sendTo(Player player, String serverAddress) {
		if (!doesPlayerHaveThisMod(player)) {
			return false;
		}
		
		final PlayerRedirectEvent event = new PlayerRedirectEvent(player, serverAddress);
		Bukkit.getPluginManager().callEvent(event);
		
		if (event.isCancelled()) {
			return false;
		}
		
		player.sendPluginMessage(instance, "ServerRedirect", generateAddressMessage(serverAddress));
		return true;
	}
	
	/**
	 * Connects all players with this mod on their client to the specified server address.
	 * 
	 * @param serverAddress the new server address the players should connect to
	 */
	public static void sendToAll(String serverAddress) {
		final byte[] message = generateAddressMessage(serverAddress);
		for (final Player player : instance.playersWithMod) {
			if (player.isOnline()) {
				final PlayerRedirectEvent event = new PlayerRedirectEvent(player, serverAddress);
				Bukkit.getPluginManager().callEvent(event);
				
				if (!event.isCancelled()) {
					player.sendPluginMessage(instance, "ServerRedirect", message);
				}
			}
		}
	}
	
	protected static byte[] generateAddressMessage(String address) {
		final byte[] addressBytes = address.getBytes();
		final byte[] message = new byte[addressBytes.length + 1];
		message[0] = 1; // discriminator
		System.arraycopy(addressBytes, 0, message, 1, addressBytes.length);
		return message;
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		new BukkitRunnable() {
			@Override
			public void run() {
				playersWithMod.add(player);
				Bukkit.getPluginManager().callEvent(new PlayerWithRedirectJoinEvent(player));
			}
		}.runTask(this);
	}
}
