package mod.jesroads2;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import mod.jesroads2.block.sign.BlockSign;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class JesRoads2Option {
    private static JesRoads2Option option;

    protected static Configuration cfg;

    public final CategoryRoad roads;
    public final CategoryRoadSign road_sign;
    public final CategoryRoadBuilder road_builder;
    public final CategoryFreewayController freeway_controller;
    public final CategoryIntersectionController intersection_controller;
    public final CategoryOther other;

    public static JesRoads2Option getInstance() {
        return getInstance(null);
    }

    public static JesRoads2Option getInstance(File file) {
        if (option == null && file != null) option = new JesRoads2Option(file);
        return option;
    }

    private JesRoads2Option(File file) {
        cfg = new Configuration(file);

        roads = new CategoryRoad();
        road_sign = new CategoryRoadSign();
        road_builder = new CategoryRoadBuilder();
        freeway_controller = new CategoryFreewayController();
        intersection_controller = new CategoryIntersectionController();
        other = new CategoryOther();
        setValues();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void configChange(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(JesRoads2.modid)) setValues();
    }

    public void setValues() {
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                Object o = field.get(this);
                if (o instanceof ICategory) {
                    ((ICategory) o).setValues();
                }
            } catch (IllegalArgumentException | IllegalAccessException ignored) {
            }
        }

        if (cfg.hasChanged()) cfg.save();
    }

    public List<IConfigElement> getElements() {
        List<IConfigElement> elements = new ArrayList<>();
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                Object o = field.get(this);
                if (o instanceof ICategory) {
                    elements.add(((ICategory) o).getElement());
                }
            } catch (IllegalArgumentException | IllegalAccessException ignored) {
            }
        }
        return elements;
    }

    private interface ICategory {
        ConfigElement getElement();

        void setValues();
    }

    public static class CategoryRoad implements ICategory {
        public static final String name = "road_blocks";
        private final ConfigElement element;

        public boolean use_gametime;
        public int gametime, realtime;
        public double speed_boost0, speed_boost1, speed_boost2;

        protected CategoryRoad() {
            element = new ConfigElement(JesRoads2Option.cfg.getCategory(name));
        }

        @Override
        public ConfigElement getElement() {
            return element;
        }

        @Override
        public void setValues() {
            use_gametime = JesRoads2Option.cfg.get(name, "use_gametime", true, "use gametime to calculate age").setRequiresWorldRestart(true).getBoolean();
            gametime = JesRoads2Option.cfg.get(name, "age_gametime", 130, "game world days before age").setMinValue(0).setRequiresWorldRestart(true).getInt();
            realtime = JesRoads2Option.cfg.get(name, "age_realtime", 50, "real world days before age").setMinValue(0).setRequiresWorldRestart(true).getInt();

            speed_boost0 = JesRoads2Option.cfg.get(name, "speed_boost_level0", 1.6D, "player speed boost with empty hand and sprinting").setMinValue(1).setMaxValue(2).getDouble();
            speed_boost1 = JesRoads2Option.cfg.get(name, "speed_boost_level1", 1.5D, "player speed boost with empty hand and not sprinting").setMinValue(1).setMaxValue(2).getDouble();
            speed_boost2 = JesRoads2Option.cfg.get(name, "speed_boost_level2", 1.2D, "player speed boost while holding block and sprinting").setMinValue(1).setMaxValue(2).getDouble();
        }
    }

    public static class CategoryRoadSign implements ICategory {
        public static final String name = "road_signs";
        protected final ConfigElement element;

        public int text_min_pos, text_max_pos, text_max_size;

        protected CategoryRoadSign() {
            element = new ConfigElement(JesRoads2Option.cfg.getCategory(name));
        }

        @Override
        public ConfigElement getElement() {
            return element;
        }

        @Override
        public void setValues() {
            text_min_pos = JesRoads2Option.cfg.get(name, "text_pos_min", -100, "min position sign text can move").getInt();
            text_max_pos = JesRoads2Option.cfg.get(name, "text_pos_max", 100, "max position sign text can move").getInt();
            text_max_size = JesRoads2Option.cfg.get(name, "text_size_max", 10, "max size of sign text").setMinValue(0).getInt();
        }
    }

    public static class CategoryRoadBuilder implements ICategory {
        public static final String name = "road_builder";
        protected final ConfigElement element;

        public int max_shoulder, max_lane, max_remove, max_terrain;
        public int limit_log_remove, limit_level, limit_slope, limit_replace, limit_place;

        protected CategoryRoadBuilder() {
            element = new ConfigElement(JesRoads2Option.cfg.getCategory(name));
        }

        @Override
        public ConfigElement getElement() {
            return element;
        }

        @Override
        public void setValues() {
            max_shoulder = JesRoads2Option.cfg.get(name, "max_shoulder", 3, "maximum blocks for shoulder").setMinValue(0).setMaxValue(10).getInt();
            max_lane = JesRoads2Option.cfg.get(name, "max_lane", 6, "maximum number of lanes").setMinValue(0).setMaxValue(10).getInt();
            max_remove = JesRoads2Option.cfg.get(name, "max_remove", 15, "maximum blocks to remove").setMinValue(0).setMaxValue(30).getInt();
            max_terrain = JesRoads2Option.cfg.get(name, "max_terrain", 5, "maximum blocks of terrain to place").setMinValue(0).setMaxValue(10).getInt();

            limit_log_remove = JesRoads2Option.cfg.get(name, "limit_tree_remove", 100, "maximum tree blocks to remove").setMinValue(0).setMaxValue(300).getInt();
            limit_level = JesRoads2Option.cfg.get(name, "limit_level", 20, "maximum blocks to remove when leveling terrain").setMinValue(0).setMaxValue(50).getInt();
            limit_slope = JesRoads2Option.cfg.get(name, "limit_slope", 5, "maximum blocks to create slope").setMinValue(0).setMaxValue(20).getInt();
            limit_replace = JesRoads2Option.cfg.get(name, "limit_replace", 3, "range of blocks to replace surface").setMinValue(0).setMaxValue(5).getInt();
            limit_place = JesRoads2Option.cfg.get(name, "limit_place", 20, "maximum blocks to place at a time in placement mode").setMinValue(1).setMaxValue(50).getInt();
        }
    }

    public static class CategoryFreewayController implements ICategory {
        public static final String name = "freeway_controller";
        protected final ConfigElement element;

        public int range_left, range_up, range_back;

        protected CategoryFreewayController() {
            element = new ConfigElement(JesRoads2Option.cfg.getCategory(name));
        }

        @Override
        public ConfigElement getElement() {
            return element;
        }

        @Override
        public void setValues() {
            range_left = JesRoads2Option.cfg.get(name, "scan_range_left", 20, "blocks to check to the left of the controller for sign scanning").setMinValue(0).setMaxValue(50).getInt();
            range_up = JesRoads2Option.cfg.get(name, "scan_range_up", 7, "blocks to check on top of the controller for sign scanning").setMinValue(0).setMaxValue(50).getInt();
            range_back = JesRoads2Option.cfg.get(name, "scan_range_back", 15, "blocks to check to the back of the controller for sign scanning").setMinValue(0).setMaxValue(50).getInt();
        }
    }

    public static class CategoryIntersectionController implements ICategory {
        public static final String name = "intersection_controller";
        protected final ConfigElement element;

        public int cycle_tick, cycle_empty, cycle_yellow, cycle_wait;
        public int time_enabled, time_disabled;

        protected CategoryIntersectionController() {
            element = new ConfigElement(JesRoads2Option.cfg.getCategory(name));
        }

        @Override
        public ConfigElement getElement() {
            return element;
        }

        @Override
        public void setValues() {
            cycle_tick = JesRoads2Option.cfg.get(name, "ticks_per_cycle", 35, "ticks between cycles").setMinValue(20).setMaxValue(100).getInt();
            cycle_empty = JesRoads2Option.cfg.get(name, "empty_cycles", 5, "number of cycles of no traffic before default reset").setMinValue(1).setMaxValue(25).getInt();
            cycle_yellow = JesRoads2Option.cfg.get(name, "yellow_cycles", 2, "number of cycles lights stay yellow").setMinValue(1).setMaxValue(10).getInt();
            cycle_wait = JesRoads2Option.cfg.get(name, "wait_cycles", 9, "maximum cycles to wait before light changes anyway").setMinValue(1).setMaxValue(15).getInt();

            time_enabled = JesRoads2Option.cfg.get(name, "enable_time", 23459, "world day time that controllers get enabled").setMinValue(0).setMaxValue(24000).getInt();
            time_disabled = JesRoads2Option.cfg.get(name, "disable_time", 13805, "world day time that controllers get disabled").setMinValue(0).setMaxValue(24000).getInt();
        }
    }

    public static class CategoryOther implements ICategory {
        public static final String name = "other";
        protected final ConfigElement element;

        public int overlay_message_display,
                traffic_random_detection,
                freeway_support_max_length;
        public boolean glowing_textures;

        public String[] sign_textcolors;

        protected CategoryOther() {
            element = new ConfigElement(JesRoads2Option.cfg.getCategory(name));
        }

        @Override
        public ConfigElement getElement() {
            return element;
        }

        @Override
        public void setValues() {
            overlay_message_display = JesRoads2Option.cfg.get(name, "overlay_message_delay", 350, "number of ticks to display message").setMinValue(100).setMaxValue(1000).getInt();
            traffic_random_detection = JesRoads2Option.cfg.get(name, "road_detector_random_traffic", 0, "1 in N chance for road detectors to simulate traffic").setMinValue(0).setMaxValue(15).getInt();
            freeway_support_max_length = JesRoads2Option.cfg.get(name, "freeway_support_max_length", 30, "max distance to search for other freeway support posts for autocomplete").setMinValue(0).setMaxValue(50).getInt();
            glowing_textures = JesRoads2Option.cfg.get(name, "glowing_textures", true, "Control whether signs/lights glow to make them more visible at night (disable to use Optifine emissive textures)").getBoolean();
            sign_textcolors = JesRoads2Option.cfg.get(name, "sign_textcolors", new String[]{}, "sign text color keyword map - entry format 'id:(0x)color' where len(id) < 6").getStringList();
            BlockSign.updateColorMap(sign_textcolors);
        }
    }
}
