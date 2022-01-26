package net.kaikk.mc.serverredirect.sponge.commands;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.CommandBlockSource;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.selector.Selector;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;

public abstract class AbstractPlayersTargetCommandExec implements CommandCallable {
	public abstract void handler(Player p, CommandSource source, String[] args);

	@Override
	public CommandResult process(CommandSource source, String arguments) throws CommandException {
		if (!testPermission(source)) {
			source.sendMessage(Text.of(TextColors.RED, "Permission denied"));
			return CommandResult.empty();
		}

		final String[] args = arguments.split("[\\s]+");
		if (!argumentsCheck(source, args)) {
			return CommandResult.empty();
		}

		if (args[0].equals("*")) {
			for (Player player : Sponge.getServer().getOnlinePlayers()) {
				handler(player, source, args);
			}
		} else if (args[0].length() >= 3 && args[0].startsWith("r=")) {
			double distance = Double.valueOf(args[0].substring(2));
			distance *= distance;

			Location<World> l;
			Entity e = null;
			if (source instanceof Entity) {
				e = (Entity) source;
				l = e.getLocation();
			} else if (source instanceof CommandBlockSource) {
				CommandBlockSource csender = (CommandBlockSource) source;
				l = csender.getLocation();
			} else {
				l = Sponge.getServer().getWorlds().iterator().next().getSpawnLocation();
			}

			Vector3d v = l.getPosition();
			for (Player p : l.getExtent().getPlayers()) {
				if (p.getLocation().getPosition().distanceSquared(v) <= distance) {
					handler(p, source, args);
				}
			}
		} else if (args[0].length() < 2 || args[0].charAt(0) != '@') {
			Player player;
			if (args[0].length() == 36) {
				UUID playerId = UUID.fromString(args[0]);
				player = Sponge.getServer().getPlayer(playerId).orElse(null);
				if (player == null) {
					player = Sponge.getServer().getPlayer(args[0]).orElse(null);
				}
			} else {
				player = Sponge.getServer().getPlayer(args[0]).orElse(null);
			}

			if (player == null) {
				return CommandResult.success();
			}

			handler(player, source, args);
		} else {
			for (Entity e : Selector.parse(args[0]).resolve(source)) {
				if (e instanceof Player) {
					handler((Player) e, source, args);
				}
			}
		}

		return CommandResult.success();
	}

	@Override
	public List<String> getSuggestions(CommandSource source, String arguments, Location<World> targetPosition) throws CommandException {
		return Collections.emptyList();
	}

	@Override
	public Optional<Text> getShortDescription(CommandSource source) {
		return Optional.empty();
	}

	@Override
	public Optional<Text> getHelp(CommandSource source) {
		return Optional.empty();
	}

	public boolean argumentsCheck(CommandSource source, String[] args) {
		if (args.length < 1) {
			Optional<Text> optHelp = getHelp(source);
			if (optHelp.isPresent()) {
				source.sendMessage(optHelp.get());
			}
			return false;
		}
		return true;
	}
}
