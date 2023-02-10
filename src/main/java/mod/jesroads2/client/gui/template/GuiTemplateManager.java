package mod.jesroads2.client.gui.template;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.client.gui.GuiBase;
import mod.jesroads2.client.gui.GuiRoadSignEdit;
import mod.jesroads2.tileentity.TileEntityRoadSign;
import mod.jesroads2.world.SignTemplateStorage;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.IOException;

public class GuiTemplateManager extends GuiBase {
    public static int ID = 178;

    private final TileEntityRoadSign signTileEntity;

    private GuiTemplateList templateList;

    private final GuiButton applyButton = new GuiButton(0, 10, 0, 70, 20,  "Apply"),
        saveButton = new GuiButton(1, applyButton.x + applyButton.width + 5, 0, applyButton.width, applyButton.height, "Save"),
        deleteButton = new GuiButton(2, saveButton.x + saveButton.width + 5, 0, saveButton.width, saveButton.height, "Delete");

    private GuiTextField templateNameField;

    public GuiTemplateManager(EntityPlayer player, World world, BlockPos pos) {
        super(player, world, pos);

        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof TileEntityRoadSign) signTileEntity = (TileEntityRoadSign) tile;
        else signTileEntity = null;
    }

    @Override
    public void initGui(){
        super.initGui();
        if(signTileEntity == null){
            Close();
            return;
        }

        templateList = new GuiTemplateList(mc, width, height - 20, 20, height - 55, 15, this);
        saveButton.x = (width / 2) - (saveButton.width / 2);
        applyButton.x = saveButton.x - applyButton.width - 5;
        deleteButton.x = saveButton.x + saveButton.width + 5;
        applyButton.y = saveButton.y = deleteButton.y = height - 25;
        buttonList.add(applyButton);
        buttonList.add(saveButton);
        buttonList.add(deleteButton);

        templateNameField = new GuiTextField(3, fontRenderer, applyButton.x, applyButton.y - applyButton.height - 5,
                saveButton.width + applyButton.width + deleteButton.width + 10, saveButton.height);
        textList.add(templateNameField);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        super.drawScreen(mouseX, mouseY, partialTicks);

        drawCenteredString(fontRenderer, "Sign templates", width / 2, 5, 0xFFFFFF);
        templateList.drawScreen(mouseX, mouseY, partialTicks);
    }

    private static boolean isMouseOverElement(GuiTextField textField, int mouseX, int mouseY){
        int x = textField.x, y = textField.y;
        return mouseX >= x && mouseX <= x + textField.width &&
            mouseY >= y && mouseY <= y + textField.height;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        if(!templateList.mouseClicked(mouseX, mouseY, button) && button == 0){
            templateList.setSelectIndex(-1);
        }

        if(!isMouseOverElement(templateNameField, mouseX, mouseY)) {
            templateNameField.setText(templateList.getSelectedTemplateName());
            if(button != 0)
                templateList.refreshTemplateList(templateNameField.getText());
        }
    }

    @Override
    public void keyTyped(char key, int code) throws IOException {
        super.keyTyped(key, code);

        if(templateNameField.isFocused()){
            templateList.refreshTemplateList(templateNameField.getText());
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        String templateName = templateList.getSelectedTemplateName();
        SignTemplateStorage templateStorage = GuiTemplateList.templateStorage;
        if(button.id == applyButton.id){
            if(templateName.length() == 0) return;
            SignTemplateStorage.SignDataTemplate template = templateStorage.getTemplate(templateName);
            if(template != null) signTileEntity.applyTemplate(template);
        }
        else if(button.id == saveButton.id){
            SignTemplateStorage.SignDataTemplate template = signTileEntity.createTemplate();
            if(template != null) templateStorage.addTemplate(templateNameField.getText(), template);
        }
        else if(button.id == deleteButton.id){
            templateStorage.deleteTemplate(templateName);
            templateList.refreshTemplateList();
            return;
        }

        player.openGui(JesRoads2.instance, GuiRoadSignEdit.ID, world, pos.getX(), pos.getY(), pos.getZ());
    }
}