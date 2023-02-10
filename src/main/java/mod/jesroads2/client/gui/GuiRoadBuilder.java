package mod.jesroads2.client.gui;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.item.ItemRoadBuilder;
import mod.jesroads2.item.ItemRoadBuilder.EnumRoadBuilderMode;
import mod.jesroads2.network.MessageItemNBTUpdate;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GuiRoadBuilder extends GuiBase {
    public static final int ID = 166;

    private final ItemStack stack;
    private EnumRoadBuilderMode mode;

    public GuiRoadBuilder(EntityPlayer player, World world, BlockPos pos) {
        super(player, world, pos);
        stack = player.getHeldItemMainhand();
        mode = stack.hasTagCompound() ? EnumRoadBuilderMode.fromID(stack.getTagCompound().getInteger("builder_mode")) : EnumRoadBuilderMode.AUTOMATIC;
    }

    @Override
    public void initGui() {
        GuiTextField left = new GuiTextField(0, fontRenderer, 100, 25, 30, 20),
                road = new GuiTextField(1, fontRenderer, 110, 55, 30, 20),
                right = new GuiTextField(2, fontRenderer, 100, 85, 30, 20),
                remove = new GuiTextField(5, fontRenderer, 140, 220, 30, 20),
                t_left = new GuiTextField(3, fontRenderer, 100, 135, 30, 20),
                t_right = new GuiTextField(4, fontRenderer, 100, 165, 30, 20);

        left.setMaxStringLength(2);
        road.setMaxStringLength(2);
        right.setMaxStringLength(2);
        remove.setMaxStringLength(2);
        t_left.setMaxStringLength(2);
        t_right.setMaxStringLength(2);

        NBTTagCompound nbt = stack.getOrCreateSubCompound(ItemRoadBuilder.nbt_name);
        left.setText(String.valueOf(nbt.getInteger("left_length")));
        road.setText(String.valueOf(nbt.getInteger("road_length")));
        right.setText(String.valueOf(nbt.getInteger("right_length")));
        remove.setText(String.valueOf(nbt.getInteger("remove_length")));
        t_left.setText(String.valueOf(nbt.getInteger("terrain_left")));
        t_right.setText(String.valueOf(nbt.getInteger("terrain_right")));

        textList.add(left);
        textList.add(road);
        textList.add(right);
        textList.add(t_left);
        textList.add(t_right);
        remove.setVisible(mode == EnumRoadBuilderMode.REMOVE);
        textList.add(remove);

        buttonList.add(new GuiButton(0, 10, 220, 50, 20, mode.name().toLowerCase()));
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        NBTTagCompound nbt = stack.getOrCreateSubCompound(ItemRoadBuilder.nbt_name);
        updateNBT(nbt, "left_length", textList.get(0).getText(), JesRoads2.options.road_builder.max_shoulder);
        updateNBT(nbt, "road_length", textList.get(1).getText(), JesRoads2.options.road_builder.max_lane);
        updateNBT(nbt, "right_length", textList.get(2).getText(), JesRoads2.options.road_builder.max_shoulder);
        updateNBT(nbt, "terrain_left", textList.get(3).getText(), JesRoads2.options.road_builder.max_terrain);
        updateNBT(nbt, "terrain_right", textList.get(4).getText(), JesRoads2.options.road_builder.max_terrain);
        updateNBT(nbt, "remove_length", textList.get(5).getText(), JesRoads2.options.road_builder.max_remove);
        updateNBT(nbt, "builder_mode", String.valueOf(mode.id), EnumRoadBuilderMode.values().length);
        nbt.setInteger("index", 0);
        nbt.removeTag("last_location");

        JesRoads2.channel.sendToServer(new MessageItemNBTUpdate(stack.getTagCompound(), stack.getItem(), EnumHand.MAIN_HAND));
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            mode = EnumRoadBuilderMode.fromID(mode.id + 1);
            button.displayString = mode.name().toLowerCase();
            textList.get(5).setVisible(mode == EnumRoadBuilderMode.REMOVE);
        }
    }

    private void updateNBT(NBTTagCompound nbt, String key, String text, int max) {
        try {
            int value = Integer.parseInt(text.replaceAll("\\D", ""));
            if (value > max) value = max;
            else if (value < 0) return;

            nbt.setInteger(key, value);
        } catch (NumberFormatException ignored) {
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        drawString(fontRenderer, "Road Options:", 10, 10, 0xFFFFFF);
        drawString(fontRenderer, "- Left Shoulder: ", 10, 30, 0xAAAAAA);
        drawString(fontRenderer, "blocks (max " + JesRoads2.options.road_builder.max_shoulder + ")", 140, 30, 0xAAAAAA);
        drawString(fontRenderer, "- Number of lanes: ", 10, 60, 0xAAAAAA);
        drawString(fontRenderer, "lanes (max " + JesRoads2.options.road_builder.max_lane + ")", 150, 60, 0xAAAAAA);
        drawString(fontRenderer, "- Right Shoulder: ", 10, 90, 0xAAAAAA);
        drawString(fontRenderer, "blocks (max " + JesRoads2.options.road_builder.max_shoulder + ")", 140, 90, 0xAAAAAA);

        drawString(fontRenderer, "Terrain Options:", 10, 115, 0xFFFFFF);
        drawString(fontRenderer, "- Left Shoulder: ", 10, 140, 0xAAAAAA);
        drawString(fontRenderer, "blocks (max " + JesRoads2.options.road_builder.max_terrain + ")", 140, 140, 0xAAAAAA);
        drawString(fontRenderer, "- Right Shoulder: ", 10, 170, 0xAAAAAA);
        drawString(fontRenderer, "blocks (max " + JesRoads2.options.road_builder.max_terrain + ")", 140, 170, 0xAAAAAA);

        drawString(fontRenderer, "Builder Mode:", 10, 200, 0xFFFFFF);

        if (mode == EnumRoadBuilderMode.REMOVE) {
            drawString(fontRenderer, "Remove up to ", 70, 225, 0xAAAAAA);
            drawString(fontRenderer, "blocks (max " + JesRoads2.options.road_builder.max_remove + ")", 180, 225, 0xAAAAAA);
        }
    }
}
