package mod.jesroads2.handler;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import mod.jesroads2.client.gui.GuiBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;

public class HandlerGui implements IGuiHandler {

    private final Map<Integer, Class<? extends GuiBase>> serverGuiMap, clientGuiMap;

    public HandlerGui() {
        serverGuiMap = new HashMap<>();
        clientGuiMap = new HashMap<>();
    }

    public void registerGui(Side side, int key, Class<? extends GuiBase> gui) {
        if (gui != null) {
            Map<Integer, Class<? extends GuiBase>> map = side == Side.SERVER ? serverGuiMap : clientGuiMap;
            if (key != -1) {
                if (!map.containsKey(key)) map.put(key, gui);
                else throw new IllegalArgumentException(gui.getName() + ": duplicate ID with " + map.get(key));
            } else throw new IllegalArgumentException(gui.getName() + ": cannot have default ID");
        }
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return createGuiFromID(Side.SERVER, ID, player, world, new BlockPos(x, y, z));
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return createGuiFromID(Side.CLIENT, ID, player, world, new BlockPos(x, y, z));
    }

    private Object createGuiFromID(Side side, int ID, EntityPlayer player, World world, BlockPos pos) {
        Map<Integer, Class<? extends GuiBase>> map = side == Side.SERVER ? serverGuiMap : clientGuiMap;
        Class<? extends GuiBase> gui = map.get(ID);
        if (gui != null) {
            try {
                Constructor<? extends GuiBase> guiC = gui.getConstructor(EntityPlayer.class, World.class, BlockPos.class);
                return guiC.newInstance(player, world, pos);
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException |
                     IllegalArgumentException | InvocationTargetException e) {
                System.out.println("[ERROR] cannot create instance of '" + gui.getName() + "' caused by: ");
                e.printStackTrace();
            }
        } else System.out.println("[WARNING] tried to create GUI instance with unknown id: " + ID);
        return null;
    }
}