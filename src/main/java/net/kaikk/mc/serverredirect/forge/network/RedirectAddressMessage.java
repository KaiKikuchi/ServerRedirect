package net.kaikk.mc.serverredirect.forge.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class RedirectAddressMessage implements IMessage {
	private String address;

	public RedirectAddressMessage() {}

	public RedirectAddressMessage(String address) {
		this.address = address;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.address = new String(buf.readBytes(buf.readableBytes()).array());
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBytes(address.getBytes());
	}

	public String getAddress() {
		return address;
	}
}
