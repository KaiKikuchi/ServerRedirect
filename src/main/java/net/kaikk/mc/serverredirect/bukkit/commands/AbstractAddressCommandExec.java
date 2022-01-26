package net.kaikk.mc.serverredirect.bukkit.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.kaikk.mc.serverredirect.Utils;

public abstract class AbstractAddressCommandExec extends AbstractPlayersTargetCommandExec {
	protected AbstractAddressCommandExec(String permission) {
		super(permission);
	}

	public abstract void handler(Player p, String addr);
	
	@Override
	public void handler(Player p, CommandSender sender, Command cmd, String label, String[] args) {
		handler(p, args[1]);
	}
	
	@Override
	public boolean argumentsCheck(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 2) {
			sender.sendMessage(getUsage(sender, cmd, label, args));
			return false;
		}
		
		if (!Utils.ADDRESS_PREVALIDATOR.matcher(args[1]).matches()) {
			sender.sendMessage(ChatColor.RED + "The server address contains forbidden characters.");
			return false;
		}
		
		return true;
	}
	
	@Override
	public String getUsage(CommandSender sender, Command cmd, String label, String[] args) {
		return "Usage: /" + label + " <Player> <Server Address>";
	}
}
