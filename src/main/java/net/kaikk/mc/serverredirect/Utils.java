package net.kaikk.mc.serverredirect;

import java.util.StringJoiner;
import java.util.UUID;
import java.util.regex.Pattern;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class Utils {
	public static final Pattern ADDRESS_PREVALIDATOR = Pattern.compile("^[A-Za-z0-9-_.:]+$"); // allowed characters in a server address

	public static String join(String[] stringArray, int initialIndex) {
		return join(" ", stringArray, initialIndex);
	}

	public static String join(CharSequence separator, String[] stringArray, int initialIndex) {
		final StringJoiner joiner = new StringJoiner(separator);
		for(; initialIndex < stringArray.length; initialIndex++){
			joiner.add(stringArray[initialIndex]);
		}

		return joiner.toString();
	}
	
	public static EntityPlayerMP parsePlayer(String playerNameOrUUID) {
		PlayerList pl = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
		EntityPlayerMP p;
		if (playerNameOrUUID.length() == 36) {
			UUID playerId = UUID.fromString(playerNameOrUUID);
			p = pl.getPlayerByUUID(playerId);
			if (p == null) {
				p = pl.getPlayerByUsername(playerNameOrUUID);
			}
		} else {
			p = pl.getPlayerByUsername(playerNameOrUUID);
		}
		
		return p;
	}
}
