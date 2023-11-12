package net.kaikk.mc.serverredirect.forge;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent.Context;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

public class PacketHandler {
	private static final int PROTOCOL_VERSION = 1;
	public static final SimpleChannel REDIRECT_CHANNEL = ChannelBuilder
			.named(new ResourceLocation("srvredirect", "red"))
			.acceptedVersions((status, version) -> true)
			.optional()
			.networkProtocolVersion(PROTOCOL_VERSION)
			.simpleChannel();
	public static final SimpleChannel FALLBACK_CHANNEL = ChannelBuilder
			.named(new ResourceLocation("srvredirect", "fal"))
			.acceptedVersions((status, version) -> true)
			.optional()
			.networkProtocolVersion(PROTOCOL_VERSION)
			.simpleChannel();
	public static final SimpleChannel ANNOUNCE_CHANNEL = ChannelBuilder
			.named(new ResourceLocation("srvredirect", "ann"))
			.acceptedVersions((status, version) -> true)
			.optional()
			.networkProtocolVersion(PROTOCOL_VERSION)
			.simpleChannel();
	
	public static final Pattern ADDRESS_PREVALIDATOR = Pattern.compile("^[A-Za-z0-9-_.:]+$"); // allowed characters in a server address
	public static final Object EMPTY_OBJECT = new Object();

	public static void init() {
		REDIRECT_CHANNEL.messageBuilder(String.class, 0, NetworkDirection.PLAY_TO_CLIENT).encoder(PacketHandler::encode).decoder(PacketHandler::decode).consumerNetworkThread(PacketHandler::handleRedirect).add();
		FALLBACK_CHANNEL.messageBuilder(String.class, 0, NetworkDirection.PLAY_TO_CLIENT).encoder(PacketHandler::encode).decoder(PacketHandler::decode).consumerNetworkThread(PacketHandler::handleFallback).add();
		ANNOUNCE_CHANNEL.messageBuilder(Object.class, 0, NetworkDirection.PLAY_TO_SERVER).encoder(PacketHandler::encodeVoid).decoder(PacketHandler::decodeVoid).consumerNetworkThread(PacketHandler::handleAnnounce).add();
	}

	public static void encode(String addr, FriendlyByteBuf buffer) {
		buffer.writeCharSequence(addr, StandardCharsets.UTF_8);
	}

	public static String decode(FriendlyByteBuf buffer) {
		return buffer.toString(StandardCharsets.UTF_8);
	}

	public static void handleRedirect(String addr, Context ctx) {
		if (ctx.getDirection() == NetworkDirection.PLAY_TO_CLIENT && ADDRESS_PREVALIDATOR.matcher(addr).matches()) {
			ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ServerRedirect.redirect(addr)));
		}
		ctx.setPacketHandled(true);
	}

	public static void handleFallback(String addr, Context ctx) {
		if (ctx.getDirection() == NetworkDirection.PLAY_TO_CLIENT && ADDRESS_PREVALIDATOR.matcher(addr).matches()) {
			ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ServerRedirect.setFallbackServerAddress(addr)));
		}
		ctx.setPacketHandled(true);
	}

	public static void encodeVoid(Object v, FriendlyByteBuf buffer) {

	}

	public static Object decodeVoid(FriendlyByteBuf buffer) {
		return EMPTY_OBJECT;
	}

	public static void handleAnnounce(Object v, Context ctx) {
		if (ctx.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
			ServerRedirect.players.add(ctx.getSender().getUUID());
		}
		ctx.setPacketHandled(true);
	}
}
