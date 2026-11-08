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

/** Tela do comando /agua configurar: Qualidade (Baixa, Media, Alta), Espuma e Respingos. */
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

	/** Se a resolucao nao bate com nenhum preset, mostra "media" ate o jogador escolher. */
	public static String currentQuality() {
		return switch (WaterSettings.resolution) {
			case 3 -> "low";
			case 6 -> "high";
			default -> "medium";
		};
	}

	/** Aplica um preset de qualidade: low, medium ou high. Espuma e respingos sao independentes. */
	public static void applyQuality(String quality) {
		switch (quality) {
			case "low" -> setQuality(3, 3, 3, 1, 2, 2);
			case "medium" -> setQuality(4, 4, 4, 1, 2, 3);
			case "high" -> setQuality(6, 6, 5, 2, 4, 5);
			default -> {
				return;
			}
		}
		WaterSettings.save();
	}

	private static void setQuality(int water, int lava, int slime, int subdivision, int steps, int cg) {
		put("resolution", String.valueOf(water));
		put("lavaResolution", String.valueOf(lava));
		put("slimeResolution", String.valueOf(slime));
		put("surfaceSubdivision", String.valueOf(subdivision));
		put("maxStepsPerFrame", String.valueOf(steps));
		put("cgIterations", String.valueOf(cg));
	}

	public static boolean foamOn() {
		return WaterSettings.whitewater && WaterSettings.foamCoverage > 0.0F;
	}

	public static boolean splashOn() {
		return WaterSettings.whitewater && WaterSettings.sprayAlpha > 0.0F;
	}

	/** Espuma: espuma na superficie e bolhas debaixo d'agua. */
	public static void setFoam(boolean on) {
		if (on) {
			enableWhitewaterBase();
			put("foamCoverage", "1.0");
			put("foamSize", "3.0");
			put("foamBubbleSize", "0.05");
			put("bubbleAlpha", "0.5");
			put("wwBubbleThreshold", "14.0");
			put("wwBubbleBuoyancy", "3.0");
			put("wwBubbleDrag", "1.0");
		} else {
			put("foamCoverage", "0.0");
			put("bubbleAlpha", "0.0");
		}
		syncWhitewaterSwitch();
	}

	/** Respingos: gotas voando no ar quando a agua bate ou cai. */
	public static void setSplash(boolean on) {
		if (on) {
			enableWhitewaterBase();
			put("sprayAlpha", "0.7");
			put("wwSprayThreshold", "6.0");
			put("wwSprayDrag", "0.3");
		} else {
			put("sprayAlpha", "0.0");
		}
		syncWhitewaterSwitch();
	}

	/** Parametros de geracao compartilhados por espuma e respingos. */
	private static void enableWhitewaterBase() {
		put("wwTrappedAirRate", "20.0");
		put("wwWaveCrestRate", "20.0");
		put("wwFoamLifeMin", "1.0");
		put("wwFoamLifeMax", "3.0");
		put("wwMaxPerParticle", "2");
		put("wwCellLimit", "20");
		put("wwRadius", "0.02");
		put("wwMaxPixels", "6.0");
	}

	/** O sistema de whitewater so roda se espuma ou respingos estiverem ligados. */
	private static void syncWhitewaterSwitch() {
		boolean any = WaterSettings.foamCoverage > 0.0F || WaterSettings.sprayAlpha > 0.0F;
		put("whitewater", String.valueOf(any));
		WaterSettings.save();
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
			.withValues(List.of("low", "medium", "high"))
			.create(Component.translatable("realistic-water.agua.quality"), (button, value) -> applyQuality(value));
		this.list.addBig(quality);
		this.list.addBig(CycleButton.onOffBuilder(foamOn())
			.create(Component.translatable("realistic-water.agua.foam"), (button, on) -> setFoam(on)));
		this.list.addBig(CycleButton.onOffBuilder(splashOn())
			.create(Component.translatable("realistic-water.agua.splash"), (button, on) -> setSplash(on)));
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
