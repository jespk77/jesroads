package mod.jesroads2.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IBindable {
    boolean onBind(World world, BlockPos binder, BlockPos pos, EntityPlayer player);

    boolean isCompatibleBlock(World world, BlockPos pos);
}