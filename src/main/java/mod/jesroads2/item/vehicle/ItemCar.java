package mod.jesroads2.item.vehicle;

import java.util.List;

import mod.jesroads2.entity.EntityCar;
import mod.jesroads2.entity.EntityCar.EnumCarType;
import mod.jesroads2.entity.EntityDrivable;
import mod.jesroads2.item.ItemBase;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemCar extends ItemBase {

    public ItemCar(int id) {
        super(id, "car", null);//JesRoads2.tabs.vehicle);

        setMaxDamage(EnumCarType.values().length);
        setMaxStackSize(1);
        setHasSubtypes(true);
        addPropertyOverride(new ResourceLocation("type"), (stack, worldIn, entityIn) -> stack.getItemDamage());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
        EnumCarType type = EnumCarType.fromID(stack.getItemDamage());
        EntityDrivable.VehicleProperty property = type.property;
        tooltip.add("Type: " + type.name().toLowerCase());
        if (GuiScreen.isShiftKeyDown()) {
            tooltip.add("- Acceleration = " + property.acceleration);
            tooltip.add("- Max Speed = " + property.maxSpeed);
        } else tooltip.add(TextFormatting.ITALIC + "SHIFT for details");
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        return false;
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        for (int i = 0; i < EnumCarType.values().length; i++) {
            items.add(new ItemStack(this, 1, i));
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer entity, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            ItemStack stack = entity.getHeldItem(hand);
            EntityCar car = new EntityCar(world, pos.getX(), pos.getY() + 1, pos.getZ());
            car.setCarType(EnumCarType.fromID(stack.getItemDamage()));
            car.rotationYaw = entity.rotationYaw;
            world.spawnEntity(car);
            entity.inventory.deleteStack(stack);
        }
        return EnumActionResult.SUCCESS;
    }
}