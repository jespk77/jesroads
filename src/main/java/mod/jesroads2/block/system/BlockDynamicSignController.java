package mod.jesroads2.block.system;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBase;
import mod.jesroads2.client.gui.GuiDynamicSignController;
import mod.jesroads2.item.ItemBinder;
import mod.jesroads2.tileentity.dynamicsigns.TileEntityDynamicSignController;
import mod.jesroads2.util.IBindable;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockDynamicSignController extends BlockBase implements ITileEntityProvider, IBindable {
    public static final Material material = new Material(MapColor.GRAY);

    public BlockDynamicSignController(int id) {
        super(id, material, "dynamic_sign_controller", JesRoads2.tabs.system);

        setHardness(0.7F).setResistance(10.F);
        setFullCube(false);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityDynamicSignController();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entity, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (entity.getHeldItemMainhand().getItem() instanceof ItemBinder || entity.getHeldItemOffhand().getItem() instanceof ItemBinder)
            return false;

        if (world.isRemote)
            entity.openGui(JesRoads2.instance, GuiDynamicSignController.ID, world, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    @Override
    public boolean onBind(World world, BlockPos binder, BlockPos pos, EntityPlayer player) {
        TileEntityDynamicSignController controller = getTileEntity(world, binder, TileEntityDynamicSignController.class);
        if (controller != null) {
            String msg = controller.addBind(pos);
            if (msg != null) {
                if (world.isRemote) JesRoads2.handlerOverlay.getMessage().addMessage(msg);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, entity, stack);

        if (!entity.isSneaking()) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntityDynamicSignController)
                ((TileEntityDynamicSignController) tile).scanForSigns(entity.getHorizontalFacing());
        }

        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            ItemStack offhand = player.getHeldItemOffhand();
            Item offhandItem = offhand.getItem();
            if (offhandItem instanceof ItemBinder) {
                ItemBinder binder = (ItemBinder) offhandItem;
                if (binder.isBound(offhand)) {
                    binder.bindBlock(world, player, pos, offhand);
                    binder.stopBind(world, offhand);
                } else binder.startBind(world, offhand, pos);
            }
        }
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        TileEntityDynamicSignController controller = getTileEntity(world, pos, TileEntityDynamicSignController.class);
        if (controller != null) controller.onBlockDestroyed();
        super.onBlockHarvested(world, pos, state, player);
    }

    @Override
    public boolean isCompatibleBlock(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        return block instanceof BlockDynamicSign || block instanceof BlockEventSign || block instanceof BlockDynamicSignController;
    }
}