package mod.jesroads2.block;

import net.minecraft.item.ItemStack;

public class BlockItemBaseSub extends BlockItemBase {
    protected BlockItemBaseSub(BlockBase block) {
        super(block);

        setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return super.getUnlocalizedName(stack) + "." + getDamage(stack);
    }
}