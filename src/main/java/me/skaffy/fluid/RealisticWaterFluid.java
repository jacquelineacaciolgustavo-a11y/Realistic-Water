package me.skaffy.fluid;

import java.util.Optional;
import me.skaffy.ModContent;
import me.skaffy.block.RealisticWaterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.InsideBlockEffectType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

public abstract class RealisticWaterFluid extends FlowingFluid {
	/** 0 = agua, 1 = lama, 2 = leite, 3 = coca-cola, 4 = bloco de lama (lama espessa). */
	protected final int kind;

	protected RealisticWaterFluid(int kind) {
		this.kind = kind;
	}

	public int kind() {
		return this.kind;
	}

	@Override
	public Fluid getFlowing() {
		return switch (this.kind) {
			case 1 -> ModContent.FLOWING_REALISTIC_MUD;
			case 2 -> ModContent.FLOWING_REALISTIC_MILK;
			case 3 -> ModContent.FLOWING_REALISTIC_COLA;
			case 4 -> ModContent.FLOWING_REALISTIC_THICK_MUD;
			default -> ModContent.FLOWING_REALISTIC_WATER;
		};
	}

	@Override
	public Fluid getSource() {
		return switch (this.kind) {
			case 1 -> ModContent.REALISTIC_MUD;
			case 2 -> ModContent.REALISTIC_MILK;
			case 3 -> ModContent.REALISTIC_COLA;
			case 4 -> ModContent.REALISTIC_THICK_MUD;
			default -> ModContent.REALISTIC_WATER;
		};
	}

	@Override
	public Item getBucket() {
		return switch (this.kind) {
			case 1 -> ModContent.REALISTIC_MUD_BUCKET;
			case 2 -> ModContent.REALISTIC_MILK_BUCKET;
			case 3 -> ModContent.REALISTIC_COLA_BUCKET;
			case 4 -> ModContent.REALISTIC_MUD_BUCKET;
			default -> ModContent.REALISTIC_WATER_BUCKET;
		};
	}

	public static Block blockFor(int kind) {
		return switch (kind) {
			case 1 -> ModContent.REALISTIC_MUD_BLOCK;
			case 2 -> ModContent.REALISTIC_MILK_BLOCK;
			case 3 -> ModContent.REALISTIC_COLA_BLOCK;
			case 4 -> ModContent.REALISTIC_THICK_MUD_BLOCK;
			default -> ModContent.REALISTIC_WATER_BLOCK;
		};
	}

	/** Qual liquido (0 agua, 1 lama, 2 leite, 3 coca, 4 bloco de lama) este bloco guarda. */
	public static int kindOf(BlockState state) {
		Block block = state.getBlock();
		if (block == ModContent.REALISTIC_MUD_BLOCK) {
			return 1;
		}
		if (block == ModContent.REALISTIC_MILK_BLOCK) {
			return 2;
		}
		if (block == ModContent.REALISTIC_THICK_MUD_BLOCK) {
			return 4;
		}
		return block == ModContent.REALISTIC_COLA_BLOCK ? 3 : 0;
	}

	public static boolean isRealistic(FluidState state) {
		return !state.isEmpty() && state.getType() instanceof RealisticWaterFluid;
	}

	@Override
	public void tick(final ServerLevel level, final BlockPos pos, BlockState blockState, FluidState fluidState) {
	}

	@Override
	public @Nullable ParticleOptions getDripParticle() {
		return ParticleTypes.DRIPPING_WATER;
	}

	@Override
	protected boolean canConvertToSource(final ServerLevel level) {
		return false;
	}

	@Override
	protected void beforeDestroyingBlock(final LevelAccessor level, final BlockPos pos, final BlockState state) {
		BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
		Block.dropResources(state, level, pos, blockEntity);
	}

	@Override
	protected void entityInside(final Level level, final BlockPos pos, final Entity entity, final InsideBlockEffectApplier effectApplier) {
		effectApplier.apply(InsideBlockEffectType.EXTINGUISH);
	}

	@Override
	public int getSlopeFindDistance(final LevelReader level) {
		return 4;
	}

	@Override
	public BlockState createLegacyBlock(final FluidState fluidState) {
		return blockFor(this.kind).defaultBlockState()
			.setValue(LiquidBlock.LEVEL, getLegacyLevel(fluidState))
			.setValue(RealisticWaterBlock.INFINITE, fluidState.isSource());
	}

	@Override
	public boolean isSame(final Fluid other) {
		return other == this.getSource() || other == this.getFlowing();
	}

	@Override
	public int getDropOff(final LevelReader level) {
		return 1;
	}

	@Override
	public int getTickDelay(final LevelReader level) {
		return 5;
	}

	@Override
	public boolean canBeReplacedWith(final FluidState state, final BlockGetter level, final BlockPos pos, final Fluid other, final Direction direction) {
		return direction == Direction.DOWN && !other.is(FluidTags.WATER);
	}

	@Override
	protected float getExplosionResistance() {
		return 100.0F;
	}

	@Override
	public Optional<SoundEvent> getPickupSound() {
		return Optional.of(SoundEvents.BUCKET_FILL);
	}

	public static class Flowing extends RealisticWaterFluid {
		public Flowing() {
			super(0);
		}

		public Flowing(int kind) {
			super(kind);
		}

		@Override
		protected void createFluidStateDefinition(final StateDefinition.Builder<Fluid, FluidState> builder) {
			super.createFluidStateDefinition(builder);
			builder.add(LEVEL);
		}

		@Override
		public int getAmount(final FluidState fluidState) {
			return fluidState.getValue(LEVEL);
		}

		@Override
		public boolean isSource(final FluidState fluidState) {
			return false;
		}
	}

	public static class Source extends RealisticWaterFluid {
		public Source() {
			super(0);
		}

		public Source(int kind) {
			super(kind);
		}

		@Override
		public int getAmount(final FluidState fluidState) {
			return 8;
		}

		@Override
		public boolean isSource(final FluidState fluidState) {
			return true;
		}
	}
}
