package net.kaikk.mc.serverredirect.sponge.commands;

import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import net.kaikk.mc.serverredirect.Utils;
import net.kaikk.mc.serverredirect.sponge.ServerRedirect;

public class IfPlayerRedirectCommandExec extends AbstractPlayersTargetCommandExec {
	protected final boolean not;
	protected final String commandName;

	public IfPlayerRedirectCommandExec(boolean not, String commandName) {
		this.not = not;
		this.commandName = commandName;
	}

	@Override
	public void handler(Player p, CommandSource source, String[] args) {
		if (ServerRedirect.isUsingServerRedirect(p) != not) {
			Sponge.getCommandManager().process(source, Utils.join(args, 1).replace("%PlayerName", p.getName()).replace("%PlayerId", p.getUniqueId().toString()));
		}
	}

	@Override
	public boolean argumentsCheck(CommandSource source, String[] args) {
		if (args.length < 2) {
			Optional<Text> optHelp = getHelp(source);
			if (optHelp.isPresent()) {
				source.sendMessage(optHelp.get());
			}
			return false;
		}

		return true;
	}

	@Override
	public boolean testPermission(CommandSource source) {
		return source.hasPermission("serverredirect.command." + this.commandName);
	}

	@Override
	public Text getUsage(CommandSource source) {
		return Text.of("Usage: /" + this.commandName + " <Player> <Command...>");
	}
}
