package mod.jesroads2.block.road;

import java.util.List;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBase;
import mod.jesroads2.util.IBlockSwitchable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockTrafficItem extends BlockBase implements IBlockSwitchable {
    public enum EnumTrafficItems implements IStringSerializable {
        TRAFFIC_CONE(0, new AxisAlignedBB(0.18F, 0.F, 0.18F, 0.82F, 0.75F, 0.82F)),
        TRAFFIC_POST(1, new AxisAlignedBB(0.3F, 0.F, 0.3F, 0.68F, 1.F, 0.68F)),
        TRAFFIC_CONE_L(2, new AxisAlignedBB(0.15F, 0.F, 0.15F, 0.85F, 0.9F, 0.85F));

        public final int id;
        public final AxisAlignedBB box;

        private static final EnumTrafficItems[] list = new EnumTrafficItems[values().length];

        EnumTrafficItems(int index, AxisAlignedBB bbox) {
            id = index;
            box = bbox;
        }

        public static int[] getMetaValues() {
            int[] res = new int[list.length];
            for (int i = 0; i < res.length; i++)
                res[i] = i;
            return res;
        }

        public static EnumTrafficItems fromID(int id) {
            if (id > 0 && id < list.length) return list[id];
            else return list[0];
        }

        @Override
        public String getName() {
            return this.name().toLowerCase();
        }

        static {
            for (EnumTrafficItems item : values())
                list[item.id] = item;
        }
    }

    public static final PropertyEnum<EnumTrafficItems> variant = PropertyEnum.create("variant", EnumTrafficItems.class);

    public BlockTrafficItem(int id) {
        super(id, new Material(MapColor.GOLD), "traffic_item", JesRoads2.tabs.road_extra, true);

        setFullCube(false);
        setVariantCount(EnumTrafficItems.values().length);
        setDefaultState(getDefaultState().withProperty(variant, EnumTrafficItems.TRAFFIC_CONE));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return super.getDefaultState().withProperty(variant, EnumTrafficItems.fromID(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(variant).id;
    }

    @Override
    protected void createProperties(List<IProperty<?>> properties) {
        super.createProperties(properties);
        properties.add(variant);
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos.down());
        return state.isBlockNormalCube() || state.getBlock() instanceof BlockRoadSlope;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getValue(variant).box;
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> stack) {
        for (EnumTrafficItems it : EnumTrafficItems.values())
            stack.add(new ItemStack(this, 1, it.id));
    }

    @Override
    public int damageDropped(IBlockState state) {
        return this.getMetaFromState(state);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(this, 1, state.getValue(variant).id);
    }

    @Override
    public ItemStack getSwitchBlock(ItemStack current) {
        current.setItemDamage(EnumTrafficItems.fromID(current.getItemDamage() + 1).id);
        return current;
    }
}