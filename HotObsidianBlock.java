package me.skaffy.block;

import me.skaffy.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Obsidiana que nasce quando lava encontra agua: comeca quente (brilhando), cresce em camadas
 * enquanto ainda ha lava e agua em contato, e esfria ate virar obsidiana de verdade.
 */
public class HotObsidianBlock extends Block {
	public static final int MAX_LAYERS = 8;
	public static final int MAX_HEAT = 5;
	public static final IntegerProperty LAYERS = IntegerProperty.create("layers", 1, MAX_LAYERS);
	public static final IntegerProperty HEAT = IntegerProperty.create("heat", 0, MAX_HEAT);

	private static final VoxelShape[] SHAPES = Block.boxes(MAX_LAYERS, height -> Block.column(16.0, 0.0, height * 2));
	private static final int TICK_DELAY = 30;

	public HotObsidianBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, 1).setValue(HEAT, MAX_HEAT));
	}

	public static BlockState hot(int layers) {
		return ModContent.HOT_OBSIDIAN.defaultBlockState()
			.setValue(LAYERS, Math.max(1, Math.min(MAX_LAYERS, layers)))
			.setValue(HEAT, MAX_HEAT);
	}

	/** Tem teto solido logo acima desta posicao? */
	public static boolean hasCeiling(BlockGetter level, BlockPos pos) {
		BlockPos above = pos.above();
		return level.getBlockState(above).isFaceSturdy(level, above, Direction.DOWN);
	}

	@Override
	protected VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos, final CollisionContext context) {
		return SHAPES[state.getValue(LAYERS)];
	}

	@Override
	protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LAYERS, HEAT);
	}

	@Override
	protected void onPlace(final BlockState state, final Level level, final BlockPos pos, final BlockState oldState, final boolean movedByPiston) {
		if (!level.isClientSide() && state.getValue(HEAT) > 0) {
			level.scheduleTick(pos, this, TICK_DELAY);
		}
	}

	@Override
	protected void tick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
		int layers = state.getValue(LAYERS);
		int heat = state.getValue(HEAT);
		if (heat > 0) {
			if (hasReaction(level, pos)) {
				if (layers < MAX_LAYERS) {
					layers++;
				} else {
					this.growUp(level, pos);
				}
			}
			heat--;
		}
		if (layers >= MAX_LAYERS && heat == 0) {
			level.setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), Block.UPDATE_ALL);
			return;
		}
		if (layers != state.getValue(LAYERS) || heat != state.getValue(HEAT)) {
			level.setBlock(pos, state.setValue(LAYERS, layers).setValue(HEAT, heat), Block.UPDATE_ALL);
		}
		if (heat > 0) {
			level.scheduleTick(pos, this, TICK_DELAY);
		}
	}

	/** So cresce enquanto lava e agua ainda se tocam aqui ao redor. */
	private static boolean hasReaction(ServerLevel level, BlockPos pos) {
		boolean water = false;
		boolean lava = false;
		for (Direction direction : Direction.values()) {
			FluidState fluid = level.getFluidState(pos.relative(direction));
			if (fluid.is(FluidTags.WATER)) {
				water = true;
			} else if (fluid.is(FluidTags.LAVA)) {
				lava = true;
			}
		}
		return water && lava;
	}

	private void growUp(ServerLevel level, BlockPos pos) {
		BlockPos above = pos.above();
		BlockState current = level.getBlockState(above);
		if (current.getBlock() instanceof HotObsidianBlock) {
			return;
		}
		FluidState fluid = current.getFluidState();
		boolean water = fluid.is(FluidTags.WATER);
		if (!current.isAir() && !water && !fluid.is(FluidTags.LAVA)) {
			return;
		}
		if (water && hasCeiling(level, above)) {
			// sem espaco para crescer: so tira o fluido
			level.setBlock(above, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
			return;
		}
		level.setBlock(above, hot(1), Block.UPDATE_ALL);
	}
}
