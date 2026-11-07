package me.skaffy.net;

import java.util.ArrayList;
import me.skaffy.RealisticWater;
import me.skaffy.block.SourceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Liga ou desliga todos os fontes de um fluido.
 * kind: 0 = agua, 1 = lava, 2 = slime, 3 = todos. mode: 0 = fechar, 1 = abrir, 2 = alternar.
 */
public record SourceToggleAllPayload(byte kind, byte mode) implements CustomPacketPayload {
	public static final Type<SourceToggleAllPayload> TYPE = new Type<>(RealisticWater.id("source_toggle_all"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SourceToggleAllPayload> CODEC = StreamCodec.of(
		(buf, p) -> {
			buf.writeByte(p.kind);
			buf.writeByte(p.mode);
		},
		buf -> new SourceToggleAllPayload(buf.readByte(), buf.readByte()));

	@Override
	public Type<SourceToggleAllPayload> type() {
		return TYPE;
	}

	public void apply(ServerPlayer player) {
		Level level = player.level();
		ArrayList<BlockPos> matches = new ArrayList<>();
		boolean anyEnabled = false;
		for (Long key : new ArrayList<>(SourceBlock.known(level))) {
			BlockPos pos = BlockPos.of(key);
			if (!level.isLoaded(pos)) {
				continue;
			}
			BlockState state = level.getBlockState(pos);
			if (!(state.getBlock() instanceof SourceBlock source)) {
				SourceBlock.known(level).remove(key);
				continue;
			}
			if (this.kind == 3 || source.kind() == this.kind) {
				matches.add(pos);
				anyEnabled |= state.getValue(SourceBlock.ENABLED);
			}
		}
		boolean enable = this.mode == 1 || (this.mode == 2 && !anyEnabled);
		for (BlockPos pos : matches) {
			BlockState state = level.getBlockState(pos);
			if (state.getBlock() instanceof SourceBlock) {
				level.setBlock(pos, state.setValue(SourceBlock.ENABLED, enable), Block.UPDATE_CLIENTS);
			}
		}
		player.sendOverlayMessage(Component.translatable(enable ? "realistic-water.sources.opened" : "realistic-water.sources.closed", matches.size()));
	}
}
