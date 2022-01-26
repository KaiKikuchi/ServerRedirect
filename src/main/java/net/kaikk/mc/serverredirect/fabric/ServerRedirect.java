package net.kaikk.mc.serverredirect.fabric;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

public class ServerRedirect implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("serverredirect");
	public static final Pattern ADDRESS_PREVALIDATOR = Pattern.compile("^[A-Za-z0-9-_.:]+$"); // allowed characters in a server address
	@Environment(EnvType.CLIENT)
	public static final Identifier announceChannelIdentifier = new Identifier("srvredirect", "ann");
	@Environment(EnvType.CLIENT)
	public static volatile String fallbackServerAddress;
	@Environment(EnvType.CLIENT)
	public static boolean connected;

	@Override
	public void onInitialize() {
		if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
			LOGGER.info("Server Redirect for Fabric is currently a client side mod only!");
			return;
		}
		
		ClientPlayNetworking.registerGlobalReceiver(new Identifier("srvredirect", "red"), (c, h, b, rs) -> {
			try {
				String addr = b.getCharSequence(1, b.capacity() - 1, StandardCharsets.UTF_8).toString();
				if (ADDRESS_PREVALIDATOR.matcher(addr).matches()) {
					c.execute(() -> {
						try {
							redirect(addr);
						} catch (Exception e) {
							e.printStackTrace();
						}
					});
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		ClientPlayNetworking.registerGlobalReceiver(new Identifier("srvredirect", "fal"), (c, h, b, rs) -> {
			try {
				String addr = b.getCharSequence(1, b.capacity() - 1, StandardCharsets.UTF_8).toString();
				if (ADDRESS_PREVALIDATOR.matcher(addr).matches()) {
					fallbackServerAddress = addr;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		ClientTickEvents.START_CLIENT_TICK.register(c -> {
			try {
				if (connected != (c.world != null)) {
					connected = c.world != null;
					if (connected) {
						ClientPlayNetworking.send(announceChannelIdentifier, PacketByteBufs.empty());
					}
				} else if (fallbackServerAddress != null) {
					if (c.currentScreen instanceof DisconnectedScreen) {
						String addr = fallbackServerAddress;
						fallbackServerAddress = null;
						redirect(addr);
					} else if (c.currentScreen instanceof TitleScreen || c.currentScreen instanceof MultiplayerScreen) {
						fallbackServerAddress = null;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	/**
	 * Processes the redirect client side.<br>
	 * This simulates clicking the disconnect button and a direct connection to the specified server address.
	 * 
	 * @param serverAddress the new server address this client should connect to
	 * @throws IllegalStateException if called while not in the main thread
	 */
	@Environment(EnvType.CLIENT)
	public static void redirect(String serverAddress) {
		if (!MinecraftClient.getInstance().isOnThread()) {
			throw new IllegalStateException("Not in the main thread");
		}
		
		if (RedirectCallback.EVENT.invoker().redirect(serverAddress) != ActionResult.SUCCESS) {
			return;
		}

		LOGGER.info("Connecting to " + serverAddress);

		final MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.world != null) {
			mc.world.disconnect();
		}
		
		mc.disconnect();

		mc.setScreen(new MultiplayerScreen(new TitleScreen()));
		ConnectScreen.connect(mc.currentScreen, mc, ServerAddress.parse(serverAddress), new ServerInfo("ServerRedirect", serverAddress, false));
	}

	@Environment(EnvType.CLIENT)
	public interface RedirectCallback {
		Event<RedirectCallback> EVENT = EventFactory.createArrayBacked(RedirectCallback.class, listeners -> address -> {
			for (RedirectCallback listener : listeners) {
				ActionResult result = listener.redirect(address);

				if (result != ActionResult.PASS) {
					return result;
				}
			}

			return ActionResult.SUCCESS;
		});

		ActionResult redirect(String address);
	}
}
