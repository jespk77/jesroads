package mod.jesroads2.entity.model.building;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBaseHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockElevator extends BlockBaseHorizontal implements ITileEntityProvider {
	public BlockElevator(int id){
		super(id, Material.ROCK, "elevator", JesRoads2.tabs.basic);

		this.setHardness(1.F).setResistance(1.F);
		JesRoads2.ensureDevelopmentMode("elevator");
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return null;
	}
}