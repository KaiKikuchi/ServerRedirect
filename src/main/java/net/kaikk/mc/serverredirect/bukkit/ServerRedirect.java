package net.kaikk.mc.serverredirect.bukkit;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.kaikk.mc.serverredirect.Utils;
import net.kaikk.mc.serverredirect.bukkit.commands.FallbackServerCommandExec;
import net.kaikk.mc.serverredirect.bukkit.commands.RedirectCommandExec;
import net.kaikk.mc.serverredirect.bukkit.event.PlayerRedirectEvent;

public class ServerRedirect extends JavaPlugin implements Listener {
	protected static ServerRedirect instance;
	protected static Set<UUID> players = Collections.synchronizedSet(new HashSet<>());

	@Override
	public void onEnable() {
		instance = this;

		this.getCommand("serverredirect").setExecutor(new RedirectCommandExec());
		this.getCommand("fallbackserver").setExecutor(new FallbackServerCommandExec());

		Bukkit.getMessenger().registerOutgoingPluginChannel(this, "srvredirect:red");
		Bukkit.getMessenger().registerOutgoingPluginChannel(this, "srvredirect:fal");
		Bukkit.getMessenger().registerIncomingPluginChannel(this, "srvredirect:ann", ServerRedirect::onAnnounceReceived);

		Bukkit.getPluginManager().registerEvents(this, this);
		
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			new PlaceholderExpansionImpl().register();
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		players.remove(event.getPlayer().getUniqueId());
	}

	protected static void onAnnounceReceived(String channel, Player player, byte[] message) {
		players.add(player.getUniqueId());
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

	/**
	 * 
	 * <b>WARNING:</b> this will likely return false for a player that just logged in,
	 * as it takes some time for the client to send the announce packet to the server. 
	 * 
	 * @param player the player to check
	 * @return whether the specified player is using Server Redirect
	 */
	public static boolean isUsingServerRedirect(Player player) {
		return isUsingServerRedirect(player.getUniqueId());
	}

	/**
	 * 
	 * <b>WARNING:</b> this will likely return false for a player that just logged in,
	 * as it takes some time for the client to send the announce packet to the server. 
	 * 
	 * @param playerId the player to check
	 * @return whether the specified player is using Server Redirect
	 */
	public static boolean isUsingServerRedirect(UUID playerId) {
		return players.contains(playerId);
	}

	/**
	 * 
	 * Loop through the players with this mod<br>
	 * <br>
	 * <b>WARNING:</b> this will likely not include a player that just logged in,
	 * as it takes some time for the client to send the announce packet to the server. 
	 * 
	 * @param consumer a consumer that can do something with the player's UUID
	 */
	public static void forEachPlayerUsingServerRedirect(Consumer<UUID> consumer) {
		synchronized(players) {
			for (UUID playerId : players) {
				consumer.accept(playerId);
			}
		}
	}

	/**
	 * 
	 * An immutable copy of the set containing the players with this mod.<br>
	 * <br>
	 * For better performances, try to use the following methods instead:
	 * <ul>
	 * <li>{@link #isUsingServerRedirect(UUID)} to check whether a player is using this mod</li>
	 * <li>{@link #forEachPlayerUsingServerRedirect(Consumer)} to loop through the players with this mod</li>
	 * </ul>
	 * <b>WARNING:</b> this will likely not include a player that just logged in,
	 * as it takes some time for the client to send the announce packet to the server. 
	 * 
	 * @return an immutable copy of the players with this mod
	 */
	public static Set<UUID> getPlayers() {
		return Collections.unmodifiableSet(new HashSet<>(players));
	}

	public static ServerRedirect instance() {
		return instance;
	}
}
