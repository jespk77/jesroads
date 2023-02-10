package mod.jesroads2.client.gui.template;

import net.minecraft.client.gui.GuiListExtended;

public class GuiTemplateItem implements GuiListExtended.IGuiListEntry {
    public final String templateName;
    private final GuiTemplateManager parentGui;
    private final GuiTemplateList parentList;

    public GuiTemplateItem(GuiTemplateManager parent, GuiTemplateList list, String name){
        parentGui = parent;
        parentList = list;
        templateName = name;
    }

    @Override
    public void updatePosition(int slotIndex, int x, int y, float partialTicks) {

    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
        parentGui.mc.fontRenderer.drawString(templateName, x + 5, y + (slotHeight / 4), 0xFFFFFF);
    }

    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
        parentList.setSelectIndex(slotIndex);
        return true;
    }

    @Override
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {

    }
}