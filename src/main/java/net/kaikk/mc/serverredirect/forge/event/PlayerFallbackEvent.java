package net.kaikk.mc.serverredirect.forge.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Called when the server is sending a fallback packet to the player
 *
 */
@Cancelable
public class PlayerFallbackEvent extends Event {
	protected final ServerPlayer player;
	protected final String address;
	
	public PlayerFallbackEvent(ServerPlayer player, String address) {
		this.player = player;
		this.address = address;
	}

	public ServerPlayer getPlayer() {
		return player;
	}

	public String getAddress() {
		return address;
	}
}
