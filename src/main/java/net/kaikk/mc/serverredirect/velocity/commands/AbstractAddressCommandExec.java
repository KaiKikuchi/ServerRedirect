package net.kaikk.mc.serverredirect.velocity.commands;

import com.velocitypowered.api.proxy.Player;

import net.kaikk.mc.serverredirect.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public abstract class AbstractAddressCommandExec extends AbstractPlayersTargetCommandExec {
	public abstract void handler(Player p, String addr);

	@Override
	public boolean argumentsCheck(Invocation invocation) {
		if (invocation.arguments().length < 2) {
			invocation.source().sendMessage(getUsage(invocation));
			return false;
		}

		if (!Utils.ADDRESS_PREVALIDATOR.matcher(invocation.arguments()[1]).matches()) {
			invocation.source().sendMessage(Component.text("The server address contains forbidden characters.").color(NamedTextColor.RED));
			return false;
		}

		return true;
	}

	@Override
	public void handler(Player p, Invocation invocation) {
		handler(p, invocation.arguments()[1]);
	}

	public TextComponent getUsage(Invocation invocation) {
		return Component.text("Usage: /" + invocation.alias() + " <Player> <Address>");
	}
}
