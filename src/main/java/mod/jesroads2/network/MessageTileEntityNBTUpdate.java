package mod.jesroads2.network;

import io.netty.buffer.ByteBuf;
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

public class MessageTileEntityNBTUpdate implements IMessage {
    private boolean toClient, makenew;
    private BlockPos pos;
    private NBTTagCompound nbt;

    public MessageTileEntityNBTUpdate() {
    }

    public MessageTileEntityNBTUpdate(boolean toClient, BlockPos pos, TileEntity tile) {
        this(toClient, pos, tile.writeToNBT(new NBTTagCompound()), false);
    }

    public MessageTileEntityNBTUpdate(boolean toClient, BlockPos pos, NBTTagCompound nbt) {
        this(toClient, pos, nbt, true);
    }

    private MessageTileEntityNBTUpdate(boolean toClient, BlockPos pos, NBTTagCompound nbt, boolean makeNew) {
        this.toClient = toClient;
        this.pos = pos;
        this.nbt = nbt;
        this.makenew = makeNew;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        toClient = buf.readBoolean();
        makenew = buf.readBoolean();
        nbt = ByteBufUtils.readTag(buf);
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(toClient);
        buf.writeBoolean(makenew);
        ByteBufUtils.writeTag(buf, nbt);
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
    }

    public static class MessageTileEntityNBTHandler implements IMessageHandler<MessageTileEntityNBTUpdate, IMessage> {
        @Override
        public IMessage onMessage(final MessageTileEntityNBTUpdate message, final MessageContext ctx) {
            IThreadListener main = message.toClient ? Minecraft.getMinecraft() : (WorldServer) ctx.getServerHandler().player.world;
            main.addScheduledTask(() -> {
				World world = message.toClient ? Minecraft.getMinecraft().world : ctx.getServerHandler().player.world;
				TileEntity tile = null;
				if (!message.makenew) tile = world.getTileEntity(message.pos);
				if (tile == null) world.setTileEntity(message.pos, TileEntity.create(world, message.nbt));
				else tile.readFromNBT(message.nbt);
			});
            return null;
        }
    }
}