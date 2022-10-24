package mod.jesroads2.tileentity;

import java.util.*;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityFloodlightController extends TileEntityBase implements ITileEntityBindable {
    public static final int maxGroups = 14;

    private static class FloodlightGroup {
        private final ArrayList<BlockPos> lights;
        public boolean enabled, ignoreRedstone;

        protected FloodlightGroup() {
            lights = new ArrayList<>();
            enabled = false;
            ignoreRedstone = false;
        }

        protected FloodlightGroup(NBTTagCompound nbt) {
            lights = new ArrayList<>();
            enabled = nbt.getBoolean("enabled");
            ignoreRedstone = nbt.getBoolean("ignoreRedstone");

            int index = 0;
            String keyX = "posX_" + index, keyY = "posY_" + index, keyZ = "posZ_" + index;
            while (nbt.hasKey(keyX) && nbt.hasKey(keyY) && nbt.hasKey(keyZ)) {
                lights.add(new BlockPos(nbt.getInteger(keyX), nbt.getInteger(keyY), nbt.getInteger(keyZ)));
                index++;
                keyX = "posX_" + index;
                keyY = "posY_" + index;
                keyZ = "posZ_" + index;
            }
        }

        public void addLight(BlockPos pos) {
            lights.add(pos);
        }

        public boolean containsLight(BlockPos pos) {
            return lights.contains(pos);
        }

        public void removeLight(BlockPos pos) {
            lights.remove(pos);
        }

        public Iterator<BlockPos> getLights() {
            return lights.iterator();
        }

        public int size() {
            return lights.size();
        }

        public NBTTagCompound getNBT() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setBoolean("enabled", enabled);
            nbt.setBoolean("ignoreRedstone", ignoreRedstone);

            int index = 0;
            for (BlockPos p : lights) {
                nbt.setInteger("posX_" + index, p.getX());
                nbt.setInteger("posY_" + index, p.getY());
                nbt.setInteger("posZ_" + index, p.getZ());
                index++;
            }
            return nbt;
        }

        @Override
        public String toString() {
            return "{FloodlightController-Group[lights: " + lights.toString() + ", enabled: " + enabled + ", ignore_redstone: " + ignoreRedstone + "]}";
        }
    }

    public interface IFloodlightBind {
        void set(World world, BlockPos pos, IBlockState state, boolean on);
    }

    private final Map<String, FloodlightGroup> groups;
    private boolean ignoreRedstone;

    private boolean isBinding;

    private String lastUsedGroup;

    public TileEntityFloodlightController() {
        groups = new HashMap<>();
        ignoreRedstone = false;
        isBinding = false;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        groups.clear();
        for (String key : nbt.getKeySet()) {
            if (nbt.hasKey(key, nbt.getId())) groups.put(key, new FloodlightGroup(nbt.getCompoundTag(key)));
        }
        ignoreRedstone = nbt.getBoolean("ignoreRedstone");
        isBinding = nbt.getBoolean("isBinding");
        if(nbt.hasKey("lastUsedGroup")) lastUsedGroup = nbt.getString("lastUsedGroup");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt = super.writeToNBT(nbt);

        for (Entry<String, FloodlightGroup> entry : groups.entrySet())
            nbt.setTag(entry.getKey(), entry.getValue().getNBT());
        nbt.setBoolean("ignoreRedstone", ignoreRedstone);
        nbt.setBoolean("isBinding", isBinding);
        if(lastUsedGroup != null) nbt.setString("LastUsedGroup", lastUsedGroup);
        return nbt;
    }

    public boolean ignoresRedstone() {
        return ignoreRedstone;
    }

    public boolean setIgnoreRedstone() {
        return setIgnoreRedstone(!ignoresRedstone());
    }

    public boolean setIgnoreRedstone(boolean ignore) {
        if (ignoreRedstone != ignore) {
            ignoreRedstone = ignore;
            markDirty();
        }
        return ignoresRedstone();
    }

    public String addToGroup(NBTTagString group, NBTTagInt posX, NBTTagInt posY, NBTTagInt posZ) {
        return addToGroup(group.getString(), new BlockPos(posX.getInt(), posY.getInt(), posZ.getInt()));
    }

    public String addToGroup(String group, BlockPos pos) {
        if(group != null) lastUsedGroup = group;
        else group = lastUsedGroup;

        if(group != null) {
            if (groups.containsKey(group)) {
                FloodlightGroup g = groups.get(group);
                g.addLight(pos);
                IBlockState s = world.getBlockState(pos);
                Block b = s.getBlock();
                if (b instanceof IFloodlightBind) ((IFloodlightBind) b).set(world, pos, s, g.enabled);
                markDirty();
                return "[Floodlight Controller] Floodlight added to group: " + group;
            } else if (groups.size() < maxGroups) {
                FloodlightGroup g = new FloodlightGroup();
                g.addLight(pos);
                groups.put(group, g);
                markDirty();
                return "[Floodlight Controller] Floodlight added to group: " + group;
            }
        }

        return "";
    }

    public boolean removeIfExist(BlockPos pos) {
        for (Entry<String, FloodlightGroup> entry : groups.entrySet()) {
            FloodlightGroup g = entry.getValue();
            if (g.containsLight(pos)) {
                g.removeLight(pos);
                if (g.size() == 0) removeGroup(entry.getKey());
                markDirty();
                return true;
            }
        }
        return false;
    }

    public void removeGroup(NBTTagString group) {
        removeGroup(group.getString());
    }

    public void removeGroup(String group) {
        if (groups.containsKey(group)) {
            setGroup(group, false, false);
            groups.remove(group);
            markDirty();
        }
    }

    public void setGroup(NBTTagString group, NBTTagByte enabled, NBTTagByte ignore) {
        setGroup(group.getString(), enabled.getByte() != 0, ignore.getByte() != 0);
    }

    public void setGroup(String group, boolean enabled, boolean toggleIgnore) {
        if (groups.containsKey(group)) {
            FloodlightGroup g = groups.get(group);
            if (toggleIgnore) g.ignoreRedstone = !g.ignoreRedstone;
            if (g.enabled == enabled) return;

            for (Iterator<BlockPos> it = g.getLights(); it.hasNext(); ) {
                BlockPos p = it.next();
                IBlockState s = world.getBlockState(p);
                Block b = s.getBlock();
                if (b instanceof IFloodlightBind) ((IFloodlightBind) b).set(world, p, s, enabled);
                else it.remove();
            }

            if (g.size() == 0) groups.remove(group);
            else g.enabled = enabled;
            markDirty();
        }
    }

    public void setAllWithRedstone(boolean enabled) {
        if (!ignoreRedstone) setAll(enabled, true);
    }

    public void setAll(NBTTagByte enabled, NBTTagByte ignore) {
        System.out.println(ignore);
        setAll(enabled.getByte() != 0);
    }

    public void setAll(boolean enabled) {
        setAll(enabled, false);
    }

    private void setAll(boolean enabled, boolean redstone) {
        for (Entry<String, FloodlightGroup> entry : groups.entrySet())
            if (!redstone || !entry.getValue().ignoreRedstone) setGroup(entry.getKey(), enabled, false);
    }

    public void onBlockDestroyed() {
        setAll(false);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("{FloodlightController[");
        for (Entry<String, FloodlightGroup> entry : groups.entrySet()) {
            s.append("{");
            s.append(entry.getKey());
            s.append("=");
            s.append(entry.getValue().toString());
            s.append("},");
        }
        s.append("], ignoreRedstone: ");
        s.append(ignoreRedstone);
        s.append("]}");
        return s.toString();
    }

    public boolean groupLimitReached() {
        return getGroupCount() >= maxGroups;
    }

    public boolean hasGroup(String group) {
        return groups.containsKey(group);
    }

    public boolean isGroupEnabled(String group) {
        return hasGroup(group) && groups.get(group).enabled;
    }

    public int getGroupCount() {
        return groups.size();
    }

    public int getGroupSize(String group) {
        return hasGroup(group) ? groups.get(group).size() : 0;
    }

    public Set<String> getGroupKeys() {
        return groups.keySet();
    }

    @Override
    public String onStartBind() {
        isBinding = true;
        return "Floodlight Controller";
    }

    @Override
    public String addBind(BlockPos pos) {
        return null;
    }

    @Override
    public String displayBinds() {
        StringBuilder builder = new StringBuilder();
        builder.append(getGroupCount()).append("/").append(maxGroups).append(" groups used:\n");
        for (String group : getGroupKeys())
            builder.append(" - ").append(group).append(isGroupEnabled(group) ? " [on] " : " [off] ").append(getGroupSize(group)).append(" lights bound\n");
        return builder.toString();
    }

    @Override
    public void bindCheck() {

    }

    @Override
    public boolean isBinding() {
        return isBinding;
    }

    @Override
    public void onStopBind() {
        isBinding = false;
        lastUsedGroup = null;
    }
}