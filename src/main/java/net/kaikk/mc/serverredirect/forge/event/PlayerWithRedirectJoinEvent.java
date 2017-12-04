package net.kaikk.mc.serverredirect.forge.event;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.SERVER)
public class PlayerWithRedirectJoinEvent extends Event {
	protected final EntityPlayerMP player;

	public PlayerWithRedirectJoinEvent(EntityPlayerMP player) {
		this.player = player;
	}

	public EntityPlayerMP getPlayer() {
		return player;
	}
}
