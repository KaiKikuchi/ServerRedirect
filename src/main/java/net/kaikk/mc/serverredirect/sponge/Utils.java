package net.kaikk.mc.serverredirect.sponge;

import org.spongepowered.api.event.cause.Cause;

public class Utils {
	
	public static Cause getCause(ServerRedirect instance) {
		try {
			return Cause.source(instance.container).build();
		} catch (Exception e) {
			return null;
		}
	}
}
