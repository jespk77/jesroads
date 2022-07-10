package mod.jesroads2.block.basic;

import java.util.List;

import javax.annotation.Nullable;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBase;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockFloodlightBeam extends BlockBase {
    private static final int maxHeight = 8;
    public static final PropertyInteger position = PropertyInteger.create("position", 0, maxHeight - 1);
    public static final PropertyBool enabled = PropertyBool.create("enabled");

    public BlockFloodlightBeam() {
        super(-1, Material.AIR, "floodlight_beam", null);

        setFullCube(false);
        setDefaultState(getDefaultState().withProperty(BlockFloodlightBeam.enabled, false).withProperty(BlockFloodlightBeam.position, 0));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(BlockFloodlightBeam.position, (meta >> 1)).withProperty(BlockFloodlightBeam.enabled, (meta & 1) == 1 ? true : false);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return (state.getValue(BlockFloodlightBeam.enabled) ? 1 : 0) | (state.getValue(BlockFloodlightBeam.position) << 1);
    }

    @Override
    protected void createProperties(List<IProperty<?>> properties) {
        super.createProperties(properties);
        properties.add(BlockFloodlightBeam.position);
        properties.add(BlockFloodlightBeam.enabled);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbor, BlockPos npos) {
        IBlockState nstate = world.getBlockState(npos);
        if (npos.equals(pos.up())) {
            if (nstate.getBlock() == neighbor) {
                try {
                    state = state.withProperty(BlockFloodlightBeam.enabled, nstate.getValue(BlockFloodlight.enabled));
                    world.setBlockState(pos, state, 3);
                } catch (Exception ignored) {
                }
            } else if (neighbor == this || neighbor == JesRoads2.blocks.floodlight) world.setBlockToAir(pos);
        } else if (npos.equals(pos.down())) {
            int p = state.getValue(BlockFloodlightBeam.position);
            if (neighbor != this && world.isAirBlock(npos) && p < maxHeight)
                world.setBlockState(npos, state.withProperty(BlockFloodlightBeam.position, p + 1), 3);
        }
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        int p = state.getValue(BlockFloodlightBeam.position);
        if (p < maxHeight) {
            BlockPos down = pos.down();
            if (world.isAirBlock(down)) {
                world.setBlockState(down, state.withProperty(BlockFloodlightBeam.position, p), 3);
            }
        }
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getValue(BlockFloodlightBeam.enabled) ? 15 : 0;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess world, BlockPos pos) {
        return null;
    }

    @Override
    public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid) {
        return false;
    }

    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }

    @Override
    public boolean isAir(IBlockState state, IBlockAccess world, BlockPos pos) {
        return true;
    }
}