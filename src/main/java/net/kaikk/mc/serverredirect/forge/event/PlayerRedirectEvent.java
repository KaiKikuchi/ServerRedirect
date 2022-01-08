package net.kaikk.mc.serverredirect.forge.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.entity.player.EntityPlayer;

@Cancelable
public class PlayerRedirectEvent extends Event {
	protected final EntityPlayer player;
	protected final String address;
	
	public PlayerRedirectEvent(EntityPlayer player, String address) {
		this.player = player;
		this.address = address;
	}

	public EntityPlayer getPlayer() {
		return player;
	}

	public String getAddress() {
		return address;
	}
}
