package mod.jesroads2.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageBlockStateUpdate implements IMessage {
    private BlockPos pos;
    private IBlockState state;

    public MessageBlockStateUpdate() {
    }

    public MessageBlockStateUpdate(BlockPos pos, IBlockState state) {
        this.pos = pos;
        this.state = state;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        this.state = Block.getBlockById(buf.readInt()).getStateFromMeta(buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeInt(Block.getIdFromBlock(state.getBlock()));
        buf.writeInt(state.getBlock().getMetaFromState(state));
    }

    public static class MessageBlockStateUpdateHandler implements IMessageHandler<MessageBlockStateUpdate, IMessage> {

        @Override
        public IMessage onMessage(final MessageBlockStateUpdate message, final MessageContext ctx) {
            IThreadListener main = (WorldServer) ctx.getServerHandler().player.world;
            main.addScheduledTask(() -> {
				World world = ctx.getServerHandler().player.world;
				world.setBlockState(message.pos, message.state, 3);
			});
            return null;
        }
    }
}