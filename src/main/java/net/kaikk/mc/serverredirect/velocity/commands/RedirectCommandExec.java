package net.kaikk.mc.serverredirect.velocity.commands;

import com.velocitypowered.api.proxy.Player;

import net.kaikk.mc.serverredirect.velocity.ServerRedirect;

public class RedirectCommandExec extends AbstractAddressCommandExec {
	@Override
	public void handler(Player p, String addr) {
		ServerRedirect.sendTo(p, addr);
	}
	
	@Override
	public boolean hasPermission(Invocation invocation) {
		return invocation.source().hasPermission("serverredirect.command.redirect");
	}
}
