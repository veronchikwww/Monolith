package net.monolith.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.monolith.module.Module;
import net.monolith.module.ModuleManager;
import net.monolith.utils.RenderUtils;
import java.util.List;

public class CsGuiScreen extends Screen {
    private String currentCategory = "Combat";
    private float openAnim = 0f;
    private final String[] categories = {"Combat", "Movement", "Visuals", "Player", "Misc"};
    private final int ACCENT = 0xFF00E5FF;
    
    private Module activeSettingsModule = null;
    private float settingsAnim = 0f;

    // Уменьшенные габариты
    private final int W = 440; 
    private final int H = 280;

    public CsGuiScreen() { super(Text.literal("Monolith CS")); }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, this.width, this.height, 0xB3000000, 0xD9000000);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        openAnim = RenderUtils.lerp(openAnim, 1f, 0.15f * delta);
        settingsAnim = RenderUtils.lerp(settingsAnim, activeSettingsModule != null ? 1f : 0f, 0.15f * delta);

        int x = (this.width - W) / 2; int y = (this.height - H) / 2;
        float shiftX = 60 * settingsAnim; 

        context.getMatrices().push();
        context.getMatrices().translate(this.width / 2f, this.height / 2f, 0);
        context.getMatrices().scale(0.96f + (0.04f * openAnim), 0.96f + (0.04f * openAnim), 1f);
        context.getMatrices().translate(-this.width / 2f - shiftX, -this.height / 2f, 0);

        // Панель настроек
        if (settingsAnim > 0.01f && activeSettingsModule != null) {
            int pW = 130; int pX = x + W + 5;
            context.enableScissor(pX, y, pX + (int)(pW * settingsAnim), y + H);
            RenderUtils.drawRoundedRect(context, pX, y, pW, H, 4, 0xFA0D0D10);
            RenderUtils.drawRect(context, pX, y + 35, pW, 1, 0x22FFFFFF);
            RenderUtils.drawText(context, textRenderer, "Settings", pX + 12, y + 14, ACCENT, false);

            int setY = y + 45;
            for (String mode : activeSettingsModule.modes) {
                boolean isCurrent = activeSettingsModule.currentMode.equals(mode);
                boolean hovered = mouseX + shiftX >= pX + 8 && mouseX + shiftX <= pX + pW - 8 && mouseY >= setY && mouseY <= setY + 24;
                RenderUtils.drawRoundedRect(context, pX + 8, setY, pW - 16, 22, 3, isCurrent ? (ACCENT & 0x44FFFFFF) : (hovered ? 0x1AFFFFFF : 0x00000000));
                RenderUtils.drawText(context, textRenderer, mode, pX + 15, setY + 7, isCurrent ? 0xFFFFFF : (hovered ? 0xDDDDDD : 0x777777), false);
                setY += 26;
            }
            context.disableScissor();
        }

        // Основное окно
        RenderUtils.drawRoundedRect(context, x, y, W, H, 4, 0xFA08080A); 
        RenderUtils.drawRoundedRect(context, x, y, W, 40, 4, 0xFF111114); 
        RenderUtils.drawRect(context, x + 10, y + 36, 60, 1, ACCENT);
        RenderUtils.drawText(context, textRenderer, "MONOLITH", x + 15, y + 15, ACCENT, true);

        // Вкладки
        int catX = x + 100;
        for (String cat : categories) {
            boolean isCurrent = currentCategory.equals(cat);
            boolean hovered = mouseX + shiftX >= catX && mouseX + shiftX <= catX + 60 && mouseY >= y && mouseY <= y + 40;
            RenderUtils.drawCenteredText(context, textRenderer, cat, catX + 30, y + 15, isCurrent ? 0xFFFFFF : (hovered ? 0xCCCCCC : 0x666666), false);
            if (isCurrent) RenderUtils.drawRoundedRect(context, catX + 12, y + 36, 36, 2, 1, ACCENT);
            catX += 65;
        }

        // Модули
        List<Module> mods = ModuleManager.getModulesByCategory(currentCategory);
        int col1X = x + 15; int col2X = x + W / 2 + 10;
        int mY1 = y + 55; int mY2 = y + 55;
        int modW = (W / 2) - 25;

        for (int i = 0; i < mods.size(); i++) {
            Module mod = mods.get(i);
            boolean inCol1 = (i % 2 == 0);
            int mX = inCol1 ? col1X : col2X;
            int mY = inCol1 ? mY1 : mY2;

            boolean hovered = mouseX + shiftX >= mX && mouseX + shiftX <= mX + modW && mouseY >= mY && mouseY <= mY + 22;
            mod.toggleAnim = RenderUtils.lerp(mod.toggleAnim, mod.enabled ? 1f : 0f, 0.2f * delta);
            
            // НОВЫЙ СТИЛЬ: Подсвечиваем весь блок
            int modBg = RenderUtils.lerpColor(0xFF0D0D10, (0x22 << 24) | (ACCENT & 0xFFFFFF), mod.toggleAnim);
            if (hovered) modBg = RenderUtils.lerpColor(modBg, 0xFF15151A, 0.5f);
            
            RenderUtils.drawRoundedRect(context, mX, mY, modW, 22, 3, modBg);
            if (mod.toggleAnim > 0.05f) RenderUtils.drawRect(context, mX, mY + 4, 2, 14, ACCENT);

            int textColor = RenderUtils.lerpColor(0xFFAAAAAA, 0xFFFFFFFF, Math.max(mod.toggleAnim, hovered ? 1f : 0f));
            RenderUtils.drawText(context, textRenderer, mod.name, mX + 8 + (int)(2 * mod.toggleAnim), mY + 7, mod.enabled ? ACCENT : textColor, false);

            if (!mod.modes.isEmpty()) RenderUtils.drawText(context, textRenderer, ">>", mX + modW - 15, mY + 7, activeSettingsModule == mod ? ACCENT : 0x44666666, false);

            if (inCol1) mY1 += 26; else mY2 += 26;
        }
        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (this.width - W) / 2; int y = (this.height - H) / 2;
        float shiftX = 60 * settingsAnim; 

        if (settingsAnim > 0.9f && activeSettingsModule != null) {
            int pX = x + W + 5; int setY = y + 45;
            for (String mode : activeSettingsModule.modes) {
                if (mouseX + shiftX >= pX + 8 && mouseX + shiftX <= pX + 130 - 8 && mouseY >= setY && mouseY <= setY + 22 && button == 0) {
                    activeSettingsModule.currentMode = mode;
                    if (activeSettingsModule.name.equals("ClickGui")) {
                        if (mode.equals("DropDown")) MinecraftClient.getInstance().setScreen(new ClickGuiScreen());
                        else if (mode.equals("Cards")) MinecraftClient.getInstance().setScreen(new CardsGuiScreen());
                    }
                    return true;
                }
                setY += 26;
            }
        }

        int catX = x + 100;
        for (String cat : categories) {
            if (mouseX + shiftX >= catX && mouseX + shiftX <= catX + 60 && mouseY >= y && mouseY <= y + 40 && button == 0) {
                currentCategory = cat; activeSettingsModule = null; return true;
            }
            catX += 65;
        }

        List<Module> mods = ModuleManager.getModulesByCategory(currentCategory);
        int col1X = x + 15; int col2X = x + W / 2 + 10;
        int mY1 = y + 55; int mY2 = y + 55;
        int modW = (W / 2) - 25;

        for (int i = 0; i < mods.size(); i++) {
            Module mod = mods.get(i);
            boolean inCol1 = (i % 2 == 0);
            int mX = inCol1 ? col1X : col2X;
            int mY = inCol1 ? mY1 : mY2;
            if (mouseX + shiftX >= mX && mouseX + shiftX <= mX + modW && mouseY >= mY && mouseY <= mY + 22) {
                if (button == 0) { mod.toggle(); return true; }
                else if (button == 1 && !mod.modes.isEmpty()) { activeSettingsModule = (activeSettingsModule == mod ? null : mod); return true; }
            }
            if (inCol1) mY1 += 26; else mY2 += 26;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    @Override public boolean shouldPause() { return false; }
}
