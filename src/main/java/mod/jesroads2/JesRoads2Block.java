package mod.jesroads2;

import java.lang.reflect.Field;

import mod.jesroads2.block.BlockBase;
import mod.jesroads2.block.basic.*;
import mod.jesroads2.block.road.*;
import mod.jesroads2.block.sign.*;
import mod.jesroads2.block.system.*;
import mod.jesroads2.block.streetlight.*;
import mod.jesroads2.tileentity.*;
import mod.jesroads2.tileentity.dynamicsigns.TileEntityDynamicSignController;
import mod.jesroads2.util.EnumFacingDiagonal;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.MissingMappings.Mapping;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

@EventBusSubscriber(modid = JesRoads2.modid)
public class JesRoads2Block {
    private static JesRoads2Block instance;

    public final BlockBase scaffolding;

    public final BlockBase concrete, roadbase, memory, extender;
    public final BlockBase floodlight, floodlight_beam, floodlight_controller;
    public final BlockBase colored_light_off, colored_light_on;
    public final BlockBase[] road, roadslope, roadslope_rotated, road_line, road_light;
    public final BlockBase road_detector;

    public final BlockBase roadfence, concrete_divider, road_item, warning_light;
    public final BlockBase[] direction_signs, traffic_signs;
    public final BlockBase signpost, freewaysupport;

    public final BlockBase[] gate;
    public final BlockBase gate_controller, gate_machine;

    public final BlockBase[] freeway_sign_directional, event_sign_directional;
    public final BlockBase freeway_sign, freeway_controller, event_sign;

    public final BlockBase[] traffic_light;
    public final BlockBase intersection_controller, traffic_light_support;

    public final BlockBase street_light, street_lamp;

    public final int maxID;

    public static JesRoads2Block getInstance() {
        return getInstance(0);
    }

    public static JesRoads2Block getInstance(int startID) {
        if (JesRoads2Block.instance == null) JesRoads2Block.instance = new JesRoads2Block(startID);
        return instance;
    }

    private JesRoads2Block(int id) {
        concrete = new BlockConcrete(id++);
        roadbase = new BlockRoadBase(id++);
        memory = new BlockMemory(id++);
        registerTileEntityLegacy(TileEntityMemory.class, "TileEntityMemory");

        floodlight = new BlockFloodlight(id++);
        floodlight_beam = new BlockFloodlightBeam();
        floodlight_controller = new BlockFloodlightController(id++);
        registerTileEntityLegacy(TileEntityFloodlightController.class, "floodlight_controller");
        extender = new BlockExtender(id++);
        registerTileEntityLegacy(TileEntityExtender.class, "extender");
        colored_light_off = new BlockColoredLight(id++, false);
        colored_light_on = new BlockColoredLight(-1, true);
        scaffolding = new BlockScaffolding(id++);

        BlockRoad.EnumRoadType[] road_type = BlockRoad.EnumRoadType.values();
        road = new BlockBase[road_type.length];
        for (BlockRoad.EnumRoadType type : road_type)
            road[type.id] = new BlockRoad(id++, type, type.name);
        registerTileEntityLegacy(TileEntityAgeble.class, "TileEntityAgeble");
        road_detector = new BlockRoadDetector(id++);
        registerTileEntityLegacy(TileEntityRoadDetector.class, "RoadDetectorDateEntity");

        BlockRoadSlope.EnumSlopeType[] slope_type = BlockRoadSlope.EnumSlopeType.values();
        roadslope = new BlockBase[slope_type.length];
        roadslope_rotated = new BlockBase[slope_type.length];
        for (BlockRoadSlope.EnumSlopeType type : slope_type) {
            roadslope[type.id] = new BlockRoadSlope(id++, type, type.name, false);
            roadslope_rotated[type.id] = new BlockRoadSlope(-1, type, type.name + "_rotated", true);
        }
        registerTileEntityLegacy(TileEntityRoadSlope.class, "RoadSlopeDateEntity");

        road_line = new BlockBase[]{new BlockRoadLine("white"), new BlockRoadLine("yellow")};

        roadfence = new BlockRoadBarrier(id++);
        concrete_divider = new BlockConcreteDivider(id++);
        road_item = new BlockTrafficItem(id++);
        warning_light = new BlockWarningLight(id++);

        BlockRoadLight.EnumRoadLightType[] light_type = BlockRoadLight.EnumRoadLightType.values();
        road_light = new BlockBase[light_type.length];
        for (BlockRoadLight.EnumRoadLightType type : light_type)
            road_light[type.id] = new BlockRoadLight(id++, type);

        street_light = new BlockStreetLight(id++);
        street_lamp = new BlockStreetLamp();

        signpost = new BlockSignPost(id++);
        freewaysupport = new BlockFreewaySupport(id++);

        BlockTrafficlight.EnumTrafficLightType[] traffic_type = BlockTrafficlight.EnumTrafficLightType.values();
        traffic_light = new BlockBase[traffic_type.length];
        for (BlockTrafficlight.EnumTrafficLightType type : traffic_type)
            traffic_light[type.id] = new BlockTrafficlight(id++, type);
        traffic_light_support = new BlockTrafficlightSupport(id++);
        intersection_controller = new BlockIntersectionController(id++);
        registerTileEntityLegacy(TileEntityIntersectionController.class, "TrafficLightControllerEntity");
        registerTileEntityLegacy(TileEntityDirectionController.class, "traffic_controller");

        freeway_sign = new BlockDynamicSign(id++);
        EnumFacingDiagonal[] direction = EnumFacingDiagonal.values();
        freeway_sign_directional = new BlockBase[direction.length];
        for (EnumFacingDiagonal dir : direction)
            freeway_sign_directional[dir.ordinal()] = new BlockDynamicSign(dir);
        event_sign = new BlockEventSign(id++);
        event_sign_directional = new BlockBase[direction.length];
        for (EnumFacingDiagonal dir : direction)
            event_sign_directional[dir.ordinal()] = new BlockEventSign(dir);
        freeway_controller = new BlockDynamicSignController(id++);
        registerTileEntity(TileEntityDynamicSignController.class, TileEntityDynamicSignController.name);

        BlockGateBarrier.EnumGateType[] gate_type = BlockGateBarrier.EnumGateType.values();
        gate = new BlockBase[gate_type.length];
        for (BlockGateBarrier.EnumGateType type : gate_type)
            gate[type.ordinal()] = new BlockGateBarrier(id++, type);
        registerTileEntityLegacy(TileEntityGateBarrier.class, "TileEntityBarrier");
        gate_controller = new BlockGateController(id++);
        registerTileEntityLegacy(TileEntityGateController.class, "TileEntityGateController");
        gate_machine = new BlockGateTicketMachine(id++);

        BlockSign.EnumSignType[] sign_type = BlockSign.EnumSignType.values();
        direction_signs = new BlockBase[sign_type.length];
        for (BlockSign.EnumSignType type : sign_type)
            direction_signs[type.ordinal()] = new BlockSign(type.showInTab() ? id++ : -1, type);
        registerTileEntityLegacy(TileEntityRoadSign.class, "SignEntity");

        traffic_signs = new BlockBase[]{
                new BlockRoadSignStop(id++),
                new BlockRoadSignMerge(id++),
                new BlockRoadSignDetour(id++),
                new BlockRoadSignInfo(id++),
                new BlockRoadSignTurn(id++),
                new BlockRoadSignMove(id++),
                new BlockRoadSignSplit(id++)
        };

        maxID = id;
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();
        for (Field field : getClass().getDeclaredFields()) {
            try {
                Object o = field.get(this);
                if (o == null) continue;
                else if (o instanceof BlockBase) registry.register((BlockBase) o);
                else if (o.getClass() == BlockBase[].class) registry.registerAll((BlockBase[]) o);
            } catch (IllegalArgumentException | IllegalAccessException ignored) {
            }
        }
    }

