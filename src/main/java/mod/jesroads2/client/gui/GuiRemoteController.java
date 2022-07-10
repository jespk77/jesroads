package mod.jesroads2.client.gui;

import java.io.IOException;
import java.util.List;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.item.ItemRemoteControl;
import mod.jesroads2.network.MessageItemNBTUpdate;
import mod.jesroads2.util.OtherUtils;
import mod.jesroads2.world.storage.RemoteControllerData;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GuiRemoteController extends GuiBase {

    public static final int ID = 162;

    private static final int SELECT_ID = 0, REMOVE_ID = 1, ADD_ID = 2, CANCEL_ID = 3;

    private static class Selector extends GuiSlot {

        private final GuiRemoteController screen;
        private List<String> controllerList;
        public String selected;

        public Selector(GuiRemoteController screen) {
            super(screen.mc, 500, 250, 25, 210, 18);
            this.screen = screen;
            selected = "";
            refreshList();
        }

        public void refreshList() {
            controllerList = screen.data.getControllerSet();
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public void setSelectedElement(String name) {
            int index = controllerList.indexOf(name);
            if (index >= 0) {
                selectedElement = index;
                selected = name;
            }
        }

        @Override
        protected int getSize() {
            return controllerList.size();
        }

        public void mouseClicked(int mouseX, int mouseY, int button) {
            this.mouseX = mouseX;
            this.mouseY = mouseY;
        }

        @Override
        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
            selected = controllerList.get(slotIndex);
        }

        @Override
        protected boolean isSelected(int slotIndex) {
            return selected.equals(controllerList.get(slotIndex));
        }

        @Override
        protected void drawSlot(int slotIndex, int insideLeft, int insideTop, int mouseX, int mouseY, int top, float bottom) {
            if (inRange(insideTop))
                screen.fontRenderer.drawString(controllerList.get(slotIndex), insideLeft, insideTop + 3, 0xFFFFFF);
        }

        @Override
        protected void drawSelectionBox(int insideLeft, int insideTop, int mouseXIn, int mouseYIn, float partialTicks) {
            showSelectionBox = inRange(insideTop + selectedElement * slotHeight + headerPadding);
            super.drawSelectionBox(insideLeft, insideTop, mouseXIn, mouseYIn, partialTicks);
        }

        private boolean inRange(int insideTop) {
            return insideTop > top && insideTop + 9 < bottom;
        }

        @Override
        protected void drawBackground() {
        }

        @Override
        protected void drawContainerBackground(Tessellator tes) {
        }

        @Override
        protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha) {
        }

    }

    private final RemoteControllerData data;
    private boolean adding;

    private final ItemStack stack;
    private final EnumHand hand;
    private final Selector selector;

    private final GuiButton select = new GuiButton(SELECT_ID, 30, 230, "Set Controller");
    private final GuiButton remove = new GuiButton(REMOVE_ID, select.x + select.width + 10, 230, "Remove Controller");

    public GuiRemoteController(EntityPlayer player, World world, BlockPos pos) {
        super(player, world, pos);

        data = ItemRemoteControl.data;
        boolean exist = data.contains(pos);
        adding = pos != null && pos.getY() > -1 && !exist;

        hand = OtherUtils.getUsedHand(player, JesRoads2.items.remote_controller.getClass());
        stack = player.getHeldItem(hand);
        selector = new Selector(this);
        if (exist) selector.setSelectedElement(data.getControllerName(pos));
    }

    @Override
    public void initGui() {
        buttonList.add(select);
        buttonList.add(remove);
        select.x = width / 2 - select.width - 10;
        select.y = height - 25;
        remove.x = width / 2 + 10;
        remove.y = height - 25;
        selector.top = height / 8;
        selector.bottom = selector.top * 7;

        if (adding) {
            select.visible = false;
            remove.visible = false;
            selector.setVisible(false);

            textList.add(new GuiTextField(0, fontRenderer, 10, 30, 200, 20));
            buttonList.add(new GuiButton(ADD_ID, 10, 60, "Add controller"));
            buttonList.add(new GuiButton(CANCEL_ID, 10, 85, "Cancel"));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        selector.drawScreen(mouseX, mouseY, partialTicks);
        if (adding)
            drawCenteredString(fontRenderer, "Enter name for controller: ", width / 2, 10, 0xFFFFFF);
        else drawCenteredString(fontRenderer, "Select controller: ", width / 2, 10, 0xFFFFFF);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        selector.handleMouseInput();
    }

    @Override
    public void actionPerformed(GuiButton button) {
        switch (button.id) {
            case SELECT_ID: {
                NBTTagCompound nbt = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
                BlockPos pos = data.getController(selector.selected);
                if (selector.selected.length() > 0 && pos != null) {
                    nbt.setString("destN", selector.selected);
                    nbt.setInteger("destX", pos.getX());
                    nbt.setInteger("destY", pos.getY());
                    nbt.setInteger("destZ", pos.getZ());
                    nbt.setInteger("status", 0);
                    stack.setTagCompound(nbt);
                    JesRoads2.channel.sendToServer(new MessageItemNBTUpdate(nbt, stack.getItem(), hand));
                }
                mc.displayGuiScreen(null);
                return;
            }
            case REMOVE_ID: {
                data.removeController(selector.selected);
                if (stack.hasTagCompound()) {
                    NBTTagCompound nbt = stack.getTagCompound();
                    if (nbt.getString("destN").equals(selector.selected)) stack.setTagCompound(null);
                }
                selector.refreshList();
                return;
            }
            case ADD_ID: {
                String name = textList.get(0).getText();
                if (name.length() > 0) data.addController(name, pos);
                return;
            }
            case CANCEL_ID: {
                textList.get(0).setVisible(false);
                buttonList.get(2).visible = false;
                buttonList.get(3).visible = false;
                select.visible = true;
                remove.visible = true;
                selector.setVisible(true);
                selector.refreshList();
                adding = false;
                return;
            }
            default:
        }
    }
}
