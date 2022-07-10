package mod.jesroads2.block.basic;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBase;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class BlockConcrete extends BlockBase {
    public BlockConcrete(int id) {
        super(id, new Material(MapColor.GRAY), "concrete", JesRoads2.tabs.basic);

        setHardness(2.2F).setResistance(50.F);
        setSoundType(SoundType.STONE);
    }
}