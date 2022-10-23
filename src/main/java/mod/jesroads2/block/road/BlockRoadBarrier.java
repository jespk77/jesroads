package mod.jesroads2.block.road;

import java.util.List;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.sign.BlockSign;
import mod.jesroads2.block.streetlight.BlockStreetLight;
import mod.jesroads2.block.BlockBase;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockRoadBarrier extends BlockBase {
    public enum EnumOrientation implements IStringSerializable {
        NS(0, "north_south", EnumFacing.NORTH),
        EW(1, "east_west", EnumFacing.EAST);

        public final int id;
        public final String name;
        public final EnumFacing.Axis axis;
        public final EnumFacing facing;
        public static final EnumOrientation[] list = new EnumOrientation[values().length];

        EnumOrientation(int index, String valueName, EnumFacing direction) {
            id = index;
            name = valueName;
            facing = direction;
            axis = facing.getAxis();
        }

        @Override
        public String getName() {
            return name;
        }

        public static EnumOrientation fromFacing(EnumFacing face) {
            if (face.getAxis() == EnumFacing.Axis.X) return EW;
            else return NS;
        }

        public static EnumOrientation fromOrdinal(int id) {
            if (id > 0 && id < list.length) return list[id];
            else return NS;
        }

        public boolean isEqualOrientation(EnumFacing face) {
            return face.getAxis() == this.axis;
        }

        public EnumOrientation getOpposite() {
            return this == NS ? EW : NS;
        }

        static {
            for (EnumOrientation orient : values())
                list[orient.id] = orient;
        }
    }

    public enum EnumDirection implements IStringSerializable {
        NONE(0, "none"),
        SOUTH(1, "south"),
        WEST(2, "west"),
        NORTH(3, "north"),
        EAST(4, "east"),
        DIAGONAL_NS(5, "diagonal_ns"),
        DIAGONAL_EW(6, "diagonal_ew");

        public final int id;
        public final String name;
        public static final EnumDirection[] list = new EnumDirection[values().length];

        EnumDirection(int index, String valueName) {
            id = index;
            name = valueName;
        }

        @Override
        public String getName() {
            return name;
        }

        public static EnumDirection fromId(int id) {
            if (id > 0 && id < list.length) return list[id];
            else return NONE;
        }

        public boolean isDiagonal() {
            return this.id > 4;
        }

        public boolean isStraight() {
            return this.id > 0 && !this.isDiagonal();
        }

        static {
            for (EnumDirection d : values()) {
                list[d.id] = d;
            }
        }
    }

    public static final PropertyEnum<EnumOrientation> orientation = PropertyEnum.create("orientation", EnumOrientation.class);
    public static final PropertyEnum<EnumDirection> northwest = PropertyEnum.create("north_west", EnumDirection.class);
    public static final PropertyEnum<EnumDirection> northeast = PropertyEnum.create("north_east", EnumDirection.class);
    public static final PropertyEnum<EnumDirection> southwest = PropertyEnum.create("south_west", EnumDirection.class);
    public static final PropertyEnum<EnumDirection> southeast = PropertyEnum.create("south_east", EnumDirection.class);

    public static final PropertyBool streetlight = PropertyBool.create("streetlight"),
            snowy = PropertyBool.create("snowy"), sign = PropertyBool.create("sign");

    public BlockRoadBarrier(int id) {
        super(id, new Material(MapColor.IRON), "roadfence", JesRoads2.tabs.road_extra);

        setHardness(0.5F).setResistance(1.F);
        setSoundType(SoundType.METAL);
        setFullCube(false);
        setDefaultState(getDefaultState().withProperty(orientation, EnumOrientation.NS)
                .withProperty(northwest, EnumDirection.NONE)
                .withProperty(northeast, EnumDirection.NONE)
                .withProperty(southwest, EnumDirection.NONE)
                .withProperty(southeast, EnumDirection.NONE)
                .withProperty(streetlight, false).withProperty(snowy, false).withProperty(sign, false));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        float minX = 0.3F, maxX = 0.7F, minZ = 0.3F, maxZ = 0.7F;
        state = getActualState(state, world, pos);

        EnumDirection nw = state.getValue(northwest);
        EnumDirection ne = state.getValue(northeast);
        EnumDirection sw = state.getValue(southwest);
        EnumDirection se = state.getValue(southeast);

        if (nw == EnumDirection.NORTH || ne == EnumDirection.NORTH) minZ = 0.F;
        if (sw == EnumDirection.SOUTH || se == EnumDirection.SOUTH) maxZ = 1.F;

        if (nw == EnumDirection.WEST || sw == EnumDirection.WEST) minX = 0.F;
        if (ne == EnumDirection.EAST || se == EnumDirection.EAST) maxX = 1.F;

        if (state.getValue(orientation) == EnumOrientation.NS) {
            if (nw.isDiagonal()) {
                minX = 0.F;
                minZ = 0.F;
            }
            if (ne.isDiagonal()) {
                maxX = 1.F;
                minZ = 0.F;
            }
            if (sw.isDiagonal()) {
                minX = 0.F;
                maxZ = 1.F;
            }
            if (se.isDiagonal()) {
                maxX = 1.F;
                maxZ = 1.F;
            }
        } else if (state.getValue(orientation) == EnumOrientation.EW) {
            if (nw.isDiagonal()) {
                minX = 0.F;
                maxZ = 1.F;
            }
            if (ne.isDiagonal()) {
                maxX = 1.F;
                minZ = 0.F;
            }
            if (sw.isDiagonal()) {
                minX = 0.F;
                minZ = 0.F;
            }
            if (se.isDiagonal()) {
                maxX = 1.F;
                maxZ = 1.F;
            }
        }
        return new AxisAlignedBB(minX, 0.F, minZ, maxX, 0.9F, maxZ);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return getBoundingBox(state, world, pos);
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return world.getBlockState(pos.down()).isBlockNormalCube();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(orientation, EnumOrientation.fromOrdinal(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(orientation).id;
    }

    @Override
    protected void createProperties(List<IProperty<?>> properties) {
        super.createProperties(properties);

        properties.add(orientation);
        properties.add(northwest);
        properties.add(northeast);
        properties.add(southwest);
        properties.add(southeast);
        properties.add(streetlight);
        properties.add(snowy);
        properties.add(sign);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        EnumOrientation face = state.getValue(orientation);

        EnumDirection nw = EnumDirection.NONE, ne = EnumDirection.NONE, sw = EnumDirection.NONE, se = EnumDirection.NONE;
        if (face == EnumOrientation.NS) {
            nw = findConnection(world, EnumDirection.NONE, face, pos.north().west(), EnumDirection.DIAGONAL_NS);
            ne = findConnection(world, EnumDirection.NONE, face, pos.north().east(), EnumDirection.DIAGONAL_NS);
            sw = findConnection(world, EnumDirection.NONE, face, pos.south().west(), EnumDirection.DIAGONAL_NS);
            se = findConnection(world, EnumDirection.NONE, face, pos.south().east(), EnumDirection.DIAGONAL_NS);

            nw = findConnection(world, nw, face, pos.north(), EnumDirection.NORTH);
            ne = findConnection(world, ne, face, pos.north(), EnumDirection.NORTH);
            sw = findConnection(world, sw, face, pos.south(), EnumDirection.SOUTH);
            se = findConnection(world, se, face, pos.south(), EnumDirection.SOUTH);
        } else if (face == EnumOrientation.EW) {
            nw = findConnection(world, EnumDirection.NONE, face, pos.south().west(), EnumDirection.DIAGONAL_EW);
            ne = findConnection(world, EnumDirection.NONE, face, pos.north().east(), EnumDirection.DIAGONAL_EW);
            sw = findConnection(world, EnumDirection.NONE, face, pos.north().west(), EnumDirection.DIAGONAL_EW);
            se = findConnection(world, EnumDirection.NONE, face, pos.south().east(), EnumDirection.DIAGONAL_EW);

            nw = findConnection(world, nw, face, pos.west(), EnumDirection.WEST);
            ne = findConnection(world, ne, face, pos.east(), EnumDirection.EAST);
            sw = findConnection(world, sw, face, pos.west(), EnumDirection.WEST);
            se = findConnection(world, se, face, pos.east(), EnumDirection.EAST);
        }

        boolean snow = world.getBlockState(pos.north()).getBlock() == Blocks.SNOW_LAYER || world.getBlockState(pos.south()).getBlock() == Blocks.SNOW_LAYER ||
                world.getBlockState(pos.east()).getBlock() == Blocks.SNOW_LAYER || world.getBlockState(pos.west()).getBlock() == Blocks.SNOW_LAYER;

        IBlockState up = world.getBlockState(pos.up());
        return state.withProperty(northwest, nw)
                .withProperty(northeast, ne)
                .withProperty(southwest, sw)
                .withProperty(southeast, se).withProperty(snowy, snow)
                .withProperty(streetlight, up.getBlock() == JesRoads2.blocks.street_light)
                .withProperty(sign, up.getBlock() == JesRoads2.blocks.direction_signs[BlockSign.EnumSignType.F_DISTANCE.ordinal()]);
    }

    private static EnumDirection findConnection(IBlockAccess world, EnumDirection current, EnumOrientation face, BlockPos pos, EnumDirection connection) {
        if (current != EnumDirection.NONE) return current;

        IBlockState state = world.getBlockState(pos);
        boolean fence = isFence(state);
        if (fence && doesFacingConnect(connection.isDiagonal(), face, world.getBlockState(pos).getValue(orientation)))
            return connection;
        else if (!fence && !connection.isDiagonal() && canConnect(world, world.getBlockState(pos), pos, face.facing))
            return connection;
        else return EnumDirection.NONE;
    }

    private static boolean canConnect(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing facing) {
        return state.getBlockFaceShape(world, pos, facing) == BlockFaceShape.SOLID;
    }

    private static boolean isFence(IBlockAccess world, BlockPos pos) {
        return isFence(world.getBlockState(pos));
    }

    private static boolean isFence(IBlockState state) {
        return state.getBlock() instanceof BlockRoadBarrier;
    }

    private static boolean doesFacingConnect(boolean diagonal, EnumOrientation f1, EnumOrientation f2) {
        if (diagonal) return true;
        else return f1 == f2;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entity, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (Block.getBlockFromItem(stack.getItem()) == JesRoads2.blocks.street_light) {
            if (!world.isRemote)
                BlockStreetLight.placeBlocksOnPos(world, pos.up(), JesRoads2.blocks.street_light.getDefaultState().withProperty(BlockStreetLight.facing, entity.getHorizontalFacing()), stack.getItemDamage() == 0);
            return true;
        } else return false;
    }

    @Override
    public void onBlockDestroyedByPlayer(World world, BlockPos pos, IBlockState state) {
        BlockStreetLight.removeBlocksOnPos(world, pos.up());
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing enfacing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase entity) {
        IBlockState state = super.getStateForPlacement(world, pos, enfacing, hitX, hitY, hitZ, meta, entity);
        return state.withProperty(orientation, EnumOrientation.fromFacing(entity.getHorizontalFacing()));
    }
}