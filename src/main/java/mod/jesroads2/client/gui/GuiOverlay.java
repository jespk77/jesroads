package mod.jesroads2.client.gui;

import java.time.LocalTime;

import mod.jesroads2.block.BlockBase;
import mod.jesroads2.item.ItemBinder;
import mod.jesroads2.item.ItemRoadBuilder;
import mod.jesroads2.item.ItemRoadBuilder.EnumRoadBuilderMode;
import mod.jesroads2.tileentity.ITileEntityBindable;
import mod.jesroads2.tileentity.TileEntityMemory;
import mod.jesroads2.util.IBindable;
import mod.jesroads2.util.IBlockOverlay;
import mod.jesroads2.util.NBTUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class GuiOverlay extends Gui implements IOverlay {
    public static final int id = 0;

    private int showInfo;
    private int displaySpeed, distance, walked;
    private int guiWidth, guiHeight;

    private final Minecraft currentMinecraft;

    public GuiOverlay(Minecraft minecraft, int width, int height) {
        onWorldLoad();
        currentMinecraft = minecraft;
        guiWidth = width;
        guiHeight = height;
    }

    @Override
    public void resize(int width, int height) {
        guiWidth = width;
        guiHeight = height;
    }

    public void onWorldLoad() {
        showInfo = 0;
        displaySpeed = 0;
        distance = 0;
        walked = 0;
    }

    public int getShowInfo() {
        return showInfo;
    }

    public int toggleShowInfo() {
        return setShowInfo(getShowInfo() + 1);
    }

    public int setShowInfo(int show) {
        showInfo = show % 4;
        return getShowInfo();
    }

    public void resetDistance(Entity entity) {
        distance = Math.round(entity.distanceWalkedModified);
        walked = 0;
    }

    public int getDistance() {
        return walked;
    }

    public void enableSpeedDisplay(Entity entity) {
        if (showInfo > 1) displaySpeed = 10;
    }

    @Override
    public void drawScreen() {
        if (currentMinecraft == null || currentMinecraft.currentScreen != null) return;
        World world = currentMinecraft.world;
        if (world == null) return;
        EntityPlayer player = currentMinecraft.player;
        if (player == null) return;

        RayTraceResult ray = player.rayTrace(7, 0);
        if (ray != null) {
            BlockPos pos = ray.getBlockPos();
            Block b = world.getBlockState(pos).getBlock();
            if (b instanceof IBlockOverlay)
                ((IBlockOverlay) b).renderOverlay(currentMinecraft.fontRenderer, currentMinecraft.getRenderItem(), world, pos);
            else if (!renderBinderOverlay(world, player, getItemStackForItem(player, ItemBinder.class)))
                renderInfoOverlay(world, player);
        }

        ItemStack builder = getItemStackForItem(player, ItemRoadBuilder.class);
        renderBuilderOverlay(player, builder);
        TileEntityMemory.render = builder != null;
    }

    private void renderInfoOverlay(World world, Entity entity) {
        LocalTime current = LocalTime.now();
        int hour = current.getHour(), minute = current.getMinute();

        String real = timeToString(hour, minute);
        long time = world.getWorldTime() % 24000;
        minute = (int) (time / 16.6666667);
        hour = (minute / 60) + 6;

        String game = timeToString(hour % 24, minute % 60);
        int days = (int) Math.floor(world.getWorldTime() / 24000L);
        walked = Math.round(entity.distanceWalkedModified - distance);
        if (displaySpeed > 0) displaySpeed--;

        switch (showInfo) {
            case 2: {
                if (entity.isRiding()) entity = entity.getRidingEntity();
                double speedAbs = Math.sqrt(Math.pow(Math.abs(entity.motionX), 2) + Math.pow(Math.abs(entity.motionZ), 2));
                int speed = (int) Math.round((speedAbs * 5.612) / 0.153);

                currentMinecraft.fontRenderer.drawString("Speed: " + speed, 5, 35, getColorFromSpeed(speed));
            }
            case 1:
                currentMinecraft.fontRenderer.drawString(entity.getHorizontalFacing() + " | " + walked + " Sq", 5, 20, 0xFFFFFF);
            case 0:
                currentMinecraft.fontRenderer.drawString(real + " | " + game + ", Day " + days, 5, 5, 0xFFFFFF);
        }
    }

    private String timeToString(int hour, int minute) {
        boolean isPM = false;
        if (hour > 12) {
            hour -= 12;
            isPM = true;
        } else if (hour == 12) isPM = true;
        else if (hour == 0) hour = 12;
        return hour + ":" + String.format("%02d", minute) + (isPM ? " PM" : " AM");
    }

    private int getColorFromSpeed(int speed) {
        if (speed < 10) return 0xFF0000;
        else if (speed < 20) return 0xFFFF00;
        else return 0x00FF00;
    }

    private void renderBuilderOverlay(EntityPlayer player, ItemStack builder) {
        if (builder != null) {
            NBTTagCompound nbt = builder.getSubCompound(ItemRoadBuilder.nbt_name);
            if (nbt == null) return;

            int xPos = 5, yPos = guiHeight - 45, color = 0xAAAAAA;
            BlockPos last_pos = ItemRoadBuilder.last_pos;
            RayTraceResult ray = player.rayTrace(8, 0);
            if (ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK) {
                BlockPos rayPos = ray.getBlockPos();
                drawCenteredString(currentMinecraft.fontRenderer, "Current [x=" + rayPos.getX() + ", y=" + rayPos.getY() + ", z=" + rayPos.getZ() + "]", guiWidth / 2, guiHeight - 33, color);
            }

            int mode = nbt.getInteger("builder_mode"), remove = nbt.getInteger("remove_length");
            switch (nbt.getInteger("placemode")) {
                case 1: { // Build mode
                    yPos += getIncrement();
                    if (GuiScreen.isCtrlKeyDown())
                        currentMinecraft.fontRenderer.drawString("Build Mode - Vertical", xPos, yPos, 0xFFFFFF);
                    else if (GuiScreen.isAltKeyDown())
                        currentMinecraft.fontRenderer.drawString("Build Mode - Horizontal", xPos, yPos, 0xFFFFFF);
                    else currentMinecraft.fontRenderer.drawString("Build Mode", xPos, yPos, 0xFFFFFF);
                    yPos += getIncrement();
                    drawPlacementBlock(nbt.getBoolean("use_relative") ? null : ItemRoadBuilder.getPlayerInventorySlot(player, nbt.getInteger("selected_slot")), xPos, yPos, color);
                    break;
                }

                case 2: { // Replace mode
                    yPos += getIncrement();
                    if (GuiScreen.isCtrlKeyDown())
                        currentMinecraft.fontRenderer.drawString("Replace Mode - Vertical", xPos, yPos, 0xFFFFFF);
                    else if (GuiScreen.isAltKeyDown())
                        currentMinecraft.fontRenderer.drawString("Replace Mode - Horizontal", xPos, yPos, 0xFFFFFF);
                    else currentMinecraft.fontRenderer.drawString("Replace Mode", xPos, yPos, 0xFFFFFF);
                    yPos += getIncrement();
                    drawPlacementBlock(ItemRoadBuilder.getPlayerInventorySlot(player, nbt.getInteger("selected_slot")), xPos, yPos, color);
                    break;
                }

                default: { // Other modes
                    if (last_pos != null)
                        drawCenteredString(currentMinecraft.fontRenderer, "Build [x=" + last_pos.getX() + ", y=" + last_pos.getY() + ", z=" + last_pos.getZ() + "]", guiWidth / 2, guiHeight - 43, color);
                    if (mode == EnumRoadBuilderMode.REMOVE.id) {
                        currentMinecraft.fontRenderer.drawString("Remove roads", xPos, yPos, 0xFF9999);
                        yPos += getIncrement();
                        currentMinecraft.fontRenderer.drawString("Up to " + remove + " blocks", xPos, yPos, color);
                        yPos += getIncrement();
                    } else {
                        currentMinecraft.fontRenderer.drawString("Road Settings", xPos + 5, yPos - getIncrement(), 0xFFFFFF);
                        currentMinecraft.fontRenderer.drawString("Left Shoulder: " + nbt.getInteger("left_length"), xPos, yPos, color);
                        yPos += getIncrement();
                        currentMinecraft.fontRenderer.drawString("Number of lanes:  " + nbt.getInteger("road_length"), xPos, yPos, color);
                        yPos += getIncrement();
                        currentMinecraft.fontRenderer.drawString("Right Shoulder:  " + nbt.getInteger("right_length"), xPos, yPos, color);
                        yPos += getIncrement();

                        xPos = guiWidth - 90;
                        yPos = guiHeight - 55;
                        currentMinecraft.fontRenderer.drawString("Terrain Settings", xPos, yPos - getIncrement(), 0xFFFFFF);
                        currentMinecraft.fontRenderer.drawString("Left Shoulder: " + nbt.getInteger("terrain_left"), xPos, yPos, color);
                        yPos += getIncrement();
                        currentMinecraft.fontRenderer.drawString("Right Shoulder: " + nbt.getInteger("terrain_right"), xPos, yPos, color);
                        yPos += getIncrement();
                        currentMinecraft.fontRenderer.drawString("Mode: " + EnumRoadBuilderMode.fromID(nbt.getInteger("builder_mode")).name().toLowerCase(), xPos, yPos, color);
                        yPos += getIncrement();
                        drawPlacementBlock(nbt.getBoolean("use_relative") ? null : ItemRoadBuilder.getPlayerInventorySlot(player, nbt.getInteger("selected_slot")), xPos, yPos, color);
                    }
                    break;
                }
            }
        }
    }

    private void drawPlacementBlock(ItemStack stack, int xPos, int yPos, int color) {
        boolean relative = stack == null;
        currentMinecraft.fontRenderer.drawString("Block: " + (relative ? "relative" : ""), xPos, yPos, color);
        if (!relative) {
            RenderHelper.enableGUIStandardItemLighting();
            currentMinecraft.getRenderItem().renderItemIntoGUI(stack, xPos + 30, yPos - 5);
        }
    }

    private boolean renderBinderOverlay(World world, EntityPlayer player, ItemStack binder) {
        if (binder != null) {
            NBTTagCompound nbt = binder.getSubCompound(ItemBinder.tag_name);
            BlockPos pos = null;
            if (nbt != null) {
                pos = NBTUtils.readBlockPos(nbt);
                drawCenteredString(currentMinecraft.fontRenderer, "Bound to " + TextFormatting.ITALIC + nbt.getString("display") + TextFormatting.RESET
                                + " (" + (int) BlockBase.calculateDistance(pos.getX(), pos.getZ(), player.posX, player.posZ) + " blocks away)"
                        , guiWidth / 2, guiHeight - 33, 0xFFFFFF);
            }

            BlockPos controller = getPosForBlock(world, player, IBindable.class);
            boolean usesBoundPos = false;
            if (controller == null && pos != null) {
                controller = pos;
                usesBoundPos = true;
            }

            if (controller != null) {
                TileEntity tile = world.getTileEntity(controller);
                if (tile instanceof ITileEntityBindable) {
                    String binds = ((ITileEntityBindable) tile).displayBinds();
                    if (usesBoundPos)
                        binds = "* Bounds " + TextFormatting.ITALIC + nbt.getString("display") + TextFormatting.RESET + ":\n" + binds;
                    drawBinds(binds);
                    return true;
                }
            }
        }
        return false;
    }

    private void drawBinds(String binds) {
        String[] lines = binds.split("\n");
        int maxLength = 0;
        for (String line : lines) {
            int length = currentMinecraft.fontRenderer.getStringWidth(line);
            if (length > maxLength) maxLength = length;
        }

        Gui.drawRect(3, 3, 6 + maxLength, 1 + lines.length * getIncrement(), Integer.MIN_VALUE);
        int yPos = 5;
        for (String line : lines) {
            currentMinecraft.fontRenderer.drawString(line, 5, yPos, 0xFFFFFF);
            yPos += getIncrement();
        }
    }

    private int getIncrement() {
        return currentMinecraft.fontRenderer.FONT_HEIGHT + 5;
    }

    public static BlockPos getPosForBlock(World world, EntityPlayer player, Class<?> blockClass) {
        RayTraceResult res = player.rayTrace(5, 0);
        if (res == null) return null;

        BlockPos pos = res.getBlockPos();
        if (blockClass.isInstance(world.getBlockState(pos).getBlock())) return pos;
        else return null;
    }

    public static ItemStack getItemStackForBlock(EntityPlayer player, Class<? extends Block> blockClass) {
        ItemStack stack = player.getHeldItemMainhand();
        if (blockClass.isInstance(Block.getBlockFromItem(stack.getItem()))) return stack;
        stack = player.getHeldItemOffhand();
        if (blockClass.isInstance(Block.getBlockFromItem(stack.getItem()))) return stack;
        else return null;
    }

    public static ItemStack getItemStackForItem(EntityPlayer player, Class<? extends Item> itemClass) {
        ItemStack stack = player.getHeldItemMainhand();
        if (itemClass.isInstance(stack.getItem())) return stack;
        stack = player.getHeldItemOffhand();
        if (itemClass.isInstance(stack.getItem())) return stack;
        else return null;
    }
}