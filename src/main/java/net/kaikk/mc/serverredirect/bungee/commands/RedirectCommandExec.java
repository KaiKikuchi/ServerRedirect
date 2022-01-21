package net.kaikk.mc.serverredirect.bungee.commands;

import net.kaikk.mc.serverredirect.bungee.ServerRedirect;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class RedirectCommandExec extends AbstractAddressCommandExec {
	public RedirectCommandExec() {
		super("serverredirect", "serverredirect.command.redirect", "redirect");
	}

	@Override
	public void handler(ProxiedPlayer p, String addr) {
		ServerRedirect.sendTo(p, addr);
	}
}
