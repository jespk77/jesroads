package mod.jesroads2.item;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemGateTicket extends ItemBase {
    public static final String nbt_name = "ticket_data";

    public ItemGateTicket() {
        super(-1, "gate_ticket", null);

        setMaxDamage(4);
        setMaxStackSize(1);
        setHasSubtypes(true);
        addPropertyOverride(new ResourceLocation("reusable"), (stack, worldIn, entityIn) -> stack.getItemDamage());
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
        NBTTagCompound nbt = stack.getSubCompound(nbt_name);
        if (nbt != null) {
            String id = nbt.getString("name");
            if (isToll(nbt))
                tooltip.add(TextFormatting.GRAY + "Toll road ticket" + (id.length() > 0 ? ": " + id : ("")));
            else if (id.length() > 0) tooltip.add(TextFormatting.GRAY + id);

            tooltip.add("Owner: " + TextFormatting.GRAY + nbt.getString("ownerName"));
            if (isReusable(nbt))
                tooltip.add(TextFormatting.GOLD + "This ticket can be used to " + (nbt.getBoolean("activated") ? "exit" : "enter"));
        }
    }

    public static boolean isReusable(NBTTagCompound nbt) {
        return nbt.hasKey("reusable");
    }

    public static boolean isToll(NBTTagCompound nbt) {
        return nbt.hasKey("tollX") && nbt.hasKey("tollY") && nbt.hasKey("tollZ");
    }
}