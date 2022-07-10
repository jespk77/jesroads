package mod.jesroads2.block.system;

import java.util.List;
import java.util.Random;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBase;
import mod.jesroads2.tileentity.TileEntityGateBarrier;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockGateBarrier extends BlockBase implements ITileEntityProvider {
    public enum EnumPart implements IStringSerializable {
        BOTTOM(0, new AxisAlignedBB(0.2D, 0.D, 0.2D, 0.8D, 1.D, 0.8D)),
        TOP(1, new AxisAlignedBB(0.2D, 0.D, 0.2D, 0.8D, 0.55D, 0.8D)),
        LIGHT(2, new AxisAlignedBB(0.3D, 0.D, 0.3D, 0.7D, 0.8D, 0.7D));

        public final int id;
        public final AxisAlignedBB box;

        private static final EnumPart[] values = new EnumPart[values().length];

        static {
            for (EnumPart part : values())
                values[part.id] = part;
        }

        EnumPart(int id, AxisAlignedBB box) {
            this.id = id;
            this.box = box;
        }

        @Override
        public String getName() {
            return this.name().toLowerCase();
        }

        public static EnumPart fromID(int id) {
            if (id > 0 && id < values.length) return values[id];
            else return values[0];
        }
    }

    public enum EnumGateType {
        ONE("barrier_gate", "basic", false),
        TWO("barrier_gate_1", "dark"),
        TOLL("gate_toll", "toll");

        public final String name, textureID;
        public final boolean addLight;

        EnumGateType(String name, String textureID) {
            this(name, textureID, true);
        }

        EnumGateType(String name, String textureID, boolean addLight) {
            this.name = name;
            this.textureID = textureID;
            this.addLight = addLight;
        }
    }

    public static final Material material = new Material(MapColor.GRAY);

    public static final PropertyEnum<EnumPart> part = PropertyEnum.create("part", EnumPart.class);
    public static final PropertyDirection orientation = PropertyDirection.create("orientation", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyBool open = PropertyBool.create("open"), post = PropertyBool.create("post");

    public final EnumGateType type;

    public BlockGateBarrier(int id, EnumGateType type) {
        super(id, material, type.name, JesRoads2.tabs.system);

        this.type = type;
        setResistance(1.F);
        setHardness(2.5F);
        setFullCube(false);
        setDefaultState(getDefaultState()
                .withProperty(orientation, EnumFacing.SOUTH).withProperty(part, EnumPart.BOTTOM)
                .withProperty(open, false).withProperty(post, false));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(part, EnumPart.fromID(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(part).id;
    }

    @Override
    protected void createProperties(List<IProperty<?>> properties) {
        super.createProperties(properties);
        properties.add(part);
        properties.add(orientation);
        properties.add(open);
        properties.add(post);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        switch (state.getValue(part)) {
            case BOTTOM: {
                pos = pos.up();
                break;
            }
            case LIGHT: {
                pos = pos.down();
                break;
            }
            case TOP: {
                state = state.withProperty(post, world.getBlockState(pos.up()).getBlock() instanceof BlockGateBarrier);
                break;
            }
        }

        TileEntityGateBarrier gate = getTileEntity(world, pos, TileEntityGateBarrier.class);
        if (gate != null)
            return state.withProperty(orientation, gate.getDirection()).withProperty(open, gate.getOpened());
        else return state;
    }

    @SuppressWarnings("deprecation")
    @Override
    @SideOnly(Side.CLIENT)
    public int getPackedLightmapCoords(IBlockState state, IBlockAccess source, BlockPos pos) {
        if (state.getValue(part) == EnumPart.LIGHT) return 15728880;
        else return super.getPackedLightmapCoords(state, source, pos);
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return world.isAirBlock(pos.up()) && world.isAirBlock(pos.up(2)) &&
                !isBarrier(world, pos.north()) && !isBarrier(world, pos.south()) &&
                !isBarrier(world, pos.east()) && !isBarrier(world, pos.west());
    }

    private boolean isBarrier(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() instanceof BlockGateBarrier;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {

    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing enfacing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase entity) {
        IBlockState state = getDefaultState();
        if (entity instanceof EntityPlayer) ((EntityPlayer) entity).addStat(StatList.getObjectUseStats(item));
        world.setBlockState(pos.up(), state.withProperty(part, EnumPart.TOP), 3);
        world.setTileEntity(pos.up(), new TileEntityGateBarrier(entity.getHorizontalFacing(), type));
        if (type.addLight) world.setBlockState(pos.up(2), state.withProperty(part, EnumPart.LIGHT), 3);
        return state;
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer entity) {
        super.onBlockDestroyedByPlayer(world, pos, state);
        entity.addStat(StatList.getBlockStats(this));
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entity, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntityGateBarrier barrier = getTileEntity(world, pos, TileEntityGateBarrier.class);
        if (barrier != null) {
            if (stack != null && Block.getBlockFromItem(stack.getItem()) == Blocks.LEVER) {
                boolean lock = barrier.toggleLock();
                if (!lock) barrier.setOpened(false);
                return true;
            }
        }
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        if (meta == EnumPart.TOP.id) return new TileEntityGateBarrier();
        else return null;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getValue(part).box;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos neighbor) {
        if (!isSupported(world, pos, state)) world.destroyBlock(pos, false);
    }

    private boolean isSupported(World world, BlockPos pos, IBlockState state) {
        switch (state.getValue(part)) {
            case BOTTOM:
                return world.getBlockState(pos.up()).getBlock() instanceof BlockGateBarrier;
            case TOP:
            case LIGHT:
                return world.getBlockState(pos.down()).getBlock() instanceof BlockGateBarrier;
            default:
                return false;
        }
    }
}