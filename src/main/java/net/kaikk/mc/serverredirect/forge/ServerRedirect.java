package net.kaikk.mc.serverredirect.forge;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;

import net.kaikk.mc.serverredirect.forge.event.ClientFallbackEvent;
import net.kaikk.mc.serverredirect.forge.event.ClientRedirectEvent;
import net.kaikk.mc.serverredirect.forge.event.PlayerFallbackEvent;
import net.kaikk.mc.serverredirect.forge.event.PlayerRedirectEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConnectingScreen;
import net.minecraft.client.gui.screen.DirtMessageScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

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
				Commands.literal("redirect")
				.requires(cs -> cs.hasPermission(2))
				.redirect(event.getDispatcher().register(
						Commands.literal("serverredirect")
						.requires(cs -> cs.hasPermission(2))
						.then(commandAddress(ServerRedirect::sendTo))
						))
				);
		event.getDispatcher().register(
				Commands.literal("fallback")
				.requires(cs -> cs.hasPermission(2))
				.redirect(event.getDispatcher().register(
						Commands.literal("fallbackserver")
						.requires(cs -> cs.hasPermission(2))
						.then(commandAddress(ServerRedirect::sendFallbackTo))
						))
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

	private ArgumentBuilder<CommandSource, ?> commandAddress(BiConsumer<ServerPlayerEntity, String> consumer) {
		return Commands.argument("Player(s)", EntityArgument.players())
				.then(Commands.argument("Server Address", StringArgumentType.greedyString())
						.executes(cs -> {
							try {
								String addr = cs.getArgument("Server Address", String.class);
								if (!PacketHandler.ADDRESS_PREVALIDATOR.matcher(addr).matches()) {
									cs.getSource().sendFailure(new StringTextComponent("Invalid Server Address"));
									return 0;
								}

								cs.getArgument("Player(s)", EntitySelector.class).findPlayers(cs.getSource()).forEach(p -> {
									try {
										consumer.accept(p, addr);
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

	private ArgumentBuilder<CommandSource, ?> commandIfPlayerRedirect(boolean not) {
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
			} else if (mc.screen instanceof MainMenuScreen || mc.screen instanceof MultiplayerScreen) {
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

		if (MinecraftForge.EVENT_BUS.post(new ClientRedirectEvent(serverAddress))) {
			return;
		}

		LOGGER.info("Connecting to " + serverAddress);

		final Minecraft mc = Minecraft.getInstance();
		if (mc.level != null) {
			mc.level.disconnect();
		}
		if (mc.isLocalServer()) {
			mc.clearLevel(new DirtMessageScreen(new TranslationTextComponent("menu.savingLevel")));
		} else {
			mc.clearLevel();
		}
		mc.setScreen(new MultiplayerScreen(new MainMenuScreen()));
		mc.setScreen(new ConnectingScreen(mc.screen, mc, new ServerData("ServerRedirect", serverAddress, false)));
	}

	@OnlyIn(Dist.CLIENT)
	public static String getFallbackServerAddress() {
		return fallbackServerAddress;
	}

	@OnlyIn(Dist.CLIENT)
	public static void setFallbackServerAddress(String fallbackServerAddress) {
		if (MinecraftForge.EVENT_BUS.post(new ClientFallbackEvent(fallbackServerAddress))) {
			return;
		}

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
	public static boolean sendTo(ServerPlayerEntity player, String serverAddress) {
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

		for (ServerPlayerEntity player : pl.getPlayers()) {
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
	public static boolean sendFallbackTo(ServerPlayerEntity player, String serverAddress) {
		if (MinecraftForge.EVENT_BUS.post(new PlayerFallbackEvent(player, serverAddress))) {
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

		for (ServerPlayerEntity player : pl.getPlayers()) {
			if (!MinecraftForge.EVENT_BUS.post(new PlayerFallbackEvent(player, serverAddress))) {
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
	public static boolean isUsingServerRedirect(ServerPlayerEntity player) {
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
