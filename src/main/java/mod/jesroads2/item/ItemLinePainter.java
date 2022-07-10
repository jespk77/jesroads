package mod.jesroads2.item;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.road.BlockRoad;
import mod.jesroads2.block.road.BlockRoadLine;
import mod.jesroads2.block.road.BlockRoadLine.EnumLineType;
import mod.jesroads2.util.IBlockSwitchable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

public class ItemLinePainter extends ItemBase implements IItemVariant, IBlockSwitchable {
    public ItemLinePainter(int id) {
        super(id, "line_painter", JesRoads2.tabs.road_extra);

        this.setMaxDamage(5);
        this.setMaxStackSize(1);
        this.addPropertyOverride(new ResourceLocation("type"), (stack, worldIn, entityIn) -> stack.getItemDamage());
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        return false;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing face, float facing, float hitX, float hitY) {
        int index = player.getHeldItem(hand).getItemDamage();
        IBlockState state = world.getBlockState(pos), place = getBlockstateFromIndex(index, state);
        BlockPos p = state.getBlock() instanceof BlockRoad ? pos.up() : state.getBlock() instanceof BlockRoadLine ? pos : null;
        if (p != null) {
            int max = EnumLineType.values().length;
            if (index > max) index -= max;
            world.setBlockState(p, place.withProperty(BlockRoadLine.facing, player.getHorizontalFacing()), 2);
            return EnumActionResult.SUCCESS;
        } else return EnumActionResult.FAIL;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
    }

    @Override
    public boolean updateStack(EntityPlayer player, ItemStack stack, int dwheel) {
        if (stack != null) {
            int damage = updateLineType(dwheel < 0, stack.getItemDamage());
            stack.setItemDamage(damage);
            IBlockState state = getBlockstateFromIndex(stack.getItemDamage(), null);
            JesRoads2.handlerOverlay.getMessage().addMessage(I18n.translateToLocal(state.getBlock().getUnlocalizedName() + "_" + state.getValue(BlockRoadLine.type).name + ".name"));
            return true;
        } else return false;
    }

    private int updateLineType(boolean positive, int current) {
        int next = positive ? current + 1 : current - 1, max = EnumLineType.values().length * 2;
        if (next >= 0 && next < max) return next;
        else return positive ? 0 : max - 1;
    }

    public static IBlockState getBlockstateFromIndex(int index, IBlockState fallback) {
        int max = EnumLineType.values().length;
        boolean white = true;
        if (index >= max) {
            index -= max;
            white = false;
        }

        if (index >= max) return fallback;
        else
            return white ? JesRoads2.blocks.road_line[0].getDefaultState().withProperty(BlockRoadLine.type, EnumLineType.fromID(index)) :
                    JesRoads2.blocks.road_line[1].getDefaultState().withProperty(BlockRoadLine.type, EnumLineType.fromID(index));
    }

    public static int getIndexFromBlockstate(IBlockState state, int fallback) {
        Block b = state.getBlock();
        if (b == JesRoads2.blocks.road_line[1])
            return EnumLineType.values().length + state.getValue(BlockRoadLine.type).id;
        else if (b == JesRoads2.blocks.road_line[0]) return state.getValue(BlockRoadLine.type).id;
        else return fallback;
    }

    @Override
    public ItemStack getSwitchBlock(ItemStack current) {
        return null;
    }

}