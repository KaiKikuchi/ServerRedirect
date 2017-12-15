package net.kaikk.mc.serverredirect.forge.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayerMP;

@Cancelable
public class PlayerRedirectEvent extends Event {
	protected final EntityPlayerMP player;
	protected final String address;
	
	public PlayerRedirectEvent(EntityPlayerMP player, String address) {
		this.player = player;
		this.address = address;
	}

	public EntityPlayerMP getPlayer() {
		return player;
	}

	public String getAddress() {
		return address;
	}
}
