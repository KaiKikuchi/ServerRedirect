package net.kaikk.mc.serverredirect.forge.event;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called when a fallback address is set
 *
 */
@Cancelable
public class ClientFallbackEvent extends Event {
	protected final String address;
	
	public ClientFallbackEvent(String address) {
		this.address = address;
	}

	public String getAddress() {
		return address;
	}
}
