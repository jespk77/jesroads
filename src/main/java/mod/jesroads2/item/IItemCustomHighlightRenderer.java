package mod.jesroads2.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;

public interface IItemCustomHighlightRenderer {
	boolean drawBlockHighlight(EntityPlayer player, ItemStack stack, RayTraceResult res, float partialTicks);
}