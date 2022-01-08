package net.kaikk.mc.serverredirect.forge.event;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class PlayerRedirectEvent extends Event {
	protected final Player player;
	protected final String address;
	
	public PlayerRedirectEvent(Player player, String address) {
		this.player = player;
		this.address = address;
	}

	public Player getPlayer() {
		return player;
	}

	public String getAddress() {
		return address;
	}
}
