package net.zorby.prc;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import java.io.IOException;
import java.util.Arrays;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class PRCCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("prc").executes(context -> {
            Runtime runtime = Runtime.getRuntime();

            try {
                Process process = runtime.exec(new String[]{ "cmd.exe", "/c", "cd" });

                process.waitFor();

                context.getSource().sendFeedback(Text.literal(new String(process.getInputStream().readAllBytes())));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return Command.SINGLE_SUCCESS;
        }));
    }

}
