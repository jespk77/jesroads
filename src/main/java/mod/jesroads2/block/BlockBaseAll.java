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
import net.minecraft.world.World;

public abstract class BlockBaseAll extends BlockBase {

    public static final PropertyDirection direction = PropertyDirection.create("direction");

    public BlockBaseAll(int id, Material mat, String name, CreativeTabs tab) {
        this(id, mat, name, tab, false);
    }

    public BlockBaseAll(int id, Material mat, String name, CreativeTabs tab, boolean subtype) {
        super(id, mat, name, tab, subtype);

        setDefaultState(getDefaultState().withProperty(getProperty("direction"), EnumFacing.DOWN));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(getProperty("direction"), EnumFacing.getFront(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(direction).getIndex();
    }

    @Override
    protected void createProperties(List<IProperty<?>> properties) {
        super.createProperties(properties);
        properties.add(direction);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing enfacing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase entity) {
        return getDefaultState().withProperty(getProperty("direction"), EnumFacing.getDirectionFromEntityLiving(pos, entity));
    }
}