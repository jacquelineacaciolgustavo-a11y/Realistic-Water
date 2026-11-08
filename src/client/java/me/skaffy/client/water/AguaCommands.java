package me.skaffy.client.water;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;

/** Unico comando do mod: /agua configurar */
public final class AguaCommands {
	private AguaCommands() {
	}

	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext context) {
		dispatcher.register(literal("agua")
			.then(literal("configurar").executes(c -> {
				AguaConfigScreen.openNextTick();
				return 1;
			})));
	}
}
