package net.kaikk.mc.serverredirect.bukkit.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.kaikk.mc.serverredirect.Utils;
import net.kaikk.mc.serverredirect.bukkit.ServerRedirect;

public class IfPlayerRedirectCommandExec extends AbstractPlayersTargetCommandExec {
	protected final boolean not;

	public IfPlayerRedirectCommandExec(String permission, boolean not) {
		super(permission);
		this.not = not;
	}

	@Override
	public void handler(Player p, CommandSender sender, Command cmd, String label, String[] args) {
		if (ServerRedirect.isUsingServerRedirect(p) != not) {
			Bukkit.dispatchCommand(sender, Utils.join(args, 1).replace("%PlayerName", p.getName()).replace("%PlayerId", p.getUniqueId().toString()));
		}
	}

	@Override
	public boolean argumentsCheck(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 2) {
			sender.sendMessage(getUsage(sender, cmd, label, args));
			return false;
		}

		return true;
	}

	@Override
	public String getUsage(CommandSender sender, Command cmd, String label, String[] args) {
		return "Usage: /" + label + " <Player> <Command...>";
	}
}
