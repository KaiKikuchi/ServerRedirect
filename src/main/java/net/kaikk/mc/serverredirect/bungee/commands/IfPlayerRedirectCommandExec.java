package net.kaikk.mc.serverredirect.bungee.commands;

import net.kaikk.mc.serverredirect.Utils;
import net.kaikk.mc.serverredirect.bungee.ServerRedirect;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class IfPlayerRedirectCommandExec extends AbstractPlayersTargetCommandExec {
	protected final boolean not;

	public IfPlayerRedirectCommandExec(String name, String permission, boolean not, String... aliases) {
		super(name, permission, aliases);
		this.not = not;
	}

	@Override
	public void handler(ProxiedPlayer p, CommandSender sender, String[] args) {
		if (ServerRedirect.isUsingServerRedirect(p) != not) {
			ProxyServer.getInstance().getPluginManager().dispatchCommand(sender, Utils.join(args, 1).replace("%PlayerName", p.getName()).replace("%PlayerId", p.getUniqueId().toString()));
		}
	}

	@Override
	public boolean argumentsCheck(CommandSender sender, String[] args) {
		if (args.length < 2) {
			sender.sendMessage(getUsage(sender, args));
			return false;
		}

		return true;
	}

	@Override
	public TextComponent getUsage(CommandSender sender, String[] args) {
		return new TextComponent("Usage: /" + this.getName() + " <Player> <Command...>");
	}
}
