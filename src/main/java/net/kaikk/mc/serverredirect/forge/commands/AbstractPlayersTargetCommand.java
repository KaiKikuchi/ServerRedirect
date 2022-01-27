package net.kaikk.mc.serverredirect.forge.commands;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import net.kaikk.mc.serverredirect.forge.ServerRedirect;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerSelector;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

public abstract class AbstractPlayersTargetCommand implements ICommand {
	public abstract void handler(EntityPlayerMP p, ICommandSender sender, String[] args);
	
	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (!argumentsCheck(sender, args)) {
			return;
		}
		
		if (!canCommandSenderUseCommand(sender)) {
			sender.addChatMessage(new ChatComponentText("Permission denied"));
			return;
		}
		
		if (args[0].charAt(0) == '*') {
			for (final Object playerObj : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
				handler((EntityPlayerMP) playerObj, sender, args);
			}
		} else if (args[0].charAt(0) == '@') {	
			List<EntityPlayerMP> arr = PlayerSelector.matchEntities(sender, args[0], EntityPlayerMP.class);
			for (EntityPlayerMP p : arr) {
				handler(p, sender, args);
			}
		} else {
			EntityPlayerMP p;
			if (args[0].length() == 36) {
				UUID playerId = UUID.fromString(args[0]);
				p = ServerRedirect.getPlayer(playerId);
				if (p == null) {
					p = ServerRedirect.getPlayer(args[0]);
				}
			} else {
				p = ServerRedirect.getPlayer(args[0]);
			}
			
			if (p == null) {
				return;
			}
			
			handler(p, sender, args);
		}
	}
	
	public boolean argumentsCheck(ICommandSender sender, String[] args) {
		if (args.length < 1) {
			sender.addChatMessage(new ChatComponentText(this.getCommandUsage(sender)));
			return false;
		}
		
		return true;
	}
	
	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
		return Collections.emptyList();
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return sender.canCommandSenderUseCommand(2, this.getCommandName());
	}
	
	@Override
	public int compareTo(ICommand o) {
		return this.getCommandName().compareTo(((ICommand) o).getCommandName());
	}
	
	@Override
	public List<String> getCommandAliases() {
		return Collections.emptyList();
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return index == 0;
	}
}
