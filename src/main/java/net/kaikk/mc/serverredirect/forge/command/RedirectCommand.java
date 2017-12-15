package net.kaikk.mc.serverredirect.forge.command;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.kaikk.mc.serverredirect.forge.ServerRedirect;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class RedirectCommand implements ICommand {
	@Override
	public String getCommandName() {
		return "redirect";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "Usage: /redirect <address> <PlayerName|PlayerUUID|\"*\">";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args.length < 2) {
			sender.addChatMessage(new ChatComponentText(this.getCommandUsage(sender)));
			return;
		}

		if (args[1].equals("*")) {
			ServerRedirect.sendAllTo(args[0]);
		} else {
			try {
				try {
					ServerRedirect.sendTo(args[0], UUID.fromString(args[1]));
				} catch (IllegalArgumentException e) {
					ServerRedirect.sendTo(args[0], args[1]);
				}
			} catch (IllegalArgumentException e) {
				sender.addChatMessage(new ChatComponentText("Error: " + e.getMessage()));
			}
		}
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		return Collections.emptyList();
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return sender.canCommandSenderUseCommand(3, this.getCommandName());
	}
	
	@Override
	public int compareTo(Object o) {
		return this.getCommandName().compareTo(((ICommand) o).getCommandName());
	}

	@Override
	public List getCommandAliases() {
		return Collections.emptyList();
	}

	@Override
	public boolean isUsernameIndex(String[] p_82358_1_, int p_82358_2_) {
		return false;
	}
}
