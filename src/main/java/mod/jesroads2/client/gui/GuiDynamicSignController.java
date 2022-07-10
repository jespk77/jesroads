package mod.jesroads2.client.gui;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.system.BlockDynamicSign;
import mod.jesroads2.block.system.BlockEventSign;
import mod.jesroads2.network.MessageFreewayEvent;
import mod.jesroads2.tileentity.dynamicsigns.TileEntityDynamicSignController;
import mod.jesroads2.tileentity.dynamicsigns.event.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.Arrays;

public class GuiDynamicSignController extends GuiBase {
    public static final int ID = 159;

    private final TileEntityDynamicSignController controller;
    private final EnumFacing facing;
    private BlockPos[] laneSigns, eventSigns;

    private enum EButtonID {
        ScanForSigns, CloseAll, ToggleEnd, ToggleTempOpen, OffsetToggle
    }

    private final GuiButton scanSign = new GuiButton(EButtonID.ScanForSigns.ordinal(), 10, 30, 90, 20, "Scan for signs"),
            closeAll = new GuiButton(EButtonID.CloseAll.ordinal(), 10, 100, 50, 20, "Close all"),
            toggleEnd = new GuiButton(EButtonID.ToggleEnd.ordinal(), 70, 100, 60, 20, "Toggle end"),
            toggleTempOpen = new GuiButton(EButtonID.ToggleTempOpen.ordinal(), 140, 100, 70, 20, "Toggle open"),
            regularSpeed = new GuiButton(100 + TileEntityDynamicSignController.ESpeedLimit.Regular.ordinal(), 50, 130, 50, 20, "regular"),
            regularReduced = new GuiButton(100 + TileEntityDynamicSignController.ESpeedLimit.RegularReduced.ordinal(), regularSpeed.x + regularSpeed.width, regularSpeed.y, 95, 20, "regular reduced"),
            reducedSpeed = new GuiButton(100 + TileEntityDynamicSignController.ESpeedLimit.Reduced.ordinal(), regularReduced.x + regularReduced.width, regularSpeed.y, 50, 20, "reduced"),
            slowSpeed = new GuiButton(100 + TileEntityDynamicSignController.ESpeedLimit.Slow.ordinal(), reducedSpeed.x + reducedSpeed.width, regularSpeed.y, 30, 20, "slow"),
            roadworksEvent = new GuiButton(300 + BlockEventSign.EnumEventType.RoadWorks.ordinal(), 50, slowSpeed.y + slowSpeed.height + 10, 80, 20, ""),
            accidentEvent = new GuiButton(300 + BlockEventSign.EnumEventType.Accident.ordinal(), roadworksEvent.x + roadworksEvent.width, roadworksEvent.y, 80, 20, ""),
            roadClosedEvent = new GuiButton(300 + BlockEventSign.EnumEventType.Closed.ordinal(), accidentEvent.x + accidentEvent.width, accidentEvent.y, 80, 20, "");

    private GuiTextField laneOffsetIn, laneOffsetOut;

    private final GuiButton[] speedButtons = new GuiButton[]{regularSpeed, regularReduced, reducedSpeed, slowSpeed};
    private final GuiButton[] eventButtons = new GuiButton[]{roadworksEvent, accidentEvent, roadClosedEvent};

