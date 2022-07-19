package mod.jesroads2.client.gui;

import java.util.Calendar;
import java.util.Locale;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.network.MessageBlockStateUpdate;
import mod.jesroads2.network.MessageTileEntityNBTUpdate;
import mod.jesroads2.tileentity.TileEntityMemory;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GuiMemory extends GuiBase {

    public static final int ID = 163;

    private final TileEntityMemory memory;
    private final Calendar cal;
    private final Calendar cal2;
    private EnumDyeColor color;
    private boolean colorEdited;
    private boolean isOther, hasOther;

    private GuiButton other;

    public GuiMemory(EntityPlayer player, World world, BlockPos pos) {
        super(player, world, pos);

        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityMemory) this.memory = (TileEntityMemory) tile;
        else this.memory = null;

        this.cal = memory.getDate(false);
        this.cal2 = memory.getDate(true);
        this.color = this.memory.getDyeColor();
        this.colorEdited = false;
        this.isOther = false;
        this.hasOther = this.memory.hasOther();
    }

    @Override
    public void initGui() {
        if (memory == null) {
            System.out.println("ERROR - GuiMemory.initGui - invalid memory block tile entity");
            Close();
            return;
        }

        buttonList.add(new GuiButton(0, 55, 35, 20, 20, "+"));
        buttonList.add(new GuiButton(0, 75, 35, 20, 20, "-"));
        buttonList.add(new GuiButton(1, 110, 35, 20, 20, "+"));
        buttonList.add(new GuiButton(1, 130, 35, 20, 20, "-"));
        buttonList.add(new GuiButton(2, 160, 35, 20, 20, "+"));
        buttonList.add(new GuiButton(2, 180, 35, 20, 20, "-"));
        buttonList.add(new GuiButton(3, 15, 105, 55, 20, color.getDyeColorName()));
        buttonList.add(new GuiButton(4, 15, 35, 30, 20, getOtherText()));

        GuiTextField text = new GuiTextField(3, fontRenderer, 15, 70, 250, 20);
        text.setMaxStringLength(45);
        text.setText(memory.title);
        textList.add(text);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        Calendar c = isOther ? cal2 : cal;
        drawCenteredString(fontRenderer, c.getDisplayName(Calendar.MONTH, Calendar.LONG_STANDALONE, Locale.US), 76, 25, 0xFFFFFF);
        drawCenteredString(fontRenderer, String.valueOf(c.get(Calendar.DAY_OF_MONTH)), 130, 25, 0xFFFFFF);
        drawCenteredString(fontRenderer, String.valueOf(c.get(Calendar.YEAR)), 180, 25, 0xFFFFFF);
    }

    @Override
    public void onGuiClosed() {
        memory.setDate(false, cal);
        if (hasOther) memory.setDate(true, cal2);
        memory.checkOrder();
        memory.title = textList.get(0).getText().replace("==", "");
        JesRoads2.channel.sendToServer(new MessageTileEntityNBTUpdate(false, memory.getPos(), memory));

        if (colorEdited) {
            IBlockState s = memory.getStateWithDye(color);
            if (s != null) JesRoads2.channel.sendToServer(new MessageBlockStateUpdate(memory.getPos(), s));
        }
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 3) {
            updateColor();
            button.displayString = color.getDyeColorName();
            return;
        } else if (button.id == 4) {
            isOther = !isOther;
            button.displayString = getOtherText();
            return;
        }

        int field = -1;
        int amount = isAltKeyDown() ? 6 : GuiScreen.isShiftKeyDown() ? 2 : 1;
        if (button.displayString.contains("-")) amount = -amount;
        switch (button.id) {
            case 0: {
                field = Calendar.MONTH;
                break;
            }
            case 1: {
                field = Calendar.DAY_OF_MONTH;
                break;
            }
            case 2: {
                field = Calendar.YEAR;
                break;
            }
        }
        if (field >= 0) (isOther ? cal2 : cal).add(field, amount);
        if (isOther)
            hasOther = memory.hasOther() || cal.getTimeInMillis() != cal2.getTimeInMillis();
    }

    private String getOtherText() {
        return isOther ? "to" : "from";
    }

    private void updateColor() {
        boolean dir = GuiScreen.isShiftKeyDown();
        if (dir && color.getMetadata() == 0) color = EnumDyeColor.byMetadata(15);
        else color = EnumDyeColor.byMetadata(dir ? color.getMetadata() - 1 : color.getMetadata() + 1);
        colorEdited = true;
    }
}