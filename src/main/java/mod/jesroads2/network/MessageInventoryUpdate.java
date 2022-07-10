package mod.jesroads2.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageInventoryUpdate implements IMessage {
    private int index;
    private ItemStack stack;

    public MessageInventoryUpdate() {
    }

    public MessageInventoryUpdate(int slot, Item item) {
        this.index = slot;
        this.stack = new ItemStack(item);
    }

    public MessageInventoryUpdate(int slot, Block block) {
        this.index = slot;
        this.stack = new ItemStack(block);
    }

    public MessageInventoryUpdate(int slot, ItemStack stack) {
        this.index = slot;
        this.stack = stack.copy();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        index = buf.readInt();
        stack = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(index);
        ByteBufUtils.writeItemStack(buf, stack);
    }

    public static class MessageInventoryUpdateHandler implements IMessageHandler<MessageInventoryUpdate, IMessage> {
        @Override
        public IMessage onMessage(final MessageInventoryUpdate message, final MessageContext ctx) {
            IThreadListener thread = (WorldServer) ctx.getServerHandler().player.world;
            thread.addScheduledTask(() -> {
				EntityPlayer player = ctx.getServerHandler().player;
				player.inventory.setInventorySlotContents(message.index, message.stack);
			});
            return null;
        }
    }
}