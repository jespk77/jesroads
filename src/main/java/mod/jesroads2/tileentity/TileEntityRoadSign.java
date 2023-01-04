package mod.jesroads2.tileentity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.sign.BlockSign;
import mod.jesroads2.block.sign.BlockSign.EnumSignType;
import mod.jesroads2.world.SignTemplateStorage;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityRoadSign extends TileEntityBase {
    private List<SignData> signData = null;
    private EnumSignType signType = null;
    private boolean hasData = false;

    public TileEntityRoadSign() { }

    public TileEntityRoadSign(EnumSignType type) {
        signType = type;
        signData = new ArrayList<>(3);
    }

    @Override
    public void readFromNBT(NBTTagCompound t) {
        super.readFromNBT(t);

        hasData = false;
        if (t.hasKey("hasData") && t.getBoolean("hasData")) {
            signData = null;
            return;
        }

        int size = t.hasKey("size") ? t.getInteger("size") : 0;
        signData = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            SignData d = new SignData(t.getCompoundTag("data_" + index));
            if (!hasData) hasData = d.data.length() > 0;
            signData.add(d);
        }

        if (t.hasKey("type")) signType = EnumSignType.fromOrdinal(t.getInteger("type"));
        else signType = null;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound t) {
        t = super.writeToNBT(t);
        if (signData == null) {
            t.setBoolean("hasData", false);
            return t;
        }

        t.setInteger("size", signData.size());
        int index = 0;
        String key = "data_" + index;
        for (SignData d : signData) {
            t.setTag(key, d.getTag());
            key = "data_" + (++index);
        }

        if (signType != null) t.setInteger("type", signType.ordinal());
        return t;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState old, IBlockState nw) {
        return !(nw.getBlock() instanceof BlockSign);
    }

    public IBlockState getState() {
        return getWorld().getBlockState(getPos());
    }

    public boolean checkForData() {
        if (signData == null) return false;
        boolean empty = true;
        for (SignData d : signData)
            if (d.data != null && d.data.length() > 0) {
                empty = false;
                break;
            }
        hasData = !empty;
        return hasData;
    }

    public List<SignData> getData() {
        if (signType != null && !hasData) {
            hasData = true;
        }
        return signData;
    }

    public void applyTemplate(String templateName){
        SignTemplateStorage storage = SignTemplateStorage.getInstance();
        SignTemplateStorage.SignDataTemplate template = storage.getTemplate(templateName);
        if(template != null){
            String[] data = new String[signData.size()];
            for(int index = 0; index < data.length; index++)
                data[index] = signData.get(index).data;

            signData.clear();
            signData.addAll(Arrays.asList(template.getData()));
            for(int index = 0; index < data.length && index < signData.size(); index++)
                signData.get(index).setText(data[index]);
        }
    }

    public void saveAsTemplate(String name){
        if(hasData) {
            SignTemplateStorage storage = SignTemplateStorage.getInstance();
            SignTemplateStorage.SignDataTemplate template = new SignTemplateStorage.SignDataTemplate();

            SignData[] signs = new SignData[signData.size()];
            for(int index = 0; index < signData.size(); index++){
                signs[index] = new SignData(signData.get(index));
                signs[index].data = "";
            }
            template.setData(signs);
            storage.addTemplate(name, template);
        }
    }

    public void update(SignData[] data) {
        int size = signData.size(), i;
        for (i = 0; i < data.length; i++) {
            if (i < size) signData.get(i).update(data[i]);
            else signData.add(data[i]);
        }
        signData.subList(i, signData.size()).clear();
    }

    public static class SignData {
        public int xPos;
        public int yPos;
        public int textColor;
        public float textSize;
        public final int max;
        public String data;

        public boolean isEditable, blackout;

        public SignData(int x, int y, int color, float size, String text, int maxLength) {
            xPos = x;
            yPos = y;
            textColor = color;
            textSize = size;
            data = text;
            max = maxLength;
            isEditable = true;
            blackout = false;
        }

        public SignData(SignData other){
            this(other.xPos, other.yPos, other.textColor, other.textSize, other.data, other.max);
            isEditable = other.isEditable;
            blackout = other.blackout;
        }

        public SignData(NBTTagCompound t) {
            xPos = t.getInteger("xPos");
            yPos = t.getInteger("yPos");
            textColor = t.getInteger("color");
            textSize = t.getFloat("fsize");
            data = t.getString("text");
            max = t.getInteger("max");
            if (t.hasKey("editable")) isEditable = t.getBoolean("editable");
            else isEditable = true;
            blackout = t.getBoolean("blackout");
        }

        public NBTTagCompound getTag() {
            NBTTagCompound t = new NBTTagCompound();
            t.setInteger("xPos", xPos);
            t.setInteger("yPos", yPos);
            t.setInteger("color", textColor);
            t.setFloat("fsize", textSize);
            t.setString("text", data);
            t.setInteger("max", max);
            t.setBoolean("editable", isEditable);
            t.setBoolean("blackout", blackout);
            return t;
        }

        public void update(SignData d) {
            xPos = d.xPos;
            yPos = d.yPos;
            textColor = d.textColor;
            textSize = d.textSize;
            data = d.data;
            isEditable = d.isEditable;
            blackout = d.blackout;
        }

        public boolean toggleBlackout() {
            blackout = !blackout;
            return blackout;
        }

        public SignData setEditable(boolean editable) {
            isEditable = editable;
            return this;
        }

        public SignData setPos(int x, int y) {
            if (isEditable) {
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
            if (isEditable) textColor = color;
            return this;
        }

        public SignData increaseSize(float amount) {
            if (isEditable) {
                float nsize = textSize + amount;
                if (nsize > 0 && nsize < JesRoads2.options.road_sign.text_max_pos) textSize = nsize;
            }
            return this;
        }

        public SignData setText(String text) {
            if (isEditable) {
                if (max > 0) data = text.length() > max ? text.substring(0, max) : text;
                else data = text;
            }
            return this;
        }

        public SignData getCopy() {
            return new SignData(xPos, yPos, textColor, textSize, data, max).setEditable(isEditable);
        }

        @Override
        public String toString() {
            return "SignData[x:" + xPos + ", y:" + yPos + ", color:" + textColor + ", fontSize:" + textSize + ", text:" + data + ", maxLength:" + max + "]";
        }
    }
}