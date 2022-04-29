package net.kaikk.mc.serverredirect.forge.commands;

import java.util.List;

import net.kaikk.mc.serverredirect.Utils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;

public abstract class AbstractPlayersTargetCommand extends CommandBase {
	public abstract void handler(EntityPlayerMP p, MinecraftServer server, ICommandSender sender, String[] args);

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (!argumentsCheck(sender, args)) {
			return;
		}

		if (!checkPermission(server, sender)) {
			sender.sendMessage(new TextComponentString("Permission denied"));
			return;
		}

		if (args[0].charAt(0) == '*') {
			for (final EntityPlayerMP p : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
				handler(p, server, sender, args);
			}
		} else if (args[0].charAt(0) == '@') {	
			List<EntityPlayerMP> list = EntitySelector.matchEntities(sender, args[0], EntityPlayerMP.class);
			for (EntityPlayerMP p : list) {
				handler(p, server, sender, args);
			}
		} else {
			EntityPlayerMP p = Utils.parsePlayer(args[0]);
			handler(p, server, sender, args);
		}
	}

	public boolean argumentsCheck(ICommandSender sender, String[] args) {
		if (args.length < 1) {
			sender.sendMessage(new TextComponentString(this.getUsage(sender)));
			return false;
		}

		return true;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return index == 0;
	}
}
