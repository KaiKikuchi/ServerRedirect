package net.kaikk.mc.serverredirect.forge.event;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * This event is not longer called. Use ClientRedirectEvent instead
 *
 */
@Cancelable
@Deprecated
public class RedirectEvent extends Event {
	protected final String address;
	
	public RedirectEvent(String address) {
		this.address = address;
	}

	public String getAddress() {
		return address;
	}
}
