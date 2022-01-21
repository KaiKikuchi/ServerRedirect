package net.kaikk.mc.serverredirect.bungee;

import net.kaikk.mc.serverredirect.Utils;
import net.kaikk.mc.serverredirect.bungee.commands.FallbackServerCommandExec;
import net.kaikk.mc.serverredirect.bungee.commands.RedirectCommandExec;
import net.kaikk.mc.serverredirect.bungee.event.PlayerRedirectEvent;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class ServerRedirect extends Plugin {
	protected static ServerRedirect instance;

	@Override
	public void onEnable() {
		instance = this;

		this.getProxy().getPluginManager().registerCommand(this, new RedirectCommandExec());
		this.getProxy().getPluginManager().registerCommand(this, new FallbackServerCommandExec());
		
		this.getProxy().registerChannel("srvredirect:red");
		this.getProxy().registerChannel("srvredirect:fal");
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
	
	public static ServerRedirect instance() {
		return instance;
	}
}
