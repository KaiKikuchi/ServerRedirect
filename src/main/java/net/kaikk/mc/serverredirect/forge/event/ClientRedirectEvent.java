package net.kaikk.mc.serverredirect.forge.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;

/**
 * Called when a transfer packet is received by the client from the server
 *
 */
@Cancelable
public class ClientRedirectEvent extends Event {
	protected final String address;
	
	public ClientRedirectEvent(String address) {
		this.address = address;
	}

	public String getAddress() {
		return address;
	}
}
