package me.skaffy.block;

import java.util.function.BiConsumer;
import me.skaffy.ModContent;
import me.skaffy.net.LavaSyncPayload;
import me.skaffy.net.SlimeSyncPayload;
import me.skaffy.net.WaterSyncPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Bloco fonte de liquido. kind: 0 = agua, 1 = lava, 2 = slime.
 * Clique direito em pe: liga/desliga a saida da face clicada.
 * Clique direito agachado: abre a tela de vazao.
 */
public class SourceBlock extends Block {
	public static final BooleanProperty UP = BlockStateProperties.UP;
	public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
	public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
	public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
	public static final BooleanProperty EAST = BlockStateProperties.EAST;
	public static final BooleanProperty WEST = BlockStateProperties.WEST;
	public static final IntegerProperty FLOW = IntegerProperty.create("flow", 0, 10);
	public static final BooleanProperty FLOOD = BooleanProperty.create("flood");

	private static final int PLACE_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SKIP_ON_PLACE;

	/** Preenchido pelo cliente para abrir a tela de configuracao. */
	public static volatile BiConsumer<BlockPos, BlockState> clientOpener;

	private static final java.util.Map<net.minecraft.resources.ResourceKey<Level>, java.util.Set<Long>> KNOWN = new java.util.concurrent.ConcurrentHashMap<>();

	/** Posicoes de todos os fontes ja vistos neste mundo (usado pela opcao TODOS). */
	public static java.util.Set<Long> known(Level level) {
		return KNOWN.computeIfAbsent(level.dimension(), key -> java.util.concurrent.ConcurrentHashMap.newKeySet());
	}

	private final int kind;

	public SourceBlock(int kind, Properties properties) {
		super(properties);
		this.kind = kind;
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(UP, false).setValue(DOWN, false).setValue(NORTH, false)
			.setValue(SOUTH, false).setValue(EAST, false).setValue(WEST, false)
			.setValue(FLOW, 5).setValue(FLOOD, false));
	}

	public static BooleanProperty faceProperty(Direction direction) {
		return switch (direction) {
			case UP -> UP;
			case DOWN -> DOWN;
			case NORTH -> NORTH;
			case SOUTH -> SOUTH;
			case EAST -> EAST;
			default -> WEST;
		};
	}

	private BlockState fullState() {
		return switch (this.kind) {
			case 1 -> LavaSyncPayload.stateFor(8);
			case 2 -> SlimeSyncPayload.stateFor(8);
			default -> WaterSyncPayload.stateFor(8);
		};
	}

	private Block liquidBlock() {
		return switch (this.kind) {
			case 1 -> ModContent.REALISTIC_LAVA_BLOCK;
			case 2 -> ModContent.REALISTIC_SLIME_BLOCK;
			default -> ModContent.REALISTIC_WATER_BLOCK;
		};
	}

	@Override
	protected void onPlace(final BlockState state, final Level level, final BlockPos pos, final BlockState oldState, final boolean movedByPiston) {
		if (!level.isClientSide()) {
			known(level).add(pos.asLong());
			if (!oldState.is(this)) {
				level.scheduleTick(pos, this, 5);
			}
		}
	}

	@Override
	protected void tick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
		known(level).add(pos.asLong());
		boolean flood = state.getValue(FLOOD);
		int flow = flood ? 10 : state.getValue(FLOW);
		if (!playerNear(level, pos)) {
			level.scheduleTick(pos, this, 40);
			return;
		}
		if (flow > 0) {
			for (Direction direction : Direction.values()) {
				if (state.getValue(faceProperty(direction))) {
					this.emit(level, pos, direction, flood);
				}
			}
		}
		int delay = flood ? 2 : flow > 0 ? Math.max(1, 11 - flow) : 20;
		level.scheduleTick(pos, this, delay);
	}

	private static boolean playerNear(ServerLevel level, BlockPos pos) {
		double limit = 96.0 * 96.0;
		for (ServerPlayer player : level.players()) {
			if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < limit) {
				return true;
			}
		}
		return false;
	}

	/** 0 = agua, 1 = lava, 2 = slime. */
	public int kind() {
		return this.kind;
	}

	private static final int FLOOD_DEPTH = 1;
	private static final int FLOOD_HALF_WIDTH = 1;
	private static final int FLOOD_CAP = 9;

	private void emit(ServerLevel level, BlockPos pos, Direction direction, boolean flood) {
		if (!flood) {
			this.fill(level, pos.relative(direction));
			return;
		}
		BlockPos.MutableBlockPos target = new BlockPos.MutableBlockPos();
		int placed = 0;
		for (int step = 1; step <= FLOOD_DEPTH && placed < FLOOD_CAP; step++) {
			for (int a = -FLOOD_HALF_WIDTH; a <= FLOOD_HALF_WIDTH && placed < FLOOD_CAP; a++) {
				for (int b = -FLOOD_HALF_WIDTH; b <= FLOOD_HALF_WIDTH && placed < FLOOD_CAP; b++) {
					int dx = direction.getStepX() * step;
					int dy = direction.getStepY() * step;
					int dz = direction.getStepZ() * step;
					switch (direction.getAxis()) {
						case X -> {
							dy += a;
							dz += b;
						}
						case Y -> {
							dx += a;
							dz += b;
						}
						default -> {
							dx += a;
							dy += b;
						}
					}
					target.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
					if (this.fill(level, target)) {
						placed++;
					}
				}
			}
		}
	}

	private boolean fill(ServerLevel level, BlockPos target) {
		BlockState current = level.getBlockState(target);
		boolean room = current.isAir();
		if (!room && current.is(this.liquidBlock())) {
			int amountLevel = current.getValue(LiquidBlock.LEVEL);
			room = amountLevel >= 1 && amountLevel <= 7;
		}
		if (room) {
			level.setBlock(target.immutable(), this.fullState(), PLACE_FLAGS);
		}
		return room;
	}

	@Override
	protected InteractionResult useWithoutItem(final BlockState state, final Level level, final BlockPos pos, final Player player, final BlockHitResult hitResult) {
		if (player.isShiftKeyDown()) {
			if (level.isClientSide() && clientOpener != null) {
				clientOpener.accept(pos.immutable(), state);
			}
			return InteractionResult.SUCCESS;
		}
		if (!level.isClientSide()) {
			Direction face = hitResult.getDirection();
			BooleanProperty property = faceProperty(face);
			BlockState next = state.cycle(property);
			level.setBlock(pos, next, Block.UPDATE_CLIENTS);
			boolean open = next.getValue(property);
			level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.5F, open ? 0.8F : 0.5F);
			player.sendOverlayMessage(Component.translatable(
				open ? "block.realistic-water.source.open" : "block.realistic-water.source.closed",
				Component.translatable("block.realistic-water.source.face." + face.getName())));
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(UP, DOWN, NORTH, SOUTH, EAST, WEST, FLOW, FLOOD);
	}
}
