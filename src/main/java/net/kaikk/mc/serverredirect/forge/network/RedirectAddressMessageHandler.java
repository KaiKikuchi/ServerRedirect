package net.kaikk.mc.serverredirect.forge.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import net.kaikk.mc.serverredirect.forge.ServerRedirect;

public class RedirectAddressMessageHandler implements IMessageHandler<RedirectAddressMessage, IMessage> {
	@Override
	public IMessage onMessage(final RedirectAddressMessage message, MessageContext ctx) {
		// client got a request from the server to redirect to another address
		ServerRedirect.sync.add(new Runnable() {
			@Override
			public void run() {
				ServerRedirect.processRedirect(message.getAddress());
			}
		});
		return null;
	}

}
