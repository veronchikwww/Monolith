package net.monolith.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.monolith.utils.RenderUtils;

import java.util.Collection;

public class PotionHudElement extends HudElement {
    private float heightAnim = 0;

    public PotionHudElement(int x, int y) {
        super(x, y, 130, 20);
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        Collection<StatusEffectInstance> effects = mc.player.getStatusEffects();
        boolean isChatOpen = mc.currentScreen instanceof net.minecraft.client.gui.screen.ChatScreen;
        
        // Если эффектов нет, но открыт чат - показываем шапку для редактирования
        float targetHeight = effects.isEmpty() ? (isChatOpen ? 22 : 0) : 22 + (effects.size() * 16);

        // Плавная анимация высоты окна
        heightAnim = RenderUtils.lerp(heightAnim, targetHeight, 0.15f * tickDelta);

        if (heightAnim < 1) return; // Полностью скрываем, если высота нулевая

        int currentHeight = (int) heightAnim;
        this.height = currentHeight; // Обновляем хитбокс для драга
        
        // Полупрозрачный красивый фон
        RenderUtils.drawRoundedRect(context, x, y, width, currentHeight, 5, 0xB30A0A0D);
        
        // Отрисовка текста шапки только если высота позволяет
        if (heightAnim > 15) {
            context.drawText(mc.textRenderer, "Potions", x + width / 2 - mc.textRenderer.getWidth("Potions") / 2, y + 6, 0xAA88CC, false);
            // Декоративная линия под шапкой
            context.fill(x + 5, y + 18, x + width - 5, y + 19, 0x33FFFFFF);
        }

        int eY = y + 22;
        for (StatusEffectInstance effect : effects) {
            if (eY + 14 > y + currentHeight) break; // Обрезаем текст, который выходит за рамки анимации
            
            String name = effect.getEffectType().value().getName().getString();
            int amp = effect.getAmplifier();
            if (amp > 0) name += " " + (amp + 1);

            int durationTicks = effect.getDuration();
            int seconds = durationTicks / 20;
            int minutes = seconds / 60;
            seconds = seconds % 60;
            String durationStr = String.format("%02d:%02d", minutes, seconds);

            context.drawText(mc.textRenderer, name, x + 8, eY + 2, 0xFFFFFF, false);
            context.drawText(mc.textRenderer, durationStr, x + width - 8 - mc.textRenderer.getWidth(durationStr), eY + 2, 0xAAAAAA, false);
            
            eY += 16;
        }
    }
}
