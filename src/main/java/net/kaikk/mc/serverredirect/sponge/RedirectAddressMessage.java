package net.kaikk.mc.serverredirect.sponge;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

public class RedirectAddressMessage implements Message {
	private String address;

	public RedirectAddressMessage() {}

	public RedirectAddressMessage(String address) {
		this.address = address;
	}

	public String getAddress() {
		return address;
	}

	@Override
	public void readFrom(ChannelBuf buf) {
		this.address = new String(buf.array());
	}

	@Override
	public void writeTo(ChannelBuf buf) {
		buf.writeBytes(address.getBytes());
	}
}
