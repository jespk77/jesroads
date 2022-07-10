package mod.jesroads2.util;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IBlockOverlay {
    void renderOverlay(FontRenderer fontRenderer, RenderItem renderItem, World world, BlockPos pos);
}