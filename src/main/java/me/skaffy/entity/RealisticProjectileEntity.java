package me.skaffy.entity;

import me.skaffy.ModContent;
import me.skaffy.net.LavaSyncPayload;
import me.skaffy.net.SlimeSyncPayload;
import me.skaffy.net.WaterSyncPayload;
import me.skaffy.portal.PortalTransit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class RealisticProjectileEntity extends ThrowableItemProjectile {
    private int kind;

    public RealisticProjectileEntity(EntityType<? extends RealisticProjectileEntity> type, Level level) {
        super(type, level);
    }

    public RealisticProjectileEntity(Level level, LivingEntity shooter, int kind) {
        super(ModContent.REALISTIC_PROJECTILE, level);
        this.kind = kind;
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ());
    }

    @Override
    protected Item getDefaultItem() {
        return switch (this.kind) {
            case 1 -> ModContent.REALISTIC_SLIME_BALL;
            case 2 -> ModContent.REALISTIC_HONEY_BALL;
            case 3 -> ModContent.REALISTIC_WATER_BALL;
            case 4 -> ModContent.REALISTIC_LAVA_BALL;
            default -> ModContent.REALISTIC_SNOWBALL;
        };
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel serverLevel && this.tickCount % 2 == 0) {
            // Rastro no ar: pinga um pouco do liquido enquanto a bola voa.
            switch (this.kind) {
                case 1 -> serverLevel.sendParticles(ParticleTypes.ITEM_SLIME, this.getX(), this.getY(), this.getZ(), 1, 0.05, 0.05, 0.05, 0.0);
                case 2 -> serverLevel.sendParticles(ParticleTypes.FALLING_HONEY, this.getX(), this.getY(), this.getZ(), 1, 0.05, 0.05, 0.05, 0.0);
                case 3 -> serverLevel.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY(), this.getZ(), 1, 0.05, 0.05, 0.05, 0.0);
                case 4 -> serverLevel.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY(), this.getZ(), 1, 0.05, 0.05, 0.05, 0.0);
                default -> serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY(), this.getZ(), 1, 0.05, 0.05, 0.05, 0.0);
            }
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide()) {
            if (this.kind >= 1) {
                // Agua, lava, slime e mel: ao bater viram fluido simulado de verdade (escorre), nao uma textura grudada.
                this.splashFluid(hitResult);
            } else if (hitResult instanceof BlockHitResult blockHit) {
                this.handleBlockHit(blockHit);
            } else {
                this.snowPuff(hitResult.getLocation());
            }
            this.discard();
        }
    }

    private void snowPuff(Vec3 at) {
        ServerLevel level = (ServerLevel) this.level();
        level.playSound(null, at.x, at.y, at.z, SoundEvents.SNOW_BREAK, SoundSource.BLOCKS, 0.8F, 1.2F);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SNOW_BLOCK.defaultBlockState()),
            at.x, at.y, at.z, 12, 0.12, 0.08, 0.12, 0.1);
    }

    /** Bolas de liquido (1 slime, 2 mel, 3 agua, 4 lava): ao bater, viram uma porcao finita do liquido realista, que escorre. */
    private void splashFluid(HitResult hit) {
        ServerLevel level = (ServerLevel) this.level();
        boolean lava = this.kind == 4;
        boolean water = this.kind == 3;
        Vec3 at = hit.getLocation();
        BlockPos target;
        if (hit instanceof BlockHitResult blockHit) {
            BlockPos pos = blockHit.getBlockPos();
            BlockState hitState = level.getBlockState(pos);
            target = hitState.canBeReplaced() && !hitState.liquid() ? pos : pos.relative(blockHit.getDirection());
        } else {
            target = BlockPos.containing(at);
        }
        BlockState current = level.getBlockState(target);
        if (!(current.isAir() || current.canBeReplaced() && !current.liquid())) {
            level.playSound(null, at.x, at.y, at.z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.4F, 1.6F);
            return;
        }
        if (water && level.environmentAttributes().getValue(EnvironmentAttributes.WATER_EVAPORATES, target)) {
            level.playSound(null, at.x, at.y, at.z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F);
            PortalTransit.steamBurst(level, new Vec3(target.getX() + 0.5, target.getY() + 0.1, target.getZ() + 0.5), 8.0F);
            return;
        }
        if (!current.isAir()) {
            level.destroyBlock(target, true);
        }
        BlockState fluid = switch (this.kind) {
            case 1 -> SlimeSyncPayload.stateFor(4);
            case 2 -> WaterSyncPayload.stateFor(5, 4);
            case 4 -> LavaSyncPayload.stateFor(8);
            default -> WaterSyncPayload.stateFor(8);
        };
        level.setBlock(target, fluid, 11);
        if (this.kind == 1 || this.kind == 2) {
            level.playSound(null, at.x, at.y, at.z, this.kind == 1 ? SoundEvents.SLIME_BLOCK_PLACE : SoundEvents.HONEY_BLOCK_PLACE, SoundSource.BLOCKS, 1.0F, 1.1F);
            level.sendParticles(this.kind == 1 ? ParticleTypes.ITEM_SLIME : ParticleTypes.FALLING_HONEY, at.x, at.y + 0.1, at.z, 10, 0.2, 0.1, 0.2, 0.05);
        } else if (lava) {
            level.playSound(null, at.x, at.y, at.z, SoundEvents.BUCKET_EMPTY_LAVA, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.sendParticles(ParticleTypes.LAVA, at.x, at.y + 0.1, at.z, 12, 0.2, 0.1, 0.2, 0.1);
        } else {
            level.playSound(null, at.x, at.y, at.z, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.2F);
            level.sendParticles(ParticleTypes.SPLASH, at.x, at.y + 0.1, at.z, 20, 0.25, 0.1, 0.25, 0.2);
        }
    }

    private void handleBlockHit(BlockHitResult hit) {
        ServerLevel level = (ServerLevel) this.level();
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        Direction face = hit.getDirection();

        if (this.kind == 0) {
            boolean hitCola = state.is(ModContent.REALISTIC_COLA_BLOCK) || level.getBlockState(pos.relative(face)).is(ModContent.REALISTIC_COLA_BLOCK);
            if (hitCola) {
                Vec3 center = hit.getLocation();
                level.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 2.0F, 1.4F);
                level.playSound(null, center.x, center.y, center.z, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.BLOCKS, 3.0F, 0.7F);

                level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y + 0.5, center.z, 5, 0.2, 0.5, 0.2, 0.1);
                level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, center.x, center.y + 1.0, center.z, 50, 0.3, 1.8, 0.3, 0.15);
                level.sendParticles(ParticleTypes.SPLASH, center.x, center.y + 0.8, center.z, 100, 0.5, 2.0, 0.5, 0.3);

                level.getEntities(this, this.getBoundingBox().inflate(3.5)).forEach(e -> {
                    e.push(0.0, 1.25, 0.0);
                });
                return;
            }

            Vec3 hitAt = hit.getLocation();
            // Uma bolinha so deixa neve ao cair numa superficie de cima, e so 1 camada (1/8 de bloco).
            // Em parede, teto ou lateral ela apenas se desfaz em pozinho, sem formar monte.
            if (face == Direction.UP) {
                BlockPos target = pos.relative(face);
                BlockState at = level.getBlockState(target);
                if (at.isAir()) {
                    level.setBlock(target, ModContent.REALISTIC_SNOW.defaultBlockState().setValue(me.skaffy.block.RealisticSnowBlock.LAYERS, 1), 3);
                } else if (at.getBlock() == ModContent.REALISTIC_SNOW) {
                    int layers = Math.min(me.skaffy.block.RealisticSnowBlock.MAX_LAYERS, at.getValue(me.skaffy.block.RealisticSnowBlock.LAYERS) + 1);
                    level.setBlock(target, at.setValue(me.skaffy.block.RealisticSnowBlock.LAYERS, layers), 3);
                }
            }
            level.playSound(null, pos, SoundEvents.SNOW_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SNOW_BLOCK.defaultBlockState()),
                hitAt.x, hitAt.y + 0.05, hitAt.z, 14, 0.12, 0.06, 0.12, 0.1);
        }
    }
}