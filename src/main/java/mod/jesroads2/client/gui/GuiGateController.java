package mod.jesroads2.client.gui;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.network.MessageTileEntityNBTUpdate;
import mod.jesroads2.tileentity.TileEntityGateController;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GuiGateController extends GuiBase {

    public static final int ID = 170;
    private static final int ID_IN = 12, ID_GUEST = 14, ID_CLOSED = 15, ID_TOLL = 17;

    private final TileEntityGateController controller;
    private boolean in, guest, closed, toll;

    public GuiGateController(EntityPlayer player, World world, BlockPos pos) {
        super(player, world, pos);

        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityGateController) this.controller = (TileEntityGateController) tile;
        else this.controller = null;
        this.dirty = false;
    }

    @Override
    public void initGui() {
        super.initGui();
        if (this.controller == null) {
            System.out.println("[WARNING] GuiGateController: invalid TileEntity null");
            this.mc.displayGuiScreen(null);
            return;
        }

        TileEntityGateController.EnumControllerMode mode = this.controller.getMode();
        this.in = mode.in;
        this.guest = mode.guests;
        this.toll = mode.toll;
        this.closed = mode.isClosed();
        GuiTextField text = new GuiTextField(1, this.fontRenderer, 40, 25, 220, 20);
        text.setMaxStringLength(32);
        text.setText(this.controller.getID());
        this.textList.add(text);
        this.buttonList.add(new GuiButton(ID_IN, 20, 55, 30, 20, this.in ? "in" : "out"));
        this.buttonList.add(new GuiButton(ID_GUEST, 20, 80, 95, 20, this.guest ? "open to guests" : "passes only"));
        this.buttonList.add(new GuiButton(ID_TOLL, 20, 105, 75, 20, this.toll ? "toll gate" : "regular gate"));
        this.buttonList.add(new GuiButton(ID_CLOSED, 20, 130, 50, 20, this.closed ? "closed" : "opened"));
    }

    @Override
    public void actionPerformed(GuiButton button) {
        this.dirty = true;
        switch (button.id) {
            case ID_IN: {
                this.in = !this.in;
                button.displayString = this.in ? "in" : "out";
                break;
            }
            case ID_GUEST: {
                this.guest = !this.guest;
                button.displayString = this.guest ? "open to guests" : "passes only";
                break;
            }
            case ID_TOLL: {
                this.toll = !this.toll;
                button.displayString = this.toll ? "toll gate" : "regular gate";
                break;
            }
            case ID_CLOSED: {
                this.closed = !this.closed;
                button.displayString = this.closed ? "closed" : "opened";
                this.controller.setMode(this.closed ? TileEntityGateController.EnumControllerMode.CLOSED : this.controller.getMode());
                return;
            }
        }
        this.controller.setMode(TileEntityGateController.EnumControllerMode.fromProperties(this.in, this.guest, this.toll));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTick) {
        super.drawScreen(mouseX, mouseY, partialTick);

        this.drawCenteredString(this.fontRenderer, "Gate Options", this.width / 2, 5, 0xFFFFFF);
        this.drawString(this.fontRenderer, "Name:", 5, 30, 0xFFFFFF);
    }

    @Override
    public void onGuiClosed() {
        if (this.dirty) {
            this.controller.setID(this.textList.get(0).getText());
            JesRoads2.channel.sendToServer(new MessageTileEntityNBTUpdate(false, this.pos, this.controller));
        }
    }
}