package mod.jesroads2.client.gui;

import java.io.IOException;
import java.util.List;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.sign.BlockSign;
import mod.jesroads2.client.gui.template.GuiTemplateManager;
import mod.jesroads2.client.renderer.RendererBlockSign;
import mod.jesroads2.network.MessageTileEntityNBTUpdate;
import mod.jesroads2.tileentity.TileEntityRoadSign;
import mod.jesroads2.tileentity.TileEntityRoadSign.SignData;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class GuiRoadSignEdit extends GuiBase {
    public static final int ID = 164;
    private static final int MAX_SIZE = 6;

    private static final GuiButton addButton = new GuiButton(0, 15, 0, 30, 20, "add"),
        openTemplateManagerButton = new GuiButton(-34, 10, 10, 80, 20, "Templates"),
        signFontButton = new GuiButton(-35, 10, 10, 50, 20, "Font");

    private final TileEntityRoadSign sign;
    private final EnumFacing facing;
    private boolean dirty;

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
            System.err.println("[ERROR] invalid tileentity: null");
            Close();
            return;
        }

        List<SignData> data = sign.getData();
        for (int i = 0; i < data.size(); i++) {
            SignData s = data.get(i);
            if (s.isEditable) {
                addDataLine(i + 1, id, yPos, s);
                yPos += 30;
            }
            id += 7;
        }
        if (!textList.isEmpty()) textList.get(0).setFocused(true);

        addButton.y = yPos;
        addButton.enabled = data.size() < MAX_SIZE;
        buttonList.add(addButton);
        buttonList.add(openTemplateManagerButton);
        signFontButton.x = width - signFontButton.width - 10;
        signFontButton.displayString = "Font #" + sign.getFontVersion();
        buttonList.add(signFontButton);
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
        buttonList.add(new GuiButton(id + 3, xPos, yPos, 20, 20, s.isBold() ? TextFormatting.BOLD + "B" : "B"));
        xPos += 25;
        buttonList.add(new GuiButton(id + 4, xPos, yPos, 20, 20, s.isUnderline() ? TextFormatting.UNDERLINE + "U" : "U"));
        xPos += 25;
        buttonList.add(new GuiButton(id + 5, xPos, yPos, 20, 20, s.isItalic() ? TextFormatting.ITALIC + "I" : "I"));
        xPos += 25;

        if (s.max == 0) {
            buttonList.add(new GuiButton(id + 6, xPos, yPos, 20, 20, s.blackout ? "-" : "O"));
            xPos += 30;
        }
        buttonList.add(new GuiButton(-index, xPos, yPos, 20, 20, "X"));

        GuiTextField text = new GuiTextField(id, fontRenderer, 155, yPos, 150, 20),
                color = new GuiTextField(id + 1, fontRenderer, 315, yPos, 50, 20);
        if (s.max > 0) text.setMaxStringLength(s.max);
        text.setText(s.data);
        color.setMaxStringLength(6);
        color.setText(Integer.toHexString(s.textColor));
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
            JesRoads2.channel.sendToServer(new MessageTileEntityNBTUpdate(false, pos, sign));
        }
    }

    private void updateSignData() {
        List<SignData> signs = sign.getData();
        int signId = 0, textId = 0;
        while (signId < signs.size() && textId < textList.size()) {
            SignData d = signs.get(signId++);
            if (d.isEditable) {
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
        drawCenteredString(fontRenderer, "Sign Editor", width / 2, 10, 0xFFFFFF);
        drawCenteredString(fontRenderer, getFacing(), width / 2, 23, 0xAAAAAA);

        drawString(fontRenderer, "X", 35, 35, 0xFFFFFF);
        drawString(fontRenderer, "Y", 105, 35, 0xFFFFFF);
        drawString(fontRenderer, "Text", 170, 35, 0xFFFFFF);
        drawString(fontRenderer, "Color", 327, 35, 0xFFFFFF);
        drawString(fontRenderer, "Size", 395, 35, 0xFFFFFF);
        drawString(fontRenderer, "Effects", 460, 35, 0xFFFFFF);

        int yPos = 55;
        for (SignData data : sign.getData()) {
            if (data.isEditable) {
                drawCenteredString(fontRenderer, String.valueOf(data.xPos), 41, yPos, 0xFFFFFF);
                drawCenteredString(fontRenderer, String.valueOf(data.yPos), 111, yPos, 0xFFFFFF);
                drawCenteredString(fontRenderer, String.valueOf(data.textSize).substring(0, 3), 405, yPos, 0xFFFFFF);

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
        if (button.id == addButton.id) {
            int x = 1, y = 1, color = 0xAAAAAA;
            float size = 3.5F;
            if(data.size() > 0){
                SignData last = data.get(data.size() - 1);
                x = last.xPos;
                y = last.yPos + Math.round(last.textSize * 10);
                if(GuiScreen.isShiftKeyDown()){
                    color = last.textColor;
                    size = last.textSize;
                }
            }

            SignData s = new SignData(x, y, color, size, "", 0);
            data.add(s);
            buttonList.clear();
            textList.clear();
            initGui();
            return;
        } else if(button.id == openTemplateManagerButton.id){
            player.openGui(JesRoads2.instance, GuiTemplateManager.ID, world, pos.getX(), pos.getY(), pos.getZ());
            return;
        } else if(button.id == signFontButton.id){
            int version = (sign.getFontVersion() + 1) % (RendererBlockSign.fontResourceLocations.length + 1);
            sign.setFontVersion(version);
            button.displayString = "Font #" + version;
            return;
        }

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
        }

        int id = (button.id - 1) / 7,
                bid = (button.id - 1) - (7 * id),
                step = GuiScreen.isShiftKeyDown() ? 10 : 2;
        SignData s = data.get(id);
        if (bid == 0) { // editing xPos
            if (button.displayString.contains("+")) s.setPos(step, 0);
            else s.setPos(-step, 0);
        } else if (bid == 1) { // editing yPos
            if (button.displayString.contains("+")) s.setPos(0, step);
            else s.setPos(0, -step);
        } else if (bid == 2) { // editing size
            if (button.displayString.contains("+")) s.increaseSize(step / 10.f);
            else s.increaseSize(-step / 10.f);
        } else if(bid == 3){ // editing bold
            button.displayString = s.toggleBold() ? TextFormatting.BOLD + "B" : "B";
        } else if(bid == 4){ // editing underline
            button.displayString = s.toggleUnderline() ? TextFormatting.UNDERLINE + "U" : "U";
        } else if(bid == 5){ // editing italic
            button.displayString = s.toggleItalic() ? TextFormatting.ITALIC + "I" : "I";
        } else if (bid == 6) { //editing blackout
            button.displayString = s.toggleBlackout() ? "-" : "O";
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