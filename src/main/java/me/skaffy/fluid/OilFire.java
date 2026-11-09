package me.skaffy.fluid;

import me.skaffy.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

/**
 * Petroleo inflamavel: qualquer faisca (fogo, lava, fogueira acesa ou quem entra pegando fogo) acende a celula,
 * e cada celula que queima acende as vizinhas de petroleo em 2 ticks, entao o fogo corre pela superficie toda.
 */
public final class OilFire {
	/** Ticks entre uma celula acender e as vizinhas acenderem (menor = espalha mais rapido). */
	public static final int SPREAD_DELAY = 2;

	private OilFire() {
	}

	public static boolean isOil(BlockState state) {
		return state.getBlock() == ModContent.REALISTIC_OIL_BLOCK;
	}

	/** Fogo, lava (vanilla ou realista) e fogueira acesa contam como faisca. */
	public static boolean isHeat(BlockState state) {
		if (state.is(BlockTags.FIRE) || state.getFluidState().is(FluidTags.LAVA)) {
			return true;
		}
		return state.is(BlockTags.CAMPFIRES) && state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT);
	}

	/** Se alguma vizinha for faisca, acende esta celula de petroleo. Retorna se acendeu. */
	public static boolean checkIgnite(ServerLevel level, BlockPos pos) {
		for (Direction direction : Direction.values()) {
			if (isHeat(level.getBlockState(pos.relative(direction)))) {
				ignite(level, pos);
				return true;
			}
		}
		return false;
	}

	/** Agenda a queima desta celula de petroleo (rapido, 1 tick). */
	public static void ignite(ServerLevel level, BlockPos pos) {
		if (isOil(level.getBlockState(pos))) {
			level.scheduleTick(pos, ModContent.REALISTIC_OIL_BLOCK, 1);
		}
	}

	/** Queima a celula: vira fogo (se ele se sustenta) ou some, solta fumaca e acende o petroleo ao redor. */
	public static void burn(ServerLevel level, BlockPos pos) {
		if (!isOil(level.getBlockState(pos))) {
			return;
		}
		BlockState fire = BaseFireBlock.getState(level, pos);
		level.setBlock(pos, fire.canSurvive(level, pos) ? fire : Blocks.AIR.defaultBlockState(), 3);
		double x = pos.getX() + 0.5;
		double y = pos.getY() + 0.5;
		double z = pos.getZ() + 0.5;
		level.sendParticles(ParticleTypes.FLAME, x, y + 0.2, z, 6, 0.3, 0.2, 0.3, 0.02);
		level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 0.6, z, 3, 0.25, 0.3, 0.25, 0.02);
		if (level.getRandom().nextInt(4) == 0) {
			level.playSound(null, x, y, z, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.6F, 0.8F + level.getRandom().nextFloat() * 0.4F);
		}
		for (Entity entity : level.getEntities((Entity) null, new AABB(pos).inflate(0.1), e -> !e.fireImmune())) {
			entity.setRemainingFireTicks(Math.max(entity.getRemainingFireTicks(), 200));
		}
		for (Direction direction : Direction.values()) {
			BlockPos next = pos.relative(direction);
			if (isOil(level.getBlockState(next))) {
				level.scheduleTick(next, ModContent.REALISTIC_OIL_BLOCK, SPREAD_DELAY);
			}
		}
	}
}
