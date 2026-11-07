package me.skaffy.fluid;

import me.skaffy.ModSettings;
import me.skaffy.block.HotObsidianBlock;
import me.skaffy.block.RealisticWaterBlock;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public final class FluidMixing {
	private FluidMixing() {
	}

	public static boolean solidifyIfTouchingWater(ServerLevel level, BlockPos pos) {
		if (!ModSettings.lavaWaterMixing) {
			return false;
		}
		BlockState state = level.getBlockState(pos);
		if (!isLavaLiquid(state)) {
			return false;
		}
		for (Direction direction : Direction.values()) {
			if (level.getFluidState(pos.relative(direction)).is(FluidTags.WATER)) {
				solidify(level, pos, state);
				return true;
			}
		}
		return false;
	}

	public static void solidifyLavaAround(ServerLevel level, BlockPos pos) {
		if (!ModSettings.lavaWaterMixing || !level.getFluidState(pos).is(FluidTags.WATER)) {
			return;
		}
		for (Direction direction : Direction.values()) {
			BlockPos neighbour = pos.relative(direction);
			BlockState state = level.getBlockState(neighbour);
			if (isLavaLiquid(state)) {
				solidify(level, neighbour, state);
			}
		}
	}

	public static boolean solidifyContact(ServerLevel level, BlockPos pos) {
		if (!ModSettings.lavaWaterMixing) {
			return false;
		}
		BlockState state = level.getBlockState(pos);
		if (!canSolidifyContact(state)) {
			return false;
		}
		solidify(level, pos, state);
		return true;
	}

	public static boolean canSolidifyContact(BlockState state) {
		if (!state.isAir() && !(state.getBlock() instanceof LiquidBlock)) {
			return false;
		}
		return !(RealisticWaterBlock.isInfinite(state) && state.getFluidState().is(FluidTags.WATER));
	}

	private static boolean isLavaLiquid(BlockState state) {
		return state.getBlock() instanceof LiquidBlock && state.getFluidState().is(FluidTags.LAVA);
	}

	private static void solidify(ServerLevel level, BlockPos pos, BlockState state) {
		level.setBlock(pos, resultStateFor(level, pos, state), Block.UPDATE_ALL);
		level.levelEvent(LevelEvent.LAVA_FIZZ, pos, 0);
	}

	public static Block resultFor(BlockState state) {
		FluidState fluid = state.getFluidState();
		if (!fluid.is(FluidTags.LAVA)) {
			return Blocks.COBBLESTONE;
		}
		boolean source = RealisticLavaFluid.isRealistic(fluid) ? RealisticWaterBlock.isInfinite(state) : fluid.isSource();
		return source ? Blocks.OBSIDIAN : Blocks.COBBLESTONE;
	}

	/**
	 * O que sobra quando lava e agua se encontram neste lugar:
	 * - agua num espaco com teto: nao cresce nada, so some o fluido;
	 * - lava realista: obsidiana quente em camadas;
	 * - agua ou ar: uma camada fina de obsidiana quente.
	 */
	public static BlockState resultStateFor(BlockGetter level, BlockPos pos, BlockState state) {
		FluidState fluid = state.getFluidState();
		if (fluid.is(FluidTags.WATER) && HotObsidianBlock.hasCeiling(level, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		if (fluid.is(FluidTags.LAVA)) {
			if (!RealisticLavaFluid.isRealistic(fluid)) {
				return resultFor(state).defaultBlockState();
			}
			boolean source = RealisticWaterBlock.isInfinite(state);
			return HotObsidianBlock.hot(source ? 4 : 2);
		}
		return HotObsidianBlock.hot(1);
	}
}
