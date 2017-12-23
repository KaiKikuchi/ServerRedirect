package net.kaikk.mc.serverredirect.sponge;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Collection;

public class CommandExec implements CommandExecutor {

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		String address = args.<String>getOne("address").get();
		Collection<Player> players = args.<Player>getAll("player");

		if (players.size() == 0) {
			ServerRedirect.sendToAll(address);
		} else {
			for (Player player : players) {
				ServerRedirect.sendTo(player, address);
			}
		}

		return CommandResult.success();
	}
}