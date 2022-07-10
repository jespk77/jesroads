package mod.jesroads2.handler;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.client.PlayerReachController;
import mod.jesroads2.item.IItemVariant;
import mod.jesroads2.item.ItemRoadBuilder;
import mod.jesroads2.item.ItemRoadBuilder.EnumRoadBuilderMode;
import mod.jesroads2.network.MessageAction;
import mod.jesroads2.network.MessageInventoryUpdate;
import mod.jesroads2.network.MessageItemNBTUpdate;
import mod.jesroads2.util.IBlockSwitchable;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

public class HandlerInputEvent {
    private boolean farReach = false;

    public HandlerInputEvent() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onMouseEvent(MouseEvent event) {
        if (!GuiScreen.isAltKeyDown()) return;

        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayer player = minecraft.player;
        if (player == null) return;
        World world = minecraft.world;
        if (world == null) return;

        int wheel = event.getDwheel();
        if (wheel != 0) {
            ItemStack stack = player.getHeldItemMainhand();
            if (stack.getItem() instanceof IItemVariant) {
                if (((IItemVariant) stack.getItem()).updateStack(player, stack, wheel)) {
                    int slot = player.inventory.currentItem;
                    JesRoads2.channel.sendToServer(new MessageInventoryUpdate(slot, stack));
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onKeyEvent(KeyInputEvent event) {
        JesRoads2.EnumKeyBindings bind = JesRoads2.EnumKeyBindings.getPressed();
        if (bind == null) return;

        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayer player = minecraft.player;
        if (player == null) {
            System.err.println("ERROR - onKeyEvent - invalid player");
            return;
        }

        World world = minecraft.world;
        if (world == null) {
            System.err.println("ERROR - onKeyEvent - invalid world");
            return;
        }
        ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
        Item item = stack.getItem();

        switch (bind) {
            case KEY_INFO_OVERLAY: {
                if (GuiScreen.isShiftKeyDown()) JesRoads2.handlerOverlay.getOverlay().setShowInfo(0);
                else JesRoads2.handlerOverlay.getOverlay().toggleShowInfo();
                break;
            }

            case KEY_INFO_DISTANCE: {
                JesRoads2.handlerOverlay.getOverlay().resetDistance(player);
                break;
            }

            case KEY_TOGGLE_FLYING: {
                if (player.isCreative()) {
                    boolean canFly = player.capabilities.allowFlying;
                    player.capabilities.allowFlying = !canFly;
                    if (canFly) player.capabilities.isFlying = false;
                    JesRoads2.channel.sendToServer(new MessageAction(MessageAction.EnumAction.ACTION_PLAYERTOGGLEFLYING));
                    JesRoads2.handlerOverlay.getMessage().addMessage(player.capabilities.allowFlying ? "Flying enabled" : "Flying disabled");
                }
                break;
            }

            case KEY_TOGGLE_REACH: {
                farReach = !farReach;
                int distance = farReach ? 1000 : 5;
                PlayerReachController.setReachDistance(Minecraft.getMinecraft(), player, distance);
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setFloat("reach", distance);
                JesRoads2.channel.sendToServer(new MessageAction(MessageAction.EnumAction.ACTION_PLAYERREACHDISTANCE, nbt));
                JesRoads2.handlerOverlay.getMessage().addMessage("Player reach set to: " + distance + " blocks");
                break;
            }

            case KEY_SWITCH_BLOCK: {
                Block block = Block.getBlockFromItem(item);
                ItemStack s;
                if (item instanceof IBlockSwitchable) s = ((IBlockSwitchable) item).getSwitchBlock(stack);
                else if (block instanceof IBlockSwitchable) s = ((IBlockSwitchable) block).getSwitchBlock(stack);
                else return;

                if (s != null) {
                    player.inventory.setInventorySlotContents(player.inventory.currentItem, s);
                    JesRoads2.channel.sendToServer(new MessageInventoryUpdate(player.inventory.currentItem, s));
                }
                break;
            }

            case BUILDER_PLACE: {
                if (item instanceof ItemRoadBuilder) {
                    NBTTagCompound nbt = stack.getSubCompound(ItemRoadBuilder.nbt_name);
                    if (nbt != null) {
                        nbt.setInteger("placemode", nbt.getInteger("placemode") != 1 ? 1 : 0);
                        JesRoads2.channel.sendToServer(new MessageItemNBTUpdate(stack.getTagCompound(), stack.getItem(), EnumHand.MAIN_HAND));
                    }
                }
                break;
            }

            case BUILDER_PLACE_R: {
                if (item instanceof ItemRoadBuilder) {
                    NBTTagCompound nbt = stack.getSubCompound(ItemRoadBuilder.nbt_name);
                    if (nbt != null) {
                        nbt.setInteger("placemode", nbt.getInteger("placemode") != 2 ? 2 : 0);
                        JesRoads2.channel.sendToServer(new MessageItemNBTUpdate(stack.getTagCompound(), stack.getItem(), EnumHand.MAIN_HAND));
                    }
                }
                break;
            }

            case BUILDER_MODE: {
                if (item instanceof ItemRoadBuilder) {
                    NBTTagCompound nbt = stack.getSubCompound(ItemRoadBuilder.nbt_name);
                    if (nbt != null) {
                        int next = GuiScreen.isShiftKeyDown() ? nbt.getInteger("builder_mode") - 1 : nbt.getInteger("builder_mode") + 1;
                        EnumRoadBuilderMode mode = EnumRoadBuilderMode.fromID(next);
                        nbt.setInteger("builder_mode", mode.id);
                        nbt.setInteger("placemode", 0);
                        JesRoads2.channel.sendToServer(new MessageItemNBTUpdate(stack.getTagCompound(), stack.getItem(), EnumHand.MAIN_HAND));
                    }
                }
                break;
            }

            case BUILDER_BLOCK: {
                if (item instanceof ItemRoadBuilder) {
                    NBTTagCompound nbt = stack.getSubCompound(ItemRoadBuilder.nbt_name);
                    if (nbt != null) {
                        nbt.setBoolean("use_relative", !nbt.getBoolean("use_relative"));
                        JesRoads2.channel.sendToServer(new MessageItemNBTUpdate(stack.getTagCompound(), stack.getItem(), EnumHand.MAIN_HAND));
                    }
                }
                break;
            }

            case KEY_UNDO: {
                if (item instanceof ItemRoadBuilder)
                    JesRoads2.channel.sendToServer(new MessageAction(MessageAction.EnumAction.ACTION_UNDO, EnumHand.MAIN_HAND));
                break;
            }

            case BUILDER_LEFTTERRAIN_ADD: {
                if (item instanceof ItemRoadBuilder) {
                    NBTTagCompound nbt = stack.getSubCompound(ItemRoadBuilder.nbt_name);
                    if (nbt != null) {
                        int left = nbt.getInteger("terrain_left");
                        if (left < JesRoads2.options.road_builder.max_terrain) {
                            nbt.setInteger("terrain_left", left + 1);
                            JesRoads2.channel.sendToServer(new MessageItemNBTUpdate(stack.getTagCompound(), stack.getItem(), EnumHand.MAIN_HAND));
                        }
                    }
                }
                break;
            }

            case BUILDER_LEFTTERRAIN_SUBTRACT: {
                if (item instanceof ItemRoadBuilder) {
                    NBTTagCompound nbt = stack.getSubCompound(ItemRoadBuilder.nbt_name);
                    if (nbt != null) {
                        int left = nbt.getInteger("terrain_left");
                        if (left > 0) {
                            nbt.setInteger("terrain_left", left - 1);
                            JesRoads2.channel.sendToServer(new MessageItemNBTUpdate(stack.getTagCompound(), stack.getItem(), EnumHand.MAIN_HAND));
                        }
                    }
                }
                break;
            }

            case BUILDER_RIGHTTERRAIN_ADD: {
                if (item instanceof ItemRoadBuilder) {
                    NBTTagCompound nbt = stack.getSubCompound(ItemRoadBuilder.nbt_name);
                    if (nbt != null) {
                        int right = nbt.getInteger("terrain_right");
                        if (right < JesRoads2.options.road_builder.max_terrain) {
                            nbt.setInteger("terrain_right", right + 1);
                            JesRoads2.channel.sendToServer(new MessageItemNBTUpdate(stack.getTagCompound(), stack.getItem(), EnumHand.MAIN_HAND));
                        }
                    }
                }
                break;
            }

            case BUILDER_RIGHTTERRAIN_SUBTRACT: {
                if (item instanceof ItemRoadBuilder) {
                    NBTTagCompound nbt = stack.getSubCompound(ItemRoadBuilder.nbt_name);
                    if (nbt != null) {
                        int right = nbt.getInteger("terrain_right");
                        if (right > 0) {
                            nbt.setInteger("terrain_right", right - 1);
                            JesRoads2.channel.sendToServer(new MessageItemNBTUpdate(stack.getTagCompound(), stack.getItem(), EnumHand.MAIN_HAND));
                        }
                    }
                }
                break;
            }

            default: {
                System.out.println("[WARNING] Pressed unbound key binding: " + bind.name());
                break;
            }
        }
    }
}