package net.monolith.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.monolith.module.Module;
import net.monolith.module.ModuleManager;
import net.monolith.utils.RenderUtils;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class CardsGuiScreen extends Screen {
    private final String[] categories = {"Combat", "Movement", "Visuals", "Player", "Misc"};
    private int currentIndex = 0;
    private float carouselAnim = 0f;
    private float openAnim = 0f;
    private float scrollY = 0f;
    private float targetScrollY = 0f;
    private final int ACCENT = 0xFF00E5FF;

    public CardsGuiScreen() { super(Text.literal("Monolith Cards")); }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, this.width, this.height, 0xEE000000, 0xFF000000); 
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        openAnim = RenderUtils.lerp(openAnim, 1f, 0.15f * delta);
        carouselAnim = RenderUtils.lerp(carouselAnim, currentIndex, 0.2f * delta); 
        scrollY = RenderUtils.lerp(scrollY, targetScrollY, 0.2f * delta);

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int cardWidth = 240;
        int cardHeight = 340;

        List<Integer> renderOrder = new ArrayList<>();
        for (int i = 0; i < categories.length; i++) renderOrder.add(i);
        renderOrder.sort((a, b) -> Float.compare(Math.abs(b - carouselAnim), Math.abs(a - carouselAnim)));

        context.getMatrices().push();
        context.getMatrices().translate(centerX, centerY, 0);
        context.getMatrices().scale(0.8f + (0.2f * openAnim), 0.8f + (0.2f * openAnim), 1f);
        context.getMatrices().translate(-centerX, -centerY, 0);

        for (int i : renderOrder) {
            float dist = i - carouselAnim;
            float absDist = Math.abs(dist);
            float scale = Math.max(0.65f, 1f - (absDist * 0.15f));
            int xOffset = (int) (dist * 160);
            float opacity = Math.max(0.2f, 1f - (absDist * 0.5f)); 

            int cardX = centerX + xOffset;
            context.getMatrices().push();
            context.getMatrices().translate(cardX, centerY, 0);
            context.getMatrices().scale(scale, scale, 1f);
            
            int drawX = -cardWidth / 2;
            int drawY = -cardHeight / 2;
            int alphaInt = (int) (opacity * 255);
            int bgColor = (alphaInt << 24) | 0x0C0C0F;
            int outlineColor = (alphaInt << 24) | (absDist < 0.1f ? (ACCENT & 0xFFFFFF) : 0x222222);

            RenderUtils.drawRoundedRect(context, drawX - 1, drawY - 1, cardWidth + 2, cardHeight + 2, 12, outlineColor);
            RenderUtils.drawRoundedRect(context, drawX, drawY, cardWidth, cardHeight, 12, bgColor);

            // ПРИМЕНЕНИЕ ШРИФТА: Заголовок категории
            RenderUtils.drawCenteredText(context, textRenderer, categories[i], 0, drawY + 15, (alphaInt << 24) | 0xFFFFFF, true);
            RenderUtils.drawRect(context, drawX + 20, drawY + 35, cardWidth - 40, 1, (alphaInt << 24) | 0x333333);

            if (i == currentIndex) {
                renderModules(context, categories[i], mouseX, mouseY, drawX, drawY, cardWidth, cardHeight, delta);
            }
            context.getMatrices().pop();
        }
        context.getMatrices().pop();
    }

    private void renderModules(DrawContext context, String category, int mouseX, int mouseY, int startX, int startY, int width, int height, float delta) {
        List<Module> mods = ModuleManager.getModulesByCategory(category);
        context.enableScissor(startX, startY + 40, startX + width, startY + height - 10);
        int mY = startY + 45 + (int) scrollY;
        
        for (Module mod : mods) {
            int realX = (this.width / 2) - (width / 2);
            int realY = (this.height / 2) - (height / 2);
            boolean hovered = mouseX >= realX + 15 && mouseX <= realX + width - 15 && mouseY >= realY + (mY - startY) && mouseY <= realY + (mY - startY) + 30;
            
            mod.hoverAnim = RenderUtils.lerp(mod.hoverAnim, hovered ? 1f : 0f, 0.2f * delta);
            mod.toggleAnim = RenderUtils.lerp(mod.toggleAnim, mod.enabled ? 1f : 0f, 0.2f * delta);
            
            int bgColor = RenderUtils.lerpColor(0xFF121215, (0x33 << 24) | (ACCENT & 0xFFFFFF), mod.toggleAnim);
            if (mod.hoverAnim > 0.05f) bgColor = RenderUtils.lerpColor(bgColor, 0xFF1A1A1E, mod.hoverAnim);

            RenderUtils.drawRoundedRect(context, startX + 15, mY, width - 30, 30, 6, bgColor);
            if (mod.toggleAnim > 0.05f) RenderUtils.drawRoundedRect(context, startX + 15, mY + 6, 2, 18, 1, ACCENT);
            
            int textColor = RenderUtils.lerpColor(0xFFAAAAAA, 0xFFFFFFFF, Math.max(mod.toggleAnim, mod.hoverAnim));
            if (mod.enabled) textColor = ACCENT; 
            
            // ПРИМЕНЕНИЕ ШРИФТА: Название модуля
            RenderUtils.drawText(context, textRenderer, mod.name, startX + 25 + (int)(4 * mod.toggleAnim), mY + 11, textColor, false);

            if (!mod.modes.isEmpty()) {
                // ПРИМЕНЕНИЕ ШРИФТА: Индикатор настроек
                RenderUtils.drawText(context, textRenderer, mod.expanded ? "[-]" : "[+]", startX + width - 35, mY + 11, mod.expanded ? ACCENT : 0x666666, false);
            }

            mY += 34;
            mod.expandAnim = RenderUtils.lerp(mod.expandAnim, (mod.expanded && !mod.modes.isEmpty()) ? 1f : 0f, 0.2f * delta);
            if (mod.expandAnim > 0.01f) {
                int expHeight = (int) (mod.modes.size() * 20 * mod.expandAnim); 
                int subY = mY;
                RenderUtils.drawRect(context, startX + 25, mY, 1, expHeight - 2, 0x33FFFFFF);
                
                int textAlpha = (int)(255 * Math.min(1f, mod.expandAnim * 1.5f));
                for (String mode : mod.modes) {
                    boolean isCurrent = mod.currentMode.equals(mode);
                    int modeColor = isCurrent ? ACCENT : 0xFF888888;
                    modeColor = (modeColor & 0x00FFFFFF) | (textAlpha << 24);

                    // ПРИМЕНЕНИЕ ШРИФТА: Режимы модуля
                    RenderUtils.drawText(context, textRenderer, mode, startX + 35, subY + 6, modeColor, false);
                    subY += 20;
                }
                mY += expHeight + 4;
            }
        }
        context.disableScissor();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int centerX = this.width / 2; int centerY = this.height / 2;
        int cardWidth = 240; int cardHeight = 340;

        for (int i = 0; i < categories.length; i++) {
            if (i == currentIndex) continue;
            int cardX = centerX + (int) ((i - carouselAnim) * 160);
            if (mouseX >= cardX - 80 && mouseX <= cardX + 80 && mouseY >= centerY - cardHeight/2 && mouseY <= centerY + cardHeight/2 && button == 0) {
                currentIndex = i; targetScrollY = 0f; return true;
            }
        }

        int startX = centerX - cardWidth / 2; int startY = centerY - cardHeight / 2;
        if (mouseX >= startX && mouseX <= startX + cardWidth && mouseY >= startY + 40 && mouseY <= startY + cardHeight - 10) {
            List<Module> mods = ModuleManager.getModulesByCategory(categories[currentIndex]);
            int mY = startY + 45 + (int) scrollY;
            for (Module mod : mods) {
                if (mouseX >= startX + 15 && mouseX <= startX + cardWidth - 15 && mouseY >= mY && mouseY <= mY + 30) {
                    if (button == 0) { mod.toggle(); return true; } 
                    else if (button == 1 && !mod.modes.isEmpty()) { mod.expanded = !mod.expanded; return true; }
                }
                mY += 34;
                if (mod.expanded && !mod.modes.isEmpty()) {
                    for (String mode : mod.modes) {
                        if (mouseX >= startX + 35 && mouseX <= startX + cardWidth - 15 && mouseY >= mY && mouseY <= mY + 20 && button == 0) { 
                            mod.currentMode = mode; 
                            if (mod.name.equals("ClickGui")) {
                                if (mode.equals("DropDown")) MinecraftClient.getInstance().setScreen(new ClickGuiScreen());
                                else if (mode.equals("CsGui")) MinecraftClient.getInstance().setScreen(new CsGuiScreen());
                            }
                            return true; 
                        }
                        mY += 20;
                    }
                    mY += 4;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        targetScrollY += verticalAmount * 30;
        if (targetScrollY > 0) targetScrollY = 0;
        return true;
    }
    @Override public boolean shouldPause() { return false; }
}
