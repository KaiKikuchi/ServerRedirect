package net.kaikk.mc.serverredirect.forge.commands;

import java.util.Arrays;
import java.util.List;

import net.kaikk.mc.serverredirect.forge.ServerRedirect;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

public class FallbackCommand extends AbstractAddressCommand {
	protected final List<String> aliases = Arrays.asList("fallback");
	
	@Override
	public String getName() {
		return "fallbackserver";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "Usage: /fallback (Player) (Server Address)";
	}
	@Override
	public List<String> getAliases() {
		return aliases;
	}

	@Override
	public void handler(EntityPlayerMP p, String addr) {
		ServerRedirect.sendFallbackTo(p, addr);
	}
}