package mod.jesroads2.block;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockBaseHorizontal extends BlockBase {

    public static final PropertyDirection facing = PropertyDirection.create("direction", EnumFacing.Plane.HORIZONTAL);

    public BlockBaseHorizontal(int id, Material mat, String name, CreativeTabs tab) {
        this(id, mat, name, tab, false);
    }

    public BlockBaseHorizontal(int id, Material mat, String name, CreativeTabs tab, boolean subtype) {
        super(id, mat, name, tab, subtype);

        setDefaultState(getDefaultState().withProperty(getProperty("direction"), EnumFacing.SOUTH));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(getProperty("direction"), EnumFacing.getHorizontal(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        EnumFacing face = state.getValue(getProperty("direction"));
        if (face.getAxis() == EnumFacing.Axis.Y)
            face = EnumFacing.NORTH;
        return face.getHorizontalIndex();
    }

    @Override
    protected void createProperties(List<IProperty<?>> properties) {
        super.createProperties(properties);
        properties.add(facing);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing enfacing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase entity) {
        return getDefaultState().withProperty(getProperty("direction"), entity.getHorizontalFacing());
    }

    protected boolean isNorthSouth(IBlockAccess world, BlockPos pos) {
        return isNorthSouth(world.getBlockState(pos));
    }

    protected boolean isNorthSouth(IBlockState state) {
        EnumFacing face = state.getValue(getProperty("direction"));
        return face == EnumFacing.NORTH || face == EnumFacing.SOUTH;
    }
}