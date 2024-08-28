package net.kaikk.mc.serverredirect.fabric;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

public class ServerRedirect implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("serverredirect");
	public static final Pattern ADDRESS_PREVALIDATOR = Pattern.compile("^[A-Za-z0-9-_.:]+$"); // allowed characters in a server address
	protected static final Set<UUID> players = Collections.synchronizedSet(new HashSet<>());
	public static final Identifier redirectChannelIdentifier = new Identifier("srvredirect", "red");
	public static final Identifier fallbackChannelIdentifier = new Identifier("srvredirect", "fal");
	public static final Identifier announceChannelIdentifier = new Identifier("srvredirect", "ann");
	
	@Environment(EnvType.CLIENT)
	public static volatile String fallbackServerAddress;
	@Environment(EnvType.CLIENT)
	public static boolean connected;

	@Override
	public void onInitialize() {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			ClientPlayNetworking.registerGlobalReceiver(redirectChannelIdentifier, (c, h, b, rs) -> {
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
			ClientPlayNetworking.registerGlobalReceiver(fallbackChannelIdentifier, (c, h, b, rs) -> {
				try {
					String addr = b.getCharSequence(1, b.capacity() - 1, StandardCharsets.UTF_8).toString();
					if (ADDRESS_PREVALIDATOR.matcher(addr).matches()) {
						if (ClientFallbackEvent.EVENT.invoker().fallback(addr) != ActionResult.SUCCESS) {
							return;
						}
						
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
		
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			try {
				dispatcher.register(
						CommandManager.literal("redirect")
						.requires(cs -> cs.hasPermissionLevel(2))
						.redirect(dispatcher.register(
								CommandManager.literal("serverredirect")
								.requires(cs -> cs.hasPermissionLevel(2))
								.then(commandAddress(ServerRedirect::sendTo))
								))
						);
				dispatcher.register(
						CommandManager.literal("fallback")
						.requires(cs -> cs.hasPermissionLevel(2))
						.redirect(dispatcher.register(
								CommandManager.literal("fallbackserver")
								.requires(cs -> cs.hasPermissionLevel(2))
								.then(commandAddress(ServerRedirect::sendFallbackTo))
								))
						);
				
				dispatcher.register(
						CommandManager.literal("ifplayercanredirect")
						.requires(cs -> cs.hasPermissionLevel(2))
						.then(commandIfPlayerRedirect(false))
						);
				dispatcher.register(
						CommandManager.literal("ifplayercannotredirect")
						.requires(cs -> cs.hasPermissionLevel(2))
						.then(commandIfPlayerRedirect(true))
						);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		ServerPlayConnectionEvents.DISCONNECT.register((handler, srv) -> {
			try {
				players.remove(handler.player.getUuid());
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		ServerPlayNetworking.registerGlobalReceiver(announceChannelIdentifier, (srv, player, handler, buf, response) -> {
			try {
				players.add(player.getUuid());
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	private ArgumentBuilder<ServerCommandSource, ?> commandAddress(BiConsumer<ServerPlayerEntity, String> consumer) {
		return CommandManager.argument("Player(s)", EntityArgumentType.players())
				.then(CommandManager.argument("Server Address", StringArgumentType.greedyString())
						.executes(cs -> {
							try {
								String addr = cs.getArgument("Server Address", String.class);
								if (!ADDRESS_PREVALIDATOR.matcher(addr).matches()) {
									cs.getSource().sendError(Text.literal("Invalid Server Address"));
									return 0;
								}

								cs.getArgument("Player(s)", EntitySelector.class).getPlayers(cs.getSource()).forEach(p -> {
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

	private ArgumentBuilder<ServerCommandSource, ?> commandIfPlayerRedirect(boolean not) {
		return CommandManager.argument("Player(s)", EntityArgumentType.players())
				.then(CommandManager.argument("Command...", StringArgumentType.greedyString())
						.executes(cs -> {
							try {
								String command = cs.getArgument("Command...", String.class);

								cs.getArgument("Player(s)", EntitySelector.class).getPlayers(cs.getSource()).forEach(p -> {
									try {
										if (isUsingServerRedirect(p) != not) {
											cs.getSource().getWorld().getServer().getCommandManager().executeWithPrefix(cs.getSource(), command.replace("%PlayerName", p.getGameProfile().getName()).replace("%PlayerId", p.getUuidAsString()));
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
		
		if (ClientRedirectEvent.EVENT.invoker().redirect(serverAddress) != ActionResult.SUCCESS) {
			return;
		}

		LOGGER.info("Connecting to " + serverAddress);

		final MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.world != null) {
			mc.world.disconnect();
		}
		
		mc.disconnect();

		mc.setScreen(new MultiplayerScreen(new TitleScreen()));
		ConnectScreen.connect(mc.currentScreen, mc, ServerAddress.parse(serverAddress), new ServerInfo(serverAddress, serverAddress, ServerInfo.ServerType.OTHER), false);
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
		if (PlayerRedirectEvent.EVENT.invoker().redirect(player, serverAddress) != ActionResult.SUCCESS) {
			return false;
		}
		ServerPlayNetworking.send(player, redirectChannelIdentifier, serverAddressPacketByteBuf(serverAddress));
		return true;
	}

	/**
	 * Connects all players with this mod on their client to the specified server address.
	 * 
	 * @param serverAddress the new server address the players should connect to
	 */
	public static void sendToAll(MinecraftServer server, String serverAddress) {
		PacketByteBuf buf = serverAddressPacketByteBuf(serverAddress);
		for (ServerPlayerEntity player : PlayerLookup.all(server)) {
			if (PlayerRedirectEvent.EVENT.invoker().redirect(player, serverAddress) == ActionResult.SUCCESS) {
				ServerPlayNetworking.send(player, redirectChannelIdentifier, buf);
			}
		}
	}

	/**
	 * Sets the fallback address to the specified player.<br>
	 * The client must have this mod in order for this to work.
	 * 
	 * @param serverAddress the new server address the player should connect to
	 * @param player the player's instance
	 * @return true if the redirect message was sent to the specified player
	 */
	public static boolean sendFallbackTo(ServerPlayerEntity player, String serverAddress) {
		if (PlayerFallbackEvent.EVENT.invoker().fallback(player, serverAddress) != ActionResult.SUCCESS) {
			return false;
		}
		ServerPlayNetworking.send(player, fallbackChannelIdentifier, serverAddressPacketByteBuf(serverAddress));
		return true;
	}

	/**
	 * Sets the fallback address to all players with this mod on their client.
	 * 
	 * @param serverAddress the new server address the players should connect to
	 */
	public static void sendFallbackToAll(MinecraftServer server, String serverAddress) {
		PacketByteBuf buf = serverAddressPacketByteBuf(serverAddress);
		for (ServerPlayerEntity player : PlayerLookup.all(server)) {
			if (PlayerFallbackEvent.EVENT.invoker().fallback(player, serverAddress) == ActionResult.SUCCESS) {
				ServerPlayNetworking.send(player, fallbackChannelIdentifier, buf);
			}
		}
	}
	
	private static PacketByteBuf serverAddressPacketByteBuf(String address) {
		final byte[] addressBytes = address.getBytes(StandardCharsets.UTF_8);
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeByte(0); // discriminator
		buf.writeBytes(addressBytes);
		return buf;
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
		return isUsingServerRedirect(player.getUuid());
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

	@Environment(EnvType.CLIENT)
	public interface ClientRedirectEvent {
		Event<ClientRedirectEvent> EVENT = EventFactory.createArrayBacked(ClientRedirectEvent.class, listeners -> address -> {
			for (ClientRedirectEvent listener : listeners) {
				ActionResult result = listener.redirect(address);

				if (result != ActionResult.PASS) {
					return result;
				}
			}

			return ActionResult.SUCCESS;
		});

		ActionResult redirect(String address);
	}

	@Environment(EnvType.CLIENT)
	public interface ClientFallbackEvent {
		Event<ClientFallbackEvent> EVENT = EventFactory.createArrayBacked(ClientFallbackEvent.class, listeners -> address -> {
			for (ClientFallbackEvent listener : listeners) {
				ActionResult result = listener.fallback(address);

				if (result != ActionResult.PASS) {
					return result;
				}
			}

			return ActionResult.SUCCESS;
		});

		ActionResult fallback(String address);
	}

	public interface PlayerRedirectEvent {
		Event<PlayerRedirectEvent> EVENT = EventFactory.createArrayBacked(PlayerRedirectEvent.class, listeners -> (player, address) -> {
			for (PlayerRedirectEvent listener : listeners) {
				ActionResult result = listener.redirect(player, address);

				if (result != ActionResult.PASS) {
					return result;
				}
			}

			return ActionResult.SUCCESS;
		});

		ActionResult redirect(ServerPlayerEntity player, String address);
	}

	public interface PlayerFallbackEvent {
		Event<PlayerFallbackEvent> EVENT = EventFactory.createArrayBacked(PlayerFallbackEvent.class, listeners -> (player, address) -> {
			for (PlayerFallbackEvent listener : listeners) {
				ActionResult result = listener.fallback(player, address);

				if (result != ActionResult.PASS) {
					return result;
				}
			}

			return ActionResult.SUCCESS;
		});

		ActionResult fallback(ServerPlayerEntity player, String address);
	}
}
