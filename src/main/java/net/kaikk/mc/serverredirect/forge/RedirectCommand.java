package net.kaikk.mc.serverredirect.forge;

import java.util.Arrays;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

public class RedirectCommand extends AbstractAddressCommand {
	protected final List<String> aliases = Arrays.asList("redirect");
	
	@Override
	public String getName() {
		return "serverredirect";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "Usage: /redirect (Player) (Server Address)";
	}
	@Override
	public List<String> getAliases() {
		return aliases;
	}

	@Override
	public void handler(EntityPlayerMP p, String addr) {
		ServerRedirect.sendTo(p, addr);
	}
}
