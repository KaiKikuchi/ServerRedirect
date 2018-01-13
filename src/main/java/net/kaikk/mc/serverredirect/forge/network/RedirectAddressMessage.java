package net.kaikk.mc.serverredirect.forge.network;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

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
		if (buf.hasArray()) {
			this.address = new String(buf.array());
		} else {
			byte[] arr = new byte[buf.readableBytes()];
			buf.getBytes(0, arr);
			this.address = new String(arr);
			System.out.println("SRDEBUG: \""+this.address+"\"");
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBytes(address.getBytes());
	}

	public String getAddress() {
		return address;
	}
}
