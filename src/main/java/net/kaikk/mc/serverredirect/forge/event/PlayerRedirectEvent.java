package net.kaikk.mc.serverredirect.forge.event;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.SERVER)
@Cancelable
public class PlayerRedirectEvent extends Event {
	protected final EntityPlayerMP player;
	protected final String address;
	
	public PlayerRedirectEvent(EntityPlayerMP player, String address) {
		this.player = player;
		this.address = address;
	}

	public EntityPlayerMP getPlayer() {
		return player;
	}

	public String getAddress() {
		return address;
	}
}
