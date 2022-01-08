package net.kaikk.mc.serverredirect.sponge;

import java.nio.charset.StandardCharsets;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

public class ServerAddressMessage implements Message {
	private String address;

	public ServerAddressMessage() {}

	public ServerAddressMessage(String address) {
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
		buf.writeBytes(address.getBytes(StandardCharsets.UTF_8));
	}
}
