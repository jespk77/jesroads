package mod.jesroads2.client.renderer;

import org.lwjgl.opengl.GL11;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.system.BlockGateBarrier;
import mod.jesroads2.tileentity.TileEntityGateBarrier;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class RendererBlockBarrierGate extends TileEntitySpecialRenderer<TileEntityGateBarrier> {
    private static final ResourceLocation[] BASE = createResources("base");
    private static final ResourceLocation[] BARRIER = createResources("barrier");

    private static ResourceLocation[] createResources(String id) {
        BlockGateBarrier.EnumGateType[] values = BlockGateBarrier.EnumGateType.values();
        ResourceLocation[] res = new ResourceLocation[values.length];
        for (BlockGateBarrier.EnumGateType type : values)
            res[type.ordinal()] = new ResourceLocation(JesRoads2.modid + ":textures/blocks/barrier_gate/" + type.textureID + "_" + id + ".png");
        return res;
    }

    private static ResourceLocation getBaseFromID(int id) {
        if (id > 0 && id < BASE.length) return BASE[id];
        else return BASE[0];
    }

    private static ResourceLocation getBarrierFromID(int id) {
        if (id > 0 && id < BARRIER.length) return BARRIER[id];
        else return BARRIER[0];
    }

    @Override
    public void render(TileEntityGateBarrier barrier, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        GlStateManager.translate(x, y, z);
        Tessellator tes = Tessellator.getInstance();
        BufferBuilder buffer = tes.getBuffer();

        RenderHelper.disableStandardItemLighting();
        GlStateManager.color(0.65F, 0.65F, 0.65F);

        switch (barrier.getDirection()) {
            case NORTH: {
                GlStateManager.rotate(180.F, 0.F, 1.F, 0.F);
                GlStateManager.translate(-1.F, 0.F, -1.F);
                break;
            }
            case EAST: {
                GlStateManager.rotate(90.F, 0.F, 1.F, 0.F);
                GlStateManager.translate(-1.F, 0.F, 0.F);
                break;
            }
            case WEST: {
                GlStateManager.rotate(270.F, 0.F, 1.F, 0.F);
                GlStateManager.translate(0.F, 0.F, -1.F);
                break;
            }
            default:
                break;
        }

        // --- RENDER BASE ---
        bindTexture(getBaseFromID(barrier.getType().ordinal()));
        drawSideSquare(tes, buffer, 0.3D, 0.D, 0.7D, 0.7D, 0.D, 0.85D);
        drawSideSquare(tes, buffer, 0.3D, 0.D, 0.7D, 0.3D, 0.35D, 0.85D);
        drawSideSquare(tes, buffer, 0.7D, 0.D, 0.85D, 0.7D, 0.35D, 0.7D);
        drawSideSquare(tes, buffer, 0.7D, 0.35D, 0.7D, 0.3D, 0.35D, 0.85D);

        drawFrontSquare(tes, buffer, 0.3D, 0.D, 0.85D, 0.7D, 0.35D, 0.85D);

        // --- RENDER BARRIER ---
        bindTexture(getBarrierFromID(barrier.getType().ordinal()));
        float rot = barrier.getRotation();
        float rtX = 0.5F, rtY = 0.2F, rtZ = rtY;
        GlStateManager.translate(rtX, rtY, rtZ);
        GlStateManager.rotate(-rot, 0.F, 0.F, 1.F);
        GlStateManager.translate(-rtX, -rtY, -rtZ);
        drawSideSquare(tes, buffer, -3.D, 0.1D, 0.79D, 0.45D, 0.1D, 0.84D);
        drawSideSquare(tes, buffer, 0.45D, 0.3D, 0.79D, -3.0D, 0.3D, 0.84D);
        drawSideSquare(tes, buffer, -3.D, 0.1D, 0.79D, -3.D, 0.3D, 0.84D);

        drawFrontSquare(tes, buffer, -3.0D, 0.1D, 0.84D, 0.45D, 0.3D, 0.84D);
        drawFrontSquare(tes, buffer, 0.45D, 0.1D, 0.79D, -3.D, 0.3D, 0.79D);

        RenderHelper.enableStandardItemLighting();
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }

    private void drawFrontSquare(Tessellator tes, BufferBuilder buffer, double xMin, double yMin, double zMin, double xMax, double yMax, double zMax) {
        drawFrontSquare(tes, buffer, xMin, yMin, zMin, xMax, yMax, zMax, xMin, xMax, zMin, zMax);
    }

    private void drawFrontSquare(Tessellator tes, BufferBuilder buffer, double xMin, double yMin, double zMin, double xMax, double yMax, double zMax, double uMin, double uMax, double vMin, double vMax) {
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(xMax, yMin, zMin).tex(uMin, vMin).endVertex();
        buffer.pos(xMax, yMax, zMin).tex(uMin, vMax).endVertex();
        buffer.pos(xMin, yMax, zMax).tex(uMax, vMin).endVertex();
        buffer.pos(xMin, yMin, zMax).tex(uMax, vMax).endVertex();
        tes.draw();
    }

    private void drawSideSquare(Tessellator tes, BufferBuilder buffer, double xMin, double yMin, double zMin, double xMax, double yMax, double zMax) {
        drawSideSquare(tes, buffer, xMin, yMin, zMin, xMax, yMax, zMax, xMax, xMin, zMin, zMax);
    }

    private void drawSideSquare(Tessellator tes, BufferBuilder buffer, double xMin, double yMin, double zMin, double xMax, double yMax, double zMax, double uMin, double uMax, double vMin, double vMax) {
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(xMax, yMin, zMin).tex(uMax, vMin).endVertex();
        buffer.pos(xMax, yMin, zMax).tex(uMax, vMax).endVertex();
        buffer.pos(xMin, yMax, zMax).tex(uMin, vMax).endVertex();
        buffer.pos(xMin, yMax, zMin).tex(uMin, vMin).endVertex();
        tes.draw();
    }
}