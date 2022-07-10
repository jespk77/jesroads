package mod.jesroads2.handler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.client.gui.GuiOverlay;
import mod.jesroads2.client.gui.GuiOverlayMessage;
import mod.jesroads2.client.gui.IOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldSummary;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HandlerGuiOverlay {
    private final Minecraft minecraft;
    private final List<IOverlay> overlays;

    private int width, height;

    public HandlerGuiOverlay() {
        MinecraftForge.EVENT_BUS.register(this);
        minecraft = Minecraft.getMinecraft();
        overlays = new ArrayList<>(5);

        updateSize();
        overlays.add(GuiOverlay.id, new GuiOverlay(minecraft, width, height));
        overlays.add(GuiOverlayMessage.id, new GuiOverlayMessage(minecraft, width, height));
    }

    public IOverlay get(int id) {
        return overlays.get(id);
    }

    public GuiOverlay getOverlay() {
        return (GuiOverlay) get(GuiOverlay.id);
    }

    public GuiOverlayMessage getMessage() {
        return (GuiOverlayMessage) get(GuiOverlayMessage.id);
    }

    @SubscribeEvent
    public void onGUIOpen(GuiOpenEvent event) {
        GuiScreen gui = event.getGui();
        if (gui instanceof GuiMainMenu) {
            if (!GuiScreen.isAltKeyDown() && JesRoads2.isDevelopmentMode()) {
                try {
                    ISaveFormat saveLoader = minecraft.getSaveLoader();
                    List<WorldSummary> worlds = saveLoader.getSaveList();
                    WorldSummary worldToLoad = worlds.get(0);
                    if (saveLoader.canLoadWorld(worldToLoad.getFileName()))
                        minecraft.launchIntegratedServer(worldToLoad.getFileName(), worldToLoad.getDisplayName(), null);
                } catch (Exception e) {
                    System.err.println("Failed to load minecraft save list: ");
                    e.printStackTrace(System.err);
                }
            }

            Calendar cal = Calendar.getInstance();
            Field text = null;
            try {
                text = GuiMainMenu.class.getDeclaredFields()[3];
                assert text.getType() == String.class;
                int month = cal.get(Calendar.MONTH), day = cal.get(Calendar.DAY_OF_MONTH);
                if (month == Calendar.JULY && day == 24) {
                    text.setAccessible(true);
                    text.set(gui, "Happy Birthday JesRoads2!");
                } else if (month == Calendar.AUGUST && day == 22) {
                    text.setAccessible(true);
                    text.set(gui, "Happy Birthday JespK77!");
                }
            } catch (AssertionError e) {
                System.out.println("Field has wrong type: " + text != null ? text.getType().toString() : "null");
            } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException | SecurityException |
                     IllegalAccessException e) {
                System.out.println("Cannot edit field: " + e);
            }
        }
    }

    @SubscribeEvent
    public void renderGUI(RenderGameOverlayEvent.Post event) {
        updateSize();
        if (event.getType() == ElementType.EXPERIENCE) {
            for (IOverlay overlay : overlays)
                overlay.drawScreen();
        }
    }

    private void updateSize() {
        if (minecraft.displayWidth != width || minecraft.displayHeight != height) {
            ScaledResolution res = new ScaledResolution(minecraft);
            width = res.getScaledWidth();
            height = res.getScaledHeight();
            for (IOverlay overlay : overlays)
                overlay.resize(width, height);
        }
    }
}