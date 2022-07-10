package mod.jesroads2.world.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

public class RemoteControllerData extends WorldSavedData {
    private static final String name = "remote_controller";
    private final Map<String, BlockPos> controllerMap;

    public RemoteControllerData() {
        this(name);
    }

    public RemoteControllerData(String name) {
        super(name);

        controllerMap = new HashMap<>();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        controllerMap.clear();
        int id = 0;
        while (nbt.hasKey("controller_" + id)) {
            NBTTagCompound tag = nbt.getCompoundTag("controller_" + id);
            BlockPos p = new BlockPos(tag.getInteger("posX"), tag.getInteger("posY"), tag.getInteger("posZ"));
            controllerMap.put(tag.getString("identifier"), p);
            id++;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        int id = 0;
        for (Entry<String, BlockPos> entry : controllerMap.entrySet()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("identifier", entry.getKey());
            BlockPos p = entry.getValue();
            tag.setInteger("posX", p.getX());
            tag.setInteger("posY", p.getY());
            tag.setInteger("posZ", p.getZ());
            nbt.setTag("controller_" + id++, tag);
        }
        return nbt;
    }

    public void addController(String name, BlockPos pos) {
        if (pos != null) {
            controllerMap.put(name, pos);
            markDirty();
        } else removeController(name);
    }

    public void removeController(String name) {
        BlockPos p = controllerMap.remove(name);
        if (p != null) markDirty();
    }

    public boolean contains(String name) {
        return controllerMap.containsKey(name);
    }

    public boolean contains(BlockPos pos) {
        return controllerMap.containsValue(pos);
    }

    public BlockPos getController(String name) {
        return controllerMap.get(name);
    }

    public String getControllerName(BlockPos pos) {
        if (pos != null) {
            for (Entry<String, BlockPos> entry : controllerMap.entrySet())
                if (entry.getValue().equals(pos)) return entry.getKey();
        }
        return "";
    }

    public List<String> getControllerSet() {
        return new ArrayList<>(controllerMap.keySet());
    }

    public static RemoteControllerData getInstance(World world) {
        return getInstance(world, null);
    }

    public static RemoteControllerData getInstance(World world, EntityPlayer player) {
        MapStorage storage = world.getPerWorldStorage();
        RemoteControllerData data = (RemoteControllerData) storage.getOrLoadData(RemoteControllerData.class, name);
        if (data == null) {
            data = new RemoteControllerData();
            storage.setData(name, data);
        } 
		/*if(player instanceof EntityPlayerMP)
			JesRoads2.network.sendTo(new MessageAction(MessageAction.EnumAction.ACTION_DATASYNC, data.tag), (EntityPlayerMP) player);*/
        return data;
    }
}