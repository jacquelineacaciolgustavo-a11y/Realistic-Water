package me.skaffy.client.water;

import java.util.List;
import me.skaffy.RealisticWater;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/** Tela do comando /agua configurar: qualidade da agua e alguns ajustes simples. */
public class AguaConfigScreen extends OptionsSubScreen {
	private static boolean openPending;

	public AguaConfigScreen(@Nullable Screen lastScreen) {
		super(lastScreen, Minecraft.getInstance().options, Component.translatable("realistic-water.agua.title"));
	}

	public static void openNextTick() {
		openPending = true;
	}

	public static void tick(Minecraft mc) {
		if (openPending) {
			openPending = false;
			mc.gui.setScreen(new AguaConfigScreen(mc.gui.screen()));
		}
	}

	public static String currentQuality() {
		return switch (WaterSettings.resolution) {
			case 3 -> "low";
			case 4 -> "medium";
			case 6 -> "high";
			default -> "custom";
		};
	}

	/** Aplica um preset de qualidade: low, medium ou high. */
	public static void applyQuality(String quality) {
		switch (quality) {
			case "low" -> setAll(3, 3, 3, 1, 2, 2, false);
			case "medium" -> setAll(4, 4, 4, 1, 2, 3, true);
			case "high" -> setAll(6, 6, 5, 2, 4, 5, true);
			default -> {
				return;
			}
		}
		WaterSettings.save();
	}

	private static void setAll(int water, int lava, int slime, int subdivision, int steps, int cg, boolean foam) {
		put("resolution", String.valueOf(water));
		put("lavaResolution", String.valueOf(lava));
		put("slimeResolution", String.valueOf(slime));
		put("surfaceSubdivision", String.valueOf(subdivision));
		put("maxStepsPerFrame", String.valueOf(steps));
		put("cgIterations", String.valueOf(cg));
		put("whitewater", String.valueOf(foam));
	}

	private static void put(String name, String value) {
		try {
			WaterSettings.set(name, value);
		} catch (ReflectiveOperationException | NumberFormatException e) {
			RealisticWater.LOGGER.warn("Couldn't set {}", name);
		}
	}

	@Override
	protected void addOptions() {
		CycleButton<String> quality = CycleButton.builder(key -> Component.translatable("realistic-water.agua.quality." + key), currentQuality())
			.withValues(List.of("low", "medium", "high", "custom"))
			.create(Component.translatable("realistic-water.agua.quality"), (button, value) -> applyQuality(value));
		this.list.addBig(quality);
		this.list.addBig(CycleButton.onOffBuilder(WaterSettings.whitewater)
			.create(Component.translatable("realistic-water.agua.foam"), (button, on) -> put("whitewater", on.toString())));
		this.list.addBig(CycleButton.onOffBuilder(WaterSettings.lavaWaterMixing)
			.create(Component.translatable("realistic-water.agua.mixing"), (button, on) -> put("lavaWaterMixing", on.toString())));
		this.list.addBig(CycleButton.onOffBuilder(WaterSettings.slimeSounds)
			.create(Component.translatable("realistic-water.agua.slime_sounds"), (button, on) -> put("slimeSounds", on.toString())));
		this.list.addBig(Button.builder(Component.translatable("realistic-water.agua.clear"), button -> {
			AguaKeys.clearAll(Minecraft.getInstance());
			this.onClose();
		}).build());
	}

	@Override
	public void removed() {
		super.removed();
		WaterSettings.save();
	}

	@Override
	protected void addFooter() {
		LinearLayout buttons = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
		buttons.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).build());
	}
}
