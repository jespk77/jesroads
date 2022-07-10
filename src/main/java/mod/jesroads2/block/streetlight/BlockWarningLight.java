package mod.jesroads2.block.streetlight;

import java.util.List;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBaseHorizontal;
import mod.jesroads2.block.sign.BlockSign;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockWarningLight extends BlockBaseHorizontal {
    public static final PropertyBool left = PropertyBool.create("left"), enabled = PropertyBool.create("enabled"), side = PropertyBool.create("side");

    public BlockWarningLight(int id) {
        super(id, new Material(MapColor.BLACK), "warning_light", JesRoads2.tabs.road_extra);

        setHardness(0F).setResistance(0.1F);
        setFullCube(false);
        setDefaultState(getDefaultState().withProperty(left, true).withProperty(enabled, false).withProperty(side, false));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return super.getMetaFromState(state) | ((state.getValue(left) ? 1 : 0) << 2) | ((state.getValue(enabled) ? 1 : 0) << 3);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return super.getStateFromMeta(meta & 3).withProperty(left, ((meta & 4) >> 2) == 1).withProperty(enabled, ((meta & 8) >> 3) == 1);
    }

    @Override
    protected void createProperties(List<IProperty<?>> properties) {
        super.createProperties(properties);
        properties.add(left);
        properties.add(enabled);
        properties.add(side);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (!state.getValue(enabled)) {
            try {
                return state.withProperty(side, world.getBlockState(pos.offset(state.getValue(left) ? (state.getValue(facing).rotateY()) : (state.getValue(facing).rotateYCCW()))).getValue(enabled));
            } catch (Exception ignored) {
            }
        }
        return state.withProperty(side, false);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entity, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (side.getOpposite() == state.getValue(facing)) {
            world.setBlockState(pos, state.withProperty(enabled, !state.getValue(enabled)), 2);
            return true;
        } else return false;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
        world.setBlockState(pos.offset(state.getValue(facing).rotateY()), getDefaultState().withProperty(facing, state.getValue(facing)).withProperty(left, false), 2);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {

    }

    @Override
    public void onBlockDestroyedByPlayer(World world, BlockPos pos, IBlockState state) {
        world.setBlockToAir(pos.offset(state.getValue(left) ? state.getValue(facing).rotateY() : state.getValue(facing).rotateYCCW()));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return BlockSign.box[state.getValue(facing).getAxis() == EnumFacing.Axis.Z ? 0 : 1];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getPackedLightmapCoords(IBlockState state, IBlockAccess source, BlockPos pos) {
        return 240;
    }
}