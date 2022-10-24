package mod.jesroads2.block.basic;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBaseAll;
import mod.jesroads2.client.gui.GuiFloodlightController;
import mod.jesroads2.item.ItemBinder;
import mod.jesroads2.tileentity.TileEntityFloodlightController;
import mod.jesroads2.util.IBindable;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockFloodlightController extends BlockBaseAll implements ITileEntityProvider, IBindable {
    public BlockFloodlightController(int id) {
        super(id, new Material(MapColor.GRAY), "floodlight_controller", JesRoads2.tabs.basic);

        setHardness(1.F).setResistance(5.F);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing enfacing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase entity) {
        IBlockState state = super.getStateForPlacement(world, pos, enfacing, hitX, hitY, hitZ, meta, entity);
        EnumFacing dir = state.getValue(direction);
        if (dir.getHorizontalIndex() >= 0) return state.withProperty(direction, dir.getOpposite());
        else return state;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return getMetaFromDirection(state.getValue(direction));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(direction, getDirectionFromMeta(meta));
    }

    private int getMetaFromDirection(EnumFacing dir) {
        if (dir.getHorizontalIndex() == -1) return dir == EnumFacing.DOWN ? 4 : 5;
        else return dir.getHorizontalIndex();
    }

    private EnumFacing getDirectionFromMeta(int meta) {
        if (meta > 3) return meta == 4 ? EnumFacing.DOWN : EnumFacing.UP;
        else return EnumFacing.getHorizontal(meta);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityFloodlightController();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entity, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (entity.getHeldItemMainhand().getItem() instanceof ItemBinder || entity.getHeldItemOffhand().getItem() instanceof ItemBinder)
            return false;

        if (state.getValue(direction) == side.getOpposite()) {
            TileEntityFloodlightController controller = this.getTileEntity(world, pos, TileEntityFloodlightController.class);
            if (world.isRemote && controller != null && controller.getGroupCount() > 0) {
                entity.openGui(JesRoads2.instance, GuiFloodlightController.ID, world, pos.getX(), pos.getY(), pos.getZ());
            }
            return true;
        } else return false;
    }
	
	/*// TODO: controlling with redstone?
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbor, BlockPos npos){
		if(world.getBlockState(npos).canProvidePower()){
			TileEntityFloodlightController controller = this.getTileEntity(world, pos, TileEntityFloodlightController.class);
			if(controller != null) controller.setAll(world.isBlockIndirectlyGettingPowered(pos) > 0);
			this.notifyBlockOfUpdate(world, npos, pos);
		}
	}*/

    @Override
    public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        TileEntityFloodlightController controller = getTileEntity(world, pos, TileEntityFloodlightController.class);
        if (!world.isRemote && controller != null) controller.onBlockDestroyed();
    }

    @Override
    public boolean onBind(World world, BlockPos binder, BlockPos pos, EntityPlayer player) {
        TileEntityFloodlightController controller = getTileEntity(world, binder, TileEntityFloodlightController.class);
        if (controller != null) {
            if (controller.removeIfExist(pos)) {
                if(world.isRemote) JesRoads2.handlerOverlay.getMessage().addMessage("[Freeway Controller] Light removed");
            }
            else {
                if(world.isRemote) player.openGui(JesRoads2.instance, GuiFloodlightController.ID, world, binder.getX(), binder.getY(), binder.getZ());
            }
        }
        return true;
    }

    @Override
    public boolean isCompatibleBlock(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() instanceof TileEntityFloodlightController.IFloodlightBind;
    }
}