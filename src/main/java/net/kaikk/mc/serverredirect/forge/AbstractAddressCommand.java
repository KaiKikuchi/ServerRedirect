package net.kaikk.mc.serverredirect.forge;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerSelector;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

public abstract class AbstractAddressCommand implements ICommand {
	public abstract void handler(EntityPlayerMP p, String addr);
	
	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args.length < 2) {
			//sender.addChatMessage(new TextComponentString(this.getUsage(sender)));
			return;
		}
		
		if (!PacketHandler.ADDRESS_PREVALIDATOR.matcher(args[1]).matches()) {
			sender.addChatMessage(new ChatComponentText("The server address contains forbidden characters."));
			return;
		}
		
		if (args[0].charAt(0) == '@') {	
			EntityPlayerMP[] arr = PlayerSelector.matchPlayers(sender, args[0]);
			for (EntityPlayerMP p : arr) {
				handler(p, args[1]);
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
			
			handler(p, args[1]);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		return Collections.emptyList();
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return sender.canCommandSenderUseCommand(2, this.getCommandName());
	}
	
	@Override
	public int compareTo(Object o) {
		return this.getCommandName().compareTo(((ICommand) o).getCommandName());
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List getCommandAliases() {
		return Collections.emptyList();
	}

	@Override
	public boolean isUsernameIndex(String[] p_82358_1_, int index) {
		return index == 0;
	}
}
