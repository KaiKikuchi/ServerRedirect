package net.kaikk.mc.serverredirect.forge.event;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

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
