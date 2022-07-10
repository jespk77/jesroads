package mod.jesroads2.block.basic;

import java.util.List;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBase;
import mod.jesroads2.tileentity.TileEntityFloodlightController.IFloodlightBind;
import mod.jesroads2.util.IBlockSwitchable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class BlockColoredLight extends BlockBase implements IFloodlightBind, IBlockSwitchable {
    public static final PropertyEnum<EnumDyeColor> color = PropertyEnum.create("color", EnumDyeColor.class);
    public final boolean isLit;

    public BlockColoredLight(int id, boolean lit) {
        super(id, new Material(MapColor.getBlockColor(EnumDyeColor.WHITE)), lit ? "colored_light_on" : "colored_light", id >= 0 ? JesRoads2.tabs.basic : null, !lit);

        isLit = lit;
        setHardness(0.1F).setResistance(0.1F);
        if (lit) setLightLevel(1.F);
        setVariantCount(EnumDyeColor.values().length);
        setDefaultState(getDefaultState().withProperty(BlockColoredLight.color, EnumDyeColor.WHITE));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(BlockColoredLight.color).getMetadata();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(BlockColoredLight.color, EnumDyeColor.byMetadata(meta));
    }

    @Override
    protected void createProperties(List<IProperty<?>> properties) {
        super.createProperties(properties);
        properties.add(BlockColoredLight.color);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing enfacing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase entity) {
        return getDefaultState().withProperty(BlockColoredLight.color, EnumDyeColor.byMetadata(meta));
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entity, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (stack != null && stack.getItem() instanceof ItemDye) {
            if (!world.isRemote)
                world.setBlockState(pos, state.withProperty(BlockColoredLight.color, EnumDyeColor.byDyeDamage(stack.getItemDamage())), 2);
            return true;
        } else return false;
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> stack) {
        stack.add(new ItemStack(this, 1, EnumDyeColor.WHITE.getMetadata()));
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(JesRoads2.blocks.colored_light_off, 1, state.getValue(BlockColoredLight.color).getMetadata());
    }

    @Override
    public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, float chance, int fortune) {
    }

    @Override
    public void set(World world, BlockPos pos, IBlockState state, boolean on) {
        if (!world.isRemote)
            world.setBlockState(pos,
					( on ? JesRoads2.blocks.colored_light_on.getDefaultState().withProperty(BlockColoredLight.color, state.getValue(BlockColoredLight.color))
					: JesRoads2.blocks.colored_light_off.getDefaultState())
					.withProperty(BlockColoredLight.color, state.getValue(BlockColoredLight.color)), 2);
    }

    @Override
    public ItemStack getSwitchBlock(ItemStack current) {
        return new ItemStack(this, current.getCount(), EnumDyeColor.byMetadata(current.getMetadata() + 1).getMetadata());
    }
}