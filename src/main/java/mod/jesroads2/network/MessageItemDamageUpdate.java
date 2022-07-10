package mod.jesroads2.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageItemDamageUpdate implements IMessage {
    private int meta;
    private EnumHand hand;

    public MessageItemDamageUpdate() {
        this.meta = -1;
        this.hand = null;
    }

    public MessageItemDamageUpdate(EnumHand hand, int meta) {
        this.meta = meta;
        this.hand = hand;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.meta = buf.readInt();
        this.hand = buf.readInt() == 0 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.meta);
        buf.writeInt(this.hand == EnumHand.MAIN_HAND ? 0 : 1);
    }

    public static class MessageItemDamageUpdateHandler implements IMessageHandler<MessageItemDamageUpdate, IMessage> {

        @Override
        public IMessage onMessage(final MessageItemDamageUpdate message, final MessageContext ctx) {
            if (message.meta >= 0) {
                IThreadListener thread = (WorldServer) ctx.getServerHandler().player.world;
                thread.addScheduledTask(() -> {
                    EntityPlayer player = ctx.getServerHandler().player;
                    ItemStack stack = player.getHeldItem(message.hand);
                    if (stack != null) stack.setItemDamage(message.meta);
                });
            }
            return null;
        }
    }
}