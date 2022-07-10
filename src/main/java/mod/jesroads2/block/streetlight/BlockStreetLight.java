package mod.jesroads2.block.streetlight;

import java.util.List;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBaseHorizontal;
import mod.jesroads2.util.IBlockSwitchable;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockStreetLight extends BlockBaseHorizontal implements IBlockSwitchable {
    public static final Material mat = new Material(MapColor.GRAY);
    private static final AxisAlignedBB BOX_POST = new AxisAlignedBB(0.35F, 0.F, 0.35F, 0.65F, 1.F, 0.65F);

    private static class BlockStreetLightPiece {
        public final Vec3i offset;
        public final EnumPart part;
        public final boolean reversed;

        public BlockStreetLightPiece(Vec3i offset, EnumPart part, boolean reversed) {
            this.offset = offset;
            this.part = part;
            this.reversed = reversed;
        }
    }

    public static final BlockStreetLightPiece[] singleX = new BlockStreetLightPiece[]{
            new BlockStreetLightPiece(new Vec3i(0, 0, 0), EnumPart.POST, false), new BlockStreetLightPiece(new Vec3i(0, 1, 0), EnumPart.POST, false),
            new BlockStreetLightPiece(new Vec3i(0, 2, 0), EnumPart.POST, false), new BlockStreetLightPiece(new Vec3i(0, 3, 0), EnumPart.POST_SINGLE, false),
            new BlockStreetLightPiece(new Vec3i(0, 4, 1), EnumPart.POST_LAMP, false), new BlockStreetLightPiece(new Vec3i(0, 4, 2), null, false)
    };
    public static final BlockStreetLightPiece[] singleZ = new BlockStreetLightPiece[]{
            new BlockStreetLightPiece(new Vec3i(0, 0, 0), EnumPart.POST, false), new BlockStreetLightPiece(new Vec3i(0, 1, 0), EnumPart.POST, false),
            new BlockStreetLightPiece(new Vec3i(0, 2, 0), EnumPart.POST, false), new BlockStreetLightPiece(new Vec3i(0, 3, 0), EnumPart.POST_SINGLE, false),
            new BlockStreetLightPiece(new Vec3i(1, 4, 0), EnumPart.POST_LAMP, false), new BlockStreetLightPiece(new Vec3i(2, 4, 0), null, false)
    };

    public static final BlockStreetLightPiece[] doubleX = new BlockStreetLightPiece[]{
            new BlockStreetLightPiece(new Vec3i(0, 0, 0), EnumPart.POST, false), new BlockStreetLightPiece(new Vec3i(0, 1, 0), EnumPart.POST, false),
            new BlockStreetLightPiece(new Vec3i(0, 2, 0), EnumPart.POST, false), new BlockStreetLightPiece(new Vec3i(0, 3, 0), EnumPart.POST_SPLIT, false),
            new BlockStreetLightPiece(new Vec3i(0, 4, 1), EnumPart.POST_LAMP, false), new BlockStreetLightPiece(new Vec3i(0, 4, 2), null, false),
            new BlockStreetLightPiece(new Vec3i(0, 4, -1), EnumPart.POST_LAMP, true), new BlockStreetLightPiece(new Vec3i(0, 4, -2), null, true)
    };
    public static final BlockStreetLightPiece[] doubleZ = new BlockStreetLightPiece[]{
            new BlockStreetLightPiece(new Vec3i(0, 0, 0), EnumPart.POST, false), new BlockStreetLightPiece(new Vec3i(0, 1, 0), EnumPart.POST, false),
            new BlockStreetLightPiece(new Vec3i(0, 2, 0), EnumPart.POST, false), new BlockStreetLightPiece(new Vec3i(0, 3, 0), EnumPart.POST_SPLIT, false),
            new BlockStreetLightPiece(new Vec3i(1, 4, 0), EnumPart.POST_LAMP, false), new BlockStreetLightPiece(new Vec3i(2, 4, 0), null, false),
            new BlockStreetLightPiece(new Vec3i(-1, 4, 0), EnumPart.POST_LAMP, true), new BlockStreetLightPiece(new Vec3i(-2, 4, 0), null, true)
    };

    public enum EnumPart implements IStringSerializable {
        POST,
        POST_SPLIT,
        POST_SINGLE,
        POST_LAMP;

        public static EnumPart fromMeta(int meta) {
            EnumPart[] values = EnumPart.values();
            if (meta > 0 && meta < values.length) return values[meta];
            else return values[0];
        }

        public BlockPos getNextPos(BlockPos pos, EnumFacing dir, boolean opposite) {
            switch (this) {
                case POST_SPLIT:
                    return opposite ? pos.up().offset(dir.getOpposite()) : pos.up().offset(dir);
                case POST_LAMP:
                    return opposite ? pos.offset(dir.getOpposite()) : pos.offset(dir);
                case POST_SINGLE:
                    return pos.up().offset(dir.getOpposite());
                default:
                    return pos.up();
            }
        }

        public BlockPos getPrevPos(BlockPos pos, EnumFacing dir, boolean opposite) {
            switch (this) {
                case POST_LAMP:
                    return opposite ? pos.down().offset(dir) : pos.down().offset(dir.getOpposite());
                default:
                    return pos.down();
            }
        }

        @Override
        public String getName() {
            return name().toLowerCase();
        }
    }

    public static final PropertyEnum<EnumPart> part = PropertyEnum.create("part", EnumPart.class);

    public BlockStreetLight(int id) {
        super(id, mat, "streetlight", JesRoads2.tabs.road_extra, true);

        setHardness(5.F).setResistance(10.F);
        setFullCube(false);
        setSoundType(SoundType.METAL);
        setVariantCount(2);
        setDefaultState(getDefaultState().withProperty(part, EnumPart.POST));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = super.getStateFromMeta(meta);
        return state.withProperty(part, EnumPart.fromMeta((meta & 12) >> 2));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return super.getMetaFromState(state) | (state.getValue(part).ordinal() << 2);
    }

    @Override
    public void createProperties(List<IProperty<?>> properties) {
        super.createProperties(properties);
        properties.add(part);
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return world.getBlockState(pos.down()).isBlockNormalCube();
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing enfacing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase entity) {
        IBlockState state = getDefaultState().withProperty(facing, entity.getHorizontalFacing().getOpposite());
        ItemStack stack = entity.getHeldItemMainhand();
        if (stack == null || stack.getItem() != item) stack = entity.getHeldItemOffhand();

        if (stack != null && stack.getItem() == item)
            if (placeBlocksOnPos(world, pos.up(), state, stack.getItemDamage() == 0)) return state;
        return Blocks.AIR.getDefaultState();
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        float minX = 0.F, minY = 0.F, minZ = 0.35F, maxX = 1.F, maxY = 1.F, maxZ = 0.65F;
        EnumFacing face = state.getValue(facing);
        switch (state.getValue(part)) {
            case POST:
                return BOX_POST;
            case POST_SINGLE: {
                maxX = 0.65F;
                if (face.getHorizontalIndex() > 1) {
                    minX = 0.35F;
                    maxX = 1.F;
                }
                break;
            }
            case POST_LAMP: {
                maxY = 0.6F;
                break;
            }
            default:
                break;
        }

        if (state.getValue(facing).getAxis() == Axis.X) {
            float temp = minX;
            minX = minZ;
            minZ = temp;
            temp = maxX;
            maxX = maxZ;
            maxZ = temp;
        }
        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> stack) {
        stack.add(new ItemStack(this, 1, 0));
        stack.add(new ItemStack(this, 1, 1));
    }

    @Override
    public ItemStack getSwitchBlock(ItemStack current) {
        return new ItemStack(this, 1, current.getItemDamage() == 0 ? 1 : 0);
    }

    @Override
    public void onBlockDestroyedByPlayer(World world, BlockPos pos, IBlockState state) {
        removeBlocksOnPos(world, pos, state);
    }

    private static BlockPos[] getStructureFrom(BlockPos pos, EnumFacing dir, boolean single) {
        BlockPos split = pos.up(3), top = split.up().offset(dir);
        if (!single) {
            BlockPos top2 = split.up().offset(dir.getOpposite());
            return new BlockPos[]{pos, pos.up(), pos.up(2), split, top, top2, top.offset(dir), top2.offset(dir.getOpposite())};
        } else return new BlockPos[]{pos, pos.up(), pos.up(2), split, top, top.offset(dir)};
    }

    private static boolean canPlaceBlocksOnPos(World world, BlockPos pos, BlockStreetLightPiece[] structure, boolean turned) {
        for (BlockStreetLightPiece block : structure) {
            int x = block.offset.getX(), y = block.offset.getY(), z = block.offset.getZ();
            BlockPos p = pos.add(turned ? -x : x, y, turned ? -z : z);
            if (!world.isAirBlock(p)) return false;
        }
        return true;
    }

    public static boolean placeBlocksOnPos(World world, BlockPos pos, IBlockState state, boolean single) {
        EnumFacing face = state.getValue(facing);
        boolean turned = false;
        if (!single) face = face.getAxis() == EnumFacing.Axis.X ? EnumFacing.EAST : EnumFacing.NORTH;
        else turned = face == EnumFacing.WEST || face == EnumFacing.SOUTH;

        BlockStreetLightPiece[] structure = single ? (face.getAxis() == EnumFacing.Axis.X ? singleX : singleZ) : (face.getAxis() == EnumFacing.Axis.X ? doubleX : doubleZ);
        if (canPlaceBlocksOnPos(world, pos, structure, turned)) {
            for (BlockStreetLightPiece block : structure) {
                int x = block.offset.getX(), y = block.offset.getY(), z = block.offset.getZ();
                BlockPos p = pos.add(turned ? -x : x, y, turned ? -z : z);
                if (block.part != null)
                    world.setBlockState(p, state.withProperty(part, block.part).withProperty(facing, block.reversed ? face.getOpposite() : face), 2);
                else
                    world.setBlockState(p, JesRoads2.blocks.street_lamp.getDefaultState().withProperty(facing, block.reversed ? face.getOpposite() : face), 2);
            }
            return true;
        }
        return false;
    }

    public static void removeBlocksOnPos(World world, BlockPos pos) {
        removeBlocksOnPos(world, pos, world.getBlockState(pos));
    }

    public static void removeBlocksOnPos(World world, BlockPos pos, IBlockState state) {
        Block b = state.getBlock();
        if (b == JesRoads2.blocks.street_light) {
            world.setBlockToAir(pos);
            EnumPart p = state.getValue(part);
            EnumFacing dir = state.getValue(facing).rotateYCCW();
            removeBlocksOnPos(world, p.getNextPos(pos, dir, false));
            removeBlocksOnPos(world, p.getNextPos(pos, dir, true));
            removeBlocksOnPos(world, p.getPrevPos(pos, dir, false));
            removeBlocksOnPos(world, p.getPrevPos(pos, dir, true));
        } else if (b == JesRoads2.blocks.street_lamp) world.setBlockToAir(pos);
    }
}