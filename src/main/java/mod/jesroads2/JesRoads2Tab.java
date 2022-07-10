package mod.jesroads2;

import java.util.Comparator;

import mod.jesroads2.block.BlockBase;
import mod.jesroads2.item.ItemBase;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class JesRoads2Tab {
    public static class JesRoads2Tabs extends CreativeTabs {

        private String[] name;
        private ItemStack icon;

        protected JesRoads2Tabs(String name) {
            super(name);
        }

        protected JesRoads2Tabs setIcon(Block block) {
            icon = new ItemStack(block);
            return this;
        }

        protected JesRoads2Tabs setIcon(Item item) {
            icon = new ItemStack(item);
            return this;
        }

        @Override
        public ItemStack getTabIconItem() {
            if (icon == null) icon = new ItemStack(Items.WOODEN_HOE);
            return icon;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void displayAllRelevantItems(NonNullList<ItemStack> items) {
            super.displayAllRelevantItems(items);

            items.sort(new Comparator<ItemStack>() {

                @Override
                public int compare(ItemStack stack0, ItemStack stack1) {
                    int id0 = getSortIDFromItem(stack0.getItem()),
                            id1 = getSortIDFromItem(stack1.getItem());

                    return Integer.compare(id0, id1);
                }

                private int getSortIDFromItem(Item item) {
                    Block b = Block.getBlockFromItem(item);
                    if (b instanceof BlockBase) return ((BlockBase) b).sortID;
                    else if (item instanceof ItemBase) return ((ItemBase) item).getSortID();
                    else return -1;
                }

            });
        }


    }

    private static JesRoads2Tab instance;

    public final JesRoads2Tabs basic;
    public final JesRoads2Tabs road, road_extra;
    public final JesRoads2Tabs system, sign;

    public static JesRoads2Tab getInstance() {
        if (instance == null) instance = new JesRoads2Tab();
        return instance;
    }

    private JesRoads2Tab() {
        basic = new JesRoads2Tabs("jesroads_basic");
        road = new JesRoads2Tabs("jesroads_road");
        road_extra = new JesRoads2Tabs("jesroads_roadextra");
        system = new JesRoads2Tabs("jesroads_system");
        sign = new JesRoads2Tabs("jesroads_sign");
    }

    protected void setIcons() {
        basic.setIcon(JesRoads2.blocks.concrete);
        road.setIcon(JesRoads2.blocks.road[1]);
        road_extra.setIcon(JesRoads2.items.road_builder);
        system.setIcon(JesRoads2.items.traffic_binder);
        sign.setIcon(JesRoads2.blocks.traffic_signs[0]);
    }
}