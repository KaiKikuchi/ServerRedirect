package net.kaikk.mc.serverredirect.forge.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

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
