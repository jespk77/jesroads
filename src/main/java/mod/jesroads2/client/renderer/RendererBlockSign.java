package mod.jesroads2.client.renderer;

import mod.jesroads2.JesRoads2;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import mod.jesroads2.block.BlockBaseHorizontal;
import mod.jesroads2.block.sign.BlockSign;
import mod.jesroads2.block.sign.BlockSign.EnumSignType;
import mod.jesroads2.tileentity.TileEntityRoadSign;
import mod.jesroads2.tileentity.TileEntityRoadSign.SignData;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class RendererBlockSign extends TileEntitySpecialRenderer<TileEntityRoadSign> {
    private static final float SCALE_FACTOR = 0.01F;

    public static final ResourceLocation[] fontResourceLocations = new ResourceLocation[]{
            new ResourceLocation(JesRoads2.modid, "textures/font/highway_gothic.png"),
    };

    private final FontRenderer[] fontRenderers;

    public RendererBlockSign(){
        Minecraft mc = Minecraft.getMinecraft();
        fontRenderers = new FontRenderer[fontResourceLocations.length + 1];
        fontRenderers[0] = mc.fontRenderer;
        for(int i = 0; i < fontResourceLocations.length; i++) {
            FontRenderer fontRenderer = new FontRenderer(mc.gameSettings, fontResourceLocations[i], mc.renderEngine, false);
            fontRenderer.onResourceManagerReload(mc.getResourceManager());
            fontRenderers[i+1] = fontRenderer;
        }
    }

    @Override
    public void render(TileEntityRoadSign sign, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        IBlockState state = sign.getState();
        if (!(state.getBlock() instanceof BlockSign) || state.getValue(BlockSign.blackout)) return;

        float tx, ty = 0.88F, tz, rot;
        switch (state.getValue(BlockBaseHorizontal.facing)) {
            case NORTH: {
                tx = 0;
                tz = 0.57F;
                rot = 0;
                break;
            }
            case SOUTH: {
                tx = 1F;
                tz = 0.43F;
                rot = 180;
                break;
            }
            case EAST: {
                tx = 0.43F;
                tz = 0;
                rot = -90;
                break;
            }
            case WEST: {
                tx = 0.57F;
                tz = 1;
                rot = 90;
                break;
            }
            default: {
                tx = 0;
                tz = 0;
                rot = 0;
                break;
            }
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + tx, y + ty, z + tz);
        GlStateManager.rotate(rot, 0, 1, 0);
        Block b = state.getBlock();
        if (b instanceof BlockSign) {
            EnumSignType tp = ((BlockSign) b).type;
            if (tp.isDiagonal()) {
                float fx;
                if (tp.ordinal() == 7) fx = -0.27F;
                else fx = -0.1F;
                GlStateManager.rotate(45.F, 0, 1, 0);
                GlStateManager.translate(fx, 0.F, 0.4F);
            }
        }
        GlStateManager.disableLighting();

        for (SignData s : sign.getData())
            renderText(fontRenderers[sign.getFontVersion()], s);

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    public void renderText(FontRenderer fontRenderer, SignData data) {
        float size = data.textSize;
        float fontSize = size * SCALE_FACTOR;
        int x = (int) (data.xPos / size);
        int y = (int) (data.yPos / size);

        GL11.glPushMatrix();
        GL11.glRotatef(180.F, 1.F, 0.F, 0.F);
        GL11.glScalef(fontSize, fontSize, fontSize);
        String text = data.getText();
        if (data.blackout) {
            x -= 1;
            y -= 1;
            size = 1.7F * size + 3.8F;
            double length = fontRenderer.getStringWidth(text) + 1;
            Tessellator tes = Tessellator.getInstance();
            BufferBuilder buffer = tes.getBuffer();
            GlStateManager.color(.1F, .1F, .1F);
            GlStateManager.disableTexture2D();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
            buffer.pos(x, y + size, 0).endVertex();
            buffer.pos(x + length, y + size, 0).endVertex();
            buffer.pos(x + length, y, 0).endVertex();
            buffer.pos(x, y, 0).endVertex();
            tes.draw();
            GlStateManager.enableTexture2D();
        } else fontRenderer.drawString(text, x, y, data.textColor);
        GL11.glPopMatrix();
    }
}