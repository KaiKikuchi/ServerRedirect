package net.kaikk.mc.serverredirect.velocity.commands;

import java.util.Optional;
import java.util.UUID;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import net.kaikk.mc.serverredirect.velocity.ServerRedirect;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public abstract class AbstractPlayersTargetCommandExec implements SimpleCommand {
	public abstract void handler(Player p, Invocation invocation);

	@Override
	public void execute(Invocation invocation) {
		CommandSource source = invocation.source();
		String[] args = invocation.arguments();

		if (!argumentsCheck(invocation)) {
			return;
		}

		if (!hasPermission(invocation)) {
			source.sendMessage(Component.text("Permission denied").color(NamedTextColor.RED));
			return;
		}

		if (args[0].equals("*")) {
			for (Player p : ServerRedirect.proxy().getAllPlayers()) {
				handler(p, invocation);
			}
		} else if (args[0].length() >= 3 && args[0].startsWith("s=")) {
			Optional<RegisteredServer> optServer = ServerRedirect.proxy().getServer(args[0].substring(2));
			if (optServer.isPresent()) {
				for (Player p : optServer.get().getPlayersConnected()) {
					handler(p, invocation);
				}
			}
		} else {
			Optional<Player> optPlayer;
			if (args[0].length() == 36) {
				UUID playerId = UUID.fromString(args[0]);
				optPlayer = ServerRedirect.proxy().getPlayer(playerId);
				if (optPlayer == null) {
					optPlayer = ServerRedirect.proxy().getPlayer(args[0]);
				}
			} else {
				optPlayer = ServerRedirect.proxy().getPlayer(args[0]);
			}

			if (!optPlayer.isPresent()) {
				return;
			}

			handler(optPlayer.get(), invocation);
		}
	}

	public boolean argumentsCheck(Invocation invocation) {
		if (invocation.arguments().length < 1) {
			invocation.source().sendMessage(getUsage(invocation));
			return false;
		}
		return true;
	}

	public TextComponent getUsage(Invocation invocation) {
		return Component.text("Usage: /" + invocation.alias() + " <Player>");
	}
}
