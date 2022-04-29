package net.kaikk.mc.serverredirect.forge.event;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

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
