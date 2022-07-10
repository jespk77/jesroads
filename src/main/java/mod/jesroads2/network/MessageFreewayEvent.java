package mod.jesroads2.network;

import io.netty.buffer.ByteBuf;
import mod.jesroads2.tileentity.dynamicsigns.TileEntityDynamicSignController;
import mod.jesroads2.tileentity.dynamicsigns.FreewayEvents;
import mod.jesroads2.tileentity.dynamicsigns.event.IFreewayEvent;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageFreewayEvent implements IMessage {
    public BlockPos pos;
    public IFreewayEvent event;

    public MessageFreewayEvent() {
    }

    public MessageFreewayEvent(BlockPos position, IFreewayEvent freewayEvent) {
        pos = position;
        event = freewayEvent;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        event = FreewayEvents.fromNBT(ByteBufUtils.readTag(buf));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        ByteBufUtils.writeTag(buf, event.getTag());
    }

    public static class MessageFreewayEventHandler implements IMessageHandler<MessageFreewayEvent, IMessage> {

        @Override
        public IMessage onMessage(final MessageFreewayEvent message, final MessageContext ctx) {
            if (message.event == null) return null;

            IThreadListener main = (WorldServer) ctx.getServerHandler().player.world;
            main.addScheduledTask(() -> {
                World world = ctx.getServerHandler().player.world;
                TileEntity tile = world.getTileEntity(message.pos);
                if (tile instanceof TileEntityDynamicSignController)
                    ((TileEntityDynamicSignController) tile).notifyEvent(message.event);
            });
            return null;
        }
    }
}