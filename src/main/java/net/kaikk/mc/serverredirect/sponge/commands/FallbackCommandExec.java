package net.kaikk.mc.serverredirect.sponge.commands;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import net.kaikk.mc.serverredirect.sponge.ServerRedirect;

public class FallbackCommandExec extends AbstractAddressCommandExec {
	@Override
	public void handler(Player p, String addr) {
		ServerRedirect.sendFallbackTo(p, addr);
	}
	
	@Override
	public boolean testPermission(CommandSource source) {
		return source.hasPermission("serverredirect.command.fallback");
	}
	
	@Override
	public Text getUsage(CommandSource source) {
		return Text.of("Usage: /fallback <Player> <Server Address>");
	}
}
