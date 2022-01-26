package net.kaikk.mc.serverredirect.velocity.commands;

import com.velocitypowered.api.proxy.Player;

import net.kaikk.mc.serverredirect.Utils;
import net.kaikk.mc.serverredirect.velocity.ServerRedirect;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public class IfPlayerRedirectCommandExec extends AbstractPlayersTargetCommandExec {
	protected final boolean not;

	public IfPlayerRedirectCommandExec(boolean not) {
		this.not = not;
	}

	@Override
	public void handler(Player p, Invocation invocation) {
		if (ServerRedirect.isUsingServerRedirect(p) != not) {
			ServerRedirect.proxy().getCommandManager().executeAsync(invocation.source(), Utils.join(invocation.arguments(), 1).replace("%PlayerName", p.getUsername()).replace("%PlayerId", p.getUniqueId().toString()));
		}
	}

	@Override
	public boolean argumentsCheck(Invocation invocation) {
		if (invocation.arguments().length < 2) {
			invocation.source().sendMessage(getUsage(invocation));
			return false;
		}

		return true;
	}

	public TextComponent getUsage(Invocation invocation) {
		return Component.text("Usage: /" + invocation.alias() + " <Player> <Command...>");
	}
}
