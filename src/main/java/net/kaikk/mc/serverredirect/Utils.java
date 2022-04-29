package net.kaikk.mc.serverredirect;

import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.regex.Pattern;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

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
    
	/**
	 * Utility method for getting a player by UUID
	 * 
	 * @param playerId the player's UUID
	 * @return the EntityPlayerMP instance of the specified player, null if the player was not found.
	 */
	public static EntityPlayerMP getPlayer(UUID playerId) {
		final List<?> list = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
		for (final Object playerObj : list) {
			if (((EntityPlayerMP) playerObj).getUniqueID().equals(playerId)) {
				return ((EntityPlayerMP) playerObj);
			}
		}
		
		return null;
	}
	
	/**
	 * Utility method for getting a player by username
	 * 
	 * @param playerName the player's username
	 * @return the EntityPlayerMP instance of the specified player, null if the player was not found.
	 */
	public static EntityPlayerMP getPlayer(String playerName) {
		final List<?> list = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
		for (final Object playerObj : list) {
			if (((EntityPlayerMP) playerObj).getName().equals(playerName)) {
				return ((EntityPlayerMP) playerObj);
			}
		}
		
		return null;
	}
	
	public static EntityPlayerMP parsePlayer(String playerNameOrUUID) {
		EntityPlayerMP p;
		if (playerNameOrUUID.length() == 36) {
			UUID playerId = UUID.fromString(playerNameOrUUID);
			p = getPlayer(playerId);
			if (p == null) {
				p = getPlayer(playerNameOrUUID);
			}
		} else {
			p = getPlayer(playerNameOrUUID);
		}
		return p;
	}
}
