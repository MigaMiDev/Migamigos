package ttv.migami.migamigos.entity.summon;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import ttv.migami.migamigos.init.ModEntities;
import ttv.migami.migamigos.init.ModParticleTypes;

import java.util.List;

import static ttv.migami.migamigos.common.network.ServerPlayHandler.sendParticlesToAll;

public class IceLotus extends SummonEntity {
    private static final int DAMAGE_RADIUS = 3;
    private static final int PARTICLE_RADIUS = 3;
    private static final int DAMAGE_INTERVAL = 100;
    private static final int LIFETIME = 200;

    public IceLotus(EntityType<? extends Mob> entityType, Level world) {
        super(entityType, world);
    }

    public IceLotus(Level level, LivingEntity owner, float power) {
        super(ModEntities.ICE_LOTUS.get(), level, owner, power);
        this.noPhysics = true;
        this.noCulling = true;
        this.setNoGravity(true);
        this.setNoAi(true);
    }

    @Override
    public void tick() {
        super.tick();

        //this.setYRot(this.getYRot() + 10.0F);

        if (this.tickCount % DAMAGE_INTERVAL == 0 || this.tickCount <= 1) {
            dealAreaDamage();
        }

        summonParticleRing();
        if (this.tickCount % 3 == 0) {
            if (this.level() instanceof ServerLevel serverLevel) {
                sendParticlesToAll(
                        serverLevel,
                        ModParticleTypes.SMALL_FREEZE_BREEZE.get(),
                        true,
                        this.getX(),
                        this.getY() + 0.1,
                        this.getZ(),
                        1,
                        this.getBbWidth() + (double) PARTICLE_RADIUS / 2, this.getBbHeight(), this.getBbWidth() + (double) PARTICLE_RADIUS / 2,
                        0.1
                );
            }
        }

        if (this.tickCount >= LIFETIME) {
            this.discard();
        }
    }

    private void dealAreaDamage() {
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class,
            this.getBoundingBox().inflate(DAMAGE_RADIUS));

        this.level().playSound(null, this.getOnPos(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
        for (LivingEntity entity : entities) {
            this.hurt(entity, this.power, this.damageSources().magic());
            this.mobEffect(entity, MobEffects.MOVEMENT_SLOWDOWN, DAMAGE_INTERVAL, 1, false, true);
            if (this.level() instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 5; i++) {
                    serverLevel.sendParticles(ModParticleTypes.FROST_GLINT.get(),
                            this.getX() + (random.nextDouble() - 0.5) * this.getBbWidth() * 3,
                            this.getY() + random.nextDouble() * this.getBbHeight(),
                            this.getZ() + (random.nextDouble() - 0.5) * this.getBbWidth() * 3,
                            1, 0, 0, 0, 0.1);
                }
            }
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 16; i++) {
                serverLevel.sendParticles(ParticleTypes.FIREWORK,
                        this.getX() + (random.nextDouble() - 0.5) * this.getBbWidth() * 6,
                        this.getY() + random.nextDouble() * this.getBbHeight() * 2,
                        this.getZ() + (random.nextDouble() - 0.5) * this.getBbWidth() * 6,
                        1, 0, 0, 0, 0.1);
            }
        }
    }

    private void summonParticleRing() {
        for (int i = 0; i < 360; i += 15) {
            double angle = Math.toRadians(i);
            double xOffset = Math.cos(angle) * PARTICLE_RADIUS;
            double zOffset = Math.sin(angle) * PARTICLE_RADIUS;
            this.level().addParticle(ParticleTypes.SNOWFLAKE,
                this.getX() + xOffset, this.getY(), this.getZ() + zOffset, 0.0, 0.0, 0.0);
        }
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D);
    }
}