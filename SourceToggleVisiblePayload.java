package me.skaffy.net;

import java.util.ArrayList;
import java.util.List;
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
 * Abre ou fecha so os fontes que o cliente viu na tela (tecla N).
 * mode: 0 = fechar, 1 = abrir. O servidor confere distancia e se o bloco realmente e um fonte.
 */
public record SourceToggleVisiblePayload(byte mode, List<BlockPos> positions) implements CustomPacketPayload {
	public static final Type<SourceToggleVisiblePayload> TYPE = new Type<>(RealisticWater.id("source_toggle_visible"));
	public static final int MAX_POSITIONS = 512;
	private static final double MAX_DISTANCE_SQ = 192.0 * 192.0;

	public static final StreamCodec<RegistryFriendlyByteBuf, SourceToggleVisiblePayload> CODEC = StreamCodec.of(
		(buf, p) -> {
			buf.writeByte(p.mode);
			int count = Math.min(p.positions.size(), MAX_POSITIONS);
			buf.writeVarInt(count);
			for (int i = 0; i < count; i++) {
				buf.writeLong(p.positions.get(i).asLong());
			}
		},
		buf -> {
			byte mode = buf.readByte();
			int count = Math.min(buf.readVarInt(), MAX_POSITIONS);
			List<BlockPos> list = new ArrayList<>(count);
			for (int i = 0; i < count; i++) {
				list.add(BlockPos.of(buf.readLong()));
			}
			return new SourceToggleVisiblePayload(mode, list);
		});

	@Override
	public Type<SourceToggleVisiblePayload> type() {
		return TYPE;
	}

	public void apply(ServerPlayer player) {
		Level level = player.level();
		boolean enable = this.mode == 1;
		int changed = 0;
		for (BlockPos pos : this.positions) {
			if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > MAX_DISTANCE_SQ || !level.isLoaded(pos)) {
				continue;
			}
			BlockState state = level.getBlockState(pos);
			if (state.getBlock() instanceof SourceBlock) {
				level.setBlock(pos, state.setValue(SourceBlock.ENABLED, enable), Block.UPDATE_CLIENTS);
				changed++;
			}
		}
		player.sendOverlayMessage(Component.translatable(enable ? "realistic-water.sources.opened" : "realistic-water.sources.closed", changed));
	}
}
