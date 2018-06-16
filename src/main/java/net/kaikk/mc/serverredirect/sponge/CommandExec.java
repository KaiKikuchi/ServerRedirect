package net.kaikk.mc.serverredirect.sponge;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.CommandBlockSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
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
		
		if (args[0].equalsIgnoreCase("playerslist")) {
			if (!source.hasPermission("serverredirect.command.list")) {
				source.sendMessage(Text.of(TextColors.RED, "Permission denied"));
				return CommandResult.empty();
			}
			
			StringJoiner players = new StringJoiner(", ");
			for (Player player : ServerRedirect.instance.playersWithMod) {
				players.add(player.getName());
			}
			source.sendMessage(Text.of(TextColors.GOLD, "Players with the mod: " + players.toString()));
			return CommandResult.success();
		}

		if (args[1].equals("*")) {
			if (!source.hasPermission("serverredirect.command.redirect.all")) {
				source.sendMessage(Text.of(TextColors.RED, "Permission denied"));
				return CommandResult.empty();
			}
			
			ServerRedirect.sendToAll(args[0]);
		} else if (args[1].startsWith("r=")) {
			if (!source.hasPermission("serverredirect.command.redirect.radius")) {
				source.sendMessage(Text.of(TextColors.RED, "Permission denied"));
				return CommandResult.empty();
			}
			
			final Location<World> loc;
			if (source instanceof Player) {
				loc = ((Player) source).getLocation();
			} else if (source instanceof CommandBlockSource) {
				loc = ((CommandBlockSource) source).getLocation();
			} else {
				source.sendMessage(Text.of("Only players and command blocks can use the \"r=\" parameter!"));
				return CommandResult.empty();
			}
			
			final int radiusSquared = (int) Math.pow(Integer.valueOf(args[1].substring(2)), 2);
			
			for (Player p : Sponge.getServer().getOnlinePlayers()) {
				if (p.getLocation().inExtent(loc.getExtent()) && p.getLocation().getPosition().distanceSquared(loc.getPosition()) < radiusSquared) {
					ServerRedirect.sendTo(p, args[0]);
				}
			}
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
			
			if (!source.hasPermission("serverredirect.command.redirect."+(playerToSend.get() == source ? "self" : "others"))) {
				source.sendMessage(Text.of(TextColors.RED, "Permission denied"));
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
		return true;
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
		return Text.of("Usage: /redirect <address> <PlayerName|PlayerUUID|\"r=[radius]\"|\"*\">", Text.NEW_LINE,
				"Usage: /redirect \"playerslist\"");
	}
}
