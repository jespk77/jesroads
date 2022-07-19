package mod.jesroads2.client.gui;

import java.util.Arrays;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.item.ItemBinder;
import mod.jesroads2.network.MessageTileEntity;
import mod.jesroads2.tileentity.TileEntityFloodlightController;
import mod.jesroads2.util.NBTUtils;
import mod.jesroads2.util.OtherUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GuiFloodlightController extends GuiBase {
    public static final int ID = 157;

    private final GuiButton[] toggle, delete;
    private GuiTextField newGroup;

    private final TileEntityFloodlightController controller;
    private final String[] groupSet;
    private BlockPos bind;
    private int middle;
    private boolean ignoreRedstone;

    public GuiFloodlightController(EntityPlayer player, World world, BlockPos pos) {
        super(player, world, pos);

        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityFloodlightController) controller = (TileEntityFloodlightController) tile;
        else throw new IllegalArgumentException("[ERROR] GuiFloodlightController - invalid TileEntity for pos: " + pos);

        groupSet = controller.getGroupKeys().toArray(new String[controller.getGroupCount()]);
        Arrays.sort(groupSet);
        toggle = new GuiButton[controller.getGroupCount()];
        delete = new GuiButton[controller.getGroupCount()];
        ignoreRedstone = false;//controller.ignoresRedstone();
    }

    @Override
    public void initGui() {
        middle = width / 2;
        EnumHand hand = OtherUtils.getUsedHand(player, ItemBinder.class);
        ItemStack stack = hand != null ? player.getHeldItem(hand) : null;
        if (stack != null && stack.hasTagCompound())
            bind = NBTUtils.readBlockPos(stack.getSubCompound("bind_pos"));
        else bind = null;

        if (bind != null) {
            int group;
            for (group = 0; group < toggle.length; group++) {
                int xPos = group < 7 ? middle - 95 : middle + 115,
                        yPos = 20 + (30 * (group % 7));
                toggle[group] = new GuiButton(group, xPos, yPos, 40, 20, "add");
                buttonList.add(toggle[group]);
            }
            if (!controller.groupLimitReached()) {
                newGroup = new GuiTextField(-1, fontRenderer, middle - 85, 240, 110, 20);
                newGroup.setMaxStringLength(17);
                newGroup.setFocused(true);
                textList.add(newGroup);
                GuiButton setNew = new GuiButton(-1, middle + 35, 240, 65, 20, "create/add");
                buttonList.add(setNew);
            }
        } else {
            GuiButton setTrue = new GuiButton(-1, middle - 65, 23, 60, 20, "all on");
            buttonList.add(setTrue);
            GuiButton setFalse = new GuiButton(-2, middle + 5, 23, 60, 20, "all off");
            buttonList.add(setFalse);
            int group = 0;
            for (String id : groupSet) {
                int xPos = group < 7 ? middle - 30 : middle + 190,
                        yPos = 50 + (30 * (group % 7));
                toggle[group] = new GuiButton(group, xPos - 50, yPos, 40, 20, controller.isGroupEnabled(id) ? "on" : "off");
                buttonList.add(toggle[group]);

                delete[group] = new GuiButton(group, xPos, yPos, 20, 20, "X");
                buttonList.add(delete[group]);
                group++;
            }
        }
    }

    @Override
    public void actionPerformed(GuiButton button) {
        int group = button.id;
        boolean quit = false, sneak = false;//GuiScreen.isShiftKeyDown();
        NBTTagCompound nbt = new NBTTagCompound();
        if (bind != null) {
            nbt.setString("0method_call", "addToGroup");
            nbt.setString("1group", group == -1 ? newGroup.getText() : getGroupName(group));
            nbt.setInteger("2setX", bind.getX());
            nbt.setInteger("3setY", bind.getY());
            nbt.setInteger("4setZ", bind.getZ());
            quit = true;
        } else if (group == -1) {
            nbt.setString("0method_call", "setAll");
            nbt.setBoolean("1enabled", true);
            nbt.setBoolean("2ignore", true);
            if (sneak) ignoreRedstone = !ignoreRedstone;
            for (GuiButton guiButton : toggle) guiButton.displayString = "on";
        } else if (group == -2) {
            nbt.setString("0method_call", "setAll");
            nbt.setBoolean("1enabled", false);
            nbt.setBoolean("2ignore", false);
            if (sneak) ignoreRedstone = !ignoreRedstone;
            for (GuiButton guiButton : toggle) guiButton.displayString = "off";
        } else if (button.displayString.equals("X")) {
            nbt.setString("0method_call", "removeGroup");
            nbt.setString("1group", getGroupName(group));
            toggle[group].visible = false;
            delete[group].visible = false;
            quit = true;
        } else if (button.displayString.equals("on")) {
            nbt.setString("0method_call", "setGroup");
            nbt.setString("1group", groupSet[group]);
            nbt.setBoolean("2enabled", false);
            nbt.setBoolean("3ignore", sneak);
            toggle[group].displayString = "off";
            if (sneak) updateGroupDisplayName(group);
        } else if (button.displayString.equals("off")) {
            nbt.setString("0method_call", "setGroup");
            nbt.setString("1group", groupSet[group]);
            nbt.setBoolean("2enabled", true);
            nbt.setBoolean("3ignore", sneak);
            toggle[group].displayString = "on";
            if (sneak) updateGroupDisplayName(group);
        } else System.out.println("WARNING unknown button text: " + button.displayString + " for group " + group);

        if (!nbt.hasNoTags()) {
            System.out.println(nbt);
            JesRoads2.handlerOverlay.getMessage().addMessage((String) controller.processMessage(nbt));
            JesRoads2.channel.sendToServer(new MessageTileEntity(controller, nbt));
        }
        if (quit) Close();
    }

    private String getGroupName(int index) {
        if (index >= 0 && index < groupSet.length) {
            String g = groupSet[index];
            if (g.startsWith("< ") && g.endsWith(" >")) return g.substring(2, g.length() - 1);
            else return g;
        } else return "";
    }

    private void updateGroupDisplayName(int index) {
        if (index >= 0 && index < groupSet.length) {
            String g = groupSet[index];
            if (g.startsWith("< ") && g.endsWith(" >")) groupSet[index] = g.substring(2, g.length() - 1);
            else groupSet[index] = "< " + g + " >";
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTick) {
        super.drawScreen(mouseX, mouseY, partialTick);

        drawCenteredString(fontRenderer, bind != null ? "Select group to add light" : ignoreRedstone ? "< Floodlight Controller > Options" : "Floodlight Controller Options", width / 2, 10, 0xFFFFFF);
        int group = 0;
        boolean binding = bind != null;
        for (String key : groupSet) {
            int xPos = group < 7 ? middle - 155 : middle + 60,
                    yPos = (binding ? 26 : 56) + (30 * (group % 7));
            drawCenteredString(fontRenderer, key, xPos, yPos, 0xFFFFFF);
            if (binding)
                drawString(fontRenderer, "(size: " + controller.getGroupSize(key) + ")", xPos + 105, yPos, 0xFFFFFF);
            group++;
        }
        if (binding && controller.groupLimitReached())
            drawCenteredString(fontRenderer, "Group limit reached: " + TileEntityFloodlightController.maxGroups, middle, 240, 0xFFFFFF);
    }
}