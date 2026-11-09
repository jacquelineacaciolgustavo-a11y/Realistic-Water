package me.skaffy.block;

import java.util.function.BiConsumer;
import me.skaffy.ModContent;
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

public class SourceBlock extends Block {
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final IntegerProperty FLOW = IntegerProperty.create("flow", 0, 10);
    public static final BooleanProperty FLOOD = BooleanProperty.create("flood");
    public static final BooleanProperty ENABLED = BooleanProperty.create("enabled");

    private static final int PLACE_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SKIP_ON_PLACE;
    public static volatile BiConsumer<BlockPos, BlockState> clientOpener;
    private static final java.util.Map<net.minecraft.resources.ResourceKey<Level>, java.util.Set<Long>> KNOWN = new java.util.concurrent.ConcurrentHashMap<>();

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
            .setValue(FLOW, 1).setValue(FLOOD, false).setValue(ENABLED, false));
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

    public BlockState fullState() {
        return switch (this.kind) {
            case 1 -> ModContent.REALISTIC_LAVA_BLOCK.defaultBlockState();
            case 2 -> ModContent.REALISTIC_SLIME_BLOCK.defaultBlockState();
            case 3 -> ModContent.REALISTIC_MUD_BLOCK.defaultBlockState();
            case 4 -> ModContent.REALISTIC_MILK_BLOCK.defaultBlockState();
            case 5 -> ModContent.REALISTIC_COLA_BLOCK.defaultBlockState();
            case 6 -> ModContent.REALISTIC_HONEY_BLOCK.defaultBlockState();
            case 7 -> ModContent.REALISTIC_OIL_BLOCK.defaultBlockState();
            default -> ModContent.REALISTIC_WATER_BLOCK.defaultBlockState();
        };
    }

    public Block liquidBlock() {
        return switch (this.kind) {
            case 1 -> ModContent.REALISTIC_LAVA_BLOCK;
            case 2 -> ModContent.REALISTIC_SLIME_BLOCK;
            case 3 -> ModContent.REALISTIC_MUD_BLOCK;
            case 4 -> ModContent.REALISTIC_MILK_BLOCK;
            case 5 -> ModContent.REALISTIC_COLA_BLOCK;
            case 6 -> ModContent.REALISTIC_HONEY_BLOCK;
            case 7 -> ModContent.REALISTIC_OIL_BLOCK;
            default -> ModContent.REALISTIC_WATER_BLOCK;
        };
    }

    @Override
    protected void onPlace(final BlockState state, final Level level, final BlockPos pos, final BlockState oldState, final boolean movedByPiston) {
        if (!level.isClientSide()) {
            known(level).add(pos.asLong());
            if (!oldState.is(this)) {
                level.scheduleTick(pos, this, 2);
            }
        }
    }

    @Override
    protected void tick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
        known(level).add(pos.asLong());
        boolean flood = state.getValue(FLOOD);
        int flow = flood ? 10 : state.getValue(FLOW);

        if (!playerNear(level, pos)) {
            level.scheduleTick(pos, this, 30);
            return;
        }

        // O jato (bomba) do cliente so empurra liquido que ja existe: o servidor mantem as saidas cheias,
        // inclusive no modo alagamento (antes ele nao enchia nada e o jato ficava sem agua).
        if (flow > 0 && (state.getValue(ENABLED) || flood)) {
            boolean anyOpen = false;
            for (Direction direction : Direction.values()) {
                anyOpen |= state.getValue(faceProperty(direction));
            }
            for (Direction direction : Direction.values()) {
                // Alagamento sem saida aberta: espirra por cima e pelos 4 lados (nunca pra baixo).
                boolean open = anyOpen ? state.getValue(faceProperty(direction)) : flood && direction != Direction.DOWN;
                if (open) {
                    this.fill(level, pos.relative(direction));
                }
            }
        }

        int delay = flood ? 1 : flow > 0 ? Math.max(1, 11 - flow) : 20;
        level.scheduleTick(pos, this, delay);
    }

    private static boolean playerNear(ServerLevel level, BlockPos pos) {
        double limit = 180.0 * 180.0;
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < limit) {
                return true;
            }
        }
        return false;
    }

    public int kind() {
        return this.kind;
    }

    private boolean fill(ServerLevel level, BlockPos target) {
        BlockState current = level.getBlockState(target);
        boolean room = current.isAir();
        if (!room && current.is(this.liquidBlock())) {
            if (current.hasProperty(LiquidBlock.LEVEL)) {
                int amountLevel = current.getValue(LiquidBlock.LEVEL);
                room = amountLevel >= 1 && amountLevel <= 7;
            }
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
            boolean open = next.getValue(property);
            if (open) {
                next = next.setValue(ENABLED, true);
            }
            level.setBlock(pos, next, Block.UPDATE_CLIENTS);
            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.5F, open ? 0.8F : 0.5F);
            player.sendOverlayMessage(Component.translatable(
                open ? "block.realistic-water.source.open" : "block.realistic-water.source.closed",
                Component.translatable("block.realistic-water.source.face." + face.getName())));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UP, DOWN, NORTH, SOUTH, EAST, WEST, FLOW, FLOOD, ENABLED);
    }
}