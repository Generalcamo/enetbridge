package enetbridge;

import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

public class EventCallback {
	public static void load() {
		if (EnetBridge.getEnableTileEntityAdaption() &&
				EnetBridge.hasRf()) {
			EventCallback cb = new EventCallback();

			MinecraftForge.EVENT_BUS.register(cb);
		}
	}

	private EventCallback() {
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onWorldTick(TickEvent.WorldTickEvent event) {
		if (event.phase != Phase.END) return;

		MainLogic.onWorldTickEnd(event.world);
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if (event.getWorld().isRemote) {
			@SuppressWarnings("unchecked")
			TrackingList<TileEntity> tl = new TrackingList<TileEntity>(event.getWorld().loadedTileEntityList);
			MainLogic.setTrackingList(event.getWorld(), tl);
		}
	}

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		MainLogic.onWorldUnload(event.getWorld());
	}
}
