package mod.jesroads2.client.gui;

import java.util.Set;

import mod.jesroads2.JesRoads2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;

public class GuiJesRoads2OptionsFactory implements IModGuiFactory {
    @Override
    public void initialize(Minecraft minecraftInstance) {

    }

    @Override
    public boolean hasConfigGui() {
        return true;
    }

    @Override
    public GuiScreen createConfigGui(GuiScreen screen) {
        return new GuiJesRoads2Options(screen);
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    public static class GuiJesRoads2Options extends GuiConfig {
        public GuiJesRoads2Options(GuiScreen screen) {
            super(screen, JesRoads2.options.getElements(), JesRoads2.modid, false, false, "JesRoads2 Options");
        }
    }
}