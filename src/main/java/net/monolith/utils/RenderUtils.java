package net.monolith.utils;

import net.minecraft.client.gui.DrawContext;

public class RenderUtils {
    public static void drawRect(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + height, color);
    }

    public static void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
        // Центральная часть
        context.fill(x + radius, y, x + width - radius, y + height, color);
        // Левая часть
        context.fill(x, y + radius, x + radius, y + height - radius, color);
        // Правая часть
        context.fill(x + width - radius, y + radius, x + width, y + height - radius, color);
        
        // Углы (упрощенная аппроксимация для кастомного 2D)
        context.fill(x + 1, y + 1, x + radius, y + radius, color); // ЛВ
        context.fill(x + width - radius, y + 1, x + width - 1, y + radius, color); // ПВ
        context.fill(x + 1, y + height - radius, x + radius, y + height - 1, color); // ЛН
        context.fill(x + width - radius, y + height - radius, x + width - 1, y + height - 1, color); // ПН
    }

    public static float lerp(float start, float end, float delta) {
        return start + (end - start) * delta;
    }
}
