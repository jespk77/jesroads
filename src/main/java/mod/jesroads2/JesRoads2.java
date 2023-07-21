package mod.jesroads2;

import org.lwjgl.input.Keyboard;

import mod.jesroads2.client.gui.*;
import mod.jesroads2.client.gui.template.*;
import mod.jesroads2.command.CommandSleep;
import mod.jesroads2.handler.*;
import mod.jesroads2.network.*;
import mod.jesroads2.proxy.ICommonProxy;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = JesRoads2.modid, name = JesRoads2.name, version = "${version}", guiFactory = "mod.jesroads2.client.gui.GuiJesRoads2OptionsFactory")
public class JesRoads2 {
    public static final String modid = "jesroads2";
    public static final String name = "JesWorld: Roads";

    @Instance(modid)
    public static JesRoads2 instance;

    public static SimpleNetworkWrapper channel;

    public static JesRoads2Tab tabs;
    public static JesRoads2Block blocks;
    public static JesRoads2Item items;
    public static JesRoads2Option options;

    public enum EnumKeyBindings {
        KEY_INFO_OVERLAY("key.info_overlay.toggle", Keyboard.KEY_Y, "key.category.jesroads2"),
        KEY_INFO_DISTANCE("key.info_overlay.distance", Keyboard.KEY_K, "key.category.jesroads2"),
        KEY_SWITCH_BLOCK("key.generic.switch", Keyboard.KEY_G, "key.category.jesroads2"),
        KEY_TOGGLE_FLYING("key.generic.toggleFlying", Keyboard.KEY_V),
        KEY_UNDO("key.generic.undo", Keyboard.KEY_N, "key.category.jesroads2"),
        BUILDER_REPLACE("key.roadbuilder.replace", Keyboard.KEY_Z, "key.category.jesroads2"),
        BUILDER_MODE("key.roadbuilder.mode", Keyboard.KEY_C, "key.category.jesroads2"),
        BUILDER_BLOCK("key.roadbuilder.block", Keyboard.KEY_R, "key.category.jesroads2"),
        BUILDER_LEFTTERRAIN_ADD("key.roadbuilder.leftterrain_add", Keyboard.KEY_LBRACKET, "key.category.jesroads2"),
        BUILDER_LEFTTERRAIN_SUBTRACT("key.roadbuilder.leftterrain_subtract", Keyboard.KEY_RBRACKET, "key.category.jesroads2"),
        BUILDER_RIGHTTERRAIN_ADD("key.roadbuilder.rightterrain_add", Keyboard.KEY_COMMA, "key.category.jesroads2"),
        BUILDER_RIGHTTERRAIN_SUBTRACT("key.roadbuilder.rightterrain_subtract", Keyboard.KEY_PERIOD, "key.category.jesroads2"),
        BUILDER_PLACE("key.roadbuilder.place", Keyboard.KEY_TAB, "key.category.jesroads2"),
        BUILDER_PLACE_R("key.roadbuilder.place_r", Keyboard.KEY_GRAVE, "key.category.jesroads2"),
        KEY_TOGGLE_REACH("key.generic.toggleReach", Keyboard.KEY_U, "key.category.jesroads2");

        private static final String category = "key.category.jesroads2";
        private final KeyBinding bind;

        EnumKeyBindings(String description, int key) {
            this(description, key, category);
        }

        EnumKeyBindings(String description, int key, String category) {
            this.bind = new KeyBinding(description, key, category);
        }

        public KeyBinding getBind() {
            return this.bind;
        }

        public static EnumKeyBindings getPressed() {
            for (EnumKeyBindings key : EnumKeyBindings.values())
                if (key.bind.isPressed()) return key;
            return null;
        }
    }

    public static class ExperimentalItemException extends RuntimeException {
        private static final long serialVersionUID = 844200262456649352L;
        private final String module;

        public ExperimentalItemException(String module) {
            this.module = module;
        }

        @Override
        public String getMessage() {
            return "Trying to call '" + this.module + "' when this is only allowed in development mode";
        }
    }

    public static HandlerWorld handlerWorld;
    public static HandlerGui handlerGui;
    public static HandlerGuiOverlay handlerOverlay;
    public static HandlerInputEvent handlerInput;
    public static HandlerItemHold handlerItem;
    public static HandlerEntity handlerEntity;
    public static HandlerWeatherEffects handlerWeather;

    public int id;

    @SidedProxy(clientSide = "mod.jesroads2.proxy.ClientProxy", serverSide = "mod.jesroads2.proxy.ServerProxy")
    public static ICommonProxy proxy;

