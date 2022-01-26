package net.kaikk.mc.serverredirect.forge.commands;

import net.kaikk.mc.serverredirect.Utils;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

public abstract class AbstractAddressCommand extends AbstractPlayersTargetCommand {
	public abstract void handler(EntityPlayerMP p, String addr);

	@Override
	public void handler(EntityPlayerMP p, ICommandSender sender, String[] args) {
		handler(p, args[1]);
	}
	
	public boolean argumentsCheck(ICommandSender sender, String[] args) {
		if (args.length < 2) {
			sender.addChatMessage(new ChatComponentText(this.getCommandUsage(sender)));
			return false;
		}
		
		if (!Utils.ADDRESS_PREVALIDATOR.matcher(args[1]).matches()) {
			sender.addChatMessage(new ChatComponentText("Invalid Server Address"));
			return false;
		}
		
		return true;
	}
}
