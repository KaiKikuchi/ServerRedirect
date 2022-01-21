package net.kaikk.mc.serverredirect.velocity.event;

import com.velocitypowered.api.proxy.Player;

public class PlayerRedirectEvent {
	protected final Player player;
	protected final String address;

	protected boolean isCancelled;
	
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

	public boolean isCancelled() {
		return isCancelled;
	}

	public void setCancelled(boolean cancel) {
		this.isCancelled = cancel;
	}
}
