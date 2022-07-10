package mod.jesroads2.block.system;

import java.util.List;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.JesRoads2Block;
import mod.jesroads2.block.BlockBase;
import mod.jesroads2.block.sign.BlockSign;
import mod.jesroads2.tileentity.TileEntityRoadSign;
import mod.jesroads2.util.EnumFacingDiagonal;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockFreewaySupport extends BlockBase {
    public static final PropertyBool north = PropertyBool.create("north");
    public static final PropertyBool north_west = PropertyBool.create("north_west");
    public static final PropertyBool north_east = PropertyBool.create("north_east");

    public static final PropertyBool south = PropertyBool.create("south");
    public static final PropertyBool south_west = PropertyBool.create("south_west");
    public static final PropertyBool south_east = PropertyBool.create("south_east");

    public static final PropertyBool east = PropertyBool.create("east");
    public static final PropertyBool west = PropertyBool.create("west");
    public static final PropertyBool post = PropertyBool.create("post");
    public static final PropertyBool snowy = PropertyBool.create("snowy");

    public BlockFreewaySupport(int id) {
        super(id, new Material(MapColor.IRON), "sign_support", JesRoads2.tabs.system);

        setHardness(3.5F).setResistance(25.F);
        setFullCube(false);
        setDefaultState(getDefaultState().withProperty(snowy, false)
                .withProperty(north, false)
                .withProperty(north_west, false)
                .withProperty(north_east, false)
                .withProperty(south, false)
                .withProperty(south_west, false)
                .withProperty(south_east, false)
                .withProperty(east, false)
                .withProperty(west, false)
                .withProperty(post, true));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        state = getActualState(state, world, pos);
        float minX = 0.3F, minY = 0.F, minZ = 0.3F, maxX = 0.7F, maxY = 1.F, maxZ = 0.7F;
        if (state.getValue(north)) minZ = 0.F;
        if (state.getValue(south)) maxZ = 1.F;
        if (state.getValue(west)) minX = 0.F;
        if (state.getValue(east)) maxX = 1.F;

        if (state.getValue(north_west)) {
            minX = 0.F;
            minZ = 0.F;
        }
        if (state.getValue(north_east)) {
            maxX = 1.F;
            minZ = 0.F;
        }
        if (state.getValue(south_west)) {
            minX = 0.F;
            maxZ = 1.F;
        }
        if (state.getValue(south_east)) {
            maxX = 1.F;
            maxZ = 1.F;
        }

        if (!state.getValue(post)) maxY = 0.9F;
        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    protected void createProperties(List<IProperty<?>> properties) {
        super.createProperties(properties);
        properties.add(north);
        properties.add(north_west);
        properties.add(north_east);
        properties.add(south);
        properties.add(south_west);
        properties.add(south_east);
        properties.add(east);
        properties.add(west);
        properties.add(snowy);
        properties.add(post);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        BlockPos pnorth = pos.north(), psouth = pos.south();
        boolean n = canConnectTo(world, pnorth);
        boolean nw = canConnectTo(world, pnorth.west());
        boolean ne = canConnectTo(world, pnorth.east());
        boolean s = canConnectTo(world, psouth);
        boolean sw = canConnectTo(world, psouth.west());
        boolean se = canConnectTo(world, psouth.east());
        boolean e = canConnectTo(world, pos.east());
        boolean w = canConnectTo(world, pos.west());
        boolean down = canConnectTo(world, pos.down()) || canConnectTo(world, pos.up());

        int count = trueCount(n, nw, ne, s, sw, se, e, w);
        boolean hasPost;
        if (!down && count == 2) hasPost = !(n && s || e && w || nw && se || ne && sw);
        else hasPost = true;

        boolean snow = world.getBlockState(pos.north()).getBlock() == Blocks.SNOW_LAYER || world.getBlockState(pos.south()).getBlock() == Blocks.SNOW_LAYER ||
                world.getBlockState(pos.east()).getBlock() == Blocks.SNOW_LAYER || world.getBlockState(pos.west()).getBlock() == Blocks.SNOW_LAYER;
        return state.withProperty(snowy, snow)
                .withProperty(north, n)
                .withProperty(north_west, nw)
                .withProperty(north_east, ne)
                .withProperty(south, s)
                .withProperty(south_west, sw)
                .withProperty(south_east, se)
                .withProperty(east, e)
                .withProperty(west, w)
                .withProperty(post, hasPost);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entity, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (GuiScreen.isAltKeyDown() && stack.getItem() == JesRoads2Block.getInstance().freewaysupport.item) {
            generateSupports(world, pos, EnumFacingDiagonal.fromEntityF(entity));
            return true;
        }

        Block b = Block.getBlockFromItem(stack.getItem());
        boolean ps = getActualState(state, world, pos).getValue(post);
        if (b instanceof IFreewaySupportable) {
            if (!ps) {
                world.setBlockState(pos, getBlockFromFacing(entity, ((IFreewaySupportable) b).getDirectionalBlocks()).getDefaultState(), 2);
                entity.addStat(StatList.getObjectUseStats(((BlockBase) b).item));
            }
            return true;
        } else if (b instanceof BlockSign) {
            if (!ps && !((BlockSign) b).type.isHighway()) {
                world.setBlockState(pos, b.getStateForPlacement(world, pos, entity.getHorizontalFacing(), hitX, hitY, hitZ, 0, entity), 3);
                if (stack.hasTagCompound()) {
                    TileEntityRoadSign sign = new TileEntityRoadSign();
                    sign.readFromNBT(stack.getSubCompound("BlockEntityTag"));
                    world.setTileEntity(pos, sign);
                }
                entity.addStat(StatList.getObjectUseStats(((BlockSign) b).item));
            }
            return true;
        } else return false;
    }

    private void generateSupports(World world, BlockPos pos, EnumFacingDiagonal facing) {
        if (world.isRemote) return;

        MutableBlockPos p = new MutableBlockPos(pos), left = new MutableBlockPos(pos);
        int count = 0;
        EnumFacing f = facing.getFacing().rotateYCCW();
        moveLeft(left, facing);
        while (count < JesRoads2.options.other.freeway_support_max_length && world.getBlockState(left).getBlock() != this) {
            moveLeft(left, facing);
            count++;
        }

        if (count < JesRoads2.options.other.freeway_support_max_length) {
            int height = 4;
            int width = MathHelper.floor(BlockBase.calculateDistance(pos, left)) - 1;
            if (facing.isDiagonal()) width -= 2;

            p.move(EnumFacing.UP);
            left.move(EnumFacing.UP);
            for (int i = 0; i < height; i++) {
                world.setBlockState(p, getDefaultState(), 3);
                world.setBlockState(left, getDefaultState(), 3);

                p.move(EnumFacing.UP);
                left.move(EnumFacing.UP);
            }
            p.move(EnumFacing.DOWN);
            moveLeft(p, facing);
            count = 0;
            while (count < width && world.getBlockState(p).getBlock() != this) {
                world.setBlockState(p, getDefaultState(), 3);
                moveLeft(p, facing);
                count++;
            }
        }
    }

    private void moveLeft(MutableBlockPos pos, EnumFacingDiagonal facing) {
        if (facing.isDiagonal()) pos.move(facing.getFacing());
        pos.move(facing.getFacing().rotateYCCW());
    }

    private BlockBase getBlockFromFacing(EntityPlayer player, BlockBase[] directionalBlocks) {
        EnumFacingDiagonal facing = EnumFacingDiagonal.fromEntityF(player);
        return directionalBlocks[facing.getIndex()];
    }

    private int trueCount(boolean... args) {
        int count = 0;
        for (boolean b : args)
            if (b) count++;
        return count;
    }

    private boolean canConnectTo(IBlockAccess world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        if (block instanceof BlockSign) return ((BlockSign) block).type.shouldConnect();
        else
            return block instanceof BlockFreewaySupport || block instanceof BlockDynamicSign || block instanceof BlockEventSign;
    }
}