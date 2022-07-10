package mod.jesroads2.block;

import java.util.ArrayList;
import java.util.List;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.network.MessageBlockUpdate;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockBase extends Block {

    public final String name;
    public final BlockItemBase item;
    public final int sortID;
    private BlockStateContainer container;

    private int meta;
    private boolean full;
    private EnumBlockRenderType type = EnumBlockRenderType.MODEL;

    private static final Block[] natural = new Block[]{Blocks.GRASS, Blocks.DIRT, Blocks.SAND, Blocks.STONE};

    public BlockBase(int id, Material mat, String name, CreativeTabs tab) {
        this(id, mat, name, tab, false);
    }

    public BlockBase(int id, Material mat, String name, CreativeTabs tab, boolean subtype) {
        super(mat);

        this.name = name;
        sortID = id;

        if (id >= 0 && tab != null) {
            if (subtype) item = new BlockItemBaseSub(this);
            else item = new BlockItemBase(this);
            setCreativeTab(tab);
        } else item = null;

        setVariantCount(1);
        setUnlocalizedName(name);
        setFullCube(true);
        setRegistryName(name);
    }

    public static boolean isNatural(Block block) {
        return isType(block, natural);
    }

    public static boolean isType(Block block, Block... blocks) {
        for (Block b : blocks)
            if (b.getClass().isInstance(block)) return true;
        return false;
    }

    public Block setFullCube(boolean full) {
        this.full = full;
        setLightOpacity(full ? 255 : 0);
        return this;
    }

    public Block setRenderType(EnumBlockRenderType type) {
        this.type = type;
        return this;
    }

    public Block setVariantCount(int count) {
        meta = count;
        return this;
    }

    public int getVariantCount() {
        return meta;
    }

    @Override
    protected final BlockStateContainer createBlockState() {
        List<IProperty<?>> properties = new ArrayList<>();
        createProperties(properties);
        container = new BlockStateContainer(this, properties.toArray(new IProperty<?>[0]));
        return container;
    }

    protected void createProperties(List<IProperty<?>> properties) {

    }

    @SuppressWarnings("unchecked")
    public final <T extends IProperty<?>> T getProperty(String name) {
        return (T) container.getProperty(name);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return type;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return full;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return full;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing facing) {
        return full ? BlockFaceShape.SOLID : BlockFaceShape.CENTER;
    }

    public static double calculateDistance(BlockPos from, BlockPos to) {
        return calculateDistance(from.getX(), from.getZ(), to.getX(), to.getZ());
    }

    public static double calculateDistance(double fromX, double fromZ, double toX, double toZ) {
        return Math.sqrt(Math.pow(fromX - toX, 2) + Math.pow(fromZ - toZ, 2));
    }

    public static String displayDistance(BlockPos from, BlockPos to) {
        return String.valueOf(Math.round(calculateDistance(from, to)));
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entity, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        return onBlockActivated(world, pos, state, entity, entity.getHeldItem(hand), side, hitX, hitY, hitZ);
    }

    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entity, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ) {
        return false;
    }

    public void notifyBlockOfUpdate(IBlockAccess world, BlockPos caller, BlockPos pos) {
        JesRoads2.channel.sendToAll(new MessageBlockUpdate(caller, pos, true));
    }

    public void scheduleBlockUpdate(IBlockAccess world, BlockPos pos, int delay) {
        JesRoads2.channel.sendToAll(new MessageBlockUpdate(pos, true, delay));
    }

    public boolean isEmpty(ItemStack stack) {
        return stack == null || stack.getItem() == Items.AIR;
    }

    public <T> T getTileEntity(IBlockAccess world, BlockPos pos, Class<? extends TileEntity> T) {
        if (this instanceof ITileEntityProvider) {
            TileEntity tile = world.getTileEntity(pos);
            if (T.isInstance(tile)) return ((T) tile);
        }
        return null;
    }
}