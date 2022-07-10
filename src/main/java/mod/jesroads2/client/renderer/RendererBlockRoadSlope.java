package mod.jesroads2.client.renderer;

import org.lwjgl.opengl.GL11;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.road.BlockRoad;
import mod.jesroads2.block.road.BlockRoadSlope;
import mod.jesroads2.block.road.BlockRoadSlope.EnumSlopeShape;
import mod.jesroads2.tileentity.TileEntityRoadSlope;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RendererBlockRoadSlope extends TileEntitySpecialRenderer<TileEntityRoadSlope> {
    @Override
    public void render(TileEntityRoadSlope slope, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        World world = slope.getWorld();
        IBlockState s = slope.getState();
        BlockPos pos = slope.getPos();
        Block b = s.getBlock();
        if (!(b instanceof BlockRoadSlope)) return;
        BlockRoadSlope block = (BlockRoadSlope) b;

        String end;
        try {
            end = s.getValue(BlockRoad.shiny) ? "_new" : "_aged";
        } catch (IllegalArgumentException e) {
            return;
        }
        EnumFacing dir;
        try {
            dir = s.getValue(BlockRoadSlope.facing);
        } catch (IllegalArgumentException e) {
            return;
        }
        EnumSlopeShape tp = slope.getShape();
        if (tp == null) return;

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.color(0.7F, 0.7F, 0.7F);

        Tessellator tes = Tessellator.getInstance();
        BufferBuilder buffer = tes.getBuffer();
        VertexFormat def = DefaultVertexFormats.POSITION_TEX;

        String tname = "paved", lname = block.tname;
        boolean rot = block.rotated, isNorthSouth = false;
        ResourceLocation top = new ResourceLocation(JesRoads2.modid, "textures/blocks/road/" + tname + end + ".png"),
                line = lname != null ? new ResourceLocation(JesRoads2.modid, "textures/blocks/road/" + lname + end + ".png") : null,
                side = new ResourceLocation(JesRoads2.modid, "textures/blocks/road/paved" + end + ".png"),
                bottom = new ResourceLocation(JesRoads2.modid, "textures/blocks/concrete.png");

        float placeDir = dir.getHorizontalIndex() * (-90.F);
        GlStateManager.rotate(placeDir, 0.F, 1.F, 0.F);
        EnumFacing north = EnumFacing.NORTH, east = EnumFacing.EAST, west = EnumFacing.WEST;
        switch (dir) {
            case SOUTH: {
                north = north.getOpposite();
                east = east.getOpposite();
                west = west.getOpposite();
                break;
            }
            case NORTH: {
                GlStateManager.translate(-1.F, 0, -1.F);
                break;
            }
            case WEST: {
                GlStateManager.translate(0.F, 0.F, -1.F);
                north = EnumFacing.WEST;
                east = EnumFacing.NORTH;
                west = EnumFacing.SOUTH;
                break;
            }
            case EAST: {
                GlStateManager.translate(-1.F, 0.F, 0.F);
                north = EnumFacing.EAST;
                east = EnumFacing.SOUTH;
                west = EnumFacing.NORTH;
                break;
            }
        }

        double lower = 0, lowerUV = 0, higher = 0, higherUV = 0;
        switch (tp) {
            case SINGLE: {
                lower = 0;
                lowerUV = 0;
                higher = 1;
                higherUV = 1;
                break;
            }
            case DOUBLE_BOTTOM: {
                lower = 0;
                lowerUV = 0;
                higher = 0.5;
                higherUV = 0.5;
                break;
            }
            case DOUBLE_TOP: {
                lower = 0.5;
                lowerUV = 0.5;
                higher = 1;
                higherUV = 1;
                break;
            }
            default:
                break;
        }

        int[][] uvTop;
        if (isNorthSouth) {
            if (rot) uvTop = new int[][]{{1, 0}, {1, 1}, {0, 0}, {0, 1}};
            else uvTop = new int[][]{{0, 0}, {0, 1}, {1, 0}, {1, 1}};
        } else {
            if (rot) uvTop = new int[][]{{0, 1}, {0, 0}, {1, 1}, {1, 0}};
            else uvTop = new int[][]{{1, 1}, {1, 0}, {0, 1}, {0, 0}};
        }

        bindTexture(top);
        buffer.begin(GL11.GL_QUADS, def);
        buffer.pos(0, lower, 0).tex(uvTop[1][0], uvTop[1][1]).endVertex();
        buffer.pos(0, higher, 1).tex(uvTop[0][0], uvTop[0][1]).endVertex();
        buffer.pos(1, higher, 1).tex(uvTop[2][0], uvTop[2][1]).endVertex();
        buffer.pos(1, lower, 0).tex(uvTop[3][0], uvTop[3][1]).endVertex();
        tes.draw();

        if (line != null) {
            bindTexture(line);
            boolean right = block.rotated != ((BlockRoadSlope.EnumSlopeType) block.roadType).inverted;
            double min = right ? 0.91 : 0.01, max = right ? 0.99 : 0.09;
            buffer.begin(GL11.GL_QUADS, def);
            buffer.pos(min, lower + 0.01, 0).tex(0.1, 1).endVertex();
            buffer.pos(min, higher + 0.01, 1).tex(0.1, 0).endVertex();
            buffer.pos(max, higher + 0.01, 1).tex(0, 0).endVertex();
            buffer.pos(max, lower + 0.01, 0).tex(0, 1).endVertex();
            tes.draw();
        }

        bindTexture(side);
        boolean drawEast = !isNormalBlock(world, pos.offset(east));
        boolean drawWest = !isNormalBlock(world, pos.offset(west));

        if (drawEast || drawWest) {
            buffer.begin(GL11.GL_TRIANGLES, def);
            if (drawEast) {
                buffer.pos(0, lower, 0).tex(0, lowerUV).endVertex();
                buffer.pos(0, lower, 1).tex(0, higherUV).endVertex();
                buffer.pos(0, higher, 1).tex(1, higherUV).endVertex();
            }

            if (drawWest) {
                buffer.pos(1, lower, 1).tex(0, lowerUV).endVertex();
                buffer.pos(1, lower, 0).tex(0, higherUV).endVertex();
                buffer.pos(1, higher, 1).tex(1, higherUV).endVertex();
            }
            tes.draw();
        }

        if (!isNormalBlock(world, pos.offset(north))) {
            buffer.begin(GL11.GL_QUADS, def);
            buffer.pos(1, lower, 1).tex(0, lowerUV).endVertex();
            buffer.pos(1, higher, 1).tex(0, higherUV).endVertex();
            buffer.pos(0, higher, 1).tex(1, higherUV).endVertex();
            buffer.pos(0, lower, 1).tex(1, lowerUV).endVertex();
            tes.draw();
        }

        if (tp == EnumSlopeShape.DOUBLE_TOP && (drawEast || drawWest)) {
            buffer.begin(GL11.GL_QUADS, def);
            buffer.pos(1, 0, 0).tex(0, 0).endVertex();
            buffer.pos(1, 0.5, 0).tex(0, 0.5).endVertex();
            buffer.pos(1, 0.5, 1).tex(1, 0.5).endVertex();
            buffer.pos(1, 0, 1).tex(1, 0).endVertex();

            buffer.pos(0, 0, 0).tex(0, 0).endVertex();
            buffer.pos(0, 0, 1).tex(0, 0.5).endVertex();
            buffer.pos(0, 0.5, 1).tex(1, 0.5).endVertex();
            buffer.pos(0, 0.5, 0).tex(1, 0).endVertex();

            buffer.pos(1, 0, 1).tex(0, 0).endVertex();
            buffer.pos(1, 0.5, 1).tex(1, 0).endVertex();
            buffer.pos(0, 0.5, 1).tex(1, 0.5).endVertex();
            buffer.pos(0, 0, 1).tex(0, 0.5).endVertex();

            buffer.pos(1, 0, 0).tex(0, 0).endVertex();
            buffer.pos(0, 0, 0).tex(1, 0).endVertex();
            buffer.pos(0, 0.5, 0).tex(1, 0.5).endVertex();
            buffer.pos(1, 0.5, 0).tex(0, 0.5).endVertex();
            tes.draw();
            lower = 0;
        }

        bindTexture(bottom);
        if (!isNormalBlock(world, pos.down())) {
            buffer.begin(GL11.GL_QUADS, def);
            buffer.pos(0, lower, 0).tex(0, 0).endVertex();
            buffer.pos(1, lower, 0).tex(0, 1).endVertex();
            buffer.pos(1, lower, 1).tex(1, 1).endVertex();
            buffer.pos(0, lower, 1).tex(1, 0).endVertex();
            tes.draw();
        }

        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    private boolean isNormalBlock(World world, BlockPos pos) {
        return world.isBlockFullCube(pos);
    }
}