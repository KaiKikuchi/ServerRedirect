package net.kaikk.mc.serverredirect.forge.network;

import net.kaikk.mc.serverredirect.forge.ServerRedirect;
import net.kaikk.mc.serverredirect.forge.event.PlayerWithRedirectJoinEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RedirectModMessageHandler implements IMessageHandler<RedirectModMessage, IMessage> {
	@Override
	public IMessage onMessage(RedirectModMessage message, MessageContext ctx) {
		if (ServerRedirect.playersWithThisMod == null) {
			return null;
		}
		
		// The server received this message from the client because they have this mod
		final EntityPlayerMP player = ctx.getServerHandler().player;
		
		Minecraft.getMinecraft().addScheduledTask(new Runnable() {
			@Override
			public void run() {
				try {
					ServerRedirect.playersWithThisMod.add(player.getUniqueID());
					
					MinecraftForge.EVENT_BUS.post(new PlayerWithRedirectJoinEvent(player));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return null;
	}
}
