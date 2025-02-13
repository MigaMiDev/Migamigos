package ttv.migami.migamigos.entity.amigo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import ttv.migami.migamigos.common.amigo.Action;
import ttv.migami.migamigos.entity.AmigoEntity;
import ttv.migami.migamigos.entity.StunEntity;
import ttv.migami.migamigos.entity.ai.AmigoMeleeAttackGoal;
import ttv.migami.migamigos.entity.fx.GroundCracksEntity;
import ttv.migami.migamigos.init.ModSounds;

import java.util.List;
import java.util.function.Consumer;

import static ttv.migami.migamigos.common.network.ServerPlayHandler.tryToHurt;

public class Claymore extends AmigoEntity {
    public Claymore(EntityType<? extends AmigoEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setDefaultItem(Items.IRON_SWORD);
        this.chime = ModSounds.CLAYMORE_CHIME.get();
    }

    @Override
    public void tick() {
        super.tick();

        // Particle Tick
        if (--this.particleTick > 0) {
            if (this.level() instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 2; i++) {
                    serverLevel.sendParticles(ParticleTypes.FLAME,
                            this.getX() + (random.nextDouble() - 0.5) * this.getBbWidth(),
                            this.getY() + random.nextDouble() * this.getBbHeight(),
                            this.getZ() + (random.nextDouble() - 0.5) * this.getBbWidth(),
                            32, 0.0, 0.0, 0.0, 0.125);
                }
            }
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(5, new AmigoMeleeAttackGoal<>(this, 1.6));
    }

    @Override
    public Action basicAction() {
        List<Integer> keyframeTimings = List.of(3, 12, 30, 50);
        List<Consumer<AmigoEntity>> attackActions = List.of(
                attacker -> basicAttack(),
                attacker -> basicAttack(),
                attacker -> basicAttack(),
                attacker -> {
                    basicAttack();
                    this.invulnerableTime = 60;
                }
        );
        return new Action(keyframeTimings, attackActions);
    }

    @Override
    public void basicAttack() {
        if (this.isDeadOrDying()) return;

        Vec3 lookAngle = this.getLookAngle().normalize();

        double coneAngle = Math.toRadians(45);

        double attackRange = 3.0;
        AABB attackBox = this.getBoundingBox().inflate(1);
        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(attackRange),
                entity -> entity != this && this.hasLineOfSight(entity)
        );

        for (LivingEntity entity : nearbyEntities) {
            boolean isInBoundingBox = attackBox.intersects(entity.getBoundingBox());
            boolean isInCone = false;

            if (!isInBoundingBox) {
                Vec3 directionToEntity = entity.position().subtract(this.position()).normalize();
                double angle = lookAngle.dot(directionToEntity);
                isInCone = angle > Math.cos(coneAngle / 2);
            }

            if (isInBoundingBox || isInCone) {
                if (entity.equals(this.getTarget())) {
                    tryToHurt(this, entity, this.damageSources().mobAttack(this), this.getAmigo().getAttackCombo().getPower() + this.getExtraPower());
                } else {
                    tryToHurt(this, entity, this.damageSources().mobAttack(this), (this.getAmigo().getAttackCombo().getPower() + this.getExtraPower()) / 3);
                }

                /*Vec3 directionToEntity = entity.position().subtract(this.position()).normalize();
                Vec3 knockbackDirection = directionToEntity.scale(0.05);
                entity.setDeltaMovement(
                        entity.getDeltaMovement().add(knockbackDirection.x, knockbackDirection.y, knockbackDirection.z)
                );*/
            }
        }
        Vec3 direction = this.getLookAngle();
        Vec3 knockbackDirection = direction.scale(this.getAmigo().getAttackCombo().getRecoil());
        this.setDeltaMovement(knockbackDirection.x, knockbackDirection.y, knockbackDirection.z);
        this.hasImpulse = true;

