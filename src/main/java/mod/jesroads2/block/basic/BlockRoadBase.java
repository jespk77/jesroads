package mod.jesroads2.block.basic;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBase;
import mod.jesroads2.block.road.BlockRoad;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockRoadBase extends BlockBase {
    public BlockRoadBase(int id) {
        super(id, new Material(MapColor.BLACK), "roadbase", JesRoads2.tabs.basic);

        setHardness(0.6F).setResistance(0.5F);
        setSoundType(SoundType.GROUND);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbor, BlockPos npos) {
        if (world.isBlockFullCube(npos) && npos.equals(pos.up())) {
            IBlockState s = world.getBlockState(npos);
            Block b = s.getBlock();
            if (b == Blocks.AIR) return;
            else if (b == JesRoads2.blocks.roadbase) world.setBlockState(pos, Blocks.DIRT.getDefaultState(), 2);
            else if (!(b instanceof BlockRoad)) world.setBlockState(pos, world.getBlockState(pos.down()), 2);
        }
    }
}