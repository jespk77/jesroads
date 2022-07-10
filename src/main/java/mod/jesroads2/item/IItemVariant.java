package mod.jesroads2.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IItemVariant {

	boolean updateStack(EntityPlayer player, ItemStack stack, int dwheel);
}