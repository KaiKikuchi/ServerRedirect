package net.kaikk.mc.serverredirect.bukkit.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerWithRedirectJoinEvent extends Event {
	protected static HandlerList handlerList = new HandlerList();
	protected final Player player;
	
	public PlayerWithRedirectJoinEvent(Player player) {
		this.player = player;
	}
	
	public Player getPlayer() {
		return this.player;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlerList;
	}
	
	public static HandlerList getHandlerList() {
		return handlerList;
	}
}
