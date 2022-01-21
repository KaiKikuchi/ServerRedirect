package net.kaikk.mc.serverredirect.bungee.event;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

public class PlayerRedirectEvent extends Event implements Cancellable {
	protected final ProxiedPlayer player;
	protected final String address;

	protected boolean isCancelled;
	
	public PlayerRedirectEvent(ProxiedPlayer player, String address) {
		this.player = player;
		this.address = address;
	}
	
	public ProxiedPlayer getPlayer() {
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
}
