package mod.jesroads2.block.sign;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.system.BlockFreewaySupport;
import mod.jesroads2.block.BlockBaseHorizontal;
import mod.jesroads2.client.gui.GuiRoadSignEdit;
import mod.jesroads2.tileentity.TileEntityRoadSign;
import mod.jesroads2.util.EnumFacingDiagonal;
import mod.jesroads2.util.IBlockSwitchable;
import mod.jesroads2.util.ITileEntityPlacement;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockSign extends BlockBaseHorizontal implements IBlockSwitchable, ITileEntityProvider, ITileEntityPlacement {
    public static final Map<String, Integer> colors = new HashMap<>();

    public static void updateColorMap(String[] map) {
        colors.clear();
        for (String line : map) {
            String[] entry = line.split(":");
            if (entry.length == 2) {
                try {
                    colors.put(entry[0], Integer.decode("0x" + entry[1]));
                } catch (NumberFormatException e) {
                    System.out.println("ERROR parsing color value from entry: " + entry[1]);
                }
            } else System.out.println("ERROR invalid textcolor entry: " + line);
        }
    }

    public enum EnumSignType {
        H_TEXT(0, "highway_text"),
        H_DIR_UP(1, "highway_dir_up"),
        H_DIR_LEFT(2, "highway_dir_left"),
        H_DIR_RIGHT(3, "highway_dir_right"),
        H_ROUTE(4, "highway_route"),
        H_ROUTEF(5, "highway_routef"),

        F_TEXT(10, "freeway_text"),
        FD_TEXT(11, "dfreeway_text"),
        F_DIR_UP(12, "freeway_dir_up"),
        FD_DIR_UP(13, "dfreeway_dir_up"),
        F_DIR_DOWN(14, "freeway_dir_down"),
        FD_DIR_DOWN(15, "dfreeway_dir_down"),
        F_DIR_RIGHT(16, "freeway_dir_right"),
        FD_DIR_RIGHT(17, "dfreeway_dir_right"),
        F_ROUTE(18, "freeway_route"),
        FD_ROUTE(19, "dfreeway_route"),
        F_ROUTEH(20, "freeway_routeh"),
        FD_ROUTEH(21, "dfreeway_routeh"),

        F_TEXT_HALF(22, "freeway_text_half"),
        FD_TEXT_HALF(23, "dfreeway_text_half"),

        F_ENTRANCE(24, "freeway_entrance"),
        F_DISTANCE(25, "freeway_distance"),
        F_SEPARATOR(26, "freeway_separator"),
        FD_SEPARATOR(27, "dfreeway_separator"),
        F_EXIT(28, "freeway_exit"),

        H_TEXT_OVERHEAD(29, "highway_text_overhead"),
        HD_TEXT_OVERHEAD(30, "dhighway_text_overhead"),
        H_DIR_UP_OVERHEAD(31, "highway_dir_up_overhead"),
        HD_DIR_UP_OVERHEAD(32, "dhighway_dir_up_overhead"),
        H_DIR_RIGHT_OVERHEAD(34, "highway_dir_right_overhead"),
        HD_DIR_RIGHT_OVERHEAD(35, "dhighway_dir_right_overhead"),
        H_ROUTE_OVERHEAD(36, "highway_route_overhead"),
        HD_ROUTE_OVERHEAD(37, "dhighway_route_overhead"),
        H_ROUTEH_OVERHEAD(38, "highway_routeh_overhead"),
        HD_ROUTEH_OVERHEAD(39, "dhighway_routeh_overhead");

        private final int id;
        public final String name;

        private static final EnumSignType[] list = new EnumSignType[values().length];

        EnumSignType(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public boolean showInTab() {
            return !isDiagonal();
        }

        public static EnumSignType fromOrdinal(int i) {
            if (i > 0 && i < list.length) return list[i];
            else return EnumSignType.H_TEXT;
        }

        public float top() {
            return isHighway() ? 0.9F : this == F_TEXT_HALF || this == FD_TEXT_HALF ? 0.5F : 1.F;
        }

        public boolean isHighway() {
            return id < 10;
        }

        public boolean isDiagonal() {
            return name.startsWith("d");
        }

        public boolean shouldConnect() {
            return !isHighway() && id != 24 && id != 25;
        }

        public boolean hasDiagonal() {
            return ordinal() < list.length - 1 && list[ordinal() + 1].name.startsWith("d");
        }

        public boolean needsPost() {
            return isHighway() || this == F_ENTRANCE || this == F_EXIT;
        }

        public CreativeTabs getTab() {
            if (!showInTab()) return null;
            else return JesRoads2.tabs.sign;
        }

        static {
            for (EnumSignType h : values()) {
                list[h.ordinal()] = h;
            }
        }
    }

    public static final PropertyBool up = PropertyBool.create("up"), down = PropertyBool.create("down");
    public static final PropertyBool blackout = PropertyBool.create("blackout"), data = PropertyBool.create("data");

    public static final AxisAlignedBB[] box = new AxisAlignedBB[]{new AxisAlignedBB(0.F, 0.F, 0.3F, 1.F, 1.F, 0.7F), new AxisAlignedBB(0.3F, 0.F, 0.F, 0.7F, 1.F, 1.F)},
            box_half = new AxisAlignedBB[]{new AxisAlignedBB(0.F, 0.F, 0.3F, 1.F, .5F, 0.7F), new AxisAlignedBB(0.3F, 0.F, 0.F, 0.7F, .5F, 1.F), new AxisAlignedBB(0.F, 0.F, 0.F, 1.F, .5F, 1.F)};

    public final EnumSignType type;

    public BlockSign(int id, EnumSignType type) {
        super(id, new Material(MapColor.GRAY), "sign_" + type.name, type.getTab());

        this.type = type;
		setHardness(2.F).setResistance(25.F);
		setFullCube(false);
        setDefaultState(getDefaultState().withProperty(up, false).withProperty(down, false)
                .withProperty(blackout, false).withProperty(data, false));

        if (item != null) {
            item.addPropertyOverride(new ResourceLocation("text"), (stack, worldIn, entityIn) -> stack.hasTagCompound() ? 1.F : 0.F);
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (type.isDiagonal()) return type.top() < 1.F ? box_half[2] : BlockSign.FULL_BLOCK_AABB;
        else if (isNorthSouth(state)) return type.top() > 0.8F ? box[0] : box_half[0];
        else return type.top() > 0.8F ? box[1] : box_half[1];
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbor, BlockPos npos) {
        EnumFacing face = state.getValue(facing);
        if (type.isHighway() && !isSupported(world, pos, face)) world.setBlockToAir(pos);
    }

    private boolean isSupported(World world, BlockPos pos, EnumFacing face) {
        return isSolidBlock(world, pos.down()) || isSolidBlock(world, pos.up());
    }

    private boolean isSolidBlock(IBlockAccess world, BlockPos pos) {
        return world.getBlockState(pos).getMaterial().isSolid();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return super.getStateFromMeta(meta).withProperty(blackout, ((meta & 4) >> 2) == 0).withProperty(data, ((meta & 8) >> 3) == 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return super.getMetaFromState(state) | ((state.getValue(blackout) ? 0 : 1) << 2) | ((state.getValue(data) ? 0 : 1) << 3);
    }

    @Override
    protected void createProperties(List<IProperty<?>> properties) {
        super.createProperties(properties);
        properties.add(up);
        properties.add(down);
        properties.add(blackout);
        properties.add(data);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.withProperty(up, isSolid(world, pos.up())).withProperty(down, isSolid(world, pos.down()));
    }

    protected boolean isSolid(IBlockAccess world, BlockPos pos) {
        return isSolid(world, world.getBlockState(pos), pos);
    }

    protected boolean isSolid(IBlockAccess world, IBlockState state, BlockPos pos) {
        return !isReplaceable(world, pos);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        int id = type.isDiagonal() && !type.isHighway() ? type.ordinal() - 1 : type.ordinal();
        return new ItemStack(JesRoads2.blocks.direction_signs[id]);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        world.setBlockState(pos, state.withProperty(data, true), 3);
        TileEntityRoadSign sign = getTileEntity(world, pos, TileEntityRoadSign.class);
        if (sign == null || !sign.checkForData()) {
            world.removeTileEntity(pos);
            world.setBlockState(pos, state.withProperty(data, false), 3);
        } else world.setBlockState(pos, state.withProperty(data, true), 3);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entity, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (GuiScreen.isAltKeyDown()) {
            world.setBlockState(pos, state.withProperty(data, true), 3);
            if (world.isRemote)
                entity.openGui(JesRoads2.instance, GuiRoadSignEdit.ID, world, pos.getX(), pos.getY(), pos.getZ());
            return true;
        }

        world.scheduleUpdate(pos, this, 5);
        if (world.isRemote) return true;

        if (stack != null && Block.getBlockFromItem(stack.getItem()) instanceof BlockFreewaySupport) {
            world.setBlockState(pos, JesRoads2.blocks.freewaysupport.getDefaultState(), 3);
            entity.addStat(StatList.getBlockStats(this));
        } else world.setBlockState(pos, state.withProperty(blackout, !state.getValue(blackout)), 3);
        return true;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing enfacing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase entity) {
        IBlockState state = super.getStateForPlacement(world, pos, enfacing, hitX, hitY, hitZ, meta, entity).withProperty(data, false);
        if (entity instanceof EntityPlayer) ((EntityPlayer) entity).addStat(StatList.getObjectUseStats(item));

        if (type.needsPost()) {
            EnumFacing face = state.getValue(facing);
            if (shouldPlacePost(world, pos)) {
                world.setBlockState(pos.up(), state, 2);
                world.setTileEntity(pos, new TileEntityRoadSign(EnumSignType.F_TEXT));
                return JesRoads2.blocks.signpost.getDefaultState().withProperty(BlockSignPost.facing, face).withProperty(BlockSignPost.size, BlockSignPost.EnumSize.LARGE);
            } else if (!isSolid(world, pos.down()) && !isSolid(world, pos.up()))
                world.setBlockState(pos.down(), JesRoads2.blocks.signpost.getDefaultState()
                        .withProperty(facing, state.getValue(facing)).withProperty(BlockSignPost.size, BlockSignPost.EnumSize.LARGE), 2);
        } else {
            EnumFacingDiagonal face = EnumFacingDiagonal.fromEntityF(entity);
            int id = type.hasDiagonal() ? type.ordinal() + 1 : type.ordinal();
            if (face.isDiagonal()) return JesRoads2.blocks.direction_signs[id].getDefaultState()
                    .withProperty(facing, face.getFacing().rotateY());
        }
        return state;
    }

    private static boolean shouldPlacePost(World world, BlockPos pos) {
        return !(world.getBlockState(pos.down()).getBlock() instanceof BlockSign);
    }

    @Override
    public ItemStack getSwitchBlock(ItemStack current) {
        if (type == null) return null;

        EnumSignType next = EnumSignType.fromOrdinal(type.ordinal() + (type.isHighway() ? 1 : 2));
        if (type.isHighway() && !next.isHighway()) next = EnumSignType.fromOrdinal(0);
        else if (!type.isHighway() && next.isHighway()) next = EnumSignType.fromOrdinal(6);
        return new ItemStack(JesRoads2.blocks.direction_signs[next.ordinal()]);
    }

    @Override
    public NBTTagCompound onBlockPlaced(World world, BlockPos pos, ItemStack stack, EntityPlayer player, NBTTagCompound blockEntity) {
        if(blockEntity != null){
            if (type == BlockSign.EnumSignType.F_DISTANCE) {
                if (blockEntity.hasKey("data_2")) {
                    NBTTagCompound data = blockEntity.getCompoundTag("data_2");
                    try {
                        int distance = Integer.parseInt(data.getString("text").replace(".", "")), walked = JesRoads2.handlerOverlay.getOverlay().getDistance() / 100;
                        if (distance + walked < 999) {
                            distance += walked;
                            StringBuilder b = new StringBuilder();
                            b.append(distance);
                            b.insert(b.length() - 1, ".");
                            data.setString("text", b.toString());
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockSign.data, true), 2);
        }

        return blockEntity;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, entity, stack);

        if (!stack.hasTagCompound() && GuiScreen.isAltKeyDown() && entity instanceof EntityPlayer)
            ((EntityPlayer) entity).openGui(JesRoads2.instance, GuiRoadSignEdit.ID, world, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return state.getValue(data) ? createNewTileEntity(world, getMetaFromState(state)) : null;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityRoadSign(type);
    }
}