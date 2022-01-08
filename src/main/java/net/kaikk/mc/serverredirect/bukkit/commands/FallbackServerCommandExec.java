package net.kaikk.mc.serverredirect.bukkit.commands;

import org.bukkit.entity.Player;

import net.kaikk.mc.serverredirect.bukkit.ServerRedirect;


public class FallbackServerCommandExec extends AbstractAddressCommandExec {
	public FallbackServerCommandExec() {
		super("serverredirect.command.fallback");
	}

	@Override
	public void handler(Player p, String addr) {
		ServerRedirect.sendFallbackTo(p, addr);
	}
}
