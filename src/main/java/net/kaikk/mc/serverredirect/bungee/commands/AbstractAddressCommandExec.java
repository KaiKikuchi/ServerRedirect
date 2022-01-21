package net.kaikk.mc.serverredirect.bungee.commands;

import java.util.UUID;


import net.kaikk.mc.serverredirect.Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public abstract class AbstractAddressCommandExec extends Command {
	protected AbstractAddressCommandExec(String name, String permission, String... aliases) {
		super(name, permission, aliases);
	}

	public abstract void handler(ProxiedPlayer p, String addr);
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		if (args.length<2) {
			sender.sendMessage(new TextComponent("Usage: /" + this.getName() + " <Player> <Server Address>"));
			return;
		}

		if (!sender.hasPermission(this.getPermission())) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "Permission denied."));
			return;
		}
		
		if (!Utils.ADDRESS_PREVALIDATOR.matcher(args[1]).matches()) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "The server address contains forbidden characters."));
			return;
		}

		if (args[0].equals("*")) {
			for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
				handler(p, args[1]);				
			}
		} else if (args[0].length() >= 3 && args[0].startsWith("s=")) {
			ServerInfo server = ProxyServer.getInstance().getServerInfo(args[0].substring(2));
			if (server != null) {
				for (ProxiedPlayer p : server.getPlayers()) {
					handler(p, args[1]);				
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
			
			handler(player, args[1]);
		}
	}
}
