package net.kaikk.mc.serverredirect.bungee.commands;

import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public abstract class AbstractPlayersTargetCommandExec extends Command {
	protected AbstractPlayersTargetCommandExec(String name, String permission, String... aliases) {
		super(name, permission, aliases);
	}

	public abstract void handler(ProxiedPlayer p, CommandSender sender, String[] args);

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!argumentsCheck(sender, args)) {
			return;
		}

		if (!sender.hasPermission(this.getPermission())) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "Permission denied."));
			return;
		}

		if (args[0].equals("*")) {
			for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
				handler(p, sender, args);
			}
		} else if (args[0].length() >= 3 && args[0].startsWith("s=")) {
			ServerInfo server = ProxyServer.getInstance().getServerInfo(args[0].substring(2));
			if (server != null) {
				for (ProxiedPlayer p : server.getPlayers()) {
					handler(p, sender, args);
				}
			}
		} else {
			ProxiedPlayer player;
			if (args[0].length() == 36) {
				UUID playerId = UUID.fromString(args[0]);
				player = ProxyServer.getInstance().getPlayer(playerId);
				if (player == null) {
					player = ProxyServer.getInstance().getPlayer(args[0]);
				}
			} else {
				player = ProxyServer.getInstance().getPlayer(args[0]);
			}

			if (player == null) {
				return;
			}

			handler(player, sender, args);
		}
	}

	public boolean argumentsCheck(CommandSender sender, String[] args) {
		if (args.length < 1) {
			sender.sendMessage(getUsage(sender, args));
			return false;
		}
		return true;
	}

	public TextComponent getUsage(CommandSender sender, String[] args) {
		return new TextComponent("Usage: /" + this.getName() + " <Player>");
	}
}
