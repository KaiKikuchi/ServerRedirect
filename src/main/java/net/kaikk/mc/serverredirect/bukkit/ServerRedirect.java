package net.kaikk.mc.serverredirect.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.kaikk.mc.serverredirect.Utils;
import net.kaikk.mc.serverredirect.bukkit.commands.FallbackServerCommandExec;
import net.kaikk.mc.serverredirect.bukkit.commands.RedirectCommandExec;
import net.kaikk.mc.serverredirect.bukkit.event.PlayerRedirectEvent;

public class ServerRedirect extends JavaPlugin {
	protected static ServerRedirect instance;

	@Override
	public void onEnable() {
		instance = this;

		this.getCommand("serverredirect").setExecutor(new RedirectCommandExec());
		this.getCommand("fallbackserver").setExecutor(new FallbackServerCommandExec());
		
		Bukkit.getMessenger().registerOutgoingPluginChannel(this, "srvredirect:red");
		Bukkit.getMessenger().registerOutgoingPluginChannel(this, "srvredirect:fal");
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
		final PlayerRedirectEvent event = new PlayerRedirectEvent(player, serverAddress);
		Bukkit.getPluginManager().callEvent(event);
		
		if (event.isCancelled()) {
			return false;
		}
		
		player.sendPluginMessage(instance, "srvredirect:red", Utils.generateAddressMessage(serverAddress));
		return true;
	}
	
	/**
	 * Connects all players with this mod on their client to the specified server address.
	 * 
	 * @param serverAddress the new server address the players should connect to
	 */
	public static void sendToAll(String serverAddress) {
		final byte[] message = Utils.generateAddressMessage(serverAddress);
		for (final Player player : Bukkit.getOnlinePlayers()) {
			final PlayerRedirectEvent event = new PlayerRedirectEvent(player, serverAddress);
			Bukkit.getPluginManager().callEvent(event);
			
			if (!event.isCancelled()) {
				player.sendPluginMessage(instance, "srvredirect:red", message);
			}
		}
	}
	
	public static boolean sendFallbackTo(Player player, String serverAddress) {
		player.sendPluginMessage(instance, "srvredirect:fal", Utils.generateAddressMessage(serverAddress));
		return true;
	}
	
	public static void sendFallbackToAll(String serverAddress) {
		final byte[] message = Utils.generateAddressMessage(serverAddress);
		for (final Player player : Bukkit.getOnlinePlayers()) {
			player.sendPluginMessage(instance, "srvredirect:fal", message);
		}
	}
	
	public static ServerRedirect instance() {
		return instance;
	}
}
