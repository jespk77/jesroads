package mod.jesroads2.block.streetlight;

import java.util.List;
import java.util.Random;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBaseHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockStreetLamp extends BlockBaseHorizontal {
    private static final AxisAlignedBB[] BOX = new AxisAlignedBB[]{
            new AxisAlignedBB(0.3F, 0.35F, 0.25F, 1.F, 0.65F, 0.75F),
            new AxisAlignedBB(0.25F, 0.35F, 0.3F, 0.75F, 0.65F, 1.F),
            new AxisAlignedBB(0.F, 0.35F, 0.25F, 0.7F, 0.65F, 0.75F),
            new AxisAlignedBB(0.25F, 0.35F, 0.F, 0.75F, 0.65F, 0.7F)
    };

    public static final PropertyBool enabled = PropertyBool.create("enabled");

    public BlockStreetLamp() {
        super(-1, new Material(MapColor.GRAY), "streetlamp", null);

        setHardness(2.F).setResistance(5.F);
        setFullCube(false);
        setSoundType(SoundType.METAL);
        setDefaultState(getDefaultState().withProperty(enabled, true));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = super.getStateFromMeta(meta);
        return state.withProperty(enabled, ((meta & 12) >> 2) == 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return super.getMetaFromState(state) | ((state.getValue(enabled) ? 1 : 0) << 2);
    }

    @Override
    protected void createProperties(List<IProperty<?>> properties) {
        super.createProperties(properties);
        properties.add(enabled);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return BOX[state.getValue(facing).getHorizontalIndex()];
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (state.getValue(enabled)) return 15;
        else return 0;
    }

    @Override
    public void onBlockDestroyedByPlayer(World world, BlockPos pos, IBlockState state) {
        EnumFacing dir = state.getValue(facing).rotateYCCW();
        BlockStreetLight.removeBlocksOnPos(world, pos.offset(dir));
        BlockStreetLight.removeBlocksOnPos(world, pos.offset(dir.getOpposite()));
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entity, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) world.setBlockState(pos, state.withProperty(enabled, !state.getValue(enabled)), 2);
        return true;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(JesRoads2.blocks.street_light);
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return new ItemStack(JesRoads2.blocks.street_light, 1);
    }
}