package net.kaikk.mc.serverredirect.bukkit.commands;

import org.bukkit.entity.Player;

import net.kaikk.mc.serverredirect.bukkit.ServerRedirect;


public class RedirectCommandExec extends AbstractAddressCommandExec {
	public RedirectCommandExec() {
		super("serverredirect.command.redirect");
	}

	@Override
	public void handler(Player p, String addr) {
		ServerRedirect.sendTo(p, addr);
	}
}
