package net.monolith.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.monolith.ui.MonolithMainMenu;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        // Если экран не пустой и является TitleScreen (ванильное меню)
        if (screen instanceof TitleScreen) {
            ci.cancel(); // Отменяем установку ванильного экрана
            // Устанавливаем наше кастомное меню
            ((MinecraftClient)(Object)this).setScreen(new MonolithMainMenu());
        }
    }
}
