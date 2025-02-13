package ttv.migami.migamigos.entity.amigo;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import ttv.migami.migamigos.common.amigo.Action;
import ttv.migami.migamigos.entity.AmigoEntity;
import ttv.migami.migamigos.entity.ai.AmigoRangedAttackGoal;
import ttv.migami.migamigos.entity.fx.ScorchMarkEntity;
import ttv.migami.migamigos.entity.projectile.wavelyn.SoulFireball;
import ttv.migami.migamigos.init.ModSounds;
import ttv.migami.migamigos.init.ModTags;

import java.util.List;
import java.util.function.Consumer;

public class Wavelyn extends AmigoEntity {
    public Wavelyn(EntityType<? extends AmigoEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setDefaultItem(Items.ENCHANTED_BOOK);
        this.chime = ModSounds.WAVELYN_CHIME.get();
    }

    @Override
    public void tick() {
        super.tick();

        // Particle Tick
        if (--this.particleTick > 0) {
            if (this.level() instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 2; i++) {
                    serverLevel.sendParticles(ParticleTypes.FIREWORK,
                            this.getX() + (random.nextDouble() - 0.5) * this.getBbWidth(),
                            this.getY() + random.nextDouble() * this.getBbHeight(),
                            this.getZ() + (random.nextDouble() - 0.5) * this.getBbWidth(),
                            64, 0.0, 0.0, 0.0, 0.25);
                }
            }
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(5, new AmigoRangedAttackGoal<>(this, 1.6));
    }

    @Override
    public Action basicAction() {
        List<Integer> keyframeTimings = List.of(10, 30, 55);
        List<Consumer<AmigoEntity>> attackActions = List.of(
                attacker -> basicAttack(),
                attacker -> basicAttack(),
                attacker -> basicAttack()
        );
        return new Action(keyframeTimings, attackActions);
    }

    @Override
    public void basicAttack() {
        if (this.isDeadOrDying()) return;

        LivingEntity target = this.getTarget();
        if (target == null) return;

        this.setTarget(target);

        Vec3 direction = target.position().subtract(this.position()).normalize();
        SoulFireball soulFireball = new SoulFireball(this.level(), this, this.getAmigo().getAttackCombo().getPower() + this.getExtraPower(), 1.5F);
        //soulFireball.setPos(soulFireball.getPosition(1F).add(0, -0.7, 0));
        this.level().playSound(null, this, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
        this.level().addFreshEntity(soulFireball);

        Vec3 knockbackDirection = direction.scale(this.getAmigo().getAttackCombo().getRecoil());
        this.setDeltaMovement(knockbackDirection.x, knockbackDirection.y, knockbackDirection.z);
        this.hasImpulse = true;
    }

    @Override
    public Action specialAction() {
        List<Integer> keyframeTimings = List.of(20);
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

        if (this.hasPlayer() && this.getPlayer() != null) {
            this.getPlayer().addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HEART, this.getPlayer().getX(), this.getPlayer().getY(), this.getPlayer().getZ(), 8, this.getPlayer().getBbWidth() / 2, this.getPlayer().getBbHeight() / 2, this.getPlayer().getBbWidth() / 2, 0.1);
            }
        }
        this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));

        List<Entity> nearbyAmigos = this.level().getEntities(this, this.getBoundingBox().inflate(10), e -> e != this && e instanceof AmigoEntity);
        for (Entity entity : nearbyAmigos) {
            if (entity instanceof AmigoEntity amigoEntity) {
                amigoEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));

                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.HEART, entity.getX(), entity.getY(), entity.getZ(), 8, entity.getBbWidth() / 2, entity.getBbHeight() / 2, entity.getBbWidth() / 2, 0.1);
                }
            }
        }

        this.level().playSound(null, this, SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public void pushEntitiesAway(Wavelyn wavelyn) {
        List<Entity> nearbyEntities = wavelyn.level().getEntities(wavelyn, wavelyn.getBoundingBox().inflate(5), e -> e != wavelyn && e instanceof LivingEntity);

        for (Entity entity : nearbyEntities) {
            Vec3 direction = entity.position().subtract(this.position()).normalize();

            if (entity instanceof LivingEntity livingEntity && livingEntity != this.getPlayer()) {
                if (entity instanceof Enemy || entity.equals(this.getTarget())) {
                    livingEntity.hurtMarked = true;
                    livingEntity.push(direction.x * 2, direction.y * 3 + 1, direction.z * 2);
                }
            }
        }
    }

    @Override
    public Action ultimateAction() {
        List<Integer> keyframeTimings = List.of(20);
        List<Consumer<AmigoEntity>> attackActions = List.of(
                attacker -> ultimateAttack()
        );
        return new Action(keyframeTimings, attackActions);
    }

    @Override
    public void ultimateAttack() {
        if (this.isDeadOrDying()) return;

        this.invulnerableTime = 60;

        AABB lightingArea = this.getBoundingBox().inflate(10D);
        List<LivingEntity> allEntities = this.level().getEntitiesOfClass(LivingEntity.class, lightingArea);

        for (LivingEntity entity : allEntities) {
            if (entity.equals(this.getTarget())) {
                summonLightingBolt(entity);
            } else if (!(entity instanceof Player) && entity instanceof Enemy) {
                //if (this.level().random.nextInt(1) == 0) {
                if (this.level().random.nextBoolean()) {
                    summonLightingBolt(entity);
                }
            }
        }

        if (this.hasPlayer() && this.getPlayer() != null) {
            this.getPlayer().heal(this.getPlayer().getMaxHealth());
            this.getPlayer().addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 1));

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HEART, this.getPlayer().getX(), this.getPlayer().getY(), this.getPlayer().getZ(), 8, this.getPlayer().getBbWidth() / 2, this.getPlayer().getBbHeight() / 2, this.getPlayer().getBbWidth() / 2, 0.1);
            }
        }
        this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 2));
        this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 1));

        List<Entity> nearbyAmigos = this.level().getEntities(this, this.getBoundingBox().inflate(5), e -> e != this && e instanceof AmigoEntity);
        for (Entity entity : nearbyAmigos) {
            if (entity instanceof AmigoEntity amigoEntity) {
                amigoEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 2));
                amigoEntity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 1));

                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.HEART, entity.getX(), entity.getY(), entity.getZ(), 8, entity.getBbWidth() / 2, entity.getBbHeight() / 2, entity.getBbWidth() / 2, 0.1);
                }
            }
        }

        Vec3 lookAngle = this.getLookAngle();
        Vec3 knockbackDirection = lookAngle.scale(this.getAmigo().getAttackUltimate().getRecoil());
        this.setDeltaMovement(knockbackDirection.x, 0.1, knockbackDirection.z);
        this.hasImpulse = true;
        this.particleTick = 3;
    }

    public void summonLightingBolt(LivingEntity livingEntity) {
        LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, this.level());
        lightningBolt.setPos(livingEntity.getPosition(1F));
        lightningBolt.setVisualOnly(true);
        if (livingEntity.getType().is(ModTags.Entities.UNDEAD)) {
            livingEntity.setSecondsOnFire((int) ((int) this.getAmigo().getAttackUltimate().getPower() + this.getExtraPower() / 2));
        }
        livingEntity.hurt(this.damageSources().magic(), this.getAmigo().getAttackUltimate().getPower() + this.getExtraPower());
        this.level().addFreshEntity(lightningBolt);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), ((int) this.getAmigo().getAttackCombo().getPower() / 2), livingEntity.getBbWidth() / 2, livingEntity.getBbHeight() / 2, livingEntity.getBbWidth() / 2, 0.1);
        }

        ScorchMarkEntity scorchMark = new ScorchMarkEntity(this.level(), livingEntity.getOnPos(), 200, 6);
        scorchMark.setPos(livingEntity.getPosition(1F));
        this.level().addFreshEntity(scorchMark);
    }
}
