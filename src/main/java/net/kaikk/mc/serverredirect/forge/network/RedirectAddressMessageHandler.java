package net.kaikk.mc.serverredirect.forge.network;

import net.kaikk.mc.serverredirect.forge.ServerRedirect;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RedirectAddressMessageHandler implements IMessageHandler<RedirectAddressMessage, IMessage> {
	@Override
	public IMessage onMessage(final RedirectAddressMessage message, MessageContext ctx) {
		// client got a request from the server to redirect to another address
		ServerRedirect.sync.add(new Runnable() {
			@Override
			public void run() {
				try {
					ServerRedirect.processRedirect(message.getAddress());
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		});
		return null;
	}

}
