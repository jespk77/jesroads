package mod.jesroads2.tileentity;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import mod.jesroads2.block.basic.BlockMemory;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityMemory extends TileEntity {
    public static boolean render = false;

    private Calendar date, date2;
    private long gametime;
    public String title;

    public TileEntityMemory() {
    }

    public TileEntityMemory(World world) {
        date = Calendar.getInstance();
        date2 = null;
        gametime = world.getWorldTime();
        title = "";
    }

    public TileEntityMemory(NBTTagCompound nbt) {
        readFromNBT(nbt);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        readNBT(nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt = super.writeToNBT(nbt);
        return getNBT(nbt);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(super.getUpdateTag());
    }

    public NBTTagCompound getNBT() {
        return getNBT(new NBTTagCompound());
    }

    public NBTTagCompound getNBT(NBTTagCompound nbt) {
        nbt.setLong("date", date.getTimeInMillis());
        if (hasOther()) nbt.setLong("date2", date2.getTimeInMillis());
        if (gametime > 0) nbt.setLong("gametime", gametime);
        nbt.setString("title", title);
        return nbt;
    }

    public void readNBT(NBTTagCompound nbt) {
        if (date == null) date = Calendar.getInstance();
        date.setTimeInMillis(nbt.getLong("date"));
        if (nbt.hasKey("date2")) {
            date2 = Calendar.getInstance();
            date2.setTimeInMillis(nbt.getLong("date2"));
        } else date2 = null;
        gametime = nbt.hasKey("gametime") ? nbt.getLong("gametime") : -1;
        title = nbt.getString("title");
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

    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 2 * super.getMaxRenderDistanceSquared();
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState old, IBlockState nw) {
        return old.getBlock() != nw.getBlock();
    }

    public Calendar setDate(boolean other, Calendar c) {
        if (other) {
            if (c == null || (date.get(Calendar.DAY_OF_MONTH) == c.get(Calendar.DAY_OF_MONTH)
                    && date.get(Calendar.MONTH) == c.get(Calendar.MONTH)
                    && date.get(Calendar.YEAR) == c.get(Calendar.YEAR))) {
                date2 = null;
                markDirty();
            } else if (date2 == null || date2.getTimeInMillis() != c.getTimeInMillis()) {
                date2 = c;
                markDirty();
            }
            return date2;
        } else {
            if (c == null) c = Calendar.getInstance();

            if (date.getTimeInMillis() != c.getTimeInMillis()) {
                date = c;
                markDirty();
            }
            return date;
        }
    }

    public void checkOrder() {
        if (hasOther() && date.getTimeInMillis() > date2.getTimeInMillis()) {
            Calendar c = date;
            date = date2;
            date2 = c;
            markDirty();
        }
    }

    public boolean hasOther() {
        return date2 != null;
    }

    public long getDateTime(boolean other) {
        return (other ? date2 : date).getTimeInMillis();
    }

    public Calendar getDate(boolean other) {
        if (other && !hasOther()) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(date.getTimeInMillis());
            return c;
        } else return other ? date2 : date;
    }

    public void removeGametime() {
        setGametime(-1);
    }

    public long getGametime() {
        return gametime;
    }

    public long setGametime(long time) {
        if (gametime != time) {
            gametime = time;
            markDirty();
        }
        return getGametime();
    }

    public String getDateString() {
        SimpleDateFormat f = new SimpleDateFormat("MMM dd, yyyy");
        StringBuilder b = new StringBuilder();
        b.append(f.format(getDateTime(false)));
        if (hasOther()) {
            b.append(" -> ");
            b.append(f.format(getDateTime(true)));
        }

        if (gametime > 0) {
            b.append(" (Day ");
            b.append(gametime / 24000);
            b.append(")");
        }
        return b.toString();
    }

    public String getString() {
        return getDateString() + "==" + title;
    }

    public EnumDyeColor getDyeColor() {
        World world = getWorld();
        if (world != null) {
            return world.getBlockState(getPos()).getValue(BlockMemory.color);
        } else return null;
    }

    public IBlockState getStateWithDye(EnumDyeColor dye) {
        World world = getWorld();
        if (world != null) return world.getBlockState(getPos()).withProperty(BlockMemory.color, dye);
        else return null;
    }

    public int getColor() {
        EnumDyeColor dye = getDyeColor();
        if (dye != null) return dye.getColorValue();
        else return 0xFFFFFF;
    }

    public float[] getColors() {
        return EntitySheep.getDyeRgb(getWorld().getBlockState(getPos()).getValue(BlockMemory.color));
    }
}
