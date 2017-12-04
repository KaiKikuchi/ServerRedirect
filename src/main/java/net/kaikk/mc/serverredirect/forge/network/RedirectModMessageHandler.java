package net.kaikk.mc.serverredirect.forge.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.kaikk.mc.serverredirect.forge.ServerRedirect;
import net.kaikk.mc.serverredirect.forge.event.PlayerWithRedirectJoinEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;

public class RedirectModMessageHandler implements IMessageHandler<RedirectModMessage, IMessage> {
	@Override
	public IMessage onMessage(RedirectModMessage message, MessageContext ctx) {
		// The server received this message from the client because they have this mod
		final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
		
		ServerRedirect.sync.add(new Runnable() {
			@Override
			public void run() {
				ServerRedirect.playersWithThisMod.add(player.getUniqueID());
				
				MinecraftForge.EVENT_BUS.post(new PlayerWithRedirectJoinEvent(player));
			}
		});
		return null;	
	}
}
