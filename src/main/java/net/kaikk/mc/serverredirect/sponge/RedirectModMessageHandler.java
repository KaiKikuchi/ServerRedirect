package net.kaikk.mc.serverredirect.sponge;

import org.spongepowered.api.Platform.Type;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RemoteConnection;

public class RedirectModMessageHandler implements MessageHandler<RedirectModMessage> {

	@Override
	public void handleMessage(RedirectModMessage message, RemoteConnection connection, Type side) {
		if (connection instanceof PlayerConnection) {
			// The server received this message from the client because they have this mod... so we add them on the Set
			ServerRedirect.instance.playersWithMod.add(((PlayerConnection) connection).getPlayer());
		}
	}
}
