package enetbridge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.common.MinecraftForge;

import adaptors.RfBlockDelegate;
import adaptors.TileEntityDelegate;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;

public class MainLogic {
	protected static BlockPos pos;
	public static void onWorldTickEnd(World world) {
		if (world == null) throw new NullPointerException("tick for null world");

		WorldData worldData = getWorldData(world);

		// remove delegates for removed tes
		for (TileEntity te : worldData.trackingList.getRemoved()) {
			TileEntityDelegate delegate = worldData.enetDelegates.remove(te);

			if (delegate != null) {
				MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(delegate));
			}
		}

		worldData.trackingList.getRemoved().clear();

		// create new delegates from pending tes
		for (TileEntity te : worldData.pendingEnetDelegates) {
			if (te.getWorld() == world && te.getPos() == pos &&
					EnergyNet.instance.getTile(world, pos) == null) {
				TileEntityDelegate enetDelegate = RfBlockDelegate.create(te);

				if (enetDelegate != null) {
					TileEntityDelegate prev = worldData.enetDelegates.put(te, enetDelegate);
					assert prev == null;
					MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(enetDelegate));
				}
			}
		}

		worldData.pendingEnetDelegates.clear();

		// fill pending tes from added tes
		for (TileEntity te : worldData.trackingList.getAdded()) {
			if (RfBlockDelegate.isApplicable(te)) {
				if (EnergyNet.instance.getTile(world, pos) == null) {
					worldData.pendingEnetDelegates.add(te);
				}
			}
		}

		worldData.trackingList.getAdded().clear();

		// tick delegates
		for (TileEntityDelegate delegate : worldData.enetDelegates.values()) {
			delegate.tick();
		}
	}

	protected static void setTrackingList(World world, TrackingList<TileEntity> tl) {
		getWorldData(world).trackingList = tl;
	}

	protected static void onWorldUnload(World world) {
		worldData.remove(world);
	}


	private static WorldData getWorldData(World world) {
		WorldData ret = worldData.get(world);

		if (ret == null) {
			ret = new WorldData();
			worldData.put(world, ret);
		}

		return ret;
	}


	private static class WorldData {
		final Map<TileEntity, TileEntityDelegate> enetDelegates = new HashMap<TileEntity, TileEntityDelegate>();
		final List<TileEntity> pendingEnetDelegates = new ArrayList<TileEntity>();
		TrackingList<TileEntity> trackingList;
	}

	private static final Map<World, WorldData> worldData = new WeakHashMap<World, WorldData>();
}
