package net.kaikk.mc.serverredirect.forge.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class PlayerRedirectEvent extends Event {
	protected final PlayerEntity player;
	protected final String address;
	
	public PlayerRedirectEvent(PlayerEntity player, String address) {
		this.player = player;
		this.address = address;
	}

	public PlayerEntity getPlayer() {
		return player;
	}

	public String getAddress() {
		return address;
	}
}
