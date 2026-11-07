package me.skaffy.client.water;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;

/**
 * /agua configurar
 * /agua qualidade baixa|media|alta
 * /agua limpar
 * /agua fontes agua|lava|slime|todos abrir|fechar
 */
public final class AguaCommands {
	private AguaCommands() {
	}

	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext context) {
		dispatcher.register(literal("agua")
			.then(literal("configurar").executes(c -> {
				AguaConfigScreen.openNextTick();
				return 1;
			}))
			.then(literal("qualidade")
				.then(literal("baixa").executes(c -> quality(c, "low")))
				.then(literal("media").executes(c -> quality(c, "medium")))
				.then(literal("alta").executes(c -> quality(c, "high"))))
			.then(literal("limpar").executes(c -> {
				AguaKeys.clearAll(Minecraft.getInstance());
				return 1;
			}))
			.then(literal("fontes")
				.then(fluid("agua", 0))
				.then(fluid("lava", 1))
				.then(fluid("slime", 2))
				.then(fluid("todos", 3))));
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> fluid(String name, int kind) {
		return literal(name)
			.then(literal("abrir").executes(c -> {
				AguaKeys.sources(Minecraft.getInstance(), kind, 1);
				return 1;
			}))
			.then(literal("fechar").executes(c -> {
				AguaKeys.sources(Minecraft.getInstance(), kind, 0);
				return 1;
			}));
	}

	private static int quality(CommandContext<FabricClientCommandSource> c, String quality) {
		AguaConfigScreen.applyQuality(quality);
		c.getSource().sendFeedback(Component.translatable("realistic-water.agua.quality_set", Component.translatable("realistic-water.agua.quality." + quality)));
		return 1;
	}
}
