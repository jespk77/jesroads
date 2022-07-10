package mod.jesroads2.client.renderer;

import mod.jesroads2.tileentity.TileEntityMemory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityBeaconRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class RendererBlockMemoryBeam extends TileEntitySpecialRenderer<TileEntityMemory> {
    @Override
    public void render(TileEntityMemory tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (TileEntityMemory.render) {
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.disableFog();
            this.bindTexture(TileEntityBeaconRenderer.TEXTURE_BEACON_BEAM);
            float[] colors;
            try { colors = tile.getColors(); }
            catch (IllegalArgumentException e) { return; }
            TileEntityBeaconRenderer.renderBeamSegment(x, y, z, partialTicks, 1.F, tile.getWorld().getTotalWorldTime(), 1, 255 - tile.getPos().getY(), colors);
            GlStateManager.enableFog();
        }
    }

    @Override
    public boolean isGlobalRenderer(TileEntityMemory te) {
        return true;
    }
}