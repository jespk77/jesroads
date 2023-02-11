package mod.jesroads2.block.sign;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBaseHorizontal;
import mod.jesroads2.util.IBlockSwitchable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public abstract class BlockRoadSign<T extends Enum<T> & IStringSerializable> extends BlockBaseHorizontal implements IBlockSwitchable {
    public enum ESupportType implements IStringSerializable {
        None, Post, Back;

        @Override
        public String getName() {
            return toString().toLowerCase();
        }
    }

    public static final PropertyBool upSign = PropertyBool.create("up_sign");
    public static final PropertyEnum<ESupportType> supportType = PropertyEnum.create("support_type", ESupportType.class);

    private PropertyEnum<T> variant;
    private final T[] variantValues;

    public BlockRoadSign(int id, String name) {
        super(id, Material.IRON, name, JesRoads2.tabs.sign, true);
        variantValues = getEnumClass().getEnumConstants();

        setHardness(0.5F);
        setFullCube(false);
        setVariantCount(variantValues.length);
        setDefaultState(getDefaultState().withProperty(upSign, false).withProperty(supportType, ESupportType.None).withProperty(variant, variantValues[0]));
    }

    public abstract Class<T> getEnumClass();

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = super.getStateFromMeta(meta);
        int type = (meta & 12) >> 2;
        return state.withProperty(variant, variantValues[type]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = super.getMetaFromState(state);
        return meta | (state.getValue(variant).ordinal() << 2);
    }

    @Override
    protected void createProperties(List<IProperty<?>> properties) {
        super.createProperties(properties);
        properties.add(upSign);
        properties.add(supportType);

        variant = PropertyEnum.create("variant", getEnumClass());
        properties.add(variant);
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for(T variant : variantValues)
            items.add(new ItemStack(this, 1, variant.ordinal()));
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos){
        EnumFacing face = world.getBlockState(pos).getValue(facing);
        boolean isSignUp = world.getBlockState(pos.up()).getBlock() instanceof BlockRoadSign;

        BlockPos signpostPosition = pos;
        while(world.getBlockState(signpostPosition).getBlock() instanceof BlockRoadSign){
            signpostPosition = signpostPosition.down();
        }

        ESupportType type = ESupportType.None;
        if(world.getBlockState(signpostPosition).getBlock() instanceof BlockSignPost){
            type = ESupportType.Post;
        } else if(world.getBlockState(pos.offset(face)).getBlock() instanceof BlockRoadSign){
            type = ESupportType.Back;
            isSignUp = false;
        }
        return super.getActualState(state, world, pos).withProperty(upSign, isSignUp).withProperty(supportType, type);
    }

    protected static boolean shouldPlacePost(World world, BlockPos pos, EnumFacing face) {
        return !(world.getBlockState(pos.down()).getBlock() instanceof BlockRoadSign) &&
                !(world.getBlockState(pos.up().offset(face)).getBlock() instanceof BlockRoadSign) &&
                !(world.getBlockState(pos.offset(face)).getBlock() instanceof BlockRoadSign);
    }

    protected boolean isSolid(IBlockAccess world, BlockPos pos) {
        return world.getBlockState(pos).getMaterial().isSolid();
    }

    protected boolean isSupported(World world, BlockPos pos, EnumFacing face) {
        return isSolid(world, pos.down()) || isSolid(world, pos.offset(face));
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing enfacing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase entity) {
        IBlockState state = super.getStateForPlacement(world, pos, enfacing, hitX, hitY, hitZ, meta, entity);

        EnumFacing face = state.getValue(facing);
        if (shouldPlacePost(world, pos, face)) {
            ItemStack stack = entity.getHeldItemMainhand();
            if (stack == null) stack = entity.getHeldItemOffhand();
            if (stack == null) return state;
            world.setBlockState(pos.up(), getDefaultState().withProperty(facing, face)
                    .withProperty(variant, variantValues[stack.getItemDamage()]), 2);
            return JesRoads2.blocks.signpost.getDefaultState().withProperty(facing, face).withProperty(BlockSignPost.size, BlockSignPost.EnumSize.SMALL);
        } else if (!isSupported(world, pos.down(), face))
            world.setBlockState(pos.down(), JesRoads2.blocks.signpost.getDefaultState()
                    .withProperty(facing, face).withProperty(BlockSignPost.size, BlockSignPost.EnumSize.SMALL), 2);

        ItemStack stack = entity.getHeldItemMainhand();
        if (stack == null) stack = entity.getHeldItemOffhand();
        return stack != null ? state.withProperty(variant, variantValues[stack.getItemDamage()]) : state;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        float minX, maxX, minZ, maxZ;
        switch (state.getValue(facing)) {
            case SOUTH: {
                minX = 0.F;
                maxX = 1.F;
                minZ = 0.7F;
                maxZ = 1.F;
                break;
            }
            case WEST: {
                minX = 0.F;
                maxX = 0.3F;
                minZ = 0.F;
                maxZ = 1.F;
                break;
            }
            case EAST: {
                minX = 0.79F;
                maxX = 1.F;
                minZ = 0.F;
                maxZ = 1.F;
                break;
            }
            default: {
                minX = 0.F;
                maxX = 1.F;
                minZ = 0.F;
                maxZ = 0.3F;
                break;
            }
        }
        return new AxisAlignedBB(minX, 0.F, minZ, maxX, 1.F, maxZ);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbor, BlockPos npos) {
        EnumFacing face = state.getValue(facing);
        if (!isSupported(world, pos, face)) world.setBlockToAir(pos);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(this, 1, state.getValue(variant).ordinal());
    }

    @Override
    public ItemStack getSwitchBlock(ItemStack current) {
        current.setItemDamage((current.getItemDamage() + 1) % variantValues.length);
        return current;
    }
}