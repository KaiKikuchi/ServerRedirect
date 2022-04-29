package net.kaikk.mc.serverredirect.forge.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Called when the server is sending a fallback packet to the player
 *
 */
@Cancelable
public class PlayerFallbackEvent extends Event {
	protected final EntityPlayerMP player;
	protected final String address;
	
	public PlayerFallbackEvent(EntityPlayerMP player, String address) {
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
