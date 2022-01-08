package net.kaikk.mc.serverredirect.sponge.commands;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import net.kaikk.mc.serverredirect.sponge.ServerRedirect;

public class RedirectCommandExec extends AbstractAddressCommandExec {
	@Override
	public void handler(Player p, String addr) {
		ServerRedirect.sendTo(p, addr);
	}
	
	@Override
	public boolean testPermission(CommandSource source) {
		return source.hasPermission("serverredirect.command.redirect");
	}
	
	@Override
	public Text getUsage(CommandSource source) {
		return Text.of("Usage: /redirect <Player> <Server Address>");
	}
}