        this.level().playSound(null, this, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    public Action specialAction() {
        List<Integer> keyframeTimings = List.of(10);
        List<Consumer<AmigoEntity>> attackActions = List.of(
                attacker -> specialAttack()
        );
        return new Action(keyframeTimings, attackActions);
    }

    @Override
    public void specialAttack() {
        if (this.isDeadOrDying()) return;

        pushEntitiesAway(this);
        this.particleTick = 3;

        summonLightingBolt();
        if (this.hasPlayer() && this.getPlayer() != null) {
            this.getPlayer().addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 150, 0));

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.FLAME, this.getPlayer().getX(), this.getPlayer().getY(), this.getPlayer().getZ(), 8, this.getPlayer().getBbWidth() / 2, this.getPlayer().getBbHeight() / 2, this.getPlayer().getBbWidth() / 2, 0.1);
            }
        }
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 150, 1));

        List<Entity> nearbyAmigos = this.level().getEntities(this, this.getBoundingBox().inflate(10), e -> e != this && e instanceof AmigoEntity);
        for (Entity entity : nearbyAmigos) {
            if (entity instanceof AmigoEntity amigoEntity) {
                amigoEntity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 150, 0));

                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.FLAME, entity.getX(), entity.getY(), entity.getZ(), 8, entity.getBbWidth() / 2, entity.getBbHeight() / 2, entity.getBbWidth() / 2, 0.1);
                }
            }
        }

        List<Entity> nearbyFoes = this.level().getEntities(this, this.getBoundingBox().inflate(10), e -> e != this && e instanceof Enemy);
        for (Entity entity : nearbyFoes) {
            if (entity instanceof Mob enemyEntity) {
                enemyEntity.setTarget(this);
                enemyEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));

                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.FLAME, entity.getX(), entity.getY(), entity.getZ(), 8, entity.getBbWidth() / 2, entity.getBbHeight() / 2, entity.getBbWidth() / 2, 0.1);
                }
            }
        }
    }

    public void summonLightingBolt() {
        LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, this.level());
        lightningBolt.setPos(this.getPosition(1F).add(0, 2, 0));
        lightningBolt.setVisualOnly(true);
        this.level().addFreshEntity(lightningBolt);
    }

    public void pushEntitiesAway(Claymore claymore) {
        List<Entity> nearbyEntities = claymore.level().getEntities(claymore, claymore.getBoundingBox().inflate(5), e -> e != claymore && e instanceof LivingEntity);

        for (Entity entity : nearbyEntities) {
            Vec3 direction = entity.position().subtract(this.position()).normalize();

            if (entity instanceof LivingEntity livingEntity && livingEntity != this.getPlayer()) {
                if (entity instanceof Enemy || entity.equals(this.getTarget())) {
                    livingEntity.hurtMarked = true;
                    livingEntity.push(direction.x * 1, direction.y * 1 + 0.1, direction.z * 1);
                }
            }
        }
    }

    @Override
    public Action ultimateAction() {
        List<Integer> keyframeTimings = List.of(10);
        List<Consumer<AmigoEntity>> attackActions = List.of(
                attacker -> ultimateAttack()
        );
        return new Action(keyframeTimings, attackActions);
    }

    @Override
    public void ultimateAttack() {
        if (this.isDeadOrDying()) return;

        groundEntities(this);
        this.particleTick = 3;
        this.invulnerableTime = 140;

        List<Entity> nearbyFoes = this.level().getEntities(this, this.getBoundingBox().inflate(5), e -> e != this && e instanceof Enemy);
        for (Entity entity : nearbyFoes) {
            if (entity instanceof Mob enemyEntity) {
                enemyEntity.setTarget(this);
                enemyEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));

                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.FLAME, entity.getX(), entity.getY(), entity.getZ(), 8, entity.getBbWidth() / 2, entity.getBbHeight() / 2, entity.getBbWidth() / 2, 0.1);
                }
            }
        }

        GroundCracksEntity groundCracks = new GroundCracksEntity(this.level(), this.getOnPos(), 200, 6);
        groundCracks.setPos(this.getPosition(1F));
        this.level().addFreshEntity(groundCracks);

        this.level().playSound(null, this, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public void groundEntities(Claymore claymore) {
        List<Entity> nearbyEntities = claymore.level().getEntities(claymore, claymore.getBoundingBox().inflate(5), e -> e != claymore && e instanceof LivingEntity);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity livingEntity && livingEntity != this.getPlayer()) {
                if (entity instanceof Enemy || entity.equals(this.getTarget())) {
                    livingEntity.hurtMarked = true;
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 4));
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 4));

                    StunEntity stunEntity = new StunEntity(this, this.level(), entity.getPosition(1F), this.getAmigo().getAttackUltimate().getPower() + this.getExtraPower(), 120);
                    stunEntity.moveTo(livingEntity.getPosition(1F).add(0, -0.9, 0));
                    entity.startRiding(stunEntity);
                    this.level().addFreshEntity(stunEntity);

                    if (this.level() instanceof ServerLevel serverLevel) {
                        BlockPos entityPos = stunEntity.blockPosition();
                        BlockPos belowPos = entityPos.below();
                        BlockState blockStateBelow = serverLevel.getBlockState(belowPos);

                        serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, this.level().getBlockState(belowPos)), belowPos.getX(), belowPos.getY() + 2, belowPos.getZ(), 16, 1.0, 0.0, 1.0, 0.0);
                        serverLevel.playSound(null, belowPos, blockStateBelow.getSoundType().getBreakSound(), SoundSource.BLOCKS, 2.0F, 1.0F);
                        serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), ((int) this.getAmigo().getAttackUltimate().getPower() / 2), livingEntity.getBbWidth() / 2, livingEntity.getBbHeight() / 2, livingEntity.getBbWidth() / 2, 0.1);
                    }
                }
            }
        }
    }
}