    @Mod.EventHandler
    public void preInitialize(FMLPreInitializationEvent event) {
        options = JesRoads2Option.getInstance(event.getSuggestedConfigurationFile());
        proxy.preInitProxies();
        this.registerNetworkMessages();
        this.registerEventHandlers();

        tabs = JesRoads2Tab.getInstance();
        blocks = JesRoads2Block.getInstance();
        items = JesRoads2Item.getInstance(blocks.maxID);
    }

    private void registerNetworkMessages() {
        int id = 0;
        channel = NetworkRegistry.INSTANCE.newSimpleChannel("jesroads2");
        channel.registerMessage(MessageItemNBTUpdate.MessageRoadBuilderHandler.class, MessageItemNBTUpdate.class, id++, Side.SERVER);
        channel.registerMessage(MessageBlockUpdate.MessageBlockUpdateHandler.class, MessageBlockUpdate.class, id++, Side.CLIENT);
        channel.registerMessage(MessageFreewayEvent.MessageFreewayEventHandler.class, MessageFreewayEvent.class, id++, Side.SERVER);
        channel.registerMessage(MessageTileEntityNBTUpdate.MessageTileEntityNBTHandler.class, MessageTileEntityNBTUpdate.class, id++, Side.SERVER);
        channel.registerMessage(MessageTileEntityNBTUpdate.MessageTileEntityNBTHandler.class, MessageTileEntityNBTUpdate.class, id++, Side.CLIENT);
        channel.registerMessage(MessageItemDamageUpdate.MessageItemDamageUpdateHandler.class, MessageItemDamageUpdate.class, id++, Side.SERVER);
        channel.registerMessage(MessageGateControllerUser.MessageEditSignHandler.class, MessageGateControllerUser.class, id++, Side.CLIENT);
        channel.registerMessage(MessageAction.MessageActionHandler.class, MessageAction.class, id++, Side.SERVER);
        channel.registerMessage(MessageAction.MessageActionHandler.class, MessageAction.class, id++, Side.CLIENT);
        channel.registerMessage(MessageInventoryUpdate.MessageInventoryUpdateHandler.class, MessageInventoryUpdate.class, id++, Side.SERVER);
        channel.registerMessage(MessageBlockStateUpdate.MessageBlockStateUpdateHandler.class, MessageBlockStateUpdate.class, id++, Side.SERVER);
        channel.registerMessage(MessageTileEntity.MessageTileEntityHandler.class, MessageTileEntity.class, id++, Side.SERVER);
        channel.registerMessage(MessageTileEntity.MessageTileEntityHandler.class, MessageTileEntity.class, id++, Side.CLIENT);
    }

    private void registerEventHandlers() {
        handlerWorld = new HandlerWorld();
        handlerGui = new HandlerGui();
        handlerOverlay = new HandlerGuiOverlay();
        handlerInput = new HandlerInputEvent();
        handlerItem = new HandlerItemHold();
        handlerEntity = new HandlerEntity();
        handlerWeather = new HandlerWeatherEffects();
    }

    @Mod.EventHandler
    public void initialize(FMLInitializationEvent event) throws ExperimentalItemException {
        tabs.setIcons();
        proxy.initProxies();
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, handlerGui);
        this.registerGui();
        if (event.getSide() != Side.CLIENT) return;

        for (EnumKeyBindings key : EnumKeyBindings.values())
            ClientRegistry.registerKeyBinding(key.bind);
    }

    private void registerGui() {
        handlerGui.registerGui(Side.CLIENT, GuiFloodlightController.ID, GuiFloodlightController.class);
        handlerGui.registerGui(Side.CLIENT, GuiDynamicSignController.ID, GuiDynamicSignController.class);
        handlerGui.registerGui(Side.CLIENT, GuiIntersectionController.ID, GuiIntersectionController.class);
        handlerGui.registerGui(Side.CLIENT, GuiMemory.ID, GuiMemory.class);
        handlerGui.registerGui(Side.CLIENT, GuiRemoteController.ID, GuiRemoteController.class);
        handlerGui.registerGui(Side.CLIENT, GuiRoadBuilder.ID, GuiRoadBuilder.class);
        handlerGui.registerGui(Side.CLIENT, GuiRoadSignEdit.ID, GuiRoadSignEdit.class);
        handlerGui.registerGui(Side.CLIENT, GuiGateController.ID, GuiGateController.class);
        handlerGui.registerGui(Side.CLIENT, GuiTemplateManager.ID, GuiTemplateManager.class);
    }

    public static boolean isDevelopmentMode() {
        return Boolean.parseBoolean(System.getenv("use_experimental"));
    }

    public static void ensureDevelopmentMode(String module) {
        if (!isDevelopmentMode()) throw new ExperimentalItemException(module);
        else System.out.println("WARNING '" + module + "' is not allowed to be used outside of this environment");
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandSleep());
    }
}