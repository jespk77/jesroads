package mod.jesroads2.network;

import io.netty.buffer.ByteBuf;
import mod.jesroads2.tileentity.TileEntityBase;
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

public class MessageTileEntity implements IMessage {
    public boolean isRemote;
    public NBTTagCompound nbt;
    public BlockPos pos;

    public MessageTileEntity() {
    }

    public MessageTileEntity(TileEntity tile, NBTTagCompound message) {
        this.isRemote = tile.getWorld().isRemote;
        this.nbt = message;
        this.pos = tile.getPos();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        isRemote = buf.readBoolean();
        nbt = ByteBufUtils.readTag(buf);
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(isRemote);
        ByteBufUtils.writeTag(buf, nbt);
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
    }

    public static class MessageTileEntityHandler implements IMessageHandler<MessageTileEntity, IMessage> {
        @Override
        public IMessage onMessage(final MessageTileEntity message, final MessageContext ctx) {
            IThreadListener main = !message.isRemote ? Minecraft.getMinecraft() : (WorldServer) ctx.getServerHandler().player.world;
            main.addScheduledTask(() -> {
				World world = !message.isRemote ? Minecraft.getMinecraft().world : ctx.getServerHandler().player.world;
				TileEntity tile = world.getTileEntity(message.pos);
				if (tile instanceof TileEntityBase) ((TileEntityBase) tile).processMessage(message.nbt);
			});
            return null;
        }
    }
}