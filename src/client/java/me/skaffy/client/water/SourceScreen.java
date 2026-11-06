package me.skaffy.client.water;

import me.skaffy.block.SourceBlock;
import me.skaffy.net.SourceConfigPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/** Tela "Vazao do fonte": slider 0-1000, caixa de texto, Modo Alagamento, TODOS e Salvar. */
public class SourceScreen extends OptionsSubScreen {
	private static @Nullable BlockPos pendingPos;
	private static @Nullable BlockState pendingState;

	private final BlockPos pos;
	private int flow;
	private boolean flood;
	private boolean all;
	private @Nullable EditBox box;
	private @Nullable FlowSlider slider;

	public SourceScreen(@Nullable Screen lastScreen, BlockPos pos, BlockState state) {
		super(lastScreen, Minecraft.getInstance().options, Component.translatable("block.realistic-water.source.title"));
		this.pos = pos;
		this.flow = state.getValue(SourceBlock.FLOW);
		this.flood = state.getValue(SourceBlock.FLOOD);
	}

	public static void openNextTick(BlockPos pos, BlockState state) {
		pendingPos = pos;
		pendingState = state;
	}

	public static void tick(Minecraft mc) {
		if (pendingPos != null && pendingState != null) {
			BlockPos p = pendingPos;
			BlockState s = pendingState;
			pendingPos = null;
			pendingState = null;
			mc.gui.setScreen(new SourceScreen(mc.gui.screen(), p, s));
		}
	}

	@Override
	protected void addOptions() {
		this.slider = new FlowSlider();
		this.list.addBig(this.slider);
		EditBox editBox = new EditBox(this.font, 150, 20, Component.translatable("block.realistic-water.source.title"));
		editBox.setMaxLength(4);
		editBox.setValue(String.valueOf(this.flow * 100));
		editBox.setResponder(text -> {
			try {
				int value = Math.clamp(Integer.parseInt(text.trim()), 0, 1000);
				this.flow = (int) Math.round(value / 100.0);
				if (this.slider != null) {
					this.slider.setFlow(this.flow);
				}
			} catch (NumberFormatException ignored) {
			}
		});
		this.box = editBox;
		this.list.addBig(editBox);
		this.list.addBig(CycleButton.onOffBuilder(this.flood)
			.create(Component.translatable("block.realistic-water.source.flood"), (button, on) -> this.flood = on));
		this.list.addBig(CycleButton.onOffBuilder(this.all)
			.create(Component.translatable("block.realistic-water.source.all"), (button, on) -> this.all = on));
	}

	@Override
	protected void addFooter() {
		LinearLayout buttons = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
		buttons.addChild(Button.builder(Component.translatable("block.realistic-water.source.save"), button -> {
			if (ClientPlayNetworking.canSend(SourceConfigPayload.TYPE)) {
				ClientPlayNetworking.send(new SourceConfigPayload(this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.flow, this.flood, this.all));
			}
			this.onClose();
		}).build());
		buttons.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).build());
	}

	private final class FlowSlider extends AbstractSliderButton {
		FlowSlider() {
			super(0, 0, 150, 20, Component.empty(), SourceScreen.this.flow / 10.0);
			this.updateMessage();
		}

		void setFlow(int flow) {
			this.value = flow / 10.0;
			this.updateMessage();
		}

		@Override
		protected void updateMessage() {
			this.setMessage(Component.translatable("block.realistic-water.source.flow_label", Math.round(this.value * 10.0) * 100));
		}

		@Override
		protected void applyValue() {
			SourceScreen.this.flow = (int) Math.round(this.value * 10.0);
			if (SourceScreen.this.box != null) {
				SourceScreen.this.box.setValue(String.valueOf(SourceScreen.this.flow * 100));
			}
		}
	}
}
