package mod.jesroads2.block.basic;

import java.util.List;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBase;
import mod.jesroads2.client.gui.GuiMemory;
import mod.jesroads2.tileentity.TileEntityMemory;
import mod.jesroads2.util.IBlockOverlay;
import mod.jesroads2.util.IBlockSwitchable;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class BlockMemory extends BlockBase implements ITileEntityProvider, IBlockSwitchable, IBlockOverlay {
    public static final Material material = new Material(MapColor.BLACK);
    public static final PropertyEnum<EnumDyeColor> color = PropertyEnum.create("color", EnumDyeColor.class);

    public BlockMemory(int id) {
        super(id, material, "memory", JesRoads2.tabs.basic, true);

        setHardness(1.F).setResistance(5.F).setLightOpacity(0);
        setSoundType(SoundType.STONE);
        setVariantCount(EnumDyeColor.values().length);
        setDefaultState(getDefaultState().withProperty(BlockMemory.color, EnumDyeColor.CYAN));
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing enfacing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase entity) {
        return getDefaultState().withProperty(BlockMemory.color, EnumDyeColor.byMetadata(meta));
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> stack) {
        stack.add(new ItemStack(this, 1, EnumDyeColor.CYAN.getMetadata()));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityMemory(world);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(BlockMemory.color, EnumDyeColor.byMetadata(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(BlockMemory.color).getMetadata();
    }

    @Override
    protected void createProperties(List<IProperty<?>> properties) {
        super.createProperties(properties);
        properties.add(BlockMemory.color);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(JesRoads2.blocks.memory, 1, state.getValue(BlockMemory.color).getMetadata());
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(BlockMemory.color).getMetadata();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entity, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ) {
        boolean change = false;
        if (!isEmpty(stack) && stack.getItem() instanceof ItemDye) {
            EnumDyeColor dye = EnumDyeColor.byMetadata(stack.getMetadata());
            change = state.getValue(BlockMemory.color) != dye;
            if (change && !world.isRemote) world.setBlockState(pos, state.withProperty(BlockMemory.color, dye), 2);
        }

        if (world.isRemote && !change)
            entity.openGui(JesRoads2.instance, GuiMemory.ID, world, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    @Override
    public ItemStack getSwitchBlock(ItemStack current) {
        return new ItemStack(this, current.getCount(), EnumDyeColor.byMetadata(current.getMetadata() + 1).getMetadata());
    }

    @Override
    public void renderOverlay(FontRenderer renderer, RenderItem item, World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityMemory) {
            TileEntityMemory mem = ((TileEntityMemory) tile);
            String[] display = mem.getString().split("==");
            int yPos = 5, color = mem.getColor();
            for (String s : display) {
                renderer.drawString(s, 5, yPos, color);
                yPos += 15;
            }
        }
    }
}