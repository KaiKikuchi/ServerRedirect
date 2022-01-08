package net.kaikk.mc.serverredirect.forge;

import java.util.Arrays;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

public class FallbackCommand extends AbstractAddressCommand {
	protected final List<String> aliases = Arrays.asList("fallback");
	
	@Override
	public String getCommandName() {
		return "fallbackserver";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "Usage: /fallback (Player) (Server Address)";
	}
	@Override
	public List<String> getCommandAliases() {
		return aliases;
	}

	@Override
	public void handler(EntityPlayerMP p, String addr) {
		ServerRedirect.sendFallbackTo(p, addr);
	}
}
