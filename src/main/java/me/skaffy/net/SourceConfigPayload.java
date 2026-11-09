package me.skaffy.net;

import me.skaffy.RealisticWater;
import me.skaffy.block.SourceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record SourceConfigPayload(int x, int y, int z, int flow, boolean flood, boolean all) implements CustomPacketPayload {
	public static final Type<SourceConfigPayload> TYPE = new Type<>(RealisticWater.id("source_config"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SourceConfigPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, SourceConfigPayload::x,
		ByteBufCodecs.VAR_INT, SourceConfigPayload::y,
		ByteBufCodecs.VAR_INT, SourceConfigPayload::z,
		ByteBufCodecs.VAR_INT, SourceConfigPayload::flow,
		ByteBufCodecs.BOOL, SourceConfigPayload::flood,
		ByteBufCodecs.BOOL, SourceConfigPayload::all,
		SourceConfigPayload::new);

	@Override
	public Type<SourceConfigPayload> type() {
		return TYPE;
	}

	public void apply(ServerPlayer player) {
		Level level = player.level();
		BlockPos pos = new BlockPos(this.x, this.y, this.z);
		if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 64.0) {
			return;
		}
		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof SourceBlock)) {
			return;
		}
		int amount = this.flood ? 10 : Mth.clamp(this.flow, 0, 10);
		set(level, pos, state, amount, this.flood);
		if (this.all) {
			for (Long key : new java.util.ArrayList<>(SourceBlock.known(level))) {
				BlockPos other = BlockPos.of(key);
				if (!level.isLoaded(other)) {
					continue;
				}
				BlockState otherState = level.getBlockState(other);
				if (otherState.getBlock() instanceof SourceBlock) {
					set(level, other, otherState, amount, this.flood);
				} else {
					SourceBlock.known(level).remove(key);
				}
			}
		}
	}

	private static void set(Level level, BlockPos pos, BlockState state, int flow, boolean flood) {
		BlockState next = state.setValue(SourceBlock.FLOW, flow).setValue(SourceBlock.FLOOD, flood);
		if (flood) {
			next = next.setValue(SourceBlock.ENABLED, true);
		}
		level.setBlock(pos, next, Block.UPDATE_CLIENTS);
	}
}
