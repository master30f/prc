package net.zorby.prc.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.zorby.prc.PRC;
import net.zorby.prc.gui.InputWorldSeedScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.Objects;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Shadow public abstract ClientConnection getConnection();

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void onGameJoin(CallbackInfo ci) {
        Long seed = PRC.getInstance().getDatabase().getCurrentSeed();

        if (seed == null) {
            PRC.logger.info("retrieving seed...");

            ClientConnection connection = getConnection();

            if (connection.isLocal()) {
                seed = Objects.requireNonNull(client.getServer()).getWorlds().iterator().next().getSeed();
                onSeedKnown(seed);
            } else {
                client.setScreen(new InputWorldSeedScreen(null, ClientPlayNetworkHandlerMixin::onSeedKnown));
            }
        } else try {
            PRC.getInstance().getFinder().getServer().restart(seed);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Unique
    private static void onSeedKnown(long seed) {
        try {
            PRC.getInstance().getFinder().getServer().restart(seed);
            PRC.getInstance().getDatabase().setCurrentSeed(seed);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
