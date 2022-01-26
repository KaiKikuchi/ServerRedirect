package net.kaikk.mc.serverredirect.forge.commands;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import net.minecraft.command.CommandException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;

public abstract class AbstractPlayersTargetCommand implements ICommand {
	public abstract void handler(EntityPlayerMP p, MinecraftServer server, ICommandSender sender, String[] args);
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (!argumentsCheck(sender, args)) {
			return;
		}
		
		if (!checkPermission(server, sender)) {
			sender.addChatMessage(new TextComponentString("Permission denied"));
			return;
		}
		
		if (args[0].charAt(0) == '*') {
			for (final EntityPlayerMP p : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerList()) {
				handler(p, server, sender, args);
			}
		} else if (args[0].charAt(0) == '@') {	
			List<EntityPlayerMP> list = EntitySelector.matchEntities(sender, args[0], EntityPlayerMP.class);
			for (EntityPlayerMP p : list) {
				handler(p, server, sender, args);
			}
		} else {
			PlayerList pl = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
			EntityPlayerMP p;
			if (args[0].length() == 36) {
				UUID playerId = UUID.fromString(args[0]);
				p = pl.getPlayerByUUID(playerId);
				if (p == null) {
					p = pl.getPlayerByUsername(args[0]);
				}
			} else {
				p = pl.getPlayerByUsername(args[0]);
			}
			
			if (p == null) {
				return;
			}
			
			handler(p, server, sender, args);
		}
	}
	
	public boolean argumentsCheck(ICommandSender sender, String[] args) {
		if (args.length < 1) {
			sender.addChatMessage(new TextComponentString(this.getCommandUsage(sender)));
			return false;
		}
		
		return true;
	}


	@Override
	public int compareTo(ICommand o) {
		return this.getCommandName().compareTo(o.getCommandName());
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return sender.canCommandSenderUseCommand(2, this.getCommandName());
	}

	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args,
			BlockPos pos) {
		return Collections.emptyList();
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return index == 0;
	}
}
