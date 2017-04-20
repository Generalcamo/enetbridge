package adaptors;

import net.minecraft.tileentity.TileEntity;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import net.minecraft.util.math.BlockPos;

public abstract class TileEntityDelegate extends TileEntity  implements IEnergySink, IEnergySource {
	public TileEntityDelegate(TileEntity parent) {
		worldObj = parent.getWorld();
		pos = BlockPos.ORIGIN;
	}

	public abstract void tick();

	@Override
	public int getSinkTier() {
		return Integer.MAX_VALUE;
	}

	@Override
	public int getSourceTier() {
		double energy = getOfferedEnergy();

		return energy > 0 ? EnergyNet.instance.getTierFromPower(energy) : 1;
	}
}
