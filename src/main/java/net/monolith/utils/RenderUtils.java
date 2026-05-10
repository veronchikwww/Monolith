package net.monolith.utils;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class RenderUtils {
    // Указываем ID нашего кастомного шрифта
    public static final Identifier CUSTOM_FONT = Identifier.of("monolith", "main");

    // Обертка для создания текста с нашим шрифтом
    public static Text getCustomText(String text) {
        return Text.literal(text).setStyle(Style.EMPTY.withFont(CUSTOM_FONT));
    }

    // Отрисовка кастомного текста
    public static void drawText(DrawContext context, TextRenderer renderer, String text, int x, int y, int color, boolean shadow) {
        context.drawText(renderer, getCustomText(text), x, y, color, shadow);
    }

    // Отрисовка кастомного текста по центру
    public static void drawCenteredText(DrawContext context, TextRenderer renderer, String text, int centerX, int y, int color, boolean shadow) {
        Text styled = getCustomText(text);
        context.drawText(renderer, styled, centerX - renderer.getWidth(styled) / 2, y, color, shadow);
    }

    // Получение ширины кастомного текста
    public static int getTextWidth(TextRenderer renderer, String text) {
        return renderer.getWidth(getCustomText(text));
    }

    public static void drawRect(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + height, color);
    }

    public static void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
        context.fill(x + radius, y, x + width - radius, y + height, color);
        context.fill(x, y + radius, x + radius, y + height - radius, color);
        context.fill(x + width - radius, y + radius, x + width, y + height - radius, color);
        
        context.fill(x + 1, y + 1, x + radius, y + radius, color);
        context.fill(x + width - radius, y + 1, x + width - 1, y + radius, color);
        context.fill(x + 1, y + height - radius, x + radius, y + height - 1, color);
        context.fill(x + width - radius, y + height - radius, x + width - 1, y + height - 1, color);
    }

    public static float lerp(float start, float end, float delta) {
        return start + (end - start) * delta;
    }

    public static int lerpColor(int from, int to, float delta) {
        int r1 = (from >> 16) & 0xFF; int g1 = (from >> 8) & 0xFF; int b1 = from & 0xFF; int a1 = (from >> 24) & 0xFF;
        int r2 = (to >> 16) & 0xFF;   int g2 = (to >> 8) & 0xFF;   int b2 = to & 0xFF;   int a2 = (to >> 24) & 0xFF;
        int r = (int) (r1 + (r2 - r1) * delta); int g = (int) (g1 + (g2 - g1) * delta);
        int b = (int) (b1 + (b2 - b1) * delta); int a = (int) (a1 + (a2 - a1) * delta);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
