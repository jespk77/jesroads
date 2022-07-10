package mod.jesroads2.block.system;

import java.util.List;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBase;
import mod.jesroads2.client.gui.GuiIntersectionController;
import mod.jesroads2.item.ItemBinder;
import mod.jesroads2.tileentity.TileEntityDirectionController;
import mod.jesroads2.tileentity.TileEntityIntersectionController;
import mod.jesroads2.util.IBindable;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockIntersectionController extends BlockBase implements ITileEntityProvider, IBindable {
    public static final PropertyEnum<EnumFacing.Axis> axis = PropertyEnum.create("axis", EnumFacing.Axis.class);
    public static final PropertyBool temporary = PropertyBool.create("temporary");

    public static final AxisAlignedBB northsouth_AABB = new AxisAlignedBB(0.1F, 0.F, 0.F, 0.9F, 1.F, 1.F),
            eastwest_AABB = new AxisAlignedBB(0.F, 0.F, 0.1F, 1.F, 1.F, 0.9F);

    public BlockIntersectionController(int id) {
        super(id, new Material(MapColor.GRAY), "trafficlight_controller", JesRoads2.tabs.system, true);

        setHardness(2.F).setResistance(50.F);
        setFullCube(false);
        setVariantCount(2);
        setDefaultState(getDefaultState().withProperty(axis, EnumFacing.Axis.Z).withProperty(temporary, false));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(axis) == EnumFacing.Axis.Z ? 0 : 1 | ((state.getValue(temporary) ? 1 : 0) << 1);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(axis, (meta & 1) == 0 ? EnumFacing.Axis.Z : EnumFacing.Axis.X)
                .withProperty(temporary, ((meta & 2) >> 1) != 0);
    }

    @Override
    protected void createProperties(List<IProperty<?>> properties) {
        super.createProperties(properties);
        properties.add(axis);
        properties.add(temporary);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        if (getStateFromMeta(meta).getValue(temporary)) return new TileEntityDirectionController(this, meta);
        else return new TileEntityIntersectionController(this, meta);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> stack) {
        stack.add(new ItemStack(this, 1, 0));
        //stack.add(new ItemStack(this, 1, 1));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        switch (state.getValue(axis)) {
            case Z:
                return northsouth_AABB;
            case X:
                return eastwest_AABB;
            default:
                return BlockBase.FULL_BLOCK_AABB;
        }
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing enfacing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase entity) {
        return getDefaultState().withProperty(axis, entity.getHorizontalFacing().getAxis()).withProperty(temporary, meta == 1);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entity, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (entity.getHeldItemMainhand().getItem() instanceof ItemBinder || entity.getHeldItemOffhand().getItem() instanceof ItemBinder)
            return false;

        if (world.isRemote)
            entity.openGui(JesRoads2.instance, GuiIntersectionController.ID, world, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        TileEntityIntersectionController controller = getTileEntity(world, pos, TileEntityIntersectionController.class);
        if (controller != null) controller.onBlockDestroyed();
        super.onBlockHarvested(world, pos, state, player);
    }

    @Override
    public boolean onBind(World world, BlockPos binder, BlockPos pos, EntityPlayer player) {
        if (binder == null) return false;

        TileEntityIntersectionController controller = getTileEntity(world, binder, TileEntityIntersectionController.class);
        if (controller != null) {
            String msg = controller.addBind(pos);
            if (world.isRemote) JesRoads2.handlerOverlay.getMessage().addMessage(msg);
            return true;
        } else return false;
    }

    @Override
    public boolean isCompatibleBlock(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() instanceof BlockTrafficlight;
    }
}