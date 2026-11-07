package me.skaffy.client.water;

import com.mojang.blaze3d.platform.InputConstants;
import me.skaffy.RealisticWater;
import me.skaffy.net.SourceToggleAllPayload;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Teclas de atalho: alternar fontes de cada fluido e limpar tudo. */
public final class AguaKeys {
	private static KeyMapping water;
	private static KeyMapping lava;
	private static KeyMapping slime;
	private static KeyMapping clear;

	private AguaKeys() {
	}

	public static void register() {
		KeyMapping.Category category = KeyMapping.Category.register(RealisticWater.id("realistic_fluids"));
		water = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.realistic-water.sources_water", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F6, category));
		lava = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.realistic-water.sources_lava", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F7, category));
		slime = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.realistic-water.sources_slime", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F8, category));
		clear = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.realistic-water.clear_all", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F9, category));
		ClientTickEvents.END_CLIENT_TICK.register(AguaKeys::tick);
	}

	private static void tick(Minecraft mc) {
		while (water.consumeClick()) {
			sources(mc, 0, 2);
		}
		while (lava.consumeClick()) {
			sources(mc, 1, 2);
		}
		while (slime.consumeClick()) {
			sources(mc, 2, 2);
		}
		while (clear.consumeClick()) {
			clearAll(mc);
		}
	}

	/** kind: 0 agua, 1 lava, 2 slime, 3 todos. mode: 0 fechar, 1 abrir, 2 alternar. */
	public static void sources(Minecraft mc, int kind, int mode) {
		if (mc.player != null && ClientPlayNetworking.canSend(SourceToggleAllPayload.TYPE)) {
			ClientPlayNetworking.send(new SourceToggleAllPayload((byte) kind, (byte) mode));
		}
	}

	/** Remove todos os fluidos realistas perto de voce e fecha todos os fontes. */
	public static void clearAll(Minecraft mc) {
		if (mc.player == null) {
			return;
		}
		int radius = mc.options.renderDistance().get() + 2;
		WaterWorld world = WaterWorld.get();
		world.removeAll(radius, Material.WATER);
		world.removeAll(radius, Material.LAVA);
		world.removeAll(radius, Material.SLIME);
		sources(mc, 3, 0);
		mc.player.sendSystemMessage(Component.translatable("realistic-water.agua.cleared"));
	}
}
