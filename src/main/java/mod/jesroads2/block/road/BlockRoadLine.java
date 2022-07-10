package mod.jesroads2.block.road;

import java.util.List;

import mod.jesroads2.block.BlockBaseHorizontal;
import mod.jesroads2.item.ItemLinePainter;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockRoadLine extends BlockBaseHorizontal {

    public enum EnumLineType implements IStringSerializable {
        SIDE(0, "side"),
        DIAGONAL(1, "diagonal"),
        MIDDLE(2, "middle");

        public final int id;
        public final String name;

        public static final EnumLineType[] list = new EnumLineType[values().length];

        static {
            for (EnumLineType tp : values())
                list[tp.id] = tp;
        }

        EnumLineType(int index, String valueName) {
            id = index;
            name = valueName;
        }

        public static EnumLineType fromID(int id) {
            if (id > 0 && id < list.length) return list[id];
            else return SIDE;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    public static final PropertyEnum<EnumLineType> type = PropertyEnum.create("variant", EnumLineType.class);

    private static final AxisAlignedBB SELECT_BOX = new AxisAlignedBB(0.F, 0.F, 0.F, 1.F, 0.1F, 1.F);

    public BlockRoadLine(String color) {
        super(-1, new Material(MapColor.YELLOW), "roadline_" + color, null);

        setHardness(0.1F).setResistance(1.F);
        setFullCube(false);
        setDefaultState(getDefaultState().withProperty(type, EnumLineType.SIDE));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return super.getStateFromMeta(meta).withProperty(type, EnumLineType.fromID((meta & 12) >> 2));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return super.getMetaFromState(state) | (state.getValue(type).id << 2);
    }

    @Override
    protected void createProperties(List<IProperty<?>> properties) {
        super.createProperties(properties);
        properties.add(type);
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return isValidPlacement(world, pos);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbor, BlockPos npos) {
        if (!isValidPlacement(world, pos.down())) world.setBlockToAir(pos);
    }

    @Override
    public boolean isReplaceable(IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entity, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ) {
        return false;
    }

    public static boolean isValidPlacement(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        return block instanceof BlockRoad || block instanceof BlockRoadLine;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
        if (stack.getItem() instanceof ItemLinePainter) {
            stack.setItemDamage(ItemLinePainter.getIndexFromBlockstate(world.getBlockState(pos), stack.getItemDamage()));
            return stack;
        } else return new ItemStack(world.getBlockState(pos.down()).getBlock());
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return SELECT_BOX;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean unkown) {

    }
}