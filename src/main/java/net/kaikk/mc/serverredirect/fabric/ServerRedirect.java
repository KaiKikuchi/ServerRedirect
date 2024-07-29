package net.kaikk.mc.serverredirect.fabric;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.network.ServerInfo.ServerType;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class ServerRedirect implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("serverredirect");
    public static final Pattern ADDRESS_PREVALIDATOR = Pattern.compile("^[A-Za-z0-9-_.:]+$"); // allowed characters in a server address
    protected static final Set<UUID> players = Collections.synchronizedSet(new HashSet<>());

    @Environment(EnvType.CLIENT)
    public static volatile String fallbackServerAddress;
    @Environment(EnvType.CLIENT)
    public static boolean connected;

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(RedirectAddressPayload.PACKET_ID, RedirectAddressPayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(FallbackAddressPayload.PACKET_ID, FallbackAddressPayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(AnnounceAddressPayload.PACKET_ID, AnnounceAddressPayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(AnnounceAddressPayload.PACKET_ID, AnnounceAddressPayload.PACKET_CODEC);

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientPlayNetworking.registerGlobalReceiver(RedirectAddressPayload.PACKET_ID, (payload, context) -> {
                try {
                    String addr = payload.addr();
                    if (ADDRESS_PREVALIDATOR.matcher(addr).matches()) {
                        context.client().executeSync(() -> {
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

            ClientPlayNetworking.registerGlobalReceiver(FallbackAddressPayload.PACKET_ID, (payload, context) -> {
                try {
                    String addr = payload.addr();
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
                    if (connected == (c.world == null)) {
                        connected = c.world != null;
                        if (connected) {
                            ClientPlayNetworking.send(new AnnounceAddressPayload());
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

        ServerPlayNetworking.registerGlobalReceiver(AnnounceAddressPayload.PACKET_ID, (payload, context) -> {
            try {
                players.add(context.player().getUuid());
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

        LOGGER.info("Connecting to {}", serverAddress);
        final MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null) {
            client.world.disconnect();
        }

        client.disconnect();

        client.setScreen(new MultiplayerScreen(new TitleScreen()));
        ConnectScreen.connect(client.currentScreen, client, ServerAddress.parse(serverAddress), new ServerInfo(serverAddress, serverAddress, ServerType.OTHER), false, null);
    }


    /**
     * Connects the specified player to the specified server address.<br>
     * The client must have this mod in order for this to work.
     *
     * @param serverAddress the new server address the player should connect to
     * @param player        the player's instance
     * @return true if the redirect message was sent to the specified player
     */
    public static boolean sendTo(ServerPlayerEntity player, String serverAddress) {
        if (PlayerRedirectEvent.EVENT.invoker().redirect(player, serverAddress) != ActionResult.SUCCESS) {
            return false;
        }
        ServerPlayNetworking.send(player, new RedirectAddressPayload(serverAddress));
        return true;
    }

    /**
     * Connects all players with this mod on their client to the specified server address.
     *
     * @param serverAddress the new server address the players should connect to
     */
    public static void sendToAll(MinecraftServer server, String serverAddress) {
        RedirectAddressPayload payload = new RedirectAddressPayload(serverAddress);
        for (ServerPlayerEntity player : PlayerLookup.all(server)) {
            if (PlayerRedirectEvent.EVENT.invoker().redirect(player, serverAddress) == ActionResult.SUCCESS) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }

    /**
     * Sets the fallback address to the specified player.<br>
     * The client must have this mod in order for this to work.
     *
     * @param serverAddress the new server address the player should connect to
     * @param player        the player's instance
     * @return true if the redirect message was sent to the specified player
     */
    public static boolean sendFallbackTo(ServerPlayerEntity player, String serverAddress) {
        if (PlayerFallbackEvent.EVENT.invoker().fallback(player, serverAddress) != ActionResult.SUCCESS) {
            return false;
        }
        ServerPlayNetworking.send(player, new FallbackAddressPayload(serverAddress));
        return true;
    }

    /**
     * Sets the fallback address to all players with this mod on their client.
     *
     * @param serverAddress the new server address the players should connect to
     */
    public static void sendFallbackToAll(MinecraftServer server, String serverAddress) {
        FallbackAddressPayload payload = new FallbackAddressPayload(serverAddress);
        for (ServerPlayerEntity player : PlayerLookup.all(server)) {
            if (PlayerFallbackEvent.EVENT.invoker().fallback(player, serverAddress) == ActionResult.SUCCESS) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }

    /**
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
     * Loop through the players with this mod<br>
     * <br>
     * <b>WARNING:</b> this will likely not include a player that just logged in,
     * as it takes some time for the client to send the announce packet to the server.
     *
     * @param consumer a consumer that can do something with the player's UUID
     */
    public static void forEachPlayerUsingServerRedirect(Consumer<UUID> consumer) {
        synchronized (players) {
            for (UUID playerId : players) {
                consumer.accept(playerId);
            }
        }
    }

    /**
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
        return Set.copyOf(players);
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

    public record RedirectAddressPayload(String addr) implements CustomPayload {
        public static final Id<RedirectAddressPayload> PACKET_ID = new Id<>(Identifier.of("srvredirect", "red"));
        public static final PacketCodec<ByteBuf, RedirectAddressPayload> PACKET_CODEC = new PacketCodec<>() {
            @Override
            public RedirectAddressPayload decode(ByteBuf buf) {
                buf.readByte(); // discriminator
                return new RedirectAddressPayload(buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8).toString());
            }

            @Override
            public void encode(ByteBuf buf, RedirectAddressPayload value) {
                buf.writeByte(0); // discriminator
                buf.writeBytes(value.addr.getBytes(StandardCharsets.UTF_8));
            }
        };

        @Override
        public Id<? extends CustomPayload> getId() {
            return PACKET_ID;
        }
    }

    public record FallbackAddressPayload(String addr) implements CustomPayload {
        public static final Id<FallbackAddressPayload> PACKET_ID = new Id<>(Identifier.of("srvredirect", "fal"));
        public static final PacketCodec<ByteBuf, FallbackAddressPayload> PACKET_CODEC = new PacketCodec<>() {
            @Override
            public FallbackAddressPayload decode(ByteBuf buf) {
                buf.readByte(); // discriminator
                return new FallbackAddressPayload(buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8).toString());
            }

            @Override
            public void encode(ByteBuf buf, FallbackAddressPayload value) {
                buf.writeByte(0); // discriminator
                buf.writeBytes(value.addr.getBytes(StandardCharsets.UTF_8));
            }
        };

        @Override
        public Id<? extends CustomPayload> getId() {
            return PACKET_ID;
        }
    }

    public record AnnounceAddressPayload() implements CustomPayload {
        public static final Id<AnnounceAddressPayload> PACKET_ID = new Id<>(Identifier.of("srvredirect", "ann"));
        public static final PacketCodec<ByteBuf, AnnounceAddressPayload> PACKET_CODEC = new PacketCodec<>() {
            @Override
            public AnnounceAddressPayload decode(ByteBuf buf) {
                buf.readByte(); // discriminator
                return new AnnounceAddressPayload();
            }

            @Override
            public void encode(ByteBuf buf, AnnounceAddressPayload value) {
                buf.writeByte(0); // discriminator
            }
        };

        @Override
        public Id<? extends CustomPayload> getId() {
            return PACKET_ID;
        }
    }
}
