package net.kaikk.mc.serverredirect.forge.commands;

import net.kaikk.mc.serverredirect.Utils;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public abstract class AbstractAddressCommand extends AbstractPlayersTargetCommand {
	public abstract void handler(EntityPlayerMP p, String addr);

	@Override
	public void handler(EntityPlayerMP p, MinecraftServer server, ICommandSender sender, String[] args) {
		handler(p, args[1]);
	}
	
	public boolean argumentsCheck(ICommandSender sender, String[] args) {
		if (args.length < 2) {
			sender.sendMessage(new TextComponentString(this.getUsage(sender)));
			return false;
		}
		
		if (!Utils.ADDRESS_PREVALIDATOR.matcher(args[1]).matches()) {
			sender.sendMessage(new TextComponentString("Invalid Server Address"));
			return false;
		}
		
		return true;
	}
}
