package net.kaikk.mc.serverredirect.sponge.commands;

import java.util.Optional;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import net.kaikk.mc.serverredirect.Utils;

public abstract class AbstractAddressCommandExec extends AbstractPlayersTargetCommandExec {
	public abstract void handler(Player p, String addr);
	
	@Override
	public boolean argumentsCheck(CommandSource source, String[] args) {
		if (args.length < 2) {
			Optional<Text> optHelp = getHelp(source);
			if (optHelp.isPresent()) {
				source.sendMessage(optHelp.get());
			}
			return false;
		}
		
		if (!Utils.ADDRESS_PREVALIDATOR.matcher(args[1]).matches()) {
			source.sendMessage(Text.of(TextColors.RED, "The server address contains forbidden characters."));
			return false;
		}
		
		return true;
	}
	
	@Override
	public void handler(Player p, CommandSource source, String[] args) {
		handler(p, args[1]);
	}
}
