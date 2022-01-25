package net.kaikk.mc.serverredirect.bukkit;

import org.bukkit.entity.Player;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kaikk.mc.serverredirect.PluginInfo;
import net.md_5.bungee.api.ChatColor;

public class PlaceholderExpansionImpl extends PlaceholderExpansion {
	@Override
	public String getIdentifier() {
		return PluginInfo.id;
	}

	@Override
	public String getAuthor() {
		return PluginInfo.author;
	}

	@Override
	public String getVersion() {
		return PluginInfo.version;
	}

	@Override
	public String onPlaceholderRequest(Player player, String params) {
		boolean b = ServerRedirect.isUsingServerRedirect(player);
		switch(params) {
		case "yesno":
			return b ? "Yes" : "No";
		case "yesnof1":
			return b ? "&aYes" : "&cNo";
		case "yesnof2":
			return b ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No";
		}
		
		return b ? "1" : "0";
	}
}
