package mod.jesroads2.block.road;

import java.util.List;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBase;
import mod.jesroads2.tileentity.TileEntityRoadSlope;
import mod.jesroads2.util.IBlockSwitchable;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockRoadSlope extends BlockRoad implements ITileEntityProvider, IBlockSwitchable {
    public enum EnumSlopeType implements IRoadType {
        BLANK(0, "roadslope", null, false),
        WHITE_SIDE(1, "roadslope_whiteside", "line_white", false),
        WHITE_MIDDLE(2, "roadslope_whitemiddle", "line_white", false),
        YELLOW_SIDE(3, "roadslope_yellowside", "line_yellow", true);

        public final int id;
        public final String name, tname;
        public final boolean inverted;

        EnumSlopeType(int index, String valueName, String textureName, boolean isInverted) {
            id = index;
            name = valueName;
            tname = textureName;
            inverted = isInverted;
        }

        public int getID() {
            return id;
        }

        public String getName() {
            return name;
        }

        public boolean isInverted() {
            return inverted;
        }
    }

    public enum EnumSlopeShape implements IStringSerializable {
        UNKNOWN(0, "unknown", 0.F),
        SINGLE(1, "single", 1.F),
        DOUBLE_TOP(2, "double_top", 1.F),
        DOUBLE_BOTTOM(3, "double_bottom", 0.5F);

        public final int id;
        public final String name;
        public final float height;
        public static final EnumSlopeShape[] list = new EnumSlopeShape[values().length];

        EnumSlopeShape(int index, String valueName, float blockHeight) {
            id = index;
            name = valueName;
            height = blockHeight;
        }

        public static EnumSlopeShape fromMeta(int meta) {
            if (meta > 0 && meta < list.length) return list[meta];
            else return UNKNOWN;
        }

        public static boolean isNormal(EnumSlopeShape t) {
            return t.id < 2;
        }

        @Override
        public String getName() {
            return name;
        }

        static {
            for (EnumSlopeShape type : values()) {
                list[type.id] = type;
            }
        }
    }

    public static final AxisAlignedBB HALF_BLOCK_AABB = new AxisAlignedBB(0.F, 0.F, 0.F, 1.F, 0.5F, 1.F);

    public final String tname;
    public final boolean rotated;

    public BlockRoadSlope(int id, EnumSlopeType type, String name, boolean isRotated) {
        super(id, type, name);

        tname = type.tname;
        rotated = isRotated;
        setFullCube(false);
        setRenderType(EnumBlockRenderType.INVISIBLE);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityRoadSlope(world);
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return createNewTileEntity(world, getMetaFromState(state));
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbor, BlockPos npos) {
        TileEntityRoadSlope road = getTileEntity(world, pos, TileEntityRoadSlope.class);
        if (road != null) road.updateType();
        notifyBlockOfUpdate(world, npos, pos);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(JesRoads2.blocks.roadslope[roadType.getID()]);
    }

    @Override
    public boolean isBlockNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        if (!entity.isSprinting() || Math.abs(entity.motionX) > 0.3D || Math.abs(entity.motionZ) > 0.3D) {
            entity.motionX *= 1.3D;
            entity.motionY = 0;
            entity.motionZ *= 1.3D;
            entity.setSprinting(true);
        }
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing enfacing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase entity) {
        IBlockState state = super.getStateForPlacement(world, pos, enfacing, hitX, hitY, hitZ, meta, entity);
        EnumFacing face = state.getValue(facing);
        BlockPos[] sideneighbors = {pos.north(), pos.south(), pos.west(), pos.east()};
        for (BlockPos p : sideneighbors) {
            this.notifyBlockOfUpdate(world, pos, p);
            IBlockState s = world.getBlockState(p);
            Block b = s.getBlock();
            if (b instanceof BlockRoadSlope) {
                TileEntityRoadSlope tile = getTileEntity(world, p, TileEntityRoadSlope.class);
                if (tile != null && tile.getShape() != EnumSlopeShape.SINGLE) continue;

                if (((BlockRoadSlope) b).rotated) {
                    return JesRoads2.blocks.roadslope_rotated[roadType.getID()].getDefaultState().withProperty(facing, s.getValue(facing)).withProperty(shiny, state.getValue(shiny));
                } else return state.withProperty(facing, s.getValue(facing));
            }

            BlockPos next = getNeighborFromFacing(face, pos);
            if (world.isBlockFullCube(next)) {
                face = face.getOpposite();
                return JesRoads2.blocks.roadslope_rotated[roadType.getID()].getDefaultState().withProperty(BlockRoadSlope.facing, face).withProperty(shiny, state.getValue(shiny));
            }
        }

        return state.withProperty(facing, face);
    }

    public static BlockPos getNeighborFromFacing(EnumFacing face, BlockPos pos) {
        return pos.offset(face.getOpposite());
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntityRoadSlope road = getTileEntity(world, pos, TileEntityRoadSlope.class);
        if (road != null && road.getShape() == EnumSlopeShape.DOUBLE_BOTTOM) return BlockRoadSlope.HALF_BLOCK_AABB;
        else return BlockRoadSlope.FULL_BLOCK_AABB;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean unknown) {
        TileEntityRoadSlope road = getTileEntity(world, pos, TileEntityRoadSlope.class);
        if (road == null) return;

        EnumSlopeShape tp = road.getShape();
        EnumFacing face = state.getValue(facing);
        float minX, minY, minZ, maxX, maxY, maxZ, stepX, stepY, stepZ;

        switch (face) {
            case NORTH: {
                minX = 0.F;
                maxX = 1.F;
                stepX = 0.F;
                minY = 0.F;
                maxY = 0.25F;
                stepY = 0.25F;
                minZ = 0.75F;
                maxZ = 1.F;
                stepZ = -0.25F;
                break;
            }
            case SOUTH: {
                minX = 0.F;
                maxX = 1.F;
                stepX = 0.F;
                minY = 0.F;
                maxY = 0.25F;
                stepY = 0.25F;
                minZ = 0.F;
                maxZ = 0.25F;
                stepZ = 0.25F;
                break;
            }
            case EAST: {
                minX = 0.F;
                maxX = 0.25F;
                minY = 0.F;
                maxY = 0.25F;
                stepY = 0.25F;
                minZ = 0.F;
                maxZ = 1.F;
                stepX = 0.25F;
                stepZ = 0.F;
                break;
            }
            case WEST: {
                minX = 0.75F;
                maxX = 1.F;
                stepX = -0.25F;
                minY = 0.F;
                maxY = 0.25F;
                stepY = 0.25F;
                minZ = 0.F;
                maxZ = 1.F;
                stepZ = 0.F;
                break;
            }
            default:
                return;
        }

        if (tp != EnumSlopeShape.SINGLE) {
            minY /= 2;
            maxY /= 2;
            stepY /= 2;
        }

        if (tp == EnumSlopeShape.DOUBLE_TOP) {
            minX += 0.5F;
            maxX += 0.5F;
            minY += 0.5F;
            maxY += 0.5F;
            minZ += 0.5F;
            maxZ += 0.5F;
        }

        for (int i = 0; i < 4; i++) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
            minX += stepX;
            maxX += stepX;
            minY += stepY;
            maxY += stepY;
            minZ += stepZ;
            maxZ += stepZ;
        }
    }

    @Override
    protected BlockBase getReplacementBlock(Block b) {
        if (!(b instanceof BlockRoadSlope)) return super.getReplacementBlock(b);
        BlockRoadSlope slope = ((BlockRoadSlope) b);
        int id = slope.roadType.getID();

        if (id < 0) id = 0;
        else if (id >= JesRoads2.blocks.roadslope.length) id = JesRoads2.blocks.roadslope.length - 1;

        EnumSlopeType slopeType = slope.roadType instanceof EnumSlopeType ? (EnumSlopeType) slope.roadType : null;
        if (roadType == slopeType)
            return rotated ? JesRoads2.blocks.roadslope[roadType.getID()] : JesRoads2.blocks.roadslope_rotated[roadType.getID()];

        if (slopeType != null && rotated == slopeType.isInverted()) return JesRoads2.blocks.roadslope_rotated[id];
        else return JesRoads2.blocks.roadslope[id];
    }

    @Override
    public boolean canReplace(Block b) {
        return b instanceof BlockRoadSlope;
    }

    @Override
    protected void setTileEntity(World world, BlockPos pos, TileEntity tile) {
        if (tile instanceof TileEntityRoadSlope)
            world.setTileEntity(pos, new TileEntityRoadSlope(((TileEntityRoadSlope) tile).getNBT()));
    }

    @Override
    public ItemStack getSwitchBlock(ItemStack current) {
        switch (roadType.getID()) {
            case 0: case 1: case 2:
                return new ItemStack(JesRoads2.blocks.road[roadType.getID()]);
            case 3:
                return new ItemStack(JesRoads2.blocks.road[5]);
            default:
                return null;
        }
    }
}