package net.kaikk.mc.serverredirect.forge.command;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import net.kaikk.mc.serverredirect.forge.ServerRedirect;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RedirectCommand implements ICommand {
	@Override
	public String getName() {
		return "redirect";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "Usage: /redirect <address> <PlayerName|PlayerUUID|\"*\">";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 2) {
			sender.sendMessage(new TextComponentString(this.getUsage(sender)));
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
				sender.sendMessage(new TextComponentString("Error: " + e.getMessage()));
			}
		}
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return sender.canUseCommand(3, this.getName());
	}

	@Override
	public int compareTo(ICommand o) {
		return this.getName().compareTo(o.getName());
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			BlockPos targetPos) {
		return Collections.emptyList();
	}

	@Override
	public List getAliases() {
		return Collections.emptyList();
	}

	@Override
	public boolean isUsernameIndex(String[] p_82358_1_, int p_82358_2_) {
		return false;
	}
}
