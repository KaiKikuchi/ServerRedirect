package net.kaikk.mc.serverredirect.sponge;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.spongepowered.api.Platform.Type;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.ChannelBinding.IndexedMessageChannel;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import com.google.inject.Inject;

import net.kaikk.mc.serverredirect.PluginInfo;
import net.kaikk.mc.serverredirect.sponge.commands.FallbackCommandExec;
import net.kaikk.mc.serverredirect.sponge.commands.RedirectCommandExec;
import net.kaikk.mc.serverredirect.sponge.event.PlayerRedirectEvent;

@Plugin(id=PluginInfo.id, name = PluginInfo.name, version = PluginInfo.version, description = PluginInfo.description, authors = {PluginInfo.author})
public class ServerRedirect {
	protected static ServerRedirect instance;

	protected IndexedMessageChannel channelRedirect, channelFallback, channelAnnounce;
	protected static Set<UUID> players = Collections.synchronizedSet(new HashSet<>());

	@Inject
	protected Logger logger;

	@Inject
	protected PluginContainer container;

	protected Cause cause;

	@Listener
	public void onServerStart(GameStartingServerEvent event) throws Exception {
		instance = this;

		log("Loading "+PluginInfo.name+" v"+PluginInfo.version);
		try {
			cause = Cause.source(container).build();
		} catch (Throwable e) {
			cause = null;
		}

		Sponge.getCommandManager().register(this, new RedirectCommandExec(), "serverredirect", "redirect");
		Sponge.getCommandManager().register(this, new FallbackCommandExec(), "fallbackserver", "fallback");

		channelRedirect = Sponge.getChannelRegistrar().createChannel(this, "srvredirect:red");
		channelRedirect.registerMessage(ServerAddressMessage.class, 0);

		channelFallback = Sponge.getChannelRegistrar().createChannel(this, "srvredirect:fal");
		channelFallback.registerMessage(ServerAddressMessage.class, 0);

		channelAnnounce = Sponge.getChannelRegistrar().createChannel(this, "srvredirect:ann");
		channelAnnounce.registerMessage(VoidMessage.class, 0, Type.SERVER, (m, rc, pt) -> {
			try {
				if (rc instanceof PlayerConnection) {
					Player p = ((PlayerConnection) rc).getPlayer();
					players.add(p.getUniqueId());
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});

		Sponge.getEventManager().registerListener(this, ClientConnectionEvent.Disconnect.class, e -> players.remove(e.getTargetEntity().getUniqueId()));

		log("Load complete");
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
		if (Sponge.getEventManager().post(new PlayerRedirectEvent(player, serverAddress, instance.getCause()))) {
			return false;
		}

		instance.channelRedirect.sendTo(player, new ServerAddressMessage(serverAddress));
		return true;
	}

	/**
	 * Connects all players with this mod on their client to the specified server address.
	 * 
	 * @param serverAddress the new server address the players should connect to
	 */
	public static void sendToAll(String serverAddress) {
		final ServerAddressMessage message = new ServerAddressMessage(serverAddress);
		for (final Player player : Sponge.getServer().getOnlinePlayers()) {
			if (!Sponge.getEventManager().post(new PlayerRedirectEvent(player, serverAddress, instance.getCause()))) {
				instance.channelRedirect.sendTo(player, message);
			}
		}
	}

	public static void sendFallbackTo(Player player, String serverAddress) {
		instance.channelRedirect.sendTo(player, new ServerAddressMessage(serverAddress));
	}

	public static void sendFallbackToAll(String serverAddress) {
		final ServerAddressMessage message = new ServerAddressMessage(serverAddress);
		for (final Player player : Sponge.getServer().getOnlinePlayers()) {
			instance.channelRedirect.sendTo(player, message);
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

	public Logger logger() {
		return logger;
	}

	public static void log(String message) {
		if (instance.logger()!=null) {
			instance.logger().info(message);
		} else {
			System.out.println(message);
		}
	}

	public Cause getCause() {
		return cause;
	}

	public static Cause getCause(ServerRedirect instance) {
		try {
			return Cause.source(instance.container).build();
		} catch (Exception e) {
			return null;
		}
	}
}
