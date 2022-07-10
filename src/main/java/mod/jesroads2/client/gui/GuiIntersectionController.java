package mod.jesroads2.client.gui;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.network.MessageAction;
import mod.jesroads2.network.MessageTileEntityNBTUpdate;
import mod.jesroads2.tileentity.TileEntityDirectionController;
import mod.jesroads2.tileentity.TileEntityIntersectionController;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GuiIntersectionController extends GuiBase {
    public static final int ID = 168;

    private final TileEntityIntersectionController controller;
    private EnumFacing.Axis orientation;
    private TileEntityIntersectionController.EnumControllerMode mode;

    private static final int ENABLE_ID = 0, DEFAULT_ID = 1, TEST_ID = 2, DELAY_ID = 3;
    private GuiButton test_toggle;
    private boolean testing;

    private boolean dirty, is_direction;

    public GuiIntersectionController(EntityPlayer player, World world, BlockPos pos) {
        super(player, world, pos);

        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityIntersectionController) this.controller = (TileEntityIntersectionController) tile;
        else this.controller = null;
    }

    @Override
    public void initGui() {
        if (controller == null) {
            System.out.println("[ERROR] GuiIntersectionController: Invalid controller - " + controller);
            mc.displayGuiScreen(null);
            return;
        }

        dirty = false;
        testing = false;
        orientation = controller.getOrientation();
        is_direction = controller instanceof TileEntityDirectionController;

        mode = controller.getMode();
        buttonList.add(new GuiButton(ENABLE_ID, 55, 38, 35, 20, mode.name().toLowerCase()));
        buttonList.add(new GuiButton(DEFAULT_ID, 100, 64, 30, 20, orientation != null ? orientation.getName() : "none"));

        if (is_direction) {
            GuiTextField text = new GuiTextField(DELAY_ID, fontRenderer, 45, 89, 30, 20);
            text.setMaxStringLength(2);
            text.setText(String.valueOf(((TileEntityDirectionController) controller).getDelay()));
            textList.add(text);
            test_toggle = new GuiButton(TEST_ID, 10, 115, 80, 20, "toggle testing");
        } else test_toggle = new GuiButton(TEST_ID, 10, 89, 80, 20, "toggle testing");

        test_toggle.visible = mode == TileEntityIntersectionController.EnumControllerMode.OFF;
        buttonList.add(test_toggle);
    }

    @Override
    public void actionPerformed(GuiButton button) {
        dirty = true;
        switch (button.id) {
            case ENABLE_ID: {
                mode = TileEntityIntersectionController.EnumControllerMode.fromID(mode.ordinal() + 1);
                button.displayString = mode.name().toLowerCase();
                test_toggle.visible = mode == TileEntityIntersectionController.EnumControllerMode.OFF;
                break;
            }
            case DEFAULT_ID: {
                orientation = getNextOrientation();
                button.displayString = orientation != null ? orientation.getName() : "none";
                break;
            }
            case TEST_ID: {
                testing = true;
                break;
            }
        }
    }

    private EnumFacing.Axis getNextOrientation() {
        if (orientation == null) return EnumFacing.Axis.Z;

        switch (orientation) {
            case Z:
                return EnumFacing.Axis.X;
            default:
                return null;
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        if (dirty) {
            controller.setMode(mode);
            controller.setOrientation(orientation);
            if (is_direction) {
                try {
                    ((TileEntityDirectionController) controller).setDelay(Integer.parseInt(textList.get(0).getText()));
                } catch (NumberFormatException ignored) {
                }
            }

            JesRoads2.channel.sendToServer(new MessageTileEntityNBTUpdate(false, controller.getPos(), controller));
            if (test_toggle.visible && testing)
                JesRoads2.channel.sendToServer(new MessageAction(MessageAction.EnumAction.ACTION_TRAFFICLIGHT_TEST, controller.getPos()));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTick) {
        super.drawScreen(mouseX, mouseY, partialTick);

        drawCenteredString(fontRenderer, controller.getName() + " Options", width / 2, 10, 0xFFFFFF);
        drawCenteredString(fontRenderer, controller.getLightCount() + " lights connected", width / 2, 25, 0xAAAAAA);
        drawString(fontRenderer, "Enabled:", 10, 45, 0xFFFFFF);
        drawString(fontRenderer, "Default Direction:", 10, 70, 0xFFFFFF);
        if (is_direction) {
            drawString(fontRenderer, "Delay:", 10, 95, 0xFFFFFF);
            drawString(fontRenderer, "cycles", 80, 95, 0xFFFFFF);
        }
    }
}