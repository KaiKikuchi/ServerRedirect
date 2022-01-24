package net.kaikk.mc.serverredirect.velocity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;

import net.kaikk.mc.serverredirect.Utils;
import net.kaikk.mc.serverredirect.sponge.PluginInfo;
import net.kaikk.mc.serverredirect.velocity.commands.FallbackCommandExec;
import net.kaikk.mc.serverredirect.velocity.commands.RedirectCommandExec;
import net.kaikk.mc.serverredirect.velocity.event.PlayerRedirectEvent;

@Plugin(id=PluginInfo.id, name = PluginInfo.name, version = PluginInfo.version, description = PluginInfo.description, authors = {PluginInfo.author})
public class ServerRedirect {
	protected static ServerRedirect instance;

	private final ProxyServer proxy;
	private final Logger logger;

	public static final ChannelIdentifier redirectChannel = MinecraftChannelIdentifier.create("srvredirect", "red");
	public static final ChannelIdentifier fallbackChannel = MinecraftChannelIdentifier.create("srvredirect", "fal");
	public static final ChannelIdentifier announceChannel = MinecraftChannelIdentifier.create("srvredirect", "ann");
	protected static Set<UUID> players = Collections.synchronizedSet(new HashSet<>());

	@Inject
	public ServerRedirect(ProxyServer server, Logger logger) {
		instance = this;

		this.proxy = server;
		this.logger = logger;
	}

	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event) {
		proxy.getCommandManager().register("serverredirect", new RedirectCommandExec(), "redirect");
		proxy.getCommandManager().register("fallbackserver", new FallbackCommandExec(), "fallback");

		proxy.getChannelRegistrar().register(announceChannel);

		proxy.getEventManager().register(this, PluginMessageEvent.class, e -> {
			try {
				if (e.getIdentifier().equals(announceChannel) && e.getSource() instanceof Player) {
					players.add(((Player) e.getSource()).getUniqueId());
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});

		proxy.getEventManager().register(this, DisconnectEvent.class, e -> players.remove(e.getPlayer().getUniqueId()));
	}

	/**
	 * Connects the specified player to the specified server address.<br>
	 * The client must have this mod in order for this to work.
	 * 
	 * @param serverAddress the new server address the player should connect to
	 * @param player the player's instance
	 */
	public static void sendTo(Player player, String serverAddress) {
		proxy().getEventManager().fire(new PlayerRedirectEvent(player, serverAddress)).thenAccept(event -> {
			if (event.isCancelled()) {
				return;
			}

			player.sendPluginMessage(redirectChannel, Utils.generateAddressMessage(serverAddress));
		});
	}

	/**
	 * Connects all players with this mod on their client to the specified server address.
	 * 
	 * @param serverAddress the new server address the players should connect to
	 */
	public static void sendToAll(String serverAddress) {
		final byte[] message = Utils.generateAddressMessage(serverAddress);
		for (final Player player : proxy().getAllPlayers()) {
			proxy().getEventManager().fire(new PlayerRedirectEvent(player, serverAddress)).thenAccept(event -> {
				if (event.isCancelled()) {
					return;
				}

				player.sendPluginMessage(redirectChannel, message);
			});
		}
	}

	public static void sendFallbackTo(Player player, String serverAddress) {
		player.sendPluginMessage(fallbackChannel, Utils.generateAddressMessage(serverAddress));
	}

	public static void sendFallbackToAll(String serverAddress) {
		final byte[] message = Utils.generateAddressMessage(serverAddress);
		for (final Player player : proxy().getAllPlayers()) {
			player.sendPluginMessage(fallbackChannel, message);
		}
	}

	public static ServerRedirect instance() {
		return instance;
	}

	public static ProxyServer proxy() {
		return instance.proxy;
	}

	public static Logger logger() {
		return instance.logger;
	}
}
