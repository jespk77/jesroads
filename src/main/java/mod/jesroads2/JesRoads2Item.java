package mod.jesroads2;

import java.lang.reflect.Field;

import mod.jesroads2.item.*;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.MissingMappings.Mapping;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@EventBusSubscriber(modid = JesRoads2.modid)
public class JesRoads2Item {
    private static JesRoads2Item instance;

    public final ItemBase gate_ticket;
    public final ItemBase road_builder;
    public final ItemBase traffic_binder;
    public final ItemBase line_painter;
    public final ItemBase remote_controller;

    public final int maxID;

    public static JesRoads2Item getInstance() {
        return getInstance(0);
    }

    public static JesRoads2Item getInstance(int startID) {
        if (instance == null) instance = new JesRoads2Item(startID);
        return instance;
    }

    private JesRoads2Item(int id) {
        this.gate_ticket = new ItemGateTicket();
        this.road_builder = new ItemRoadBuilder(id++);
        this.traffic_binder = new ItemBinder(id++);
        this.line_painter = new ItemLinePainter(id++);
        this.remote_controller = new ItemRemoteControl(id++);

        this.maxID = id;
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        for (Field field : this.getClass().getDeclaredFields()) {
            try {
                Object o = field.get(this);
                if (o == null) continue;
                else if (o instanceof ItemBase) registry.register((ItemBase) o);
                else if (o.getClass() == ItemBase[].class) {
                    for (ItemBase item : (ItemBase[]) o)
                        registry.register(item);
                }
            } catch (IllegalArgumentException | IllegalAccessException ignored) {
            }
        }
    }

    @SubscribeEvent
    public void missingItem(RegistryEvent.MissingMappings<Item> event) {
        for (Mapping<Item> map : event.getMappings()) {
            if (JesRoads2.isDevelopmentMode()) map.ignore();
        }
    }

    @SubscribeEvent
    public void registerItemModels(ModelRegistryEvent event) {
        for (Field field : this.getClass().getDeclaredFields()) {
            try {
                Object o = field.get(this);
                if (o == null) continue;
                else if (o instanceof ItemBase) registerItem((ItemBase) o);
                else if (o.getClass() == ItemBase[].class) {
                    for (ItemBase item : (ItemBase[]) o)
                        registerItem(item);
                }
            } catch (IllegalArgumentException | IllegalAccessException ignored) {
            }
        }
    }

    private void registerItem(ItemBase item) {
        if (item == null) throw new IllegalArgumentException("[ERROR] didn't create item");

        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
}