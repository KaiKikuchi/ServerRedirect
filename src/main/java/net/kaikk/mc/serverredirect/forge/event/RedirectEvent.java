package net.kaikk.mc.serverredirect.forge.event;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

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
