package net.kaikk.mc.serverredirect.forge.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;

@Cancelable
public class RedirectEvent extends Event {
	protected final String address;
	
	public RedirectEvent(String address) {
		this.address = address;
	}

	public String getAddress() {
		return address;
	}
}
