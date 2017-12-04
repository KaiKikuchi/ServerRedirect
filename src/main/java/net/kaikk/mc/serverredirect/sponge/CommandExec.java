package net.kaikk.mc.serverredirect.sponge;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class CommandExec implements CommandCallable {
	@Override
	public CommandResult process(CommandSource source, String arguments) throws CommandException {
		final String[] args = arguments.split("[\\s]+");
		
		if (args.length<2) {
			source.sendMessage(this.getUsage(source));
			return CommandResult.empty();
		}

		if (args[1].equals("*")) {
			ServerRedirect.sendToAll(args[0]);
		} else {
			Optional<Player> playerToSend;
			try {
				playerToSend = Sponge.getServer().getPlayer(UUID.fromString(args[1]));
			} catch (IllegalArgumentException e) {
				playerToSend = Sponge.getServer().getPlayer(args[1]);
			}
			
			if (!playerToSend.isPresent()) {
				source.sendMessage(Text.of("Player "+args[1]+" not found"));
				return CommandResult.empty();
			}
			
			ServerRedirect.sendTo(playerToSend.get(), args[0]);
		}
		return CommandResult.success();
	}

	@Override
	public List<String> getSuggestions(CommandSource source, String arguments, Location<World> targetPosition) throws CommandException {
		return Collections.emptyList();
	}

	@Override
	public boolean testPermission(CommandSource source) {
		return source.hasPermission("serverredirect.command.redirect");
	}

	@Override
	public Optional<Text> getShortDescription(CommandSource source) {
		return Optional.empty();
	}

	@Override
	public Optional<Text> getHelp(CommandSource source) {
		return Optional.empty();
	}

	@Override
	public Text getUsage(CommandSource source) {
		return Text.of("Usage: /redirect <address> <PlayerName|PlayerUUID|\"*\">");
	}
}
