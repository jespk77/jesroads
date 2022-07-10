package mod.jesroads2.tileentity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import mod.jesroads2.block.road.BlockRoad;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityAgeble extends TileEntity {
    protected Calendar calendar;
    protected long placetime;
    private boolean checked;

    public TileEntityAgeble() {
    }

    public TileEntityAgeble(World world) {
        this.calendar = Calendar.getInstance();
        this.placetime = world.getWorldTime();
        this.checked = false;
    }

    public TileEntityAgeble(NBTTagCompound nbt) {
        readFromNBT(nbt);
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

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        checked = false;
        if (nbt.hasKey("calendar")) {
            if (calendar == null) calendar = Calendar.getInstance();
            calendar.setTimeInMillis(nbt.getLong("calendar"));
        } else calendar = null;
        placetime = nbt.hasKey("placetime") ? nbt.getLong("placetime") : -1;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt = super.writeToNBT(nbt);

        if (calendar != null) nbt.setLong("calendar", calendar.getTimeInMillis());
        if (hasTime()) nbt.setLong("placetime", placetime);
        return nbt;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState old, IBlockState nw) {
        return !(nw.getBlock() instanceof BlockRoad);
    }

    public Calendar getDate() {
        return calendar;
    }

    public long getTime() {
        return placetime;
    }

    public boolean hasTime() {
        return placetime > 0;
    }

    public boolean shouldAge(boolean gametime, int age) {
        if (!checked) {
            checked = true;
            if (gametime) {
                if (!hasTime()) return true;
                else return getGameDaysSincePlaced() > age;
            } else {
                if (calendar == null) return true;
                else return getRealDaysSincePlaced() > age;
            }
        } else return false;
    }

    public void onAging() {
        world.removeTileEntity(pos);
        invalidate();
    }

    public String getAgeDisplay(boolean gametime) {
        StringBuilder b = new StringBuilder();
        if (calendar != null) {
            SimpleDateFormat f = new SimpleDateFormat("MMM dd, yyyy");
            b.append(f.format(calendar.getTimeInMillis()));
        }

        if (hasTime()) {
            b.append(" | Day ");
            b.append(getTime() / 24000);
        }

        if (gametime) {
            b.append(" (");
            b.append(getGameDaysSincePlaced());
            b.append(" game days)");
        } else {
            b.append(" (");
            b.append(getRealDaysSincePlaced());
            b.append(" real days)");
        }
        return b.toString();
    }

    protected int getRealDaysSincePlaced() {
        return getRealDaysSincePlaced(Calendar.getInstance());
    }

    protected int getRealDaysSincePlaced(Calendar current) {
        return (int) TimeUnit.MILLISECONDS.toDays(Math.abs(calendar.getTimeInMillis() - current.getTimeInMillis()));
    }

    protected int getGameDaysSincePlaced() {
        return getGameDaysSincePlaced(getWorld().getWorldTime());
    }

    protected int getGameDaysSincePlaced(long time) {
        long diff = time - getTime();
        if (diff < 0) return 0;
        else return Math.round(diff / 24000);
    }
}