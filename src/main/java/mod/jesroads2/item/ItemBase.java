package mod.jesroads2.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public abstract class ItemBase extends Item {

    public final String name;
    private final int id;

    public ItemBase(int id, String name, CreativeTabs tab) {
        this.id = id;
        this.name = name;

        setUnlocalizedName(name).setRegistryName(name);
        setCreativeTab(tab);
    }

    public int getSortID() {
        return id;
    }

    @Override
    public int getMetadata(int damage) {
        return hasSubtypes ? damage : super.getMetadata(damage);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return hasSubtypes ? super.getUnlocalizedName(stack) + "." + getDamage(stack) : super.getUnlocalizedName(stack);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return !hasSubtypes && super.showDurabilityBar(stack);
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        return !hasSubtypes && super.isDamaged(stack);
    }
}