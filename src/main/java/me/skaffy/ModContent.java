package me.skaffy;

import me.skaffy.block.HotObsidianBlock;
import me.skaffy.block.RealisticLavaBlock;
import me.skaffy.block.RealisticLeavesBlock;
import me.skaffy.block.RealisticSandBlock;
import me.skaffy.block.RealisticWaterBlock;
import me.skaffy.block.SourceBlock;
import me.skaffy.block.WaveMakerBlock;
import me.skaffy.entity.DrippingSplatEntity;
import me.skaffy.entity.RealisticProjectileEntity;
import me.skaffy.fluid.FiniteBucketItem;
import me.skaffy.fluid.RealisticLavaFluid;
import me.skaffy.fluid.RealisticSlimeFluid;
import me.skaffy.fluid.RealisticWaterFluid;
import me.skaffy.fluid.SmallWaterBucketItem;
import me.skaffy.item.ThrowableBallItem;
import me.skaffy.net.LavaSyncPayload;
import me.skaffy.net.SlimeSyncPayload;
import me.skaffy.net.WaterSyncPayload;
import me.skaffy.realistic.ConverterItem;
import me.skaffy.realistic.ConverterMenu;
import me.skaffy.realistic.RealisticBlockEntity;
import me.skaffy.realistic.RealisticBlockItem;
import me.skaffy.realistic.RealisticBoatEntity;
import me.skaffy.realistic.RealisticBoatItem;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.jspecify.annotations.Nullable;

public final class ModContent {
    public static final FlowingFluid FLOWING_REALISTIC_WATER = Registry.register(
        BuiltInRegistries.FLUID, RealisticWater.id("flowing_realistic_water"), new RealisticWaterFluid.Flowing());
    public static final FlowingFluid REALISTIC_WATER = Registry.register(
        BuiltInRegistries.FLUID, RealisticWater.id("realistic_water"), new RealisticWaterFluid.Source());
    public static final ResourceKey<Block> REALISTIC_WATER_BLOCK_KEY = ResourceKey.create(Registries.BLOCK, RealisticWater.id("realistic_water"));
    public static final Block REALISTIC_WATER_BLOCK = Registry.register(BuiltInRegistries.BLOCK, REALISTIC_WATER_BLOCK_KEY, new RealisticWaterBlock(
        REALISTIC_WATER, BlockBehaviour.Properties.of().mapColor(MapColor.WATER).replaceable().noCollision().strength(100.0F).pushReaction(PushReaction.POPPED).noLootTable().liquid().sound(SoundType.EMPTY).setId(REALISTIC_WATER_BLOCK_KEY)));
    public static final ResourceKey<Item> REALISTIC_WATER_BUCKET_KEY = ResourceKey.create(Registries.ITEM, RealisticWater.id("realistic_water_bucket"));
    public static final Item REALISTIC_WATER_BUCKET = Registry.register(BuiltInRegistries.ITEM, REALISTIC_WATER_BUCKET_KEY, new FiniteBucketItem(
        REALISTIC_WATER, WaterSyncPayload::stateFor, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1).setId(REALISTIC_WATER_BUCKET_KEY)));
    public static final ResourceKey<Item> SMALL_REALISTIC_WATER_BUCKET_KEY = ResourceKey.create(Registries.ITEM, RealisticWater.id("small_realistic_water_bucket"));
    public static final Item SMALL_REALISTIC_WATER_BUCKET = Registry.register(BuiltInRegistries.ITEM, SMALL_REALISTIC_WATER_BUCKET_KEY, new SmallWaterBucketItem(
        REALISTIC_WATER, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1).setId(SMALL_REALISTIC_WATER_BUCKET_KEY)));

    public static final FlowingFluid FLOWING_REALISTIC_LAVA = Registry.register(
        BuiltInRegistries.FLUID, RealisticWater.id("flowing_realistic_lava"), new RealisticLavaFluid.Flowing());
    public static final FlowingFluid REALISTIC_LAVA = Registry.register(
        BuiltInRegistries.FLUID, RealisticWater.id("realistic_lava"), new RealisticLavaFluid.Source());
    public static final ResourceKey<Block> REALISTIC_LAVA_BLOCK_KEY = ResourceKey.create(Registries.BLOCK, RealisticWater.id("realistic_lava"));
    public static final Block REALISTIC_LAVA_BLOCK = Registry.register(BuiltInRegistries.BLOCK, REALISTIC_LAVA_BLOCK_KEY, new RealisticLavaBlock(
        REALISTIC_LAVA, BlockBehaviour.Properties.of().mapColor(MapColor.FIRE).replaceable().noCollision().strength(100.0F).pushReaction(PushReaction.POPPED).noLootTable().liquid().lightLevel(state -> 15).sound(SoundType.EMPTY).setId(REALISTIC_LAVA_BLOCK_KEY)));
    public static final ResourceKey<Item> REALISTIC_LAVA_BUCKET_KEY = ResourceKey.create(Registries.ITEM, RealisticWater.id("realistic_lava_bucket"));
    public static final Item REALISTIC_LAVA_BUCKET = Registry.register(BuiltInRegistries.ITEM, REALISTIC_LAVA_BUCKET_KEY, new FiniteBucketItem(
        REALISTIC_LAVA, LavaSyncPayload::stateFor, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1).setId(REALISTIC_LAVA_BUCKET_KEY)));

    public static final FlowingFluid FLOWING_REALISTIC_SLIME = Registry.register(
        BuiltInRegistries.FLUID, RealisticWater.id("flowing_realistic_slime"), new RealisticSlimeFluid.Flowing());
    public static final FlowingFluid REALISTIC_SLIME = Registry.register(
        BuiltInRegistries.FLUID, RealisticWater.id("realistic_slime"), new RealisticSlimeFluid.Source());
    public static final ResourceKey<Block> REALISTIC_SLIME_BLOCK_KEY = ResourceKey.create(Registries.BLOCK, RealisticWater.id("realistic_slime"));
    public static final Block REALISTIC_SLIME_BLOCK = Registry.register(BuiltInRegistries.BLOCK, REALISTIC_SLIME_BLOCK_KEY, new RealisticWaterBlock(
        REALISTIC_SLIME, BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).replaceable().noCollision().strength(100.0F).pushReaction(PushReaction.POPPED).noLootTable().liquid().sound(SoundType.SLIME_BLOCK).setId(REALISTIC_SLIME_BLOCK_KEY)));
    public static final ResourceKey<Item> REALISTIC_SLIME_BUCKET_KEY = ResourceKey.create(Registries.ITEM, RealisticWater.id("realistic_slime_bucket"));
    public static final Item REALISTIC_SLIME_BUCKET = Registry.register(BuiltInRegistries.ITEM, REALISTIC_SLIME_BUCKET_KEY, new BucketItem(
        REALISTIC_SLIME, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1).setId(REALISTIC_SLIME_BUCKET_KEY)) {
        @Override
        protected void playEmptySound(final @Nullable LivingEntity user, final LevelAccessor level, final BlockPos pos) {
            level.playSound(user, pos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(user, GameEvent.FLUID_PLACE, pos);
        }
    });

    public static final FlowingFluid FLOWING_REALISTIC_MUD = Registry.register(
        BuiltInRegistries.FLUID, RealisticWater.id("flowing_realistic_mud"), new RealisticWaterFluid.Flowing());
    public static final FlowingFluid REALISTIC_MUD = Registry.register(
        BuiltInRegistries.FLUID, RealisticWater.id("realistic_mud"), new RealisticWaterFluid.Source());
    public static final ResourceKey<Block> REALISTIC_MUD_BLOCK_KEY = ResourceKey.create(Registries.BLOCK, RealisticWater.id("realistic_mud"));
    public static final Block REALISTIC_MUD_BLOCK = Registry.register(BuiltInRegistries.BLOCK, REALISTIC_MUD_BLOCK_KEY, new RealisticWaterBlock(
        REALISTIC_MUD, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).replaceable().noCollision().strength(100.0F).pushReaction(PushReaction.POPPED).noLootTable().liquid().sound(SoundType.MUD).setId(REALISTIC_MUD_BLOCK_KEY)));
    public static final ResourceKey<Item> REALISTIC_MUD_BUCKET_KEY = ResourceKey.create(Registries.ITEM, RealisticWater.id("realistic_mud_bucket"));
    public static final Item REALISTIC_MUD_BUCKET = Registry.register(BuiltInRegistries.ITEM, REALISTIC_MUD_BUCKET_KEY, new FiniteBucketItem(
        REALISTIC_MUD, amount -> ModContent.REALISTIC_MUD_BLOCK.defaultBlockState(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1).setId(REALISTIC_MUD_BUCKET_KEY)));

    public static final FlowingFluid FLOWING_REALISTIC_MILK = Registry.register(
        BuiltInRegistries.FLUID, RealisticWater.id("flowing_realistic_milk"), new RealisticWaterFluid.Flowing());
    public static final FlowingFluid REALISTIC_MILK = Registry.register(
        BuiltInRegistries.FLUID, RealisticWater.id("realistic_milk"), new RealisticWaterFluid.Source());
    public static final ResourceKey<Block> REALISTIC_MILK_BLOCK_KEY = ResourceKey.create(Registries.BLOCK, RealisticWater.id("realistic_milk"));
    public static final Block REALISTIC_MILK_BLOCK = Registry.register(BuiltInRegistries.BLOCK, REALISTIC_MILK_BLOCK_KEY, new RealisticWaterBlock(
        REALISTIC_MILK, BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).replaceable().noCollision().strength(100.0F).pushReaction(PushReaction.POPPED).noLootTable().liquid().sound(SoundType.EMPTY).setId(REALISTIC_MILK_BLOCK_KEY)));
    public static final ResourceKey<Item> REALISTIC_MILK_BUCKET_KEY = ResourceKey.create(Registries.ITEM, RealisticWater.id("realistic_milk_bucket"));
    public static final Item REALISTIC_MILK_BUCKET = Registry.register(BuiltInRegistries.ITEM, REALISTIC_MILK_BUCKET_KEY, new FiniteBucketItem(
        REALISTIC_MILK, amount -> ModContent.REALISTIC_MILK_BLOCK.defaultBlockState(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1).setId(REALISTIC_MILK_BUCKET_KEY)));

    public static final FlowingFluid FLOWING_REALISTIC_COLA = Registry.register(
        BuiltInRegistries.FLUID, RealisticWater.id("flowing_realistic_cola"), new RealisticWaterFluid.Flowing());
    public static final FlowingFluid REALISTIC_COLA = Registry.register(
        BuiltInRegistries.FLUID, RealisticWater.id("realistic_cola"), new RealisticWaterFluid.Source());
    public static final ResourceKey<Block> REALISTIC_COLA_BLOCK_KEY = ResourceKey.create(Registries.BLOCK, RealisticWater.id("realistic_cola"));
    public static final Block REALISTIC_COLA_BLOCK = Registry.register(BuiltInRegistries.BLOCK, REALISTIC_COLA_BLOCK_KEY, new RealisticWaterBlock(
        REALISTIC_COLA, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).replaceable().noCollision().strength(100.0F).pushReaction(PushReaction.POPPED).noLootTable().liquid().sound(SoundType.EMPTY).setId(REALISTIC_COLA_BLOCK_KEY)));
    public static final ResourceKey<Item> REALISTIC_COLA_BUCKET_KEY = ResourceKey.create(Registries.ITEM, RealisticWater.id("realistic_cola_bucket"));
    public static final Item REALISTIC_COLA_BUCKET = Registry.register(BuiltInRegistries.ITEM, REALISTIC_COLA_BUCKET_KEY, new FiniteBucketItem(
        REALISTIC_COLA, amount -> ModContent.REALISTIC_COLA_BLOCK.defaultBlockState(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1).setId(REALISTIC_COLA_BUCKET_KEY)));

    public static final ResourceKey<Block> SOURCE_WATER_KEY = ResourceKey.create(Registries.BLOCK, RealisticWater.id("source_water"));
    public static final Block SOURCE_WATER = Registry.register(BuiltInRegistries.BLOCK, SOURCE_WATER_KEY, new SourceBlock(0, BlockBehaviour.Properties.of().mapColor(MapColor.WATER).instrument(NoteBlockInstrument.BASEDRUM).strength(1.5F, 6.0F).sound(SoundType.METAL).setId(SOURCE_WATER_KEY)));
    public static final ResourceKey<Item> SOURCE_WATER_ITEM_KEY = ResourceKey.create(Registries.ITEM, RealisticWater.id("source_water"));
    public static final Item SOURCE_WATER_ITEM = registerBlockItem(SOURCE_WATER_ITEM_KEY, SOURCE_WATER);

    public static final ResourceKey<Block> SOURCE_LAVA_KEY = ResourceKey.create(Registries.BLOCK, RealisticWater.id("source_lava"));
    public static final Block SOURCE_LAVA = Registry.register(BuiltInRegistries.BLOCK, SOURCE_LAVA_KEY, new SourceBlock(1, BlockBehaviour.Properties.of().mapColor(MapColor.FIRE).instrument(NoteBlockInstrument.BASEDRUM).strength(1.5F, 6.0F).sound(SoundType.METAL).lightLevel(state -> 7).setId(SOURCE_LAVA_KEY)));
    public static final ResourceKey<Item> SOURCE_LAVA_ITEM_KEY = ResourceKey.create(Registries.ITEM, RealisticWater.id("source_lava"));
    public static final Item SOURCE_LAVA_ITEM = registerBlockItem(SOURCE_LAVA_ITEM_KEY, SOURCE_LAVA);

    public static final ResourceKey<Block> SOURCE_SLIME_KEY = ResourceKey.create(Registries.BLOCK, RealisticWater.id("source_slime"));
    public static final Block SOURCE_SLIME = Registry.register(BuiltInRegistries.BLOCK, SOURCE_SLIME_KEY, new SourceBlock(2, BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).instrument(NoteBlockInstrument.BASEDRUM).strength(1.5F, 6.0F).sound(SoundType.METAL).setId(SOURCE_SLIME_KEY)));
    public static final ResourceKey<Item> SOURCE_SLIME_ITEM_KEY = ResourceKey.create(Registries.ITEM, RealisticWater.id("source_slime"));
    public static final Item SOURCE_SLIME_ITEM = registerBlockItem(SOURCE_SLIME_ITEM_KEY, SOURCE_SLIME);

    public static final ResourceKey<Block> SOURCE_MUD_KEY = ResourceKey.create(Registries.BLOCK, RealisticWater.id("source_mud"));
    public static final Block SOURCE_MUD = Registry.register(BuiltInRegistries.BLOCK, SOURCE_MUD_KEY, new SourceBlock(3, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).instrument(NoteBlockInstrument.BASEDRUM).strength(1.5F, 6.0F).sound(SoundType.METAL).setId(SOURCE_MUD_KEY)));
    public static final ResourceKey<Item> SOURCE_MUD_ITEM_KEY = ResourceKey.create(Registries.ITEM, RealisticWater.id("source_mud"));
    public static final Item SOURCE_MUD_ITEM = registerBlockItem(SOURCE_MUD_ITEM_KEY, SOURCE_MUD);

    public static final ResourceKey<Block> SOURCE_MILK_KEY = ResourceKey.create(Registries.BLOCK, RealisticWater.id("source_milk"));
    public static final Block SOURCE_MILK = Registry.register(BuiltInRegistries.BLOCK, SOURCE_MILK_KEY, new SourceBlock(4, BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).instrument(NoteBlockInstrument.BASEDRUM).strength(1.5F, 6.0F).sound(SoundType.METAL).setId(SOURCE_MILK_KEY)));
    public static final ResourceKey<Item> SOURCE_MILK_ITEM_KEY = ResourceKey.create(Registries.ITEM, RealisticWater.id("source_milk"));
    public static final Item SOURCE_MILK_ITEM = registerBlockItem(SOURCE_MILK_ITEM_KEY, SOURCE_MILK);

    public static final ResourceKey<Block> SOURCE_COLA_KEY = ResourceKey.create(Registries.BLOCK, RealisticWater.id("source_cola"));
    public static final Block SOURCE_COLA = Registry.register(BuiltInRegistries.BLOCK, SOURCE_COLA_KEY, new SourceBlock(5, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM).strength(1.5F, 6.0F).sound(SoundType.METAL).setId(SOURCE_COLA_KEY)));
    public static final ResourceKey<Item> SOURCE_COLA_ITEM_KEY = ResourceKey.create(Registries.ITEM, RealisticWater.id("source_cola"));
    public static final Item SOURCE_COLA_ITEM = registerBlockItem(SOURCE_COLA_ITEM_KEY, SOURCE_COLA);

    public static final ResourceKey<Block> HOT_OBSIDIAN_KEY = ResourceKey.create(Registries.BLOCK, RealisticWater.id("hot_obsidian"));
    public static final Block HOT_OBSIDIAN = Registry.register(BuiltInRegistries.BLOCK, HOT_OBSIDIAN_KEY, new HotObsidianBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM).strength(50.0F, 1200.0F).sound(SoundType.STONE).noOcclusion().lightLevel(state -> state.getValue(HotObsidianBlock.HEAT) > 0 ? 9 : 0).setId(HOT_OBSIDIAN_KEY)));

    public static final ResourceKey<Block> REALISTIC_SAND_KEY = ResourceKey.create(Registries.BLOCK, RealisticWater.id("realistic_sand"));
    public static final Block REALISTIC_SAND = Registry.register(BuiltInRegistries.BLOCK, REALISTIC_SAND_KEY, new RealisticSandBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND).noOcclusion().pushReaction(PushReaction.POPPED).isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos, box) -> false).setId(REALISTIC_SAND_KEY)));
    public static final ResourceKey<Item> REALISTIC_SAND_ITEM_KEY = ResourceKey.create(Registries.ITEM, RealisticWater.id("realistic_sand"));
    public static final Item REALISTIC_SAND_ITEM = registerBlockItem(REALISTIC_SAND_ITEM_KEY, REALISTIC_SAND);

    public static final ResourceKey<Block> REALISTIC_LEAVES_KEY = ResourceKey.create(Registries.BLOCK, RealisticWater.id("realistic_leaves"));
    public static final Block REALISTIC_LEAVES = Registry.register(BuiltInRegistries.BLOCK, REALISTIC_LEAVES_KEY, new RealisticLeavesBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(0.2F).sound(SoundType.AZALEA_LEAVES).noOcclusion().ignitedByLava().pushReaction(PushReaction.POPPED).isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos, box) -> false).setId(REALISTIC_LEAVES_KEY)));
    public static final ResourceKey<Item> REALISTIC_LEAVES_ITEM_KEY = ResourceKey.create(Registries.ITEM, RealisticWater.id("realistic_leaves"));
    public static final Item REALISTIC_LEAVES_ITEM = registerBlockItem(REALISTIC_LEAVES_ITEM_KEY, REALISTIC_LEAVES);

    public static final ResourceKey<Block> WAVE_MAKER_KEY = ResourceKey.create(Registries.BLOCK, RealisticWater.id("wave_maker"));
    public static final Block WAVE_MAKER = Registry.register(BuiltInRegistries.BLOCK, WAVE_MAKER_KEY, new WaveMakerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_WART_BLOCK).instrument(NoteBlockInstrument.BASEDRUM).strength(1.5F, 6.0F).sound(SoundType.METAL).setId(WAVE_MAKER_KEY)));
    public static final ResourceKey<Item> WAVE_MAKER_ITEM_KEY = ResourceKey.create(Registries.ITEM, RealisticWater.id("wave_maker"));
    public static final Item WAVE_MAKER_ITEM = registerBlockItem(WAVE_MAKER_ITEM_KEY, WAVE_MAKER);

    public static final ResourceKey<Item> REALISTIC_BLOCK_ITEM_KEY = ResourceKey.create(Registries.ITEM, RealisticWater.id("realistic_block"));
    public static final Item REALISTIC_BLOCK_ITEM = Registry.register(BuiltInRegistries.ITEM, REALISTIC_BLOCK_ITEM_KEY, new RealisticBlockItem(new Item.Properties().setId(REALISTIC_BLOCK_ITEM_KEY)));

    public static final ResourceKey<Item> REALISTIC_BOAT_ITEM_KEY = ResourceKey.create(Registries.ITEM, RealisticWater.id("realistic_boat"));
    public static final Item REALISTIC_BOAT_ITEM = Registry.register(BuiltInRegistries.ITEM, REALISTIC_BOAT_ITEM_KEY, new RealisticBoatItem(new Item.Properties().stacksTo(1).setId(REALISTIC_BOAT_ITEM_KEY)));

    public static final ResourceKey<Item> CONVERTER_KEY = ResourceKey.create(Registries.ITEM, RealisticWater.id("realistic_converter"));
    public static final Item CONVERTER = Registry.register(BuiltInRegistries.ITEM, CONVERTER_KEY, new ConverterItem(new Item.Properties().stacksTo(1).setId(CONVERTER_KEY)));

    public static final ResourceKey<Item> REALISTIC_SNOWBALL_KEY = ResourceKey.create(Registries.ITEM, RealisticWater.id("realistic_snowball"));
    public static final Item REALISTIC_SNOWBALL = Registry.register(BuiltInRegistries.ITEM, REALISTIC_SNOWBALL_KEY, new ThrowableBallItem(0, new Item.Properties().stacksTo(16).setId(REALISTIC_SNOWBALL_KEY)));

    public static final ResourceKey<Item> REALISTIC_SLIME_BALL_KEY = ResourceKey.create(Registries.ITEM, RealisticWater.id("realistic_slime_ball"));
    public static final Item REALISTIC_SLIME_BALL = Registry.register(BuiltInRegistries.ITEM, REALISTIC_SLIME_BALL_KEY, new ThrowableBallItem(1, new Item.Properties().stacksTo(16).setId(REALISTIC_SLIME_BALL_KEY)));

    public static final ResourceKey<Item> REALISTIC_HONEY_BALL_KEY = ResourceKey.create(Registries.ITEM, RealisticWater.id("realistic_honey_ball"));
    public static final Item REALISTIC_HONEY_BALL = Registry.register(BuiltInRegistries.ITEM, REALISTIC_HONEY_BALL_KEY, new ThrowableBallItem(2, new Item.Properties().stacksTo(16).setId(REALISTIC_HONEY_BALL_KEY)));

    public static final ResourceKey<EntityType<?>> REALISTIC_PROJECTILE_KEY = ResourceKey.create(Registries.ENTITY_TYPE, RealisticWater.id("realistic_projectile"));
    public static final EntityType<RealisticProjectileEntity> REALISTIC_PROJECTILE = Registry.register(BuiltInRegistries.ENTITY_TYPE, REALISTIC_PROJECTILE_KEY,
        EntityType.Builder.<RealisticProjectileEntity>of(RealisticProjectileEntity::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10).noLootTable().build(REALISTIC_PROJECTILE_KEY));

    public static final ResourceKey<EntityType<?>> DRIPPING_SPLAT_KEY = ResourceKey.create(Registries.ENTITY_TYPE, RealisticWater.id("dripping_splat"));
    public static final EntityType<DrippingSplatEntity> DRIPPING_SPLAT = Registry.register(BuiltInRegistries.ENTITY_TYPE, DRIPPING_SPLAT_KEY,
        EntityType.Builder.<DrippingSplatEntity>of(DrippingSplatEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(8).updateInterval(2).noLootTable().build(DRIPPING_SPLAT_KEY));

    public static final MenuType<ConverterMenu> CONVERTER_MENU = Registry.register(BuiltInRegistries.MENU, RealisticWater.id("realistic_converter"),
        new MenuType<>(ConverterMenu::new, FeatureFlags.VANILLA_SET));
    public static final TicketType GRID_TICKET = Registry.register(BuiltInRegistries.TICKET_TYPE, RealisticWater.id("realistic_grid"),
        new TicketType(TicketType.NO_TIMEOUT, TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION));
    public static final TicketType BACKGROUND_TICKET = Registry.register(BuiltInRegistries.TICKET_TYPE, RealisticWater.id("realistic_background"),
        new TicketType(100L, TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION | TicketType.FLAG_KEEP_DIMENSION_ACTIVE));

    public static final ResourceKey<EntityType<?>> REALISTIC_BLOCK_ENTITY_KEY = ResourceKey.create(Registries.ENTITY_TYPE, RealisticWater.id("realistic_block"));
    public static final EntityType<RealisticBlockEntity> REALISTIC_BLOCK_ENTITY = Registry.register(BuiltInRegistries.ENTITY_TYPE, REALISTIC_BLOCK_ENTITY_KEY,
        EntityType.Builder.<RealisticBlockEntity>of(RealisticBlockEntity::new, MobCategory.MISC).sized(1.0F, 1.0F).clientTrackingRange(10).updateInterval(1).noLootTable().build(REALISTIC_BLOCK_ENTITY_KEY));

    public static final ResourceKey<EntityType<?>> REALISTIC_BOAT_KEY = ResourceKey.create(Registries.ENTITY_TYPE, RealisticWater.id("realistic_boat"));
    public static final EntityType<RealisticBoatEntity> REALISTIC_BOAT = Registry.register(BuiltInRegistries.ENTITY_TYPE, REALISTIC_BOAT_KEY,
        EntityType.Builder.<RealisticBoatEntity>of(RealisticBoatEntity::new, MobCategory.MISC).sized(1.375F, 0.5625F).clientTrackingRange(10).updateInterval(1).noLootTable().build(REALISTIC_BOAT_KEY));

    public static final SimpleParticleType STEAM = Registry.register(BuiltInRegistries.PARTICLE_TYPE, RealisticWater.id("steam"), FabricParticleTypes.simple(true));
    public static final SoundEvent BOILING = Registry.register(BuiltInRegistries.SOUND_EVENT, RealisticWater.id("block.realistic_water.boil"), SoundEvent.createVariableRangeEvent(RealisticWater.id("block.realistic_water.boil")));
    public static final SoundEvent WAVE_MAKER_RUN = Registry.register(BuiltInRegistries.SOUND_EVENT, RealisticWater.id("block.wave_maker.run"), SoundEvent.createVariableRangeEvent(RealisticWater.id("block.wave_maker.run")));

    public static final DataComponentType<Identifier> SOURCE_BLOCK = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, RealisticWater.id("source_block"),
        DataComponentType.<Identifier>builder().persistent(Identifier.CODEC).networkSynchronized(Identifier.STREAM_CODEC).build());
    public static final DataComponentType<Identifier> SOURCE_ITEM = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, RealisticWater.id("source_item"),
        DataComponentType.<Identifier>builder().persistent(Identifier.CODEC).networkSynchronized(Identifier.STREAM_CODEC).build());

    public static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, RealisticWater.id("realistic_fluids"));
    public static final CreativeModeTab TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_KEY, FabricCreativeModeTab.builder()
        .title(Component.translatable("itemGroup.realistic-water.realistic_fluids"))
        .icon(() -> new ItemStack(REALISTIC_WATER_BUCKET))
        .displayItems((params, output) -> {
            output.accept(REALISTIC_WATER_BUCKET);
            output.accept(SMALL_REALISTIC_WATER_BUCKET);
            output.accept(REALISTIC_LAVA_BUCKET);
            output.accept(REALISTIC_SLIME_BUCKET);
            output.accept(REALISTIC_MUD_BUCKET);
            output.accept(REALISTIC_MILK_BUCKET);
            output.accept(REALISTIC_COLA_BUCKET);

            output.accept(SOURCE_WATER_ITEM);
            output.accept(SOURCE_LAVA_ITEM);
            output.accept(SOURCE_SLIME_ITEM);
            output.accept(SOURCE_MUD_ITEM);
            output.accept(SOURCE_MILK_ITEM);
            output.accept(SOURCE_COLA_ITEM);

            output.accept(REALISTIC_SNOWBALL);
            output.accept(REALISTIC_SLIME_BALL);
            output.accept(REALISTIC_HONEY_BALL);

            output.accept(WAVE_MAKER_ITEM);
            output.accept(CONVERTER);
            output.accept(REALISTIC_BOAT_ITEM);
            output.accept(RealisticBlockItem.of(Blocks.SNOW_BLOCK, 1));
            output.accept(REALISTIC_SAND_ITEM);
            output.accept(REALISTIC_LEAVES_ITEM);
        })
        .build());

    private ModContent() {}

    private static Item registerBlockItem(ResourceKey<Item> key, Block block) {
        BlockItem item = new BlockItem(block, new Item.Properties().useBlockDescriptionPrefix().setId(key));
        item.registerBlocks(Item.BY_BLOCK, item);
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    public static void init() {}
}