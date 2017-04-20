package adaptors;

import java.util.EnumSet;
import java.util.Set;

import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyEmitter;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.MinecraftForge;

//import cofh.api.energy.IEnergyConnection;
//import cofh.api.energy.IEnergyProvider;
//import cofh.api.energy.IEnergyReceiver;
import enetbridge.EnetBridge;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;

public class RfBlockDelegate extends TileEntityDelegate {
	public static TileEntityDelegate create(TileEntity te) {
		if (!isApplicable(te)) return null;

		try {
			return new RfBlockDelegate(te);
		} catch (Exception e) {
			EnetBridge.log.warn("RF delegate init error: {}.", e.getMessage());
			return null;
		}
	}

	public static boolean isApplicable(TileEntity te) {
		return EnetBridge.hasRf();// && (te instanceof IEnergyProvider || te instanceof IEnergyReceiver);
	}

	private RfBlockDelegate(TileEntity base) {
		super(base);

		this.base = (IEnergyStorage) base;

		if (base instanceof IEnergyStorage) {
			this.provider = (IEnergyStorage) base;
		} else {
			this.provider = null;
		}

		if (base instanceof IEnergyStorage) {
			this.receiver = (IEnergyStorage) base;
		} else {
			this.receiver = null;
		}

		updateDirs(false);
	}

	@Override
	public void tick() {
		if (!faulty && updateDirs(true)) {
			// refresh connectivity
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
			updateDirs(false);
			MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
		}
	}

	@Override
	public boolean acceptsEnergyFrom(IEnergyEmitter emitter, EnumFacing direction) {
		return !faulty && receiver != null && sinkDirs.contains(direction);
	}

	@Override
	public boolean emitsEnergyTo(IEnergyAcceptor receiver, EnumFacing direction) {
		return !faulty && provider != null && sourceDirs.contains(direction);
	}

	@Override
	public double getDemandedEnergy() {
		if (faulty) return 0;
		if (receiver == null) return 0;

		int max = 0;

		for (EnumFacing dir : sinkDirs) {
			int amount = receiver.receiveEnergy(Integer.MAX_VALUE, true);
			if (amount > max) max = amount;
		}

		return max * 1. / EnetBridge.getRfPerEu();
	}

	@Override
	public double getOfferedEnergy() {
		if (faulty) return 0;
		if (provider == null) return 0;

		int max = 0;

		for (EnumFacing dir : sourceDirs) {
			int amount = provider.extractEnergy(Integer.MAX_VALUE, true);
			if (amount > max) max = amount;
		}

		return max * EnetBridge.getEuPerRf();
	}

	@Override
	public double injectEnergy(EnumFacing directionFrom, double amount, double voltage) {
		if (receiver == null) return amount;

		amount *= EnetBridge.getRfPerEu();
		int iAmount = (int) amount;
		if (iAmount <= 0) return amount;

		amount -= iAmount;

		if (directionFrom != null) {
			iAmount -= receiver.receiveEnergy(iAmount, false);
		}

		for (EnumFacing dir : sinkDirs) {
			if (iAmount <= 0) break;
			iAmount -= receiver.receiveEnergy(iAmount, false);
		}

		return (amount + iAmount) * 1. / EnetBridge.getRfPerEu();
	}

	@Override
	public void drawEnergy(double amount) {
		if (provider != null) {
			for (EnumFacing dir : sourceDirs) {
				amount -= provider.extractEnergy((int) Math.ceil(amount / EnetBridge.getEuPerRf()), false) * EnetBridge.getEuPerRf();
				if (amount <= 0) break;
			}
		}

		if (amount > 0) {
			EnetBridge.log.warn("Can't draw the full amount from {}, {} left over, disabling the delegate.", base, amount);
			faulty = true;
		}
	}

	private boolean updateDirs(boolean simulate) {
		EnumSet<EnumFacing> newSinkdirs = EnumSet.noneOf(EnumFacing.class);
		EnumSet<EnumFacing> newSourcedirs = EnumSet.noneOf(EnumFacing.class);

		// probe directions if they are connectable and can source or sink energy
		for (EnumFacing dir : EnumFacing.VALUES) {

			if (receiver != null && EnetBridge.getRfPerEu() > 0) { // sink
				if (receiver.getEnergyStored() >= receiver.getMaxEnergyStored() || // be optimistic if we can't check
						receiver.receiveEnergy(Integer.MAX_VALUE, true) > 0) {
					newSinkdirs.add(dir);
				}
			}

			if (provider != null && EnetBridge.getEuPerRf() > 0) { // source
				if (provider.getEnergyStored() <= 0 ||  // be optimistic if we can't check
						provider.extractEnergy(Integer.MAX_VALUE, true) > 0) {
					newSourcedirs.add(dir);
				}
			}
		}

		if (sinkDirs.equals(newSinkdirs) && sourceDirs.equals(newSourcedirs)) {
			return false;
		} else {
			if (!simulate) {
				sinkDirs.clear();
				sinkDirs.addAll(newSinkdirs);

				sourceDirs.clear();
				sourceDirs.addAll(newSourcedirs);
			}

			//EnetBridge.log.debug("Detected new dirs for {}: {}, {}.", base, sinkDirs, sourceDirs);

			return true;
		}
	}


	private final IEnergyStorage base;
	private final IEnergyStorage provider;
	private final IEnergyStorage receiver;
	private boolean faulty = false;
	private final Set<EnumFacing> sinkDirs = EnumSet.noneOf(EnumFacing.class);
	private final Set<EnumFacing> sourceDirs = EnumSet.noneOf(EnumFacing.class);
}
