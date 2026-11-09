package me.skaffy.block;

import me.skaffy.ModContent;
import me.skaffy.fluid.OilFire;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.redstone.Orientation;
import me.skaffy.ocean.OceanOrigin;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FlowingFluid;
import org.jspecify.annotations.Nullable;

public class RealisticWaterBlock extends LiquidBlock {
	public static final BooleanProperty INFINITE = BooleanProperty.create("infinite");
	public static final EnumProperty<OceanOrigin> OCEAN = EnumProperty.create("ocean", OceanOrigin.class);

	public RealisticWaterBlock(FlowingFluid fluid, Properties properties) {
		super(fluid, properties);
		this.registerDefaultState(this.defaultBlockState().setValue(INFINITE, false).setValue(OCEAN, OceanOrigin.NONE));
	}

	public static boolean isInfinite(BlockState state) {
		return state.getBlock() instanceof RealisticWaterBlock && state.getValue(INFINITE);
	}

	public static boolean isOcean(BlockState state) {
		return state.getBlock() == ModContent.REALISTIC_WATER_BLOCK && state.getValue(OCEAN) != OceanOrigin.NONE;
	}

	public static OceanOrigin origin(BlockState state) {
		return state.getBlock() == ModContent.REALISTIC_WATER_BLOCK ? state.getValue(OCEAN) : OceanOrigin.NONE;
	}

	@Override
	protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(INFINITE, OCEAN);
	}

	@Override
	public ItemStack pickupBlock(final @Nullable LivingEntity user, final LevelAccessor level, final BlockPos pos, final BlockState state) {
		if (isOcean(state)) {
			if (state.getValue(LEVEL) != 0) {
				return ItemStack.EMPTY;
			}
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
			return new ItemStack(Items.WATER_BUCKET);
		}
		if (this == ModContent.REALISTIC_WATER_BLOCK && state.getValue(LEVEL) == 0 && !state.getValue(INFINITE)) {
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
			return new ItemStack(ModContent.SMALL_REALISTIC_WATER_BUCKET);
		}
		return super.pickupBlock(user, level, pos, state);
	}

	@Override
	protected void neighborChanged(final BlockState state, final Level level, final BlockPos pos, final Block block, final @Nullable Orientation orientation, final boolean movedByPiston) {
		super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
		if (this == ModContent.REALISTIC_OIL_BLOCK && level instanceof ServerLevel serverLevel) {
			OilFire.checkIgnite(serverLevel, pos);
		}
	}

	/** So o petroleo usa tick de bloco: a queima agendada pelo OilFire. */
	@Override
	protected void tick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
		if (this == ModContent.REALISTIC_OIL_BLOCK) {
			OilFire.burn(level, pos);
			return;
		}
		super.tick(state, level, pos, random);
	}

	/** O petroleo confere de vez em quando se tem lava/fogo encostado (a lava da simulacao nao avisa os vizinhos). */
	@Override
	protected boolean isRandomlyTicking(final BlockState state) {
		return this == ModContent.REALISTIC_OIL_BLOCK || super.isRandomlyTicking(state);
	}

	@Override
	protected void randomTick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
		if (this == ModContent.REALISTIC_OIL_BLOCK) {
			OilFire.checkIgnite(level, pos);
			return;
		}
		super.randomTick(state, level, pos, random);
	}

	@Override
	protected boolean propagatesSkylightDown(final BlockState state) {
		return state.getValue(OCEAN) != OceanOrigin.WATER;
	}
}