    @SubscribeEvent
    public void registerItemBlocks(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        for (Field field : getClass().getDeclaredFields()) {
            try {
                Object o = field.get(this);
                if (o == null) continue;
                else if (o instanceof BlockBase) {
                    BlockBase b = (BlockBase) o;
                    if (b.item != null) registry.register(b.item);
                } else if (o.getClass() == BlockBase[].class) {
                    for (BlockBase b : (BlockBase[]) o)
                        if (b.item != null) registry.register(b.item);
                }
            } catch (IllegalArgumentException | IllegalAccessException ignored) {
            }
        }
    }

    @SubscribeEvent
    public void missingMapping(RegistryEvent.MissingMappings<Block> event) {
        for (Mapping<Block> map : event.getMappings()) {
            if (JesRoads2.isDevelopmentMode()) map.ignore();
        }
    }

    @SubscribeEvent
    public void registerBlockModels(ModelRegistryEvent event) {
        for (Field field : getClass().getDeclaredFields()) {
            try {
                Object o = field.get(this);
                if (o == null) continue;
                else if (o instanceof BlockBase) {
                    BlockBase block = (BlockBase) o;
                    registerBlock(block);
                } else if (o.getClass() == BlockBase[].class) {
                    BlockBase[] block = (BlockBase[]) o;
                    for (BlockBase b : block)
                        registerBlock(b);
                }
            } catch (IllegalArgumentException | IllegalAccessException ignored) {
            }
        }
    }

    private void registerBlock(BlockBase block) {
        if (block == null) throw new IllegalArgumentException("[ERROR] didn't create block");

        if (block.item != null) {
            int variants = block.getVariantCount();
            if (variants > 1) {
                for (int meta = 0; meta < block.getVariantCount(); meta++)
                    registerItem(block.item, meta, new ResourceLocation(JesRoads2.modid, block.name + "_" + meta));
            } else
                registerItem(block.item, 0, block.getRegistryName());
        }
    }

    private void registerItem(Item item, int metadata, ResourceLocation location){
        ModelLoader.setCustomModelResourceLocation(item, metadata, new ModelResourceLocation(location, "inventory"));
    }

    private void registerTileEntityLegacy(Class<? extends TileEntity> tileClass, String name) {
        GameRegistry.registerTileEntity(tileClass, new ResourceLocation("minecraft", name));
    }

    private void registerTileEntity(Class<? extends TileEntity> tileClass, String name) {
        GameRegistry.registerTileEntity(tileClass, new ResourceLocation(JesRoads2.modid, name));
    }
}