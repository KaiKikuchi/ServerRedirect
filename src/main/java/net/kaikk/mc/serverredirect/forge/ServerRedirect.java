package net.kaikk.mc.serverredirect.forge;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.kaikk.mc.serverredirect.forge.command.RedirectCommand;
import net.kaikk.mc.serverredirect.forge.event.PlayerRedirectEvent;
import net.kaikk.mc.serverredirect.forge.event.RedirectEvent;
import net.kaikk.mc.serverredirect.forge.network.RedirectAddressMessage;
import net.kaikk.mc.serverredirect.forge.network.RedirectAddressMessageHandler;
import net.kaikk.mc.serverredirect.forge.network.RedirectModMessage;
import net.kaikk.mc.serverredirect.forge.network.RedirectModMessageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

@Mod(modid = ServerRedirect.MODID, name = ServerRedirect.NAME, version = ServerRedirect.VERSION, acceptableRemoteVersions = "*")
public class ServerRedirect {
	public static final String MODID = "serverredirect";
	public static final String NAME = "ServerRedirect";
	public static final String VERSION = "1.3.5";

	// Channel to send messages between server and client... like a request from the server to the client to connect to another server address
	public static SimpleNetworkWrapper net;

	// this set contains a list of UUIDs of the players that have this mod on their client
	public static Set<UUID> playersWithThisMod;

	// Unfortunately, I couldn't find a synchronous task scheduler on this Forge version, so I am using this instead.
	public static LinkedBlockingQueue<Runnable> sync = new LinkedBlockingQueue<Runnable>();
	
	@SideOnly(Side.SERVER)
	@EventHandler
	public void initServer(FMLConstructionEvent event) {
		playersWithThisMod = new HashSet<UUID>();
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		// initialize the channel
		net = NetworkRegistry.INSTANCE.newSimpleChannel("ServerRedirect");
		
		// register the message used by the client to let the server know they have the mod 
		net.registerMessage(RedirectModMessageHandler.class, RedirectModMessage.class, 0, Side.SERVER);
		
		// register the message used by the server to request to the client a connection to another server address
		net.registerMessage(RedirectAddressMessageHandler.class, RedirectAddressMessage.class, 1, Side.CLIENT);
		
		// register all event listeners on this instance
		MinecraftForge.EVENT_BUS.register(this);
		
		// necessary for TickEvent.ClientTickEvent on Forge 1.7.10
		FMLCommonHandler.instance().bus().register(this);
	}

	@SideOnly(Side.SERVER)
	@EventHandler
	public void serverLoad(FMLServerStartingEvent event) {
		event.registerServerCommand(new RedirectCommand());
	}

	// Unfortunately, I couldn't find a scheduler on Forge 1.7.10, so I am using this instead.
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority=EventPriority.HIGHEST)
	public void onTick(TickEvent.ClientTickEvent event) {
		if (event.phase == Phase.END) {
			Runnable r;
			while ((r = this.sync.poll()) != null) {
				try {
					r.run();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@SideOnly(Side.SERVER)
	@SubscribeEvent(priority=EventPriority.HIGHEST)
	public void onTick(TickEvent.ServerTickEvent event) {
		if (event.phase == Phase.END) {
			Runnable r;
			while ((r = this.sync.poll()) != null) {
				try {
					r.run();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Processes the redirect client side.<br>
	 * This basically emulates the disconnect button and a direct connection.<br>
	 * Must run on tick.<br>
	 * This shouldn't be called by other mods.
	 * 
	 * @param redirectAddress the new server address this client should connect to
	 */
	@SideOnly(Side.CLIENT)
	public static void processRedirect(String redirectAddress) {
		if (MinecraftForge.EVENT_BUS.post(new RedirectEvent(redirectAddress))) {
			return;
		}
		
		final Minecraft mc = Minecraft.getMinecraft();
		mc.displayGuiScreen(new GuiIngameMenu());
		mc.theWorld.sendQuittingDisconnectingPacket();
		mc.loadWorld((WorldClient)null);
		mc.displayGuiScreen(new GuiMainMenu());
		mc.displayGuiScreen(new GuiConnecting(mc.currentScreen, mc, new ServerData("ServerRedirect", redirectAddress, false)));
	}

	/**
	 * Sends the specified player to the specified server.<br>
	 * The client must have this mod in order for this to work.
	 * 
	 * @param serverAddress the new server address the player should connect to
	 * @param playerName the player's name
	 * @return true if the redirect message was sent to the specified player
	 */
	@SideOnly(Side.SERVER)
	public static boolean sendTo(String serverAddress, String playerName) {
		final EntityPlayerMP player = getPlayer(playerName);
		if (player == null) {
			throw new IllegalArgumentException("Player \""+playerName+"\" not found");
		}

		return sendTo(serverAddress, player);
	}
	
	/**
	 * Connects the specified player to the specified server address.<br>
	 * The client must have this mod in order for this to work.
	 * 
	 * @param serverAddress the new server address the player should connect to
	 * @param playerId the player's UUID
	 * @return true if the redirect message was sent to the specified player
	 */
	@SideOnly(Side.SERVER)
	public static boolean sendTo(String serverAddress, UUID playerId) {
		final EntityPlayerMP player = getPlayer(playerId);
		if (player==null) {
			throw new IllegalArgumentException("Player \""+playerId+"\" not found");
		}

		return sendTo(serverAddress, player);
	}
	
	/**
	 * Connects the specified player to the specified server address.<br>
	 * The client must have this mod in order for this to work.
	 * 
	 * @param serverAddress the new server address the player should connect to
	 * @param player the player's instance
	 * @return true if the redirect message was sent to the specified player
	 */
	@SideOnly(Side.SERVER)
	public static boolean sendTo(String serverAddress, EntityPlayerMP player) {
		if (!playersWithThisMod.contains(player.getUniqueID())) {
			return false;
		}
		
		if (MinecraftForge.EVENT_BUS.post(new PlayerRedirectEvent(player, serverAddress))) {
			return false;
		}
		
		net.sendTo(new RedirectAddressMessage(serverAddress), player);
		return true;
	}
	
	/**
	 * Connects all players with this mod on their client to the specified server address.
	 * 
	 * @param serverAddress the new server address the players should connect to
	 */
	@SideOnly(Side.SERVER)
	public static void sendAllTo(String serverAddress) {
		final RedirectAddressMessage message = new RedirectAddressMessage(serverAddress);
		
		for (UUID playerId : playersWithThisMod) {
			final EntityPlayerMP player = getPlayer(playerId);
			if (player != null) {
				net.sendTo(message, player);
			}
		}
		
	}
	
	@SideOnly(Side.SERVER)
	@SubscribeEvent(priority=EventPriority.LOWEST)
    public void onPlayerLoginServer(PlayerEvent.PlayerLoggedInEvent event) {
		/*
		 *  Players that are going to connect to the server should get removed from
		 *  playersWithThisMod because we need to check again if they have the mod.
		 */
		playersWithThisMod.remove(event.player.getUniqueID());
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPlayerJoinServer(EntityJoinWorldEvent event) {
		if (event.entity == Minecraft.getMinecraft().thePlayer) {
			/*
			 * Send a message to the server informing it that this client has this mod. 
			 * */
			net.sendToServer(new RedirectModMessage());
		}
	}
    
	/**
	 * Utility method for getting a player by UUID
	 * 
	 * @param playerId the player's UUID
	 * @return the EntityPlayerMP instance of the specified player, null if the player was not found.
	 */
	public static EntityPlayerMP getPlayer(UUID playerId) {
		final List<?> list = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
		for (final Object playerObj : list) {
			if (((EntityPlayerMP) playerObj).getUniqueID().equals(playerId)) {
				return ((EntityPlayerMP) playerObj);
			}
		}
		
		return null;
	}
	
	/**
	 * Utility method for getting a player by username
	 * 
	 * @param playerName the player's username
	 * @return the EntityPlayerMP instance of the specified player, null if the player was not found.
	 */
	public static EntityPlayerMP getPlayer(String playerName) {
		final List<?> list = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
		for (final Object playerObj : list) {
			if (((EntityPlayerMP) playerObj).getCommandSenderName().equals(playerName)) {
				return ((EntityPlayerMP) playerObj);
			}
		}
		
		return null;
	}
}
