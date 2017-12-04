package net.kaikk.mc.serverredirect.sponge.event;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;

public class PlayerRedirectEvent implements Event, Cancellable {
	protected final Cause cause;
	protected final Player player;
	protected final String address;

	protected boolean isCancelled;
	
	public PlayerRedirectEvent(Player player, String address, Cause cause) {
		this.player = player;
		this.address = address;
		this.cause = cause;
	}

	public Player getPlayer() {
		return player;
	}

	public String getAddress() {
		return address;
	}

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.isCancelled = cancel;
	}

	@Override
	public Cause getCause() {
		return cause;
	}
}
