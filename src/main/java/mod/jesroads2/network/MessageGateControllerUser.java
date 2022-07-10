package mod.jesroads2.network;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import mod.jesroads2.tileentity.TileEntityGateController;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageGateControllerUser implements IMessage {
    private UUID user;
    private BlockPos pos;

    public MessageGateControllerUser() {
    }

    public MessageGateControllerUser(UUID user, BlockPos pos) {
        this.user = user;
        this.pos = pos;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());

        if (user != null) {
            buf.writeLong(user.getMostSignificantBits());
            buf.writeLong(user.getLeastSignificantBits());
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        if (buf.isReadable()) user = new UUID(buf.readLong(), buf.readLong());
    }

    public static class MessageEditSignHandler implements IMessageHandler<MessageGateControllerUser, IMessage> {
        @Override
        public IMessage onMessage(final MessageGateControllerUser message, final MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                World world = Minecraft.getMinecraft().world;
                BlockPos pos = message.pos;
                TileEntity tile = world.getTileEntity(pos);
                if (tile instanceof TileEntityGateController) {
                    TileEntityGateController controller = ((TileEntityGateController) tile);
                    EntityPlayer player = message.user != null ? world.getPlayerEntityByUUID(message.user) : null;
                    controller.setUser(player, false);
                }
            });
            return null;
        }
    }
}