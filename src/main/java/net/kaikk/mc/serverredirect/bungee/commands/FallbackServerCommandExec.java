package net.kaikk.mc.serverredirect.bungee.commands;

import net.kaikk.mc.serverredirect.bungee.ServerRedirect;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class FallbackServerCommandExec extends AbstractAddressCommandExec {
	public FallbackServerCommandExec() {
		super("fallbackserver", "serverredirect.command.fallback", "fallback");
	}

	@Override
	public void handler(ProxiedPlayer p, String addr) {
		ServerRedirect.sendFallbackTo(p, addr);
	}
}