    public GuiDynamicSignController(EntityPlayer player, World world, BlockPos pos) {
        super(player, world, pos);

        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityDynamicSignController) controller = (TileEntityDynamicSignController) tile;
        else controller = null;
        facing = player.getHorizontalFacing();
    }

    @Override
    public void initGui() {
        if (controller == null) {
            mc.displayGuiScreen(null);
            return;
        } else controller.bindCheck();

        laneOffsetIn = new GuiTextField(0, fontRenderer, 70, height - 30, 40, 20);
        laneOffsetOut = new GuiTextField(1, fontRenderer, 150, laneOffsetIn.y, 40, 20);
        resetButtons();
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id >= 300) {
            int eventId = button.id - 300;
            notifyEvent(new EventUpdateEvent(BlockEventSign.EnumEventType.fromID(eventId)));
        } else if (button.id >= 200) {
            int signId = button.id - 200;
            closeLane(button, signId);
            refreshButtons();
        } else if (button.id >= 100) {
            TileEntityDynamicSignController.ESpeedLimit newSpeed = TileEntityDynamicSignController.ESpeedLimit.values()[button.id - 100];
            notifyEvent(new EventUpdateSpeed(newSpeed));
        } else {
            EButtonID buttonID = EButtonID.values()[button.id];
            switch (buttonID) {
                case ScanForSigns:
                    notifyEvent(new EventScanSign(facing));
                    return;
                case CloseAll:
                    notifyEvent(EventCloseAll.instance);
                    return;
                case ToggleEnd:
                    notifyEvent(EventEnd.instance);
                    return;
                case ToggleTempOpen:
                    notifyEvent(EventTempOpen.instance);
                    return;
                default:
            }
        }
    }

    private void refreshButtons() {
        TileEntityDynamicSignController.ESpeedLimit speed = controller.getSpeedLimit();
        for (GuiButton button : buttonList) {
            if (button.id >= 300) {
                BlockEventSign.EnumEventType type = BlockEventSign.EnumEventType.fromID(button.id - 300);
                button.displayString = type.getName() + " [" + (controller.getEvent(type) ? "O]" : "X]");
            } else if (button.id >= 200) button.displayString = getTextForLaneSign(button.id - 200);
            else if (button.id >= 100) button.enabled = button.id - 100 != speed.ordinal();
        }

        laneOffsetIn.setText(String.valueOf(controller.getLaneOffsetIn()));
        laneOffsetOut.setText(String.valueOf(controller.getLaneOffsetOut()));
    }

    private void resetButtons() {
        buttonList.clear();
        textList.clear();

        laneSigns = controller.getLaneSigns();
        int x = 80;
        for (int i = 0; i < laneSigns.length; i++) {
            buttonList.add(new GuiButton(200 + i, x, scanSign.y, 20, 20, ""));
            x += 30;
        }

        eventSigns = controller.getEventSigns();
        scanSign.x = x;
        buttonList.add(scanSign);
        buttonList.add(closeAll);
        buttonList.add(toggleEnd);
        if (controller.hasTempLanes()) buttonList.add(toggleTempOpen);
        buttonList.addAll(Arrays.asList(speedButtons));
        buttonList.addAll(Arrays.asList(eventButtons));
        textList.add(laneOffsetIn);
        textList.add(laneOffsetOut);
        refreshButtons();
    }

    private String getTextForLaneSign(int index) {
        return controller.getWorld().getBlockState(laneSigns[index]).getValue(BlockDynamicSign.type).display;
    }

    private void closeLane(GuiButton button, int index) {
        notifyEvent(new EventCloseLane(index, GuiDynamicSignController.isShiftKeyDown()));
        button.displayString = getTextForLaneSign(index);
    }

    private void notifyEvent(IFreewayEvent event) {
        controller.notifyEvent(event);
        JesRoads2.channel.sendToServer(new MessageFreewayEvent(controller.getPos(), event));
        resetButtons();
    }

    @Override
    public void onGuiClosed() {
        try {
            int offsetIn = Integer.parseInt(textList.get(0).getText()),
                    offsetOut = Integer.parseInt(textList.get(1).getText());
            if (offsetIn != controller.getLaneOffsetIn() || offsetOut != controller.getLaneOffsetOut())
                notifyEvent(new EventLaneOffset(offsetIn, offsetOut));
        } catch (NumberFormatException | NullPointerException | IndexOutOfBoundsException ignored) {
        }
    }

    @Override
    public void onResize(Minecraft m, int width, int height) {
        super.onResize(m, width, height);
        laneOffsetIn.y = laneOffsetOut.y = height - 30;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTick) {
        super.drawScreen(mouseX, mouseY, partialTick);
        drawCenteredString(fontRenderer, TextFormatting.UNDERLINE + "Freeway Controller Options", width / 2, 10, 0xFFFFFF);
        drawString(fontRenderer, "Lane signs:", 10, 35, 0xFFFFFF);
        drawString(fontRenderer, "Event signs: " + eventSigns.length + " connected", 10, 65, 0xFFFFFF);
        drawHorizontalLine(0, width, 80, 0x333333);
        drawString(fontRenderer, "Speed:", 10, 135, 0xFFFFFF);
        drawString(fontRenderer, "Event:", 10, 165, 0xFFFFFF);
        drawString(fontRenderer, "Offsets: in=", 10, height - 25, 0xFFFFFF);
        drawString(fontRenderer, " out=", 120, height - 25, 0xFFFFFF);
    }
}