package mod.jesroads2.client.gui.template;

import mod.jesroads2.world.SignTemplateStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import java.util.ArrayList;
import java.util.List;

public class GuiTemplateList extends GuiListExtended {
    public static final SignTemplateStorage templateStorage = SignTemplateStorage.getInstance();

    private final GuiTemplateManager parentGui;

    private final List<GuiTemplateItem> templates;

    private int selectedIndex = -1;

    public GuiTemplateList(Minecraft mc, int width, int height, int top, int bottom, int slotHeight, GuiTemplateManager parent) {
        super(mc, width, height, top, bottom, slotHeight);

        parentGui = parent;
        templates = new ArrayList<>();
        refreshTemplateList();
    }

    public void refreshTemplateList(){
        refreshTemplateList("");
    }

    public void refreshTemplateList(String filter){
        templates.clear();
        for(String template : templateStorage.getTemplateNames()){
            if(template.toLowerCase().contains(filter.toLowerCase())) {
                templates.add(new GuiTemplateItem(parentGui, this, template));
            }
        }
    }

    @Override
    public GuiTemplateItem getListEntry(int index) {
        return templates.get(index);
    }

    @Override
    protected int getSize() {
        return templates.size();
    }

    @Override
    protected boolean isSelected(int index){
        return index == selectedIndex;
    }

    @Override
    protected void drawContainerBackground(Tessellator tessellator){
        BufferBuilder buffer = tessellator.getBuffer();
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.disableTexture2D();
        float f = 32.0F;
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(left,  bottom, 0.0D).tex(((float)left  / 32F), ((float)(bottom + (int)amountScrolled) / f)).color(10, 10, 10, 255).endVertex();
        buffer.pos(right, bottom, 0.0D).tex(((float)right / 32F), ((float)(bottom + (int)amountScrolled) / f)).color(10, 10, 10, 255).endVertex();
        buffer.pos(right, top,    0.0D).tex(((float)right / 32F), ((float)(top    + (int)amountScrolled) / f)).color(10, 10, 10, 255).endVertex();
        buffer.pos(left,  top,    0.0D).tex(((float)left  / 32F), ((float)(top    + (int)amountScrolled) / f)).color(10, 10, 10, 255).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        int insideLeft = left + width / 2 - getListWidth() / 2 + 2,
                insideTop = top + 4 - (int)amountScrolled;
        drawContainerBackground(Tessellator.getInstance());
        drawSelectionBox(insideLeft, insideTop, mouseX, mouseY, partialTicks);
    }

    public void setSelectIndex(int index){
        selectedIndex = index;
    }

    public String getSelectedTemplateName(){
        return selectedIndex >= 0 && selectedIndex < templates.size() ? templates.get(selectedIndex).templateName : "";
    }
}