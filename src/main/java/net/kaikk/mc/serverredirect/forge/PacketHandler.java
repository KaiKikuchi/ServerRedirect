package net.kaikk.mc.serverredirect.forge;

import java.nio.charset.StandardCharsets;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.kaikk.mc.serverredirect.Utils;

public class PacketHandler {
	public static final SimpleNetworkWrapper REDIRECT_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("srvredirect:red");
	public static final SimpleNetworkWrapper FALLBACK_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("srvredirect:fal");
	public static final SimpleNetworkWrapper ANNOUNCE_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("srvredirect:ann");

	public static void init() {
		REDIRECT_CHANNEL.registerMessage(PacketHandler::onRedirectAddressMessage, AddressMessage.class, 0, Side.CLIENT);
		FALLBACK_CHANNEL.registerMessage(PacketHandler::onFallbackAddressMessage, AddressMessage.class, 0, Side.CLIENT);
		ANNOUNCE_CHANNEL.registerMessage(PacketHandler::onAnnounceMessage, VoidMessage.class, 0, Side.SERVER);
	}

	public static IMessage onRedirectAddressMessage(final AddressMessage message, MessageContext ctx) {
		if (Utils.ADDRESS_PREVALIDATOR.matcher(message.getAddress()).matches()) {
			ServerRedirect.setRedirectServerAddress(message.getAddress());
		}
		return null;
	}

	public static IMessage onFallbackAddressMessage(final AddressMessage message, MessageContext ctx) {
		if (Utils.ADDRESS_PREVALIDATOR.matcher(message.getAddress()).matches()) {
			ServerRedirect.setFallbackServerAddress(message.getAddress());
		}
		return null;
	}

	public static IMessage onAnnounceMessage(final VoidMessage message, MessageContext ctx) {
		ServerRedirect.players.add(ctx.getServerHandler().playerEntity.getUniqueID());
		return null;
	}

	public static class AddressMessage implements IMessage {
		private String address;

		public AddressMessage() {}

		public AddressMessage(String address) {
			this.address = address;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			this.address = buf.toString(StandardCharsets.UTF_8);
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeBytes(this.address.getBytes(StandardCharsets.UTF_8));
		}

		public String getAddress() {
			return address;
		}
	}

	public final static class VoidMessage implements IMessage {
		public static final VoidMessage INSTANCE = new VoidMessage();

		private VoidMessage() {

		}

		@Override
		public void fromBytes(ByteBuf arg0) {

		}

		@Override
		public void toBytes(ByteBuf arg0) {

		}
	}
}
