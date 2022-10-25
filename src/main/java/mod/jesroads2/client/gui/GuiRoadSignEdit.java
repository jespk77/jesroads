package mod.jesroads2.client.gui;

import java.io.IOException;
import java.util.List;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.sign.BlockSign;
import mod.jesroads2.network.MessageEditSign;
import mod.jesroads2.tileentity.TileEntityRoadSign;
import mod.jesroads2.tileentity.TileEntityRoadSign.SignData;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GuiRoadSignEdit extends GuiBase {

    public static final int ID = 164;

    private final TileEntityRoadSign sign;
    private boolean dirty;

    private final EnumFacing facing;

    public GuiRoadSignEdit(EntityPlayer player, World world, BlockPos pos) {
        super(player, world, pos);

        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityRoadSign) sign = (TileEntityRoadSign) tile;
        else sign = null;
        facing = player.getHorizontalFacing();
        dirty = false;
    }

    @Override
    public void initGui() {
        int id = 1, yPos = 50;
        if (sign == null) {
            System.out.println("[ERROR] invalid tileentity: null");
            Close();
            return;
        }

        List<SignData> data = sign.getData();
        for (int i = 0; i < data.size(); i++) {
            SignData s = data.get(i);
            if (s.editable) {
                addDataLine(i + 1, id, yPos, s);
                yPos += 30;
            }
            id += 4;
        }
        if (!textList.isEmpty()) textList.get(0).setFocused(true);

        buttonList.add(new GuiButton(0, 15, yPos, 30, 20, "add"));
    }

    private void addDataLine(int index, int id, int yPos, SignData s) {
        int xPos = 10;
        buttonList.add(new GuiButton(id, xPos, yPos, 20, 20, "+"));
        xPos += 40;
        buttonList.add(new GuiButton(id, xPos, yPos, 20, 20, "-"));
        xPos += 30;
        buttonList.add(new GuiButton(id + 1, xPos, yPos, 20, 20, "+"));
        xPos += 40;
        buttonList.add(new GuiButton(id + 1, xPos, yPos, 20, 20, "-"));
        xPos += 255;
        buttonList.add(new GuiButton(id + 2, xPos, yPos, 20, 20, "+"));
        xPos += 40;
        buttonList.add(new GuiButton(id + 2, xPos, yPos, 20, 20, "-"));
        xPos += 25;
        if (s.max == 0) {
            buttonList.add(new GuiButton(id + 3, 440, yPos, 20, 20, s.blackout ? "X" : "O"));
            xPos += 30;
        }
        buttonList.add(new GuiButton(-index, xPos, yPos, 20, 20, "del"));

        GuiTextField text = new GuiTextField(id, fontRenderer, 155, yPos, 150, 20),
                color = new GuiTextField(id + 1, fontRenderer, 315, yPos, 50, 20);
        if (s.max > 0) text.setMaxStringLength(s.max);
        text.setText(s.text);
        color.setMaxStringLength(6);
        color.setText(Integer.toHexString(s.color));
        textList.add(text);
        textList.add(color);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        if (dirty) {
            updateSignData();
            sign.markDirty();
            sign.checkForData();
            List<SignData> data = sign.getData();
            JesRoads2.channel.sendToServer(new MessageEditSign(true, data.toArray(new SignData[0]), pos));
        }
    }

    private void updateSignData() {
        List<SignData> signs = sign.getData();
        int signId = 0, textId = 0;
        while (signId < signs.size() && textId < textList.size()) {
            SignData d = signs.get(signId++);
            if (d.editable) {
                d.setText(textList.get(textId++).getText());
                GuiTextField field = textList.get(textId++);
                String color = field.getText();
                boolean valid = true;
                if (BlockSign.colors.containsKey(color)) d.setColor(BlockSign.colors.get(color));
                else {
                    try {
                        d.setColor(Integer.decode(color.startsWith("0x") ? color : "0x" + color));
                    } catch (NumberFormatException e) {
                        valid = false;
                    }
                }
                field.setTextColor(valid ? 0xFFFFFF : 0xFF0000);
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(fontRenderer, "Edit Sign Text:", width / 2, 10, 0xFFFFFF);
        drawCenteredString(fontRenderer, getFacing(), width / 2, 23, 0xAAAAAA);

        drawString(fontRenderer, "xPos", 30, 35, 0xFFFFFF);
        drawString(fontRenderer, "yPos", 100, 35, 0xFFFFFF);
        drawString(fontRenderer, "text", 170, 35, 0xFFFFFF);
        drawString(fontRenderer, "color", 325, 35, 0xFFFFFF);
        drawString(fontRenderer, "size", 395, 35, 0xFFFFFF);

        int yPos = 55;
        for (SignData data : sign.getData()) {
            if (data.editable) {
                drawCenteredString(fontRenderer, String.valueOf(data.xPos), 41, yPos, 0xFFFFFF);
                drawCenteredString(fontRenderer, String.valueOf(data.yPos), 111, yPos, 0xFFFFFF);
                drawCenteredString(fontRenderer, String.valueOf(data.size).substring(0, 3), 405, yPos, 0xFFFFFF);

                yPos += 30;
            }
        }
    }

    private String getFacing() {
        if (facing != null)
            return "^ " + facing + " | <- " + facing.rotateYCCW() + ", " + facing.rotateY() + " -> |";
        else return "";
    }

    @Override
    public void actionPerformed(GuiButton button) {
        dirty = true;
        List<SignData> data = sign.getData();
        if (button.id < 0) {
            try {
                int id = Math.abs(button.id) - 1;
                data.remove(id);
                buttonList.clear();
                textList.clear();
                initGui();
            } catch (Exception ignored) {
            }
            return;
        } else if (button.id == 0) {
            SignData s = new SignData(1, 1, 0xFFFFFF, 2.5F, "", 0);
            data.add(s);
            buttonList.clear();
            textList.clear();
            initGui();
            return;
        }

        int id = (button.id - 1) / 4,
                bid = (button.id - 1) - (4 * id),
                step = GuiScreen.isShiftKeyDown() ? 10 : 2;
        if (bid == 0) { // editing xPos
            if (button.displayString.contains("+")) data.get(id).setPos(step, 0);
            else data.get(id).setPos(-step, 0);
        } else if (bid == 1) { // editing yPos
            if (button.displayString.contains("+")) data.get(id).setPos(0, step);
            else data.get(id).setPos(0, -step);
        } else if (bid == 2) { // editing size
            if (button.displayString.contains("+")) data.get(id).increaseSize(step / 10.f);
            else data.get(id).increaseSize(-step / 10.f);
        } else if (bid == 3) { //editing blackout
            SignData d = data.get(id);
            button.displayString = d.toggleBlackout() ? "X" : "O";
        }
        updateSignData();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        dirty = true;
    }

    @Override
    public void keyTyped(char key, int code) throws IOException {
        super.keyTyped(key, code);

        dirty = true;
        updateSignData();
    }
}
