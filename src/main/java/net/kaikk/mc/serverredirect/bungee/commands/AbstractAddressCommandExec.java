package net.kaikk.mc.serverredirect.bungee.commands;

import net.kaikk.mc.serverredirect.Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public abstract class AbstractAddressCommandExec extends AbstractPlayersTargetCommandExec {
	protected AbstractAddressCommandExec(String name, String permission, String... aliases) {
		super(name, permission, aliases);
	}

	public abstract void handler(ProxiedPlayer p, String addr);
	
	@Override
	public void handler(ProxiedPlayer p, CommandSender sender, String[] args) {
		handler(p, args[1]);
	}
	
	@Override
	public boolean argumentsCheck(CommandSender sender, String[] args) {
		if (args.length < 2) {
			sender.sendMessage(getUsage(sender, args));
			return false;
		}
		
		if (!Utils.ADDRESS_PREVALIDATOR.matcher(args[1]).matches()) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "The server address contains forbidden characters."));
			return false;
		}
		
		return true;
	}
	
	@Override
	public TextComponent getUsage(CommandSender sender, String[] args) {
		return new TextComponent("Usage: /" + this.getName() + " <Player> <Server Address>");
	}
}
