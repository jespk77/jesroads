package mod.jesroads2.handler;

import mod.jesroads2.item.IItemCustomHighlightRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HandlerItemHold {
    public HandlerItemHold() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onDrawHighlight(DrawBlockHighlightEvent event) {
        EntityPlayer player = event.getPlayer();
        if (player == null) return;

        RayTraceResult res = event.getTarget();
        if (res.typeOfHit != RayTraceResult.Type.BLOCK) return;

        ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
        Item item = stack.getItem();
        if (item instanceof IItemCustomHighlightRenderer)
            event.setCanceled(((IItemCustomHighlightRenderer) item).drawBlockHighlight(player, stack, res, event.getPartialTicks()));
    }
}