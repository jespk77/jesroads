package mod.jesroads2.block;

import net.minecraft.item.ItemBlock;

public class BlockItemBase extends ItemBlock {
    protected BlockItemBase(BlockBase block) {
        super(block);

        this.setUnlocalizedName(block.name);
        this.setRegistryName(block.name);
    }
}