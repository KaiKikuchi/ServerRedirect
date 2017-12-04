package net.kaikk.mc.serverredirect.sponge;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.spongepowered.api.Platform.Type;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.network.ChannelBinding.IndexedMessageChannel;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RawDataListener;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.SpongeExecutorService;

import com.google.inject.Inject;

import net.kaikk.mc.serverredirect.sponge.event.PlayerRedirectEvent;
import net.kaikk.mc.serverredirect.sponge.event.PlayerWithRedirectJoinEvent;

@Plugin(id=PluginInfo.id, name = PluginInfo.name, version = PluginInfo.version, description = PluginInfo.description, authors = {PluginInfo.author})
public class ServerRedirect implements RawDataListener {
	protected static ServerRedirect instance;

	protected IndexedMessageChannel channel;
	protected Set<Player> playersWithMod = Collections.newSetFromMap(new WeakHashMap<Player, Boolean>());
	protected SpongeExecutorService sync;
	
	@Inject
	protected Logger logger;

	@Inject
	protected PluginContainer container;
	
	@Listener
	public void onServerStart(GameInitializationEvent event) throws Exception {
		instance = this;
		
		sync = Sponge.getScheduler().createSyncExecutor(this);

		log("Loading "+PluginInfo.name+" v"+PluginInfo.version);

		// Register command
		Sponge.getCommandManager().register(this, new CommandExec(), PluginInfo.id, "redirect");

		channel = Sponge.getChannelRegistrar().createChannel(this, "ServerRedirect");
		channel.addHandler(RedirectModMessage.class, Type.SERVER, new RedirectModMessageHandler());
		channel.registerMessage(RedirectAddressMessage.class, 1);
		
		log("Load complete");
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
		
		if (Sponge.getEventManager().post(new PlayerRedirectEvent(player, serverAddress, instance.getCause()))) {
			return false;
		}
		
		instance.channel.sendTo(player, new RedirectAddressMessage(serverAddress));
		return true;
	}
	
	/**
	 * Connects all players with this mod on their client to the specified server address.
	 * 
	 * @param serverAddress the new server address the players should connect to
	 */
	public static void sendToAll(String serverAddress) {
		final RedirectAddressMessage message = new RedirectAddressMessage(serverAddress);
		for (final Player player : instance.playersWithMod) {
			if (player.isOnline()) {
				if (doesPlayerHaveThisMod(player)) {
					if (!Sponge.getEventManager().post(new PlayerRedirectEvent(player, serverAddress, instance.getCause()))) {
						instance.channel.sendTo(player, message);
					}
				}
			}
		}
	}
	
	@Override
	public void handlePayload(ChannelBuf data, RemoteConnection connection, Type side) {
		if (connection instanceof PlayerConnection) {
			sync.execute(() -> {
				Player player = ((PlayerConnection) connection).getPlayer();
				playersWithMod.add(player);
				Sponge.getEventManager().post(new PlayerWithRedirectJoinEvent(player, this.getCause()));
			});
		}
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
		return Cause.source(container).build();
	}
}
