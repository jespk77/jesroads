package mod.jesroads2.client.renderer;

import mod.jesroads2.tileentity.ITileEntityBindable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

public class RendererBlockBound<E extends TileEntity & ITileEntityBindable> extends TileEntitySpecialRenderer<E> {
    @Override
    public void render(E tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (tile.isBinding()) {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            RenderHelper.disableStandardItemLighting();

            IBlockState state = tile.getWorld().getBlockState(tile.getPos());
            RenderGlobal.drawSelectionBoundingBox(state.getBoundingBox(tile.getWorld(), tile.getPos()).offset(x, y, z).grow(0.01), 1.F, 0.1F, 0.3F, 1);

            RenderHelper.enableStandardItemLighting();
            GlStateManager.disableBlend();
            GlStateManager.enableTexture2D();
            GlStateManager.depthMask(true);
        }
    }
}