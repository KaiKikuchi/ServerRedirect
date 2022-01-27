package net.kaikk.mc.serverredirect.forge.commands;

import java.util.Collections;
import java.util.List;

import net.kaikk.mc.serverredirect.Utils;
import net.kaikk.mc.serverredirect.forge.ServerRedirect;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class IfPlayerRedirectCommand extends AbstractPlayersTargetCommand {
	protected final boolean not;
	protected final String commandName;

	public IfPlayerRedirectCommand(boolean not, String commandName) {
		this.not = not;
		this.commandName = commandName;
	}
	
	@Override
	public void handler(EntityPlayerMP p, ICommandSender sender, String[] args) {
		if (ServerRedirect.isUsingServerRedirect(p) != not) {
			MinecraftServer.getServer().getCommandManager().executeCommand(sender, Utils.join(args, 1).replace("%PlayerName", p.getName()).replace("%PlayerId", p.getUniqueID().toString()));
		}
	}
	
	@Override
	public String getCommandName() {
		return commandName;
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "Usage: /" + commandName + " (Player) (Command...)";
	}
	
	@Override
	public List<String> getCommandAliases() {
		return Collections.emptyList();
	}
}
