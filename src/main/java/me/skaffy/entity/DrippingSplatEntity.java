package me.skaffy.entity;

import me.skaffy.ModContent;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class DrippingSplatEntity extends Entity implements ItemSupplier {
    private static final EntityDataAccessor<Boolean> IS_SLIME = SynchedEntityData.defineId(DrippingSplatEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Direction> ATTACHED_FACE = SynchedEntityData.defineId(DrippingSplatEntity.class, EntityDataSerializers.DIRECTION);

    private int lifeTicks = 0;
    private static final int MAX_LIFE = 160;

    public DrippingSplatEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public DrippingSplatEntity(Level level, Vec3 pos, Direction face, boolean isSlime) {
        this(ModContent.DRIPPING_SPLAT, level);
        this.setPos(pos.x, pos.y, pos.z);
        this.entityData.set(IS_SLIME, isSlime);
        this.entityData.set(ATTACHED_FACE, face);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(this.entityData.get(IS_SLIME) ? ModContent.REALISTIC_SLIME_BALL : ModContent.REALISTIC_HONEY_BALL);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(IS_SLIME, true);
        builder.define(ATTACHED_FACE, Direction.NORTH);
    }

    @Override
    public void tick() {
        super.tick();
        this.lifeTicks++;

        if (this.lifeTicks % 3 == 0) {
            this.setPos(this.getX(), this.getY() - 0.015, this.getZ());
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            boolean slime = this.entityData.get(IS_SLIME);
            if (this.random.nextFloat() < 0.25F) {
                serverLevel.sendParticles(slime ? ParticleTypes.ITEM_SLIME : ParticleTypes.DRIPPING_HONEY,
                    this.getX(), this.getY(), this.getZ(), 1, 0.05, 0.05, 0.05, 0.01);
            }

            if (this.lifeTicks == 1) {
                serverLevel.playSound(null, this.blockPosition(),
                    slime ? SoundEvents.SLIME_BLOCK_PLACE : SoundEvents.HONEY_BLOCK_PLACE,
                    SoundSource.BLOCKS, 0.8F, 1.2F);
            }

            if (this.lifeTicks >= MAX_LIFE) {
                this.discard();
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.lifeTicks = input.getIntOr("LifeTicks", 0);
        this.entityData.set(IS_SLIME, input.getIntOr("IsSlime", 1) != 0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("LifeTicks", this.lifeTicks);
        output.putInt("IsSlime", this.entityData.get(IS_SLIME) ? 1 : 0);
    }
}
