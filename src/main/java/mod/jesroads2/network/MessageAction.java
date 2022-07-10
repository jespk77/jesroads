package mod.jesroads2.network;

import io.netty.buffer.ByteBuf;
import mod.jesroads2.item.ItemRoadBuilder;
import mod.jesroads2.tileentity.TileEntityIntersectionController;
import mod.jesroads2.world.storage.RemoteControllerData;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageAction implements IMessage {
    public enum EnumAction {
        ACTION_UNDO(0, false),
        ACTION_DATASYNC(2, true),
        ACTION_TRAFFICLIGHT_TEST(1, false),
        ACTION_PLAYERREACHDISTANCE(2, true),
        ACTION_PLAYERTOGGLEFLYING(3, true);

        /* 0 = ITEM, 1 = BLOCK, 2 = WORLD, 3 = NONE*/
        public final int actionType;
        public final boolean isRemote;

        EnumAction(int type, boolean remote) {
            actionType = type;
            isRemote = remote;
        }
    }

    private EnumAction actionType;
    private Object data;

    public MessageAction() {
    }

    public MessageAction(EnumAction action, NBTTagCompound nbt) {
        assert action.actionType == 2 : "Invalid parameter (NBTTagCompound) for action " + action.actionType;

        actionType = action;
        data = nbt;
    }

    public MessageAction(EnumAction action, EnumHand hand) {
        assert action.actionType == 0 && hand != null : "Invalid parameter (EnumHand) for action " + action.actionType;

        actionType = action;
        data = hand;
    }

    public MessageAction(EnumAction action, BlockPos pos) {
        assert action.actionType == 1 && pos != null : "Invalid parameter (BlockPos) for action " + action.actionType;

        actionType = action;
        data = pos;
    }

    public MessageAction(EnumAction action) {
        assert action.actionType == 3 : "Invalid parameter (null) for action " + action.actionType;

        actionType = action;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        actionType = EnumAction.values()[buf.readInt()];
        switch (actionType.actionType) {
            case 0: {
                data = buf.readInt() == 0 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
                break;
            }
            case 1: {
                data = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
                break;
            }
            case 2: {
                data = ByteBufUtils.readTag(buf);
                break;
            }
            case 3:
                break;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(actionType.ordinal());
        switch (actionType.actionType) {
            case 0: {
                buf.writeInt(data == EnumHand.MAIN_HAND ? 0 : 1);
                break;
            }
            case 1: {
                BlockPos pos = (BlockPos) data;
                buf.writeInt(pos.getX());
                buf.writeInt(pos.getY());
                buf.writeInt(pos.getZ());
                break;
            }
            case 2: {
                ByteBufUtils.writeTag(buf, (NBTTagCompound) data);
                break;
            }
            case 3:
                break;
        }
    }

    public static class MessageActionHandler implements IMessageHandler<MessageAction, IMessage> {

        @Override
        public IMessage onMessage(final MessageAction message, final MessageContext ctx) {
            IThreadListener main = message.actionType.isRemote ? Minecraft.getMinecraft() : (WorldServer) ctx.getServerHandler().player.world;

            main.addScheduledTask(() -> {
                Minecraft minecraft = Minecraft.getMinecraft();
                World world = message.actionType.isRemote ? minecraft.world : (WorldServer) ctx.getServerHandler().player.world;
                switch (message.actionType) {
                    case ACTION_UNDO: {
                        ItemStack stack = ctx.getServerHandler().player.getHeldItem((EnumHand) message.data);
                        ItemRoadBuilder builder = (ItemRoadBuilder) stack.getItem();
                        builder.undoLastAction(world, stack);
                        break;
                    }
                    case ACTION_DATASYNC: {
                        RemoteControllerData data = RemoteControllerData.getInstance(world);
                        data.readFromNBT((NBTTagCompound) message.data);
                        break;
                    }
                    case ACTION_TRAFFICLIGHT_TEST: {
                        BlockPos pos = (BlockPos) message.data;
                        TileEntity tile = world.getTileEntity(pos);
                        if (tile instanceof TileEntityIntersectionController)
                            ((TileEntityIntersectionController) tile).setTesting();
                        break;
                    }
                    case ACTION_PLAYERREACHDISTANCE: {
                        //PlayerReachController.setReachDistance(minecraft, minecraft.player, ((NBTTagCompound) message.data).getFloat("reach"));
                        ctx.getServerHandler().player.interactionManager.setBlockReachDistance(((NBTTagCompound) message.data).getFloat("reach"));
                        break;
                    }
                    case ACTION_PLAYERTOGGLEFLYING: {
                        ctx.getServerHandler().player.capabilities.allowFlying = !ctx.getServerHandler().player.capabilities.allowFlying;
                        break;
                    }

                    default: {
                        System.out.println("No action taken: unknown action type");
                        break;
                    }
                }
            });
            return null;
        }

    }

}
