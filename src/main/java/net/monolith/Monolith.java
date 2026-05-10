package net.monolith;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.monolith.ui.ClickGuiScreen;
import net.monolith.ui.CardsGuiScreen;
import net.monolith.ui.CsGuiScreen;
import net.monolith.module.Module;
import net.monolith.module.ModuleManager;
import net.monolith.hud.HudManager;

public class Monolith implements ClientModInitializer {
    public static final String MOD_ID = "monolith";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static KeyBinding guiKeyBinding;

    @Override
    public void onInitializeClient() {
        ModuleManager.init();
        HudManager.init();

        guiKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.monolith.opengui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.monolith.main"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (guiKeyBinding.wasPressed()) {
                if (client.currentScreen == null) {
                    Module cg = ModuleManager.getModule("ClickGui");
                    if (cg != null) {
                        switch (cg.currentMode) {
                            case "Cards": client.setScreen(new CardsGuiScreen()); break;
                            case "CsGui": client.setScreen(new CsGuiScreen()); break;
                            case "DropDown": default: client.setScreen(new ClickGuiScreen()); break;
                        }
                    } else {
                        client.setScreen(new CsGuiScreen());
                    }
                }
            }
        });

        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            HudManager.render(context, tickDelta.getTickDelta(true));
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof ChatScreen) {
                ScreenMouseEvents.allowMouseClick(screen).register((screen1, mouseX, mouseY, button) -> {
                    if (HudManager.mouseClicked(mouseX, mouseY, button)) return false;
                    return true;
                });
                ScreenMouseEvents.allowMouseRelease(screen).register((screen1, mouseX, mouseY, button) -> {
                    HudManager.mouseReleased(button);
                    return true;
                });
            }
        });
    }
}
