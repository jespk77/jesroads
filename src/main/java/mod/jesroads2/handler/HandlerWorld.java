package mod.jesroads2.handler;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBase;
import mod.jesroads2.block.road.BlockRoad;
import mod.jesroads2.block.road.BlockRoadLine;
import mod.jesroads2.item.ItemLinePainter;
import mod.jesroads2.item.ItemRemoteControl;
import mod.jesroads2.item.ItemRoadBuilder;
import mod.jesroads2.network.MessageTileEntityNBTUpdate;
import mod.jesroads2.util.ITileEntityPlacement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HandlerWorld {
    public HandlerWorld() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.PlaceEvent event) {
        if (event.getState().getBlock() instanceof BlockBase) {
            World world = event.getWorld();
            EntityPlayer player = event.getPlayer();
            BlockPos pos = event.getPos();
            IBlockState state = event.getPlacedBlock();
            ItemStack stack = player.getHeldItem(event.getHand());
            NBTTagCompound blockEntity = null;
            if (stack.hasTagCompound()) {
                NBTTagCompound nbt = stack.getTagCompound();
                if (nbt != null) blockEntity = nbt.getCompoundTag("BlockEntityTag");
            }

            Block b = state.getBlock();
            if (b instanceof ITileEntityPlacement)
                blockEntity = ((ITileEntityPlacement) b).onBlockPlaced(world, pos, stack, player, blockEntity);
            if (blockEntity != null) {
                world.setTileEntity(pos, TileEntity.create(world, blockEntity));
                JesRoads2.channel.sendToAll(new MessageTileEntityNBTUpdate(true, pos, blockEntity));
            }
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        EntityPlayer player = event.getPlayer();
        World world = event.getWorld();
        IBlockState state = event.getState();
        BlockPos pos = event.getPos();
        ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
        Item item = stack.getItem();
        if (player.capabilities.isCreativeMode && item instanceof ItemRoadBuilder) {
            event.setCanceled(!item.onBlockDestroyed(stack, world, state, pos, player));
            return;
        }

        Block block = Block.getBlockFromItem(item), breaking = state.getBlock();
        if (item instanceof ItemLinePainter) event.setCanceled(!(breaking instanceof BlockRoadLine));
        else if (block instanceof BlockRoad) event.setCanceled(false);
        else event.setCanceled(breaking instanceof BlockRoad || breaking instanceof BlockRoadLine);
    }

    private boolean isEntityHoldingItem(ItemStack stack, Class<? extends Item> item) {
        return item.isInstance(stack.getItem());
    }

    @SubscribeEvent
    public void onNeighborUpdate(BlockEvent.NeighborNotifyEvent event) {
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        for (EnumFacing side : event.getNotifiedSides()) {
            BlockPos p = pos.offset(side);
            IBlockState s = world.getBlockState(p);
            Block b = s.getBlock();
            if (b instanceof BlockLeaves)
                world.scheduleUpdate(p, b, 5);
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        JesRoads2.handlerOverlay.getOverlay().onWorldLoad();
        ((ItemRoadBuilder) JesRoads2.items.road_builder).clearHistory();
        ItemRoadBuilder.last_pos = null;
        ItemRemoteControl.data = null;
    }

    @SubscribeEvent
    public void onLivingDropItemPickup(LivingDropsEvent event) {
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onBlockItemPickup(EntityItemPickupEvent event) {
        NBTTagCompound item = event.getItem().getEntityData();
        if (item.getBoolean("dropped")) item.removeTag("dropped");
        else event.setCanceled(event.getEntityLiving() instanceof EntityPlayer && event.getEntityPlayer().capabilities.isCreativeMode);
    }

    @SubscribeEvent
    public void onBlockItemDrop(ItemTossEvent event) {
        event.getEntityItem().getEntityData().setBoolean("dropped", true);
    }

    @SubscribeEvent
    public void renderBlockHighlight(RenderWorldLastEvent event) {

    }
}