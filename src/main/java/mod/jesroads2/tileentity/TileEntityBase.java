package mod.jesroads2.tileentity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TileEntityBase extends TileEntity {
    private Calendar place_date;

    public TileEntityBase() {
        place_date = null;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.hasKey("place_date")) {
            place_date = Calendar.getInstance();
            place_date.setTimeInMillis(nbt.getLong("place_date"));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt = super.writeToNBT(nbt);
        if (place_date != null) nbt.setLong("place_date", place_date.getTimeInMillis());
        return nbt;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(super.getUpdateTag());
    }

    @Override
    @SideOnly(Side.SERVER)
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), 0, writeToNBT(new NBTTagCompound()));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onDataPacket(NetworkManager network, SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
    }

    public Object processMessage(NBTTagCompound nbt) {
        if (nbt.hasKey("0method_call")) {
            String method = nbt.getString("0method_call");
            Set<String> set = nbt.getKeySet();
            Class<?>[] classes = new Class<?>[set.size() - 1];
            Object[] parameters = new Object[set.size() - 1];
            int index = 0;
            for (String key : set) {
                if (key.contains("0method_call")) continue;

                parameters[index] = nbt.getTag(key);
                classes[index] = parameters[index].getClass();
                index++;
            }
            try {
                Method m = getClass().getDeclaredMethod(method, classes);
                return m.invoke(this, parameters);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException |
                     InvocationTargetException e) {
                System.out.println("[WARNING] exception occured while processing message: " + e);
            }
        } else System.out.println("[WARNING] process message without '0method_call' declaration: " + nbt);
        return null;
    }
}
