package net.zorby.prc;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.Vec3i;
import net.zorby.prc.database.WorldEntry;
import org.lwjgl.glfw.GLFW;

public class Keybindings {
    private static KeyBinding toggleHud;
    private static KeyBinding skip;

    public static void register() {
        toggleHud = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.prc.hud",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.prc.keybindings"
        ));

        skip = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.prc.skip",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "category.prc.keybindings"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleHud.wasPressed()) {
                PRC.getInstance().getHud().toggle();
            }

            if (skip.wasPressed() && PRC.getInstance().getHud().isEnabled()) {
                Finder finder = PRC.getInstance().getFinder();

                Vec3i pos = new Vec3i(finder.getElytraCol().x(), 0, finder.getElytraCol().z());

                PRC.getInstance().getDatabase().addEntry(pos, WorldEntry.Style.Skipped);

                finder.findApproximate();
            }
        });
    }
}
