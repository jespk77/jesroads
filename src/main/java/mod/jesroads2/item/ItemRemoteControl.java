package mod.jesroads2.item;

import java.util.List;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.client.gui.GuiRemoteController;
import mod.jesroads2.util.IRemoteBinding;
import mod.jesroads2.util.NBTUtils;
import mod.jesroads2.util.OtherUtils;
import mod.jesroads2.world.storage.RemoteControllerData;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemRemoteControl extends ItemBase {
    public static RemoteControllerData data;
    private static final String main_tag = "action";

    public ItemRemoteControl(int id) {
        super(id, "remote_control", JesRoads2.tabs.system);

        this.setMaxDamage(0);
        this.setMaxStackSize(1);
        this.addPropertyOverride(new ResourceLocation("status"), (stack, world, entity) -> {
            NBTTagCompound nbt = stack.getSubCompound(main_tag);
            if (nbt != null) return nbt.getInteger("status");
            else return 0;
        });
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
        NBTTagCompound nbt = stack.getSubCompound(main_tag);
        if (nbt != null) {
            tooltip.add("Set to " + nbt.getString("event") + (nbt.getInteger("status") == 2 ? " (active)" : " (inactive)"));
            if (GuiScreen.isShiftKeyDown()) {
                tooltip.add("Start position: " + OtherUtils.formatString(NBTUtils.readBlockPos(nbt.getCompoundTag("destination_start"))));
                tooltip.add("End position: " + OtherUtils.formatString(NBTUtils.readBlockPos(nbt.getCompoundTag("destination_end"))));
            }
        } else tooltip.add("No controller bound");
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (entity instanceof EntityPlayerMP && data == null)
            data = RemoteControllerData.getInstance(world, (EntityPlayerMP) entity);

        NBTTagCompound nbt = stack.getSubCompound(main_tag);
        if (nbt != null) {
            if (nbt.getInteger("status") == 0) {
                TileEntity tile = world.getTileEntity(new BlockPos(nbt.getInteger("destX"), nbt.getInteger("destY"), nbt.getInteger("destZ")));
                if (tile instanceof IRemoteBinding)
                    nbt.setInteger("status", ((IRemoteBinding) tile).isEnabled() ? 2 : 1);
            }
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer entity, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (entity.isSneaking()) {
            if (world.isRemote && world.getTileEntity(pos) instanceof IRemoteBinding) {
                entity.setActiveHand(hand);
                entity.openGui(JesRoads2.instance, GuiRemoteController.ID, world, pos.getX(), pos.getY(), pos.getZ());
                return EnumActionResult.SUCCESS;
            } else return EnumActionResult.FAIL;
        }
        return this.onItemRightClick(world, entity, hand).getType();
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        NBTTagCompound nbt = stack.getSubCompound(main_tag);
        if (!player.isSneaking() && nbt != null) {
            if (!world.isRemote) {
                TileEntity tile = world.getTileEntity(NBTUtils.readBlockPos(nbt.getCompoundTag("destination_start")));
                if (tile instanceof IRemoteBinding) {
                    IRemoteBinding bind = (IRemoteBinding) tile;
                    bind.execute();
                    nbt.setInteger("status", bind.isEnabled() ? 2 : 1);
                } else {
                    data.removeController(nbt.getString("destN"));
                    stack.setTagCompound(null);
                }
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        } else if (world.isRemote) player.openGui(JesRoads2.instance, GuiRemoteController.ID, world, -1, -1, -1);
        return new ActionResult<>(EnumActionResult.PASS, stack);
    }
}