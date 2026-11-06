package me.skaffy.fluid;

import java.util.function.IntFunction;
import me.skaffy.portal.PortalTransit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Balde que coloca 1 bloco finito de liquido (nao vira fonte infinita). */
public class FiniteBucketItem extends BucketItem {
	private final IntFunction<BlockState> stateFor;

	public FiniteBucketItem(FlowingFluid content, IntFunction<BlockState> stateFor, Properties properties) {
		super(content, properties);
		this.stateFor = stateFor;
	}

	@Override
	public boolean emptyContents(final @Nullable LivingEntity user, final Level level, final BlockPos pos, final @Nullable BlockHitResult hitResult) {
		BlockState blockState = level.getBlockState(pos);
		boolean mayReplace = blockState.canBeReplaced(this.content);
		boolean shiftKeyDown = user != null && user.isShiftKeyDown();
		boolean placeLiquid = mayReplace
			|| blockState.getBlock() instanceof LiquidBlockContainer container && container.canPlaceLiquid(user, level, pos, blockState, this.content);
		boolean canPlaceFluidInsideBlock = blockState.isAir() || placeLiquid && (!shiftKeyDown || hitResult == null);
		if (!canPlaceFluidInsideBlock) {
			return hitResult != null && this.emptyContents(user, level, hitResult.getBlockPos().relative(hitResult.getDirection()), null);
		}
		if (level.environmentAttributes().getValue(EnvironmentAttributes.WATER_EVAPORATES, pos) && this.content.is(FluidTags.WATER)) {
			RandomSource random = level.getRandom();
			level.playSound(user, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F);
			if (level instanceof ServerLevel serverLevel) {
				PortalTransit.steamBurst(serverLevel, new Vec3(pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5), 8.0F);
			}
			return true;
		}
		if (!level.isClientSide() && mayReplace && !blockState.liquid()) {
			level.destroyBlock(pos, true);
		}
		if (!level.setBlock(pos, this.stateFor.apply(8), 11) && !blockState.getFluidState().isSource()) {
			return false;
		}
		this.playEmptySound(user, level, pos);
		return true;
	}
}
