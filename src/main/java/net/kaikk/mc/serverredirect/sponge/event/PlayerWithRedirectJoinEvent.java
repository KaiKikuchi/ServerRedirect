package net.kaikk.mc.serverredirect.sponge.event;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;

public class PlayerWithRedirectJoinEvent implements Event {
	protected final Cause cause;
	protected final Player player;
	
	public PlayerWithRedirectJoinEvent(Player player, Cause cause) {
		this.player = player;
		this.cause = cause;
	}

	public Player getPlayer() {
		return player;
	}

	@Override
	public Cause getCause() {
		return cause;
	}
}
