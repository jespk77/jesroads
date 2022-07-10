package mod.jesroads2.block.system;

import java.util.List;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBaseHorizontal;
import mod.jesroads2.client.gui.GuiGateController;
import mod.jesroads2.item.ItemBinder;
import mod.jesroads2.tileentity.TileEntityGateController;
import mod.jesroads2.util.IBindable;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockGateController extends BlockBaseHorizontal implements ITileEntityProvider, IBindable {
    private static final AxisAlignedBB box = new AxisAlignedBB(0.18F, 0, 0.18F, 0.82F, 1, 0.82F);
    public static final PropertyEnum<BlockGateBarrier.EnumPart> part = PropertyEnum.create("part", BlockGateBarrier.EnumPart.class);

    public BlockGateController(int id) {
        super(id, new Material(MapColor.GRAY), "gate_controller", JesRoads2.tabs.system);

        setHardness(5.F).setResistance(1.F);
        setFullCube(false);
        setDefaultState(getDefaultState().withProperty(part, BlockGateBarrier.EnumPart.BOTTOM));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return super.getStateFromMeta(meta).withProperty(part, BlockGateBarrier.EnumPart.fromID((meta & 12) >> 2));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return super.getMetaFromState(state) | (state.getValue(part).id << 2);
    }

    @Override
    protected void createProperties(List<IProperty<?>> properties) {
        super.createProperties(properties);
        properties.add(part);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos neighbor) {
        if (!isSupported(world, pos, state)) world.setBlockToAir(pos);
    }

    private boolean isSupported(World world, BlockPos pos, IBlockState state) {
        switch (state.getValue(part)) {
            case BOTTOM:
                return world.getBlockState(pos.up()).getBlock() instanceof BlockGateController;
            case TOP:
            case LIGHT:
                return world.getBlockState(pos.down()).getBlock() instanceof BlockGateController;
            default:
                return false;
        }
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing enfacing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase entity) {
        IBlockState state = super.getStateForPlacement(world, pos, enfacing, hitX, hitY, hitZ, meta, entity);
        world.setBlockState(pos.up(), state.withProperty(part, BlockGateBarrier.EnumPart.TOP), 2);
        return state.withProperty(part, BlockGateBarrier.EnumPart.BOTTOM);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entity, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (entity.getHeldItemMainhand().getItem() instanceof ItemBinder || entity.getHeldItemOffhand().getItem() instanceof ItemBinder)
            return false;
        if (state.getValue(part) == BlockGateBarrier.EnumPart.BOTTOM) pos = pos.up();

        if (GuiScreen.isAltKeyDown()) {
            if (world.isRemote)
                entity.openGui(JesRoads2.instance, GuiGateController.ID, world, pos.getX(), pos.getY(), pos.getZ());
            return true;
        } else if (side == state.getValue(facing).getOpposite()) {
            TileEntityGateController controller = getTileEntity(world, pos, TileEntityGateController.class);
            if (controller != null) return controller.onAction(stack);
        }
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        if (getStateFromMeta(meta).getValue(part) == BlockGateBarrier.EnumPart.TOP)
            return new TileEntityGateController();
        else return null;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return box;
    }

    @Override
    public boolean onBind(World world, BlockPos binder, BlockPos pos, EntityPlayer player) {
        TileEntityGateController controller = getTileEntity(world, binder, TileEntityGateController.class);
        if (controller != null) {
            String msg = controller.addBind(pos);
            if (!world.isRemote || msg == null) return true;
            JesRoads2.handlerOverlay.getMessage().addMessage(msg);
            return true;
        } else return false;
    }

    @Override
    public boolean isCompatibleBlock(World world, BlockPos pos) {
        Block b = world.getBlockState(pos).getBlock();
        return b instanceof BlockGateBarrier || b instanceof BlockDynamicSign;
    }
}