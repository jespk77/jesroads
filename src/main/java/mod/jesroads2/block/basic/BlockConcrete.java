package mod.jesroads2.block.basic;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBase;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class BlockConcrete extends BlockBase {
    public BlockConcrete(int id) {
        super(id, new Material(MapColor.GRAY), "concrete", JesRoads2.tabs.basic);

        setHardness(0.5F).setResistance(1.F);
        setSoundType(SoundType.STONE);
    }

    @Override
    public void onBlockDestroyedByExplosion(World world, BlockPos pos, Explosion explosion){
        dropBlockAsItem(world, pos, world.getBlockState(pos), 0);
    }
}