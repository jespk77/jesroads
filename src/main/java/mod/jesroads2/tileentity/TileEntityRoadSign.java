package mod.jesroads2.tileentity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.sign.BlockSign;
import mod.jesroads2.block.sign.BlockSign.EnumSignType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityRoadSign extends TileEntity {
    private List<SignData> data = null;
    private EnumSignType type = null;
    private boolean hasData = false;

    public TileEntityRoadSign() {
    }

    public TileEntityRoadSign(EnumSignType type) {
        this.type = type;
        if (type != null) {
            SignData[] data = type.getData();
            if (this.data != null) this.data = new ArrayList<>(Arrays.asList(data));
        }
        if (data == null) data = new ArrayList<>(3);
    }

    @Override
    public void readFromNBT(NBTTagCompound t) {
        super.readFromNBT(t);
        hasData = false;
        if (t.hasKey("hasData") && t.getBoolean("hasData")) {
            data = null;
            return;
        }

        data = new ArrayList<>(t.hasKey("size") ? t.getInteger("size") + 1 : 4);
        int index = 0;
        String key = "data_" + index;
        while (t.hasKey(key)) {
            SignData d = new SignData(t.getCompoundTag(key));
            if (!hasData) hasData = d.text.length() > 0;
            data.add(d);
            key = "data_" + (++index);
        }

        if (t.hasKey("type")) type = EnumSignType.fromOrdinal(t.getInteger("type"));
        else type = null;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound t) {
        t = super.writeToNBT(t);
        if (data == null) {
            t.setBoolean("hasData", false);
            return t;
        }

        t.setInteger("size", data.size());
        int index = 0;
        String key = "data_" + index;
        for (SignData d : data) {
            t.setTag(key, d.getTag());
            key = "data_" + (++index);
        }
        if (type != null) t.setInteger("type", type.ordinal());
        return t;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState old, IBlockState nw) {
        return !(nw.getBlock() instanceof BlockSign);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    @SideOnly(Side.SERVER)
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 1, getUpdateTag());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onDataPacket(NetworkManager network, SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
    }

    public IBlockState getState() {
        return getWorld().getBlockState(getPos());
    }

    public boolean hasData() {
        return hasData;
    }

    public boolean checkForData() {
        if (data == null) return false;
        boolean empty = true;
        for (SignData d : data)
            if (d.text != null && d.text.length() > 0) {
                empty = false;
                break;
            }
        hasData = !empty;
        return hasData();
    }

    public SignData addData(int id, SignData data) {
        if (id < this.data.size()) return this.data.set(id, data);
        else this.data.add(data);
        checkForData();
        return data;
    }

    public List<SignData> getData() {
        if (type != null && !hasData) {
            data = new ArrayList<>(Arrays.asList(type.getData()));
            hasData = true;
        }
        return data;
    }

    public void update(SignData[] data) {
        int size = size(), i;
        for (i = 0; i < data.length; i++) {
            if (i < size) this.data.get(i).update(data[i]);
            else this.data.add(data[i]);
        }
        this.data.subList(i, this.data.size()).clear();
    }

    public int size() {
        return data.size();
    }

    public static class SignData {
        public int xPos;
        public int yPos;
        public int color;
        public float size;
        public final int max;
        public String text;

        public boolean editable, blackout;

        public SignData(int x, int y, int color, float size, String text, int maxLength) {
            xPos = x;
            yPos = y;
            this.color = color;
            this.size = size;
            this.text = text;
            max = maxLength;
            editable = true;
            blackout = false;
        }

        public SignData(NBTTagCompound t) {
            xPos = t.getInteger("xPos");
            yPos = t.getInteger("yPos");
            color = t.getInteger("color");
            size = t.getFloat("fsize");
            text = t.getString("text");
            max = t.getInteger("max");
            if (t.hasKey("editable")) editable = t.getBoolean("editable");
            else editable = true;
            blackout = t.getBoolean("blackout");
        }

        public NBTTagCompound getTag() {
            NBTTagCompound t = new NBTTagCompound();
            t.setInteger("xPos", xPos);
            t.setInteger("yPos", yPos);
            t.setInteger("color", color);
            t.setFloat("fsize", size);
            t.setString("text", text);
            t.setInteger("max", max);
            t.setBoolean("editable", editable);
            t.setBoolean("blackout", blackout);
            return t;
        }

        public void update(SignData d) {
            xPos = d.xPos;
            yPos = d.yPos;
            color = d.color;
            size = d.size;
            text = d.text;
            editable = d.editable;
            blackout = d.blackout;
        }

        public boolean toggleBlackout() {
            blackout = !blackout;
            return blackout;
        }

        public SignData setEditable(boolean editable) {
            this.editable = editable;
            return this;
        }

        public SignData setPos(int x, int y) {
            if (editable) {
                xPos = posInBounds(xPos + x);
                yPos = posInBounds(yPos + y);
            }
            return this;
        }

        private static int posInBounds(int pos) {
            if (pos > JesRoads2.options.road_sign.text_max_pos) return JesRoads2.options.road_sign.text_max_pos;
            else return Math.max(pos, JesRoads2.options.road_sign.text_min_pos);
        }

        public SignData setColor(int color) {
            if (editable) this.color = color;
            return this;
        }

        public SignData increaseSize(float amount) {
            if (editable) {
                float nsize = size + amount;
                if (nsize > 0 && nsize < JesRoads2.options.road_sign.text_max_pos) size = nsize;
            }
            return this;
        }

        public SignData setText(String text) {
            if (editable) {
                if (max > 0) this.text = text.length() > max ? text.substring(0, max) : text;
                else this.text = text;
            }
            return this;
        }

        public SignData getCopy() {
            return new SignData(xPos, yPos, color, size, text, max).setEditable(editable);
        }

        @Override
        public String toString() {
            return "SignData[x:" + xPos + ", y:" + yPos + ", color:" + color + ", fontSize:" + size + ", text:" + text + ", maxLength:" + max + "]";
        }
    }

}
