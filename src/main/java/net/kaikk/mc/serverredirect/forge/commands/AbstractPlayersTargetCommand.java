package net.kaikk.mc.serverredirect.forge.commands;

import java.util.List;

import net.kaikk.mc.serverredirect.Utils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerSelector;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

public abstract class AbstractPlayersTargetCommand extends CommandBase {
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
			EntityPlayerMP p = Utils.parsePlayer(args[0]);
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
	public boolean isUsernameIndex(String[] args, int index) {
		return index == 0;
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
}
