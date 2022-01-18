package net.kaikk.mc.serverredirect.forge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.kaikk.mc.serverredirect.forge.PacketHandler.AddressMessage;
import net.kaikk.mc.serverredirect.forge.event.PlayerRedirectEvent;
import net.kaikk.mc.serverredirect.forge.event.RedirectEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = ServerRedirect.MODID, name = ServerRedirect.NAME, version = ServerRedirect.VERSION, acceptableRemoteVersions = "*")
public class ServerRedirect {
	public static final String MODID = "serverredirect";
	public static final String NAME = "ServerRedirect";
	public static final String VERSION = "1.4";
	public static final Logger LOGGER = LogManager.getLogger();
	@SideOnly(Side.CLIENT)
	public static volatile String fallbackServerAddress;

	@EventHandler
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
		PacketHandler.init();
	}

	@EventHandler
	public void serverLoad(FMLServerStartingEvent event) {
		event.registerServerCommand(new RedirectCommand());
		event.registerServerCommand(new FallbackCommand());
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (fallbackServerAddress != null) {
			Minecraft mc = Minecraft.getMinecraft();
			if (mc.currentScreen instanceof GuiDisconnected) {
				String addr = fallbackServerAddress;
				fallbackServerAddress = null;
				redirect(addr);
			} else if (mc.currentScreen instanceof GuiMainMenu || mc.currentScreen instanceof GuiMultiplayer) {
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
	@SideOnly(Side.CLIENT)
	public static void redirect(String serverAddress) {
		if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
			throw new IllegalStateException("Not in the main thread");
		}
		
		if (MinecraftForge.EVENT_BUS.post(new RedirectEvent(serverAddress))) {
			return;
		}

		LOGGER.info("Connecting to " + serverAddress);

		final Minecraft mc = Minecraft.getMinecraft();
		if (mc.world != null) {
			mc.world.sendQuittingDisconnectingPacket();
			mc.loadWorld((WorldClient) null);
		}
		mc.displayGuiScreen(new GuiMultiplayer(new GuiMainMenu()));
		mc.displayGuiScreen(new GuiConnecting(mc.currentScreen, mc, new ServerData("ServerRedirect", serverAddress, false)));
	}

	@SideOnly(Side.CLIENT)
	public static String getFallbackServerAddress() {
		return fallbackServerAddress;
	}

	@SideOnly(Side.CLIENT)
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
	public static boolean sendTo(EntityPlayerMP player, String serverAddress) {
		if (MinecraftForge.EVENT_BUS.post(new PlayerRedirectEvent(player, serverAddress))) {
			return false;
		}
		
		PacketHandler.REDIRECT_CHANNEL.sendTo(new AddressMessage(serverAddress), player);
		return true;
	}
	
	/**
	 * Connects all players with this mod on their client to the specified server address.
	 * 
	 * @param serverAddress the new server address the players should connect to
	 */
	public static void sendToAll(String serverAddress) {
		final AddressMessage message = new AddressMessage(serverAddress);
		
		final PlayerList pl = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
		
		for (EntityPlayerMP player : pl.getPlayers()) {
			if (!MinecraftForge.EVENT_BUS.post(new PlayerRedirectEvent(player, serverAddress))) {
				PacketHandler.REDIRECT_CHANNEL.sendTo(message, player);
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
	public static boolean sendFallbackTo(EntityPlayerMP player, String serverAddress) {
		PacketHandler.FALLBACK_CHANNEL.sendTo(new AddressMessage(serverAddress), player);
		return true;
	}
	
	/**
	 * Connects all players with this mod on their client to the specified server address.
	 * 
	 * @param serverAddress the new server address the players should connect to
	 */
	public static void sendFallbackToAll(String serverAddress) {
		final AddressMessage message = new AddressMessage(serverAddress);
		
		final PlayerList pl = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
		
		for (EntityPlayerMP player : pl.getPlayers()) {
			PacketHandler.FALLBACK_CHANNEL.sendTo(message, player);
		}
	}
}
