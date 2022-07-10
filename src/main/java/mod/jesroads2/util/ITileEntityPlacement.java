package mod.jesroads2.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ITileEntityPlacement {
    NBTTagCompound onBlockPlaced(World world, BlockPos pos, ItemStack stack, EntityPlayer player, NBTTagCompound blockEntity);
}