package me.skaffy.entity;

import me.skaffy.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
            default -> ModContent.REALISTIC_SNOWBALL;
        };
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide()) {
            if (hitResult instanceof BlockHitResult blockHit) {
                this.handleBlockHit(blockHit);
            }
            this.discard();
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

            level.playSound(null, pos, SoundEvents.SNOW_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SNOW_BLOCK.defaultBlockState()),
                hit.getLocation().x, hit.getLocation().y + 0.05, hit.getLocation().z, 40, 0.25, 0.1, 0.25, 0.15);
        } else if (face.getAxis().isHorizontal()) {
            DrippingSplatEntity splat = new DrippingSplatEntity(level, hit.getLocation(), face, this.kind == 1);
            level.addFreshEntity(splat);
        }
    }
}