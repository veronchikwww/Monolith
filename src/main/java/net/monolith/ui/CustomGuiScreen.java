package net.monolith.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.monolith.module.Module;
import net.monolith.module.ModuleManager;
import net.monolith.utils.RenderUtils;

import java.util.List;

public class CustomGuiScreen extends Screen {
    private String currentCategory = "Combat";
    private float openAnim = 0f;
    private final String[] categories = {"Combat", "Movement", "Visuals", "Player", "Misc"};

    public CustomGuiScreen() {
        super(Text.literal("Monolith Dashboard"));
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, this.width, this.height, 0x99050508, 0xDD000000);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        openAnim = RenderUtils.lerp(openAnim, 1f, 0.15f * delta);

        int guiWidth = 460;
        int guiHeight = 300;
        int x = (this.width - guiWidth) / 2;
        int y = (this.height - guiHeight) / 2;

        context.getMatrices().push();
        context.getMatrices().translate(this.width / 2f, this.height / 2f, 0);
        context.getMatrices().scale(0.9f + (0.1f * openAnim), 0.9f + (0.1f * openAnim), 1f);
        context.getMatrices().translate(-this.width / 2f, -this.height / 2f, 0);

        // Главный фон Хаба (сильное закругление - "не квадрат")
        RenderUtils.drawRoundedRect(context, x, y, guiWidth, guiHeight, 20, 0xFA101014);
        RenderUtils.drawRoundedRect(context, x, y, 130, guiHeight, 20, 0xFA15151A); // Панель категорий

        // Логотип
        context.drawText(textRenderer, "MONOLITH", x + 35, y + 25, 0xFF8E2DE2, true);
        context.fill(x + 20, y + 40, x + 110, y + 41, 0x33FFFFFF);

        // Отрисовка Категорий (Левая панель)
        int catY = y + 60;
        for (String cat : categories) {
            boolean isCurrent = currentCategory.equals(cat);
            boolean hovered = mouseX >= x + 10 && mouseX <= x + 120 && mouseY >= catY && mouseY <= catY + 25;
            
            if (isCurrent) {
                RenderUtils.drawRoundedRect(context, x + 15, catY, 100, 25, 8, 0x558E2DE2);
            } else if (hovered) {
                RenderUtils.drawRoundedRect(context, x + 15, catY, 100, 25, 8, 0x22FFFFFF);
            }
            
            context.drawText(textRenderer, cat, x + 40, catY + 8, isCurrent ? 0xFFFFFF : 0xAAAAAA, false);
            catY += 35;
        }

        // Отрисовка Модулей (Сетка справа)
        List<Module> mods = ModuleManager.getModulesByCategory(currentCategory);
        int startX = x + 150;
        int startY = y + 25;
        int modWidth = 140;
        int modHeight = 35;

        context.drawText(textRenderer, currentCategory + " Modules", startX, startY, 0xFFFFFF, true);
        
        int row = 0;
        int col = 0;
        for (int i = 0; i < mods.size(); i++) {
            Module mod = mods.get(i);
            int mX = startX + (col * (modWidth + 15));
            int mY = startY + 30 + (row * (modHeight + 15));
            
            boolean hovered = mouseX >= mX && mouseX <= mX + modWidth && mouseY >= mY && mouseY <= mY + modHeight;

            // Фон модуля (в виде скругленной таблетки/pill)
            RenderUtils.drawRoundedRect(context, mX, mY, modWidth, modHeight, 10, mod.enabled ? 0x998E2DE2 : (hovered ? 0x44FFFFFF : 0x22FFFFFF));
            context.drawText(textRenderer, mod.name, mX + 15, mY + 13, mod.enabled ? 0xFFFFFF : 0xDDDDDD, false);

            // Если у модуля есть режимы, показываем текущий
            if (!mod.modes.isEmpty()) {
                String modeText = mod.currentMode;
                context.drawText(textRenderer, modeText, mX + modWidth - 10 - textRenderer.getWidth(modeText), mY + 13, 0xAAAAAA, false);
            }

            col++;
            if (col > 1) {
                col = 0;
                row++;
            }
        }

        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int guiWidth = 460;
        int guiHeight = 300;
        int x = (this.width - guiWidth) / 2;
        int y = (this.height - guiHeight) / 2;

        // Клик по категории
        int catY = y + 60;
        for (String cat : categories) {
            if (mouseX >= x + 10 && mouseX <= x + 120 && mouseY >= catY && mouseY <= catY + 25 && button == 0) {
                currentCategory = cat;
                return true;
            }
            catY += 35;
        }

        // Клик по модулю
        List<Module> mods = ModuleManager.getModulesByCategory(currentCategory);
        int startX = x + 150;
        int startY = y + 25;
        int modWidth = 140;
        int modHeight = 35;
        int row = 0;
        int col = 0;

        for (Module mod : mods) {
            int mX = startX + (col * (modWidth + 15));
            int mY = startY + 30 + (row * (modHeight + 15));

            if (mouseX >= mX && mouseX <= mX + modWidth && mouseY >= mY && mouseY <= mY + modHeight) {
                if (button == 0) {
                    mod.toggle();
                    return true;
                } else if (button == 1) { // ПКМ по модулю в Custom GUI меняет его режим по кругу
                    mod.cycleMode();
                    return true;
                }
            }

            col++;
            if (col > 1) {
                col = 0;
                row++;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
