package mod.jesroads2.block.sign;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBaseHorizontal;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockRoadSign extends BlockBaseHorizontal {
    public BlockRoadSign(int id, String name) {
        super(id, Material.IRON, name, JesRoads2.tabs.sign, true);

        setHardness(0.5F);
        setFullCube(false);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        float minX, maxX, minZ, maxZ;
        switch (state.getValue(facing)) {
            case SOUTH: {
                minX = 0.F;
                maxX = 1.F;
                minZ = 0.9F;
                maxZ = 1.F;
                break;
            }
            case WEST: {
                minX = 0.F;
                maxX = 0.1F;
                minZ = 0.F;
                maxZ = 1.F;
                break;
            }
            case EAST: {
                minX = 0.9F;
                maxX = 1.F;
                minZ = 0.F;
                maxZ = 1.F;
                break;
            }
            default: {
                minX = 0.F;
                maxX = 1.F;
                minZ = 0.F;
                maxZ = 0.1F;
                break;
            }
        }
        return new AxisAlignedBB(minX, 0.F, minZ, maxX, 1.F, maxZ);
    }

    protected static boolean shouldPlacePost(World world, BlockPos pos) {
        return !(world.getBlockState(pos.down()).getBlock() instanceof BlockRoadSign);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbor, BlockPos npos) {
        EnumFacing face = state.getValue(facing);
        if (!isSupported(world, pos, face)) world.setBlockToAir(pos);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state);
    }

    protected boolean isSupported(World world, BlockPos pos, EnumFacing face) {
        return isSolid(world, pos.down()) || isSolid(world, pos.offset(face));
    }

    protected boolean isSolid(IBlockAccess world, BlockPos pos) {
        return world.getBlockState(pos).getMaterial().isSolid();
    }
}