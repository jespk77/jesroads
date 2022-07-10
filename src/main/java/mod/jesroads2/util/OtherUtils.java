package mod.jesroads2.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class OtherUtils {
    public static EnumHand getUsedHand(EntityPlayer player, Class<?> item) {
        ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
        if (!item.isInstance(stack.getItem())) {
            stack = player.getHeldItem(EnumHand.OFF_HAND);
            if (item.isInstance(stack.getItem())) return EnumHand.OFF_HAND;
            else return null;
        } else return EnumHand.MAIN_HAND;
    }

    public static String formatString(BlockPos p) {
        return p != null ? String.format("x=%d, y=%d, z=%d", p.getX(), p.getY(), p.getZ()) : "<None>";
    }
}