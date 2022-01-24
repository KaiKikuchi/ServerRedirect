package net.kaikk.mc.serverredirect.bungee;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import net.kaikk.mc.serverredirect.Utils;
import net.kaikk.mc.serverredirect.bungee.commands.FallbackServerCommandExec;
import net.kaikk.mc.serverredirect.bungee.commands.RedirectCommandExec;
import net.kaikk.mc.serverredirect.bungee.event.PlayerRedirectEvent;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class ServerRedirect extends Plugin implements Listener {
	protected static ServerRedirect instance;
	protected static Set<UUID> players = Collections.synchronizedSet(new HashSet<>());

	@Override
	public void onEnable() {
		instance = this;

		this.getProxy().getPluginManager().registerCommand(this, new RedirectCommandExec());
		this.getProxy().getPluginManager().registerCommand(this, new FallbackServerCommandExec());

		this.getProxy().getPluginManager().registerListener(this, this);

		this.getProxy().registerChannel("srvredirect:red");
		this.getProxy().registerChannel("srvredirect:fal");
		this.getProxy().registerChannel("srvredirect:ann");
	}

	@EventHandler
	public void onPlayerQuit(PlayerDisconnectEvent e) {
		players.remove(e.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onPluginMessage(PluginMessageEvent e) {
		if (e.getTag().equals("srvredirect:ann") && e.getSender() instanceof ProxiedPlayer) {
			players.add(((ProxiedPlayer) e.getSender()).getUniqueId());
		}
	}

	/**
	 * Connects the specified player to the specified server address.<br>
	 * The client must have this mod in order for this to work.
	 * 
	 * @param serverAddress the new server address the player should connect to
	 * @param player the player's instance
	 * @return true if the redirect message was sent to the specified player
	 */
	public static boolean sendTo(ProxiedPlayer player, String serverAddress) {
		final PlayerRedirectEvent event = new PlayerRedirectEvent(player, serverAddress);
		ProxyServer.getInstance().getPluginManager().callEvent(event);

		if (event.isCancelled()) {
			return false;
		}

		player.sendData("srvredirect:red", Utils.generateAddressMessage(serverAddress));
		return true;
	}

	/**
	 * Connects all players with this mod on their client to the specified server address.
	 * 
	 * @param serverAddress the new server address the players should connect to
	 */
	public static void sendToAll(String serverAddress) {
		final byte[] message = Utils.generateAddressMessage(serverAddress);
		for (final ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
			final PlayerRedirectEvent event = new PlayerRedirectEvent(player, serverAddress);
			ProxyServer.getInstance().getPluginManager().callEvent(event);

			if (!event.isCancelled()) {
				player.sendData("srvredirect:red", message);
			}
		}
	}

	public static boolean sendFallbackTo(ProxiedPlayer player, String serverAddress) {
		player.sendData("srvredirect:fal", Utils.generateAddressMessage(serverAddress));
		return true;
	}

	public static void sendFallbackToAll(String serverAddress) {
		final byte[] message = Utils.generateAddressMessage(serverAddress);
		for (final ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
			player.sendData("srvredirect:fal", message);
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
	public static boolean isUsingServerRedirect(ProxiedPlayer player) {
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
