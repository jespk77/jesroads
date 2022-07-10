package mod.jesroads2.item;

import java.util.List;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.tileentity.ITileEntityBindable;
import mod.jesroads2.util.IBindable;
import mod.jesroads2.util.NBTUtils;
import mod.jesroads2.util.OtherUtils;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBinder extends ItemBase {
    public static final String tag_name = "bound_controller";

    public ItemBinder(int id) {
        super(id, "traffic_binder", JesRoads2.tabs.system);

        setMaxDamage(0);
        setMaxStackSize(1);
        addPropertyOverride(new ResourceLocation("bind_mode"), (stack, worldIn, entityIn) -> stack.getSubCompound(tag_name) != null ? 1 : 0);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
        NBTTagCompound tag = stack.getSubCompound(tag_name);
        if (tag != null) {
            tooltip.add("Bound to " + TextFormatting.ITALIC + tag.getString("display"));
            if (GuiScreen.isShiftKeyDown()) tooltip.add(OtherUtils.formatString(NBTUtils.readBlockPos(tag)));
        } else tooltip.add(TextFormatting.YELLOW + "No position bound");
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        NBTTagCompound nbt = stack.getSubCompound(tag_name);
        BlockPos boundPosition = NBTUtils.readBlockPos(nbt);
        if (GuiScreen.isAltKeyDown()) {
            stopBind(world, stack);
            return EnumActionResult.SUCCESS;
        }

        if (boundPosition == null)
            return startBind(world, stack, pos) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
        else if (boundPosition.equals(pos))
            return stopBind(world, stack) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
        else return bindBlock(world, player, pos, stack) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
    }

    public boolean startBind(World world, ItemStack stack, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof ITileEntityBindable) {
            ITileEntityBindable bind = (ITileEntityBindable) tile;
            String display = bind.onStartBind();
            if (display == null) display = OtherUtils.formatString(pos);
            NBTTagCompound nbt = NBTUtils.writeBlockPos(pos);
            nbt.setString("display", display);
            stack.setTagInfo(tag_name, nbt);
            return true;
        }
        return false;
    }

    public boolean isBound(ItemStack stack) {
        return stack.getSubCompound(tag_name) != null;
    }

    public boolean bindBlock(World world, EntityPlayer player, BlockPos pos, ItemStack stack) {
        NBTTagCompound nbt = stack.getSubCompound(tag_name);
        BlockPos boundPosition = NBTUtils.readBlockPos(nbt);
        if (boundPosition != null) {
            Block bindingBlock = world.getBlockState(boundPosition).getBlock();
            if (bindingBlock instanceof IBindable) {
                IBindable bindBlock = (IBindable) bindingBlock;
                if (bindBlock.isCompatibleBlock(world, pos)) {
                    stack.setTagInfo("bind_pos", NBTUtils.writeBlockPos(pos));
                    bindBlock.onBind(world, boundPosition, pos, player);
                    stack.removeSubCompound("bind_pos");
                    return true;
                }
            }
        }
        return false;
    }

    public boolean stopBind(World world, ItemStack stack) {
        NBTTagCompound nbt = stack.getSubCompound(tag_name);
        if (nbt != null) {
            BlockPos pos = NBTUtils.readBlockPos(nbt);
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof ITileEntityBindable) ((ITileEntityBindable) tile).onStopBind();
            stack.removeSubCompound(tag_name);
            return true;
        }
        return false;
    }
}