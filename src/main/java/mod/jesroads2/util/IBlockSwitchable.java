package mod.jesroads2.util;

import net.minecraft.item.ItemStack;

public interface IBlockSwitchable {
	ItemStack getSwitchBlock(ItemStack current);
}