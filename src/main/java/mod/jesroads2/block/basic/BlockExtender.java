package mod.jesroads2.block.basic;

import java.util.List;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBaseAll;
import mod.jesroads2.tileentity.TileEntityExtender;
import mod.jesroads2.util.IBlockOverlay;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockExtender extends BlockBaseAll implements ITileEntityProvider, IBlockOverlay {
    public static final PropertyBool extended = PropertyBool.create("extended");

    public BlockExtender(int id) {
        super(id, new Material(MapColor.GRAY), "extender", JesRoads2.tabs.basic);

        setHardness(0.5F).setResistance(0.9F);
        setDefaultState(getDefaultState().withProperty(BlockExtender.extended, false));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return (state.getValue(BlockExtender.extended) ? 1 : 0) | super.getMetaFromState(state) << 1;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return super.getStateFromMeta(meta >> 1).withProperty(BlockExtender.extended, (meta & 1) == 1);
    }

    @Override
    protected void createProperties(List<IProperty<?>> properties) {
        super.createProperties(properties);
        properties.add(extended);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityExtender();
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing enfacing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase entity) {
        return super.getStateForPlacement(world, pos, enfacing, hitX, hitY, hitZ, meta, entity).withProperty(BlockExtender.extended, world.isBlockIndirectlyGettingPowered(pos) > 0);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbor, BlockPos npos) {
        if (world.isRemote || npos.equals(pos.offset(state.getValue(direction)))) return;

        IBlockState nstate = world.getBlockState(npos);
        boolean powered = nstate.getBlock() == this ? nstate.getValue(BlockExtender.extended) : world.isBlockIndirectlyGettingPowered(pos) > 0;
        if (state.getValue(BlockExtender.extended) != powered) {
            TileEntityExtender extender = this.getTileEntity(world, pos, TileEntityExtender.class);
            if (extender != null) extender.toggleExtend(powered);
            world.setBlockState(pos, state.withProperty(BlockExtender.extended, powered), 3);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entity, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (GuiScreen.isCtrlKeyDown()) {
            TileEntityExtender extender = this.getTileEntity(world, pos, TileEntityExtender.class);
            if (extender != null) {
                if (stack == null || stack.getItem() == Items.AIR) extender.setExtendingBlock(null);
                else extender.setExtendingBlock(stack);
            }
            return true;
        } else return stack.getItem() != Items.AIR && !this.canBlockPlacedOnSide(world, pos, side);
    }

    private boolean canBlockPlacedOnSide(World world, BlockPos pos, EnumFacing side) {
        return side != world.getBlockState(pos).getValue(direction);
    }

    @Override
    public void renderOverlay(FontRenderer font, RenderItem item, World world, BlockPos pos) {
        TileEntityExtender extender = this.getTileEntity(world, pos, TileEntityExtender.class);
        if (extender != null) {
            ItemStack stack = extender.getExtendingBlock();
            if (stack != null) {
                font.drawString("Block: ", 5, 5, 0xFFFFFF);
                if (stack.getCount() > 1) font.drawString("" + stack.getCount(), 55, 10, 0xFFFFFF);
                RenderHelper.enableGUIStandardItemLighting();
                item.renderItemIntoGUI(stack, 40, 1);
            } else font.drawString("No block set", 5, 5, 0xFFFFFF);
        } else font.drawString("No TileEntity set", 5, 5, 0xFF0000);
    }
}