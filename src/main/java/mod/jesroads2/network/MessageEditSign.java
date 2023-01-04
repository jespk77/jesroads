package mod.jesroads2.network;

import io.netty.buffer.ByteBuf;
import mod.jesroads2.block.sign.BlockSign;
import mod.jesroads2.tileentity.TileEntityRoadSign;
import mod.jesroads2.tileentity.TileEntityRoadSign.SignData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageEditSign implements IMessage {
    private boolean server;
    private SignData[] data;
    private BlockPos pos;

    public MessageEditSign() {
    }

    public MessageEditSign(boolean server, SignData[] data, BlockPos pos) {
        this.server = server;
        this.data = data;
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int length = buf.readInt();
        data = new SignData[length];
        for (int i = 0; i < length; i++) {
            data[i] = new SignData(ByteBufUtils.readTag(buf));
        }
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        server = buf.readBoolean();

    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(data.length);
        for (SignData sign : data) {
            if (sign != null) ByteBufUtils.writeTag(buf, sign.getTag());
            else ByteBufUtils.writeTag(buf, new NBTTagCompound());
        }
        if (pos != null) {
            buf.writeInt(pos.getX());
            buf.writeInt(pos.getY());
            buf.writeInt(pos.getZ());
        } else {
            buf.writeInt(-1);
            buf.writeInt(-1);
            buf.writeInt(-1);
        }
        buf.writeBoolean(server);
    }

    public static class MessageEditSignHandler implements IMessageHandler<MessageEditSign, IMessage> {
        @Override
        public IMessage onMessage(final MessageEditSign message, final MessageContext ctx) {
            IThreadListener main;
            if (message.server) main = (WorldServer) ctx.getServerHandler().player.world;
            else main = Minecraft.getMinecraft();
            main.addScheduledTask(new Runnable() {
                private boolean isEmpty() {
                    for (SignData d : message.data)
                        if (d.data.length() > 0) return false;
                    return true;
                }

                @Override
                public void run() {
                    World world;
                    if (message.server) world = ctx.getServerHandler().player.world;
                    else world = Minecraft.getMinecraft().world;
                    BlockPos pos = message.pos;
                    IBlockState state = world.getBlockState(pos);
                    if (!state.getValue(BlockSign.data) && !this.isEmpty()) {
                        world.setBlockState(pos, state.withProperty(BlockSign.data, true), 3);
                        world.setTileEntity(pos, new TileEntityRoadSign(((BlockSign) state.getBlock()).type));
                    }

                    TileEntity tile = world.getTileEntity(pos);
                    if (tile instanceof TileEntityRoadSign) {
                        TileEntityRoadSign sign = ((TileEntityRoadSign) tile);
                        sign.update(message.data);
                        sign.checkForData();
                        sign.markDirty();
                    }
                    world.scheduleUpdate(pos, state.getBlock(), 5);
                }
            });
            return null;
        }
    }
}