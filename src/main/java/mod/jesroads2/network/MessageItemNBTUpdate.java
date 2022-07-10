package mod.jesroads2.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageItemNBTUpdate implements IMessage {
    private NBTTagCompound nbt;
    private Item item;
    private EnumHand hand;

    public MessageItemNBTUpdate() {
    }

    public MessageItemNBTUpdate(NBTTagCompound nbt, Item item, EnumHand hand) {
        this.nbt = nbt;
        this.item = item;
        this.hand = hand;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.item = Item.getItemById(buf.readInt());
        this.hand = buf.readInt() == 0 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
        this.nbt = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(Item.getIdFromItem(this.item));
        buf.writeInt(this.hand == EnumHand.MAIN_HAND ? 0 : 1);
        ByteBufUtils.writeTag(buf, this.nbt);
    }

    public static class MessageRoadBuilderHandler implements IMessageHandler<MessageItemNBTUpdate, IMessage> {

        @Override
        public IMessage onMessage(final MessageItemNBTUpdate message, final MessageContext ctx) {
            IThreadListener thread = (WorldServer) ctx.getServerHandler().player.world;
            thread.addScheduledTask(() -> {
				EntityPlayer player = ctx.getServerHandler().player;
				ItemStack stack = player.getHeldItem(message.hand);
				if (stack != null) stack.setTagCompound(message.nbt);
			});
            return null;
        }
    }
}