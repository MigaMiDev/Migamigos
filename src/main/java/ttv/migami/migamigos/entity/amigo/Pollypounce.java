package ttv.migami.migamigos.entity.amigo;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import ttv.migami.migamigos.common.amigo.Action;
import ttv.migami.migamigos.common.network.ServerPlayHandler;
import ttv.migami.migamigos.entity.AmigoEntity;
import ttv.migami.migamigos.entity.ai.AmigoMeleeAttackGoal;
import ttv.migami.migamigos.init.ModParticleTypes;
import ttv.migami.migamigos.init.ModSounds;

import java.util.List;
import java.util.function.Consumer;

import static ttv.migami.migamigos.common.network.ServerPlayHandler.tryToHurt;

public class Pollypounce extends AmigoEntity {
    public Pollypounce(EntityType<? extends AmigoEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setDefaultItem(Items.IRON_SWORD);
        this.chime = ModSounds.WAVELYN_CHIME.get();
        this.setAmigoName("pollypounce");
    }

    @Override
    public void tick() {
        super.tick();

        // Particle Tick
        if (--this.particleTick > 0) {
            if (this.level() instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 2; i++) {
                    serverLevel.sendParticles(ParticleTypes.LAVA,
                            this.getX() + (random.nextDouble() - 0.5) * this.getBbWidth(),
                            this.getY() + random.nextDouble() * this.getBbHeight(),
                            this.getZ() + (random.nextDouble() - 0.5) * this.getBbWidth(),
                            6, 0.0, 0.0, 0.0, 0.125);
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
        List<Integer> keyframeTimings = List.of(4, 10, 24, 59);
        List<Consumer<AmigoEntity>> attackActions = List.of(
                attacker -> basicAttack(),
                attacker -> basicAttack(),
                attacker -> {
                    basicAttack();
                    this.invulnerableTime = 40;
                },
                attacker -> {
                    basicAttack();
                    this.invulnerableTime = 20;
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
                if (ServerPlayHandler.shouldHurt(this, entity)) {
                    entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20));
                    entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 4, false, false));
                    entity.setDeltaMovement(0, 0, 0);
                    entity.hasImpulse = false;
                }
            }
        }
        this.invulnerableTime = 10;

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
    }

    @Override
    public Action ultimateAction() {
        List<Integer> keyframeTimings = List.of(15, 25, 35, 45, 55, 65);
        List<Consumer<AmigoEntity>> attackActions = List.of(
                attacker -> ultimateAttack(),
                attacker -> ultimateAttack(),
                attacker -> ultimateAttack(),
                attacker -> ultimateAttack(),
                attacker -> ultimateAttack(),
                attacker -> ultimateAttack()

        );
        return new Action(keyframeTimings, attackActions);
    }

    @Override
    public void ultimateAttack() {
        this.invulnerableTime = 20;

        List<Entity> nearbyFoes = this.level().getEntities(this, this.getBoundingBox().inflate(3), e -> e != this && e instanceof Enemy);
        for (Entity entity : nearbyFoes) {
            if (entity instanceof Mob enemyEntity && ServerPlayHandler.shouldHurt(this, enemyEntity)) {
                if (entity.equals(this.getTarget())) {
                    tryToHurt(this, enemyEntity, this.damageSources().mobAttack(this), this.getAmigo().getAttackSpecial().getPower() + this.getExtraPower());
                } else {
                    tryToHurt(this, enemyEntity, this.damageSources().mobAttack(this), (this.getAmigo().getAttackSpecial().getPower() + this.getExtraPower()) / 2);
                }

                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ModParticleTypes.SPARK.get(), entity.getX(), entity.getY(), entity.getZ(), 8, entity.getBbWidth() / 2, entity.getBbHeight() / 2, entity.getBbWidth() / 2, 0.1);
                }
            }
        }

        this.level().playSound(null, this, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    public void remove(Entity.RemovalReason pReason) {
        super.remove(pReason);

        if (!this.isHeartless()) {
            Parrot polly = new Parrot(EntityType.PARROT, this.level());
            polly.setVariant(Parrot.Variant.RED_BLUE);
            polly.setCustomName(Component.literal("Captain Beakbeard"));
            polly.setPos(this.position().add(0, 1, 0));
            this.level().addFreshEntity(polly);
        }
    }
}
