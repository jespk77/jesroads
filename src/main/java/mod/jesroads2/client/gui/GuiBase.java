package mod.jesroads2.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class GuiBase extends GuiScreen {

    protected final List<GuiTextField> textList = new ArrayList<>();

    protected final EntityPlayer player;
    protected final World world;
    protected final BlockPos pos;
    protected boolean dirty;

    public GuiBase(EntityPlayer player, World world, BlockPos pos) {
        this.player = player;
        this.world = world;
        this.pos = pos;
        this.dirty = false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        for (GuiTextField text : textList)
            text.drawTextBox();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        for (GuiTextField text : textList)
            text.updateCursorCounter();
    }

    @Override
    public void keyTyped(char key, int code) throws IOException {
        super.keyTyped(key, code);
        int count = 0, focused = -1;
        for (GuiTextField text : textList) {
            text.textboxKeyTyped(key, code);
            if (text.isFocused() && focused == -1) focused = count;
            count++;
        }

        if (focused >= 0) dirty = true;
        if (focused >= 0 && code == 15) {
            textList.get(focused).setFocused(false);
            if (focused + 1 < textList.size()) focused++;
            else focused = 0;
            textList.get(focused).setFocused(true);
        } else if (focused == -1 && code == 18) mc.displayGuiScreen(null);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);

        boolean isField = false;
        for (GuiTextField text : textList) {
            if (mouseX >= text.x && mouseX < text.x + text.width && mouseY >= text.y && mouseY < text.y + text.height) {
                if (button == 1) text.setText("");
                isField = true;
            }
            text.mouseClicked(mouseX, mouseY, button);
        }

        if (!isField && button == 1) {
            mc.displayGuiScreen(null);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}