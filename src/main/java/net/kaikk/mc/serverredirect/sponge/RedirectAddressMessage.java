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
		this.address = buf.readString();
	}

	@Override
	public void writeTo(ChannelBuf buf) {
		buf.writeString(address);
	}
}
