package mod.jesroads2.block.system;

import java.util.List;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBase;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockTrafficlightSupport extends BlockBase {
    public static final PropertyBool post = PropertyBool.create("post"),
            up = PropertyBool.create("up"),
            north = PropertyBool.create("north"),
            south = PropertyBool.create("south"),
            east = PropertyBool.create("east"),
            west = PropertyBool.create("west"),
            snowy = PropertyBool.create("snowy");

    public BlockTrafficlightSupport(int id) {
        super(id, new Material(MapColor.GRAY), "trafficlight_post", JesRoads2.tabs.system);

        setFullCube(false);
        setHardness(3.5F);
        setResistance(10.F);
        setDefaultState(getDefaultState().withProperty(snowy, false)
                .withProperty(post, true)
                .withProperty(up, false)
                .withProperty(north, false)
                .withProperty(south, false)
                .withProperty(east, false)
                .withProperty(west, false));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        float minX = 0.35F, minY = 0.5F, minZ = 0.35F, maxX = 0.65F, maxY = 0.75F, maxZ = 0.65F;
        state = getActualState(state, world, pos);
        if (state.getValue(post)) minY = 0.F;
        if (state.getValue(up)) maxY = 1.F;
        if (state.getValue(east)) maxX = 1.F;
        if (state.getValue(west)) minX = 0.F;
        if (state.getValue(north)) minZ = 0.F;
        if (state.getValue(south)) maxZ = 1.F;
        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState();
    }

    @Override
    protected void createProperties(List<IProperty<?>> properties) {
        super.createProperties(properties);
        properties.add(snowy);
        properties.add(post);
        properties.add(up);
        properties.add(north);
        properties.add(south);
        properties.add(east);
        properties.add(west);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        boolean connectU = canConnect(world, pos.up()), connectN = canConnect(world, pos.north()), connectS = canConnect(world, pos.south()),
                connectE = canConnect(world, pos.east()), connectW = canConnect(world, pos.west());
        int count = trueCount(connectN, connectS, connectE, connectW);
        boolean connectP = count == 2 ? !((connectN && connectS && !connectE && !connectW) || (!connectN && !connectS && connectE && connectW)) : count != 1;
        boolean snow = world.getBlockState(pos.north()).getBlock() == Blocks.SNOW_LAYER || world.getBlockState(pos.south()).getBlock() == Blocks.SNOW_LAYER ||
                world.getBlockState(pos.east()).getBlock() == Blocks.SNOW_LAYER || world.getBlockState(pos.west()).getBlock() == Blocks.SNOW_LAYER;
        return state.withProperty(snowy, snow)
                .withProperty(up, connectU)
                .withProperty(north, connectN)
                .withProperty(south, connectS)
                .withProperty(east, connectE)
                .withProperty(west, connectW)
                .withProperty(post, world.getBlockState(pos.down()).isBlockNormalCube() || canConnect(world, pos.down()));
    }

    private int trueCount(boolean... args) {
        int count = 0;
        for (boolean b : args)
            if (b) count++;
        return count;
    }

    private boolean canConnect(IBlockAccess world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        return block instanceof BlockTrafficlightSupport || block instanceof BlockTrafficlight;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entity, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ) {
        Block block = stack != null ? Block.getBlockFromItem(stack.getItem()) : null;
        if (block instanceof BlockTrafficlight) {
            if (!world.isRemote) {
                BlockTrafficlight light = (BlockTrafficlight) block;
                world.setBlockState(pos, JesRoads2.blocks.traffic_light[light.type.id].getDefaultState().withProperty(BlockTrafficlight.facing, entity.getHorizontalFacing().getOpposite()), 3);
                entity.addStat(StatList.getObjectUseStats(light.item));
            }
            return true;
        } else return false;
    }
}