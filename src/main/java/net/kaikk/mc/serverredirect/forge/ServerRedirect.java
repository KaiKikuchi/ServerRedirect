package net.kaikk.mc.serverredirect.forge;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;

import net.kaikk.mc.serverredirect.forge.event.PlayerRedirectEvent;
import net.kaikk.mc.serverredirect.forge.event.RedirectEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod(ServerRedirect.MODID)
public class ServerRedirect {
	public static final String MODID = "serverredirect";
	public static final Logger LOGGER = LogManager.getLogger();
	protected static final Set<UUID> players = Collections.synchronizedSet(new HashSet<>());
	@OnlyIn(Dist.CLIENT)
	public static volatile String fallbackServerAddress;
	@OnlyIn(Dist.CLIENT)
	public static boolean connected;

	public ServerRedirect() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
	}

	private void setup(final FMLCommonSetupEvent event) {
		PacketHandler.init();
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onRegisterCommands(RegisterCommandsEvent event) {
		event.getDispatcher().register(
				Commands.literal("serverredirect")
				.requires(cs -> cs.hasPermission(2))
				.then(commandAddress(PacketHandler.REDIRECT_CHANNEL))
				);
		event.getDispatcher().register(
				Commands.literal("redirect")
				.requires(cs -> cs.hasPermission(2))
				.then(commandAddress(PacketHandler.REDIRECT_CHANNEL))
				);

		event.getDispatcher().register(
				Commands.literal("fallbackserver")
				.requires(cs -> cs.hasPermission(2))
				.then(commandAddress(PacketHandler.FALLBACK_CHANNEL))
				);
		event.getDispatcher().register(
				Commands.literal("fallback")
				.requires(cs -> cs.hasPermission(2))
				.then(commandAddress(PacketHandler.FALLBACK_CHANNEL))
				);

		event.getDispatcher().register(
				Commands.literal("ifplayercanredirect")
				.requires(cs -> cs.hasPermission(2))
				.then(commandIfPlayerRedirect(false))
				);
		event.getDispatcher().register(
				Commands.literal("ifplayercannotredirect")
				.requires(cs -> cs.hasPermission(2))
				.then(commandIfPlayerRedirect(true))
				);
	}

	private ArgumentBuilder<CommandSourceStack, ?> commandAddress(SimpleChannel channel) {
		return Commands.argument("Player(s)", EntityArgument.players())
				.then(Commands.argument("Server Address", StringArgumentType.greedyString())
						.executes(cs -> {
							try {
								String addr = cs.getArgument("Server Address", String.class);
								if (!PacketHandler.ADDRESS_PREVALIDATOR.matcher(addr).matches()) {
									cs.getSource().sendFailure(new TextComponent("Invalid Server Address"));
									return 0;
								}

								cs.getArgument("Player(s)", EntitySelector.class).findPlayers(cs.getSource()).forEach(p -> {
									try {
										sendTo(p, addr);
									} catch (Exception e) {
										e.printStackTrace();
									}
								});
							} catch (Exception e) {
								e.printStackTrace();
							}
							return 0;
						}));
	}

	private ArgumentBuilder<CommandSourceStack, ?> commandIfPlayerRedirect(boolean not) {
		return Commands.argument("Player(s)", EntityArgument.players())
				.then(Commands.argument("Command...", StringArgumentType.greedyString())
						.executes(cs -> {
							try {
								String command = cs.getArgument("Command...", String.class);

								cs.getArgument("Player(s)", EntitySelector.class).findPlayers(cs.getSource()).forEach(p -> {
									try {
										if (isUsingServerRedirect(p) != not) {
											cs.getSource().getServer().getCommands().performCommand(cs.getSource(), command.replace("%PlayerName", p.getGameProfile().getName()).replace("%PlayerId", p.getStringUUID()));
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
								});
							} catch (Exception e) {
								e.printStackTrace();
							}
							return 0;
						}));
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase != Phase.END) {
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		if (connected != (mc.level != null)) {
			connected = mc.level != null;
			if (connected) {
				PacketHandler.ANNOUNCE_CHANNEL.sendToServer(PacketHandler.EMPTY_OBJECT);
			}
		} else if (fallbackServerAddress != null) {
			if (mc.screen instanceof DisconnectedScreen) {
				String addr = fallbackServerAddress;
				fallbackServerAddress = null;
				redirect(addr);
			} else if (mc.screen instanceof TitleScreen || mc.screen instanceof JoinMultiplayerScreen) {
				fallbackServerAddress = null;
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		players.remove(event.getPlayer().getUUID());
	}

	/**
	 * Processes the redirect client side.<br>
	 * This simulates clicking the disconnect button and a direct connection to the specified server address.
	 * 
	 * @param serverAddress the new server address this client should connect to
	 * @throws IllegalStateException if called while not in the main thread
	 */
	@OnlyIn(Dist.CLIENT)
	public static void redirect(String serverAddress) {
		if (!Minecraft.getInstance().isSameThread()) {
			throw new IllegalStateException("Not in the main thread");
		}

		if (MinecraftForge.EVENT_BUS.post(new RedirectEvent(serverAddress))) {
			return;
		}

		LOGGER.info("Connecting to " + serverAddress);

		final Minecraft mc = Minecraft.getInstance();
		if (mc.level != null) {
			mc.level.disconnect();
		}
		if (mc.isLocalServer()) {
			mc.clearLevel(new GenericDirtMessageScreen(new TranslatableComponent("menu.savingLevel")));
		} else {
			mc.clearLevel();
		}
		mc.setScreen(new JoinMultiplayerScreen(new TitleScreen()));
		ConnectScreen.startConnecting(mc.screen, mc, ServerAddress.parseString(serverAddress), new ServerData("ServerRedirect", serverAddress, false));
	}

	@OnlyIn(Dist.CLIENT)
	public static String getFallbackServerAddress() {
		return fallbackServerAddress;
	}

	@OnlyIn(Dist.CLIENT)
	public static void setFallbackServerAddress(String fallbackServerAddress) {
		ServerRedirect.fallbackServerAddress = fallbackServerAddress;
	}

	/**
	 * Connects the specified player to the specified server address.<br>
	 * The client must have this mod in order for this to work.
	 * 
	 * @param serverAddress the new server address the player should connect to
	 * @param player the player's instance
	 * @return true if the redirect message was sent to the specified player
	 */
	public static boolean sendTo(ServerPlayer player, String serverAddress) {
		if (MinecraftForge.EVENT_BUS.post(new PlayerRedirectEvent(player, serverAddress))) {
			return false;
		}
		PacketHandler.REDIRECT_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), serverAddress);
		return true;
	}

	/**
	 * Connects all players with this mod on their client to the specified server address.
	 * 
	 * @param serverAddress the new server address the players should connect to
	 */
	public static void sendToAll(String serverAddress) {
		final PlayerList pl = ServerLifecycleHooks.getCurrentServer().getPlayerList();

		for (ServerPlayer player : pl.getPlayers()) {
			if (!MinecraftForge.EVENT_BUS.post(new PlayerRedirectEvent(player, serverAddress))) {
				PacketHandler.REDIRECT_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), serverAddress);
			}
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
	public static boolean sendFallbackTo(ServerPlayer player, String serverAddress) {
		if (MinecraftForge.EVENT_BUS.post(new PlayerRedirectEvent(player, serverAddress))) {
			return false;
		}
		PacketHandler.FALLBACK_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), serverAddress);
		return true;
	}

	/**
	 * Connects all players with this mod on their client to the specified server address.
	 * 
	 * @param serverAddress the new server address the players should connect to
	 */
	public static void sendFallbackToAll(String serverAddress) {
		final PlayerList pl = ServerLifecycleHooks.getCurrentServer().getPlayerList();

		for (ServerPlayer player : pl.getPlayers()) {
			if (!MinecraftForge.EVENT_BUS.post(new PlayerRedirectEvent(player, serverAddress))) {
				PacketHandler.FALLBACK_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), serverAddress);
			}
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
	public static boolean isUsingServerRedirect(ServerPlayer player) {
		return isUsingServerRedirect(player.getUUID());
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
}
