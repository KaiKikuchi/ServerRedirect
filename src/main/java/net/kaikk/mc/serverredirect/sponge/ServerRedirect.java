package net.kaikk.mc.serverredirect.sponge;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.network.ChannelBinding.IndexedMessageChannel;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.SpongeExecutorService;

import com.google.inject.Inject;

import net.kaikk.mc.serverredirect.sponge.commands.FallbackCommandExec;
import net.kaikk.mc.serverredirect.sponge.commands.RedirectCommandExec;
import net.kaikk.mc.serverredirect.sponge.event.PlayerRedirectEvent;

@Plugin(id=PluginInfo.id, name = PluginInfo.name, version = PluginInfo.version, description = PluginInfo.description, authors = {PluginInfo.author})
public class ServerRedirect {
	protected static ServerRedirect instance;

	protected IndexedMessageChannel channelRedirect, channelFallback;
	protected SpongeExecutorService sync;
	
	@Inject
	protected Logger logger;

	@Inject
	protected PluginContainer container;
	
	protected Cause cause;
	
	@Listener
	public void onServerStart(GameInitializationEvent event) throws Exception {
		instance = this;

		log("Loading "+PluginInfo.name+" v"+PluginInfo.version);
		try {
			cause = Cause.source(container).build();
		} catch (Throwable e) {
			cause = null;
		}
		
		sync = Sponge.getScheduler().createSyncExecutor(this);

		Sponge.getCommandManager().register(this, new RedirectCommandExec(), "serverredirect", "redirect");
		Sponge.getCommandManager().register(this, new FallbackCommandExec(), "fallbackserver", "fallback");

		channelRedirect = Sponge.getChannelRegistrar().createChannel(this, "srvredirect:red");
		channelRedirect.registerMessage(ServerAddressMessage.class, 0);

		channelFallback = Sponge.getChannelRegistrar().createChannel(this, "srvredirect:fal");
		channelFallback.registerMessage(ServerAddressMessage.class, 0);
		
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