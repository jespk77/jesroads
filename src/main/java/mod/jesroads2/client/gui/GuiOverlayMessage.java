package mod.jesroads2.client.gui;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

import mod.jesroads2.JesRoads2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class GuiOverlayMessage extends Gui implements IOverlay {
    public static final int id = 1;

    private static class Message {
        public final String text;
        public final int color;
        public int delay;

        public Message(String text, int color) {
            this.text = text;
            this.color = color;
            this.delay = JesRoads2.options.other.overlay_message_display;
        }
    }

    private final Minecraft minecraft;
    private final List<Message> messages;
    private int width, height;

    public GuiOverlayMessage(Minecraft minecraft, int width, int height) {
        this.minecraft = minecraft;
        this.messages = new ArrayList<>(5);
        this.width = width;
        this.height = height;
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void drawScreen() {
        int yPos = height - 55;
        Iterator<Message> it = messages.iterator();
        while (it.hasNext()) {
            try {
                Message msg = it.next();
                if (msg.delay > 0) {
                    drawCenteredString(minecraft.fontRenderer, msg.text, width / 2, yPos, msg.color);
                    msg.delay--;
                    yPos -= 10;
                } else it.remove();
            } catch (ConcurrentModificationException ignored) {
            }
        }
    }

    public void addMessage(String message) {
        addMessage(message, 0xAAAAAA);
    }

    public void addMessage(String message, int color) {
        if (messages.size() > 4) messages.remove(0);
        messages.add(new Message(message, color));
    }
}