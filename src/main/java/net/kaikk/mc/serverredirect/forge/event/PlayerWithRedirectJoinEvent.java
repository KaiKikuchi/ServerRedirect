package net.kaikk.mc.serverredirect.forge.event;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayerMP;

public class PlayerWithRedirectJoinEvent extends Event {
	protected final EntityPlayerMP player;

	public PlayerWithRedirectJoinEvent(EntityPlayerMP player) {
		this.player = player;
	}

	public EntityPlayerMP getPlayer() {
		return player;
	}
}
