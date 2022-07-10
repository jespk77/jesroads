package mod.jesroads2.network;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import mod.jesroads2.world.storage.RemoteControllerData;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageUpdateControllerData implements IMessage {
    private String name;
    private BlockPos pos;

    private static final Charset charset = StandardCharsets.UTF_8;

    public MessageUpdateControllerData() {
    }

    public MessageUpdateControllerData(String name, BlockPos pos) {
        this.name = name;
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        name = buf.readCharSequence(buf.readInt(), charset).toString();
        if (buf.isReadable()) pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        else pos = null;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(name.length());
        buf.writeCharSequence(name, charset);
        if (pos != null) {
            buf.writeInt(pos.getX());
            buf.writeInt(pos.getY());
            buf.writeInt(pos.getZ());
        }
    }

    public static class MessageUpdateControllerDataHandler implements IMessageHandler<MessageUpdateControllerData, IMessage> {
        @Override
        public IMessage onMessage(final MessageUpdateControllerData message, final MessageContext ctx) {
            IThreadListener thread = (WorldServer) ctx.getServerHandler().player.world;
            thread.addScheduledTask(() -> {
                World world = ctx.getServerHandler().player.world;
                RemoteControllerData data = RemoteControllerData.getInstance(world);
                data.addController(message.name, message.pos);
            });
            return null;
        }
    }
}