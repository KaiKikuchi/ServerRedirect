package net.kaikk.mc.serverredirect.bukkit.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerRedirectEvent extends Event implements Cancellable {
	protected static HandlerList handlerList = new HandlerList();
	
	protected final Player player;
	protected final String address;

	protected boolean isCancelled;
	
	public PlayerRedirectEvent(Player player, String address) {
		this.player = player;
		this.address = address;
	}
	
	public Player getPlayer() {
		return this.player;
	}

	public String getAddress() {
		return this.address;
	}

	@Override
	public boolean isCancelled() {
		return this.isCancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.isCancelled = cancel;
	}

	@Override
	public HandlerList getHandlers() {
		return handlerList;
	}
	
	public static HandlerList getHandlerList() {
		return handlerList;
	}
}
