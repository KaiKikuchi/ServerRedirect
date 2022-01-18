package net.kaikk.mc.serverredirect.forge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;

import net.kaikk.mc.serverredirect.forge.event.PlayerRedirectEvent;
import net.kaikk.mc.serverredirect.forge.event.RedirectEvent;
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
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

@Mod(ServerRedirect.MODID)
public class ServerRedirect {
	public static final String MODID = "serverredirect";
	public static final Logger LOGGER = LogManager.getLogger();
	@OnlyIn(Dist.CLIENT)
	public static volatile String fallbackServerAddress;

	public ServerRedirect() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		MinecraftForge.EVENT_BUS.register(this);
	}

	private void setup(final FMLCommonSetupEvent event) {
		PacketHandler.init();
	}

	@SubscribeEvent
	public void onRegisterCommands(RegisterCommandsEvent event) {
		event.getDispatcher().register(
				Commands.literal("serverredirect")
				.requires(cs -> cs.hasPermission(2))
				.then(command(PacketHandler.REDIRECT_CHANNEL))
				);
		event.getDispatcher().register(
				Commands.literal("fallbackserver")
				.requires(cs -> cs.hasPermission(2))
				.then(command(PacketHandler.FALLBACK_CHANNEL))
				);
	}

	private ArgumentBuilder<CommandSource, ?> command(SimpleChannel channel) {
		return Commands.argument("Player(s)", EntityArgument.players())
				.then(Commands.argument("Server Address", StringArgumentType.string())
						.executes(cs -> {
							try {
								String addr = cs.getArgument("Server Address", String.class);
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

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (fallbackServerAddress != null) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.screen instanceof DisconnectedScreen) {
				String addr = fallbackServerAddress;
				fallbackServerAddress = null;
				redirect(addr);
			} else if (mc.screen instanceof MainMenuScreen || mc.screen instanceof MultiplayerScreen) {
				fallbackServerAddress = null;
			}
		}
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
		
		for (ServerPlayerEntity player : pl.getPlayers()) {
			if (!MinecraftForge.EVENT_BUS.post(new PlayerRedirectEvent(player, serverAddress))) {
				PacketHandler.FALLBACK_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), serverAddress);
			}
		}
	}
}
