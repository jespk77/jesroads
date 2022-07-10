package mod.jesroads2.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageBlockUpdate implements IMessage {
    private BlockPos caller, pos;
    private boolean isRemote;
    private int delay;

    public MessageBlockUpdate() {
    }

    public MessageBlockUpdate(BlockPos caller, BlockPos pos, boolean isRemote) {
        this(caller, pos, isRemote, -1);
    }

    public MessageBlockUpdate(BlockPos pos, boolean isRemote, int delay) {
        this(null, pos, isRemote, delay);
    }

    private MessageBlockUpdate(BlockPos caller, BlockPos pos, boolean isRemote, int delay) {
        assert this.pos != null : "Invalid BlockPos for message: null";
        this.pos = pos.toImmutable();
        if (caller != null) this.caller = caller.toImmutable();
        else this.caller = null;

        this.delay = delay;
        this.isRemote = isRemote;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        isRemote = buf.readBoolean();
        delay = buf.readInt();

        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        if (buf.isReadable()) caller = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        else caller = null;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(isRemote);
        buf.writeInt(delay);

        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());

        if (caller != null) {
            buf.writeInt(caller.getX());
            buf.writeInt(caller.getY());
            buf.writeInt(caller.getZ());
        }
    }

    public static class MessageBlockUpdateHandler implements IMessageHandler<MessageBlockUpdate, IMessage> {
        @Override
        public IMessage onMessage(final MessageBlockUpdate message, final MessageContext ctx) {
            IThreadListener thread = Minecraft.getMinecraft();
            thread.addScheduledTask(() -> {
				World world = message.isRemote ? Minecraft.getMinecraft().world : ctx.getServerHandler().player.world;
				BlockPos caller = message.caller, pos = message.pos;
				if (world == null) return;
				IBlockState state = world.getBlockState(pos);
				if (state == null) return;
				Block block = state.getBlock();
				if (block == null) return;
				if (message.caller != null && message.delay == -1)
					block.neighborChanged(state, world, pos, block, caller);
				else if (message.delay > 0) block.updateTick(world, pos, state, world.rand);
			});
            return null;
        }
    }
}