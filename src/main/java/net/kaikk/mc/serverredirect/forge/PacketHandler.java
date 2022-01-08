package net.kaikk.mc.serverredirect.forge;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {
	public static final SimpleNetworkWrapper REDIRECT_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("srvredirect:red");
	public static final SimpleNetworkWrapper FALLBACK_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("srvredirect:fal");
	public static final Pattern ADDRESS_PREVALIDATOR = Pattern.compile("^[A-Za-z0-9-_.:]+$"); // allowed characters in a server address

	public static void init() {
		REDIRECT_CHANNEL.registerMessage(RedirectAddressMessageHandler.class, AddressMessage.class, 0, Side.CLIENT);
		FALLBACK_CHANNEL.registerMessage(FallbackAddressMessageHandler.class, AddressMessage.class, 0, Side.CLIENT);
	}
	
	public static class RedirectAddressMessageHandler implements IMessageHandler<AddressMessage, IMessage> {
		@Override
		public IMessage onMessage(final AddressMessage message, MessageContext ctx) {
			if (ADDRESS_PREVALIDATOR.matcher(message.getAddress()).matches()) {
				ServerRedirect.setRedirectServerAddress(message.getAddress());
			}
			return null;
		}
	}
	
	public static class FallbackAddressMessageHandler implements IMessageHandler<AddressMessage, IMessage> {
		@Override
		public IMessage onMessage(final AddressMessage message, MessageContext ctx) {
			if (ADDRESS_PREVALIDATOR.matcher(message.getAddress()).matches()) {
				ServerRedirect.setFallbackServerAddress(message.getAddress());
			}
			return null;
		}
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
			buf.writeCharSequence(this.address, StandardCharsets.UTF_8);
		}

		public String getAddress() {
			return address;
		}
	}
}
