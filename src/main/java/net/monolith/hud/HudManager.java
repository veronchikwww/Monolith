package net.monolith.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.monolith.module.ModuleManager;
import net.monolith.utils.RenderUtils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HudManager {
    public static final List<HudElement> elements = new ArrayList<>();

    public static void init() {
        elements.add(new HudElement(10, 10, 140, 16) {
            @Override
            public void render(DrawContext context, float tickDelta) {
                MinecraftClient mc = MinecraftClient.getInstance();
                String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                String name = mc.getSession() != null ? mc.getSession().getUsername() : "Player";
                int fps = mc.getCurrentFps();
                
                String text = "Monolith | " + name + " | " + time + " | " + fps + " FPS";
                this.width = mc.textRenderer.getWidth(text) + 12;
                
                RenderUtils.drawRoundedRect(context, x, y, width, height, 4, 0x990A0A0A);
                // Градиентная полоска сверху (премиум стиль)
                context.fillGradient(x + 2, y + 1, x + width - 2, y + 2, 0xFF8E2DE2, 0xFF4A00E0);
                context.drawText(mc.textRenderer, text, x + 6, y + 5, 0xFFFFFF, false);
            }
        });

        // Добавляем Potion HUD
        elements.add(new PotionHudElement(10, 40));
    }

    public static void render(DrawContext context, float tickDelta) {
        if (ModuleManager.getModule("HUD") != null && !ModuleManager.getModule("HUD").enabled) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        boolean inChat = mc.currentScreen instanceof ChatScreen;

        for (HudElement el : elements) {
            if (inChat) {
                int mouseX = (int)(mc.mouse.getX() * mc.getWindow().getScaledWidth() / mc.getWindow().getWidth());
                int mouseY = (int)(mc.mouse.getY() * mc.getWindow().getScaledHeight() / mc.getWindow().getHeight());
                el.renderEditMode(context, mouseX, mouseY);
            } else {
                el.render(context, tickDelta);
            }
        }
    }

    public static boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (ModuleManager.getModule("HUD") != null && !ModuleManager.getModule("HUD").enabled) return false;
        for (HudElement el : elements) {
            if (el.mouseClicked(mouseX, mouseY, button)) return true;
        }
        return false;
    }

    public static void mouseReleased(int button) {
        for (HudElement el : elements) {
            el.mouseReleased(button);
        }
    }
}
