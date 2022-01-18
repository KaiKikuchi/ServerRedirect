package net.kaikk.mc.serverredirect.forge;

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

public abstract class AbstractAddressCommand implements ICommand {
	public abstract void handler(EntityPlayerMP p, String addr);
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 2) {
			sender.addChatMessage(new TextComponentString(this.getCommandUsage(sender)));
			return;
		}
		
		if (!PacketHandler.ADDRESS_PREVALIDATOR.matcher(args[1]).matches()) {
			sender.addChatMessage(new TextComponentString("Invalid Server Address"));
			return;
		}
		
		if (args[0].charAt(0) == '@') {	
			List<EntityPlayerMP> list = EntitySelector.matchEntities(sender, args[0], EntityPlayerMP.class);
			for (EntityPlayerMP p : list) {
				handler(p, args[1]);
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
			
			handler(p, args[1]);
		}
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
