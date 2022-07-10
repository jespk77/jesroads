package mod.jesroads2.block.sign;

import java.util.List;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.util.IBlockSwitchable;
import net.minecraft.block.properties.IProperty;
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

public class BlockRoadSignStop extends BlockRoadSign implements IBlockSwitchable {
    public enum EnumType implements IStringSerializable {
        STOP(0, "stop"),
        GIVEWAY(1, "giveway"),
        NO_ENTRY(2, "no_entry"),
        ROAD_CLOSED(3, "road_closed");

        public final int id;
        public final String name;

        public static final EnumType[] list = new EnumType[values().length];

        EnumType(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public static EnumType fromID(int id) {
            if (id > 0 && id < list.length) return list[id];
            else return STOP;
        }

        @Override
        public String getName() {
            return name;
        }

        static {
            for (EnumType s : values()) {
                list[s.id] = s;
            }
        }
    }

    public static final PropertyEnum<EnumType> variant = PropertyEnum.create("variant", EnumType.class);

    public BlockRoadSignStop(int id) {
        super(id, "sign_stop");

        setVariantCount(EnumType.values().length);
        setDefaultState(getDefaultState().withProperty(variant, EnumType.STOP));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = super.getStateFromMeta(meta);
        int type = (meta & 12) >> 2;
        return state.withProperty(variant, EnumType.fromID(type));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = super.getMetaFromState(state);
        int type = state.getValue(variant).id << 2;
        return meta | type;
    }

    @Override
    protected void createProperties(List<IProperty<?>> properties) {
        super.createProperties(properties);
        properties.add(variant);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        AxisAlignedBB box = super.getBoundingBox(state, world, pos);
        if (state.getValue(variant) == EnumType.ROAD_CLOSED) return box.contract(0, 0.5, 0);
        else return box;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing enfacing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase entity) {
        IBlockState state = super.getStateForPlacement(world, pos, enfacing, hitX, hitY, hitZ, meta, entity);

        EnumFacing face = state.getValue(facing);
        if (shouldPlacePost(world, pos)) {
            ItemStack stack = entity.getHeldItemMainhand();
            if (stack == null) stack = entity.getHeldItemOffhand();
            if (stack == null) return state;
            world.setBlockState(pos.up(), getDefaultState().withProperty(facing, face)
                    .withProperty(variant, EnumType.fromID(stack.getItemDamage())), 2);
            return JesRoads2.blocks.signpost.getDefaultState().withProperty(facing, face).withProperty(BlockSignPost.size, BlockSignPost.EnumSize.SMALL);
        } else if (!isSupported(world, pos.down(), face))
            world.setBlockState(pos.down(), JesRoads2.blocks.signpost.getDefaultState()
                    .withProperty(facing, face).withProperty(BlockSignPost.size, BlockSignPost.EnumSize.SMALL), 2);

        ItemStack stack = entity.getHeldItemMainhand();
        if (stack == null) stack = entity.getHeldItemOffhand();
        return stack != null ? state.withProperty(variant, EnumType.fromID(stack.getItemDamage())) : state;
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> stack) {
        for (EnumType t : EnumType.values()) {
            stack.add(new ItemStack(this, 1, t.id));
        }
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(this, 1, state.getValue(variant).id);
    }

    @Override
    public ItemStack getSwitchBlock(ItemStack current) {
        current.setItemDamage(EnumType.fromID(current.getItemDamage() + 1).id);
        return current;
    }
}