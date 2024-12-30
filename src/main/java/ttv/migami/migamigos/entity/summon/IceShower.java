package ttv.migami.migamigos.entity.summon;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import ttv.migami.migamigos.entity.Companion;
import ttv.migami.migamigos.init.ModEntities;
import ttv.migami.migamigos.init.ModParticleTypes;

import java.util.List;

public class IceShower extends SummonEntity {
    private static final int PARTICLE_RADIUS = 10;
    private static final int SPAWN_INTERVAL = 8;
    private static final int LIFETIME = 400;

    public IceShower(EntityType<? extends IceShower> entityType, Level level) {
        super(entityType, level);
    }

    public IceShower(Level level, LivingEntity owner) {
        super(ModEntities.ICE_SHOWER.get(), level, owner);

        this.noPhysics = true;
        this.noCulling = true;
        this.setNoGravity(true);
        this.setNoAi(true);
        this.setSilent(true);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount % SPAWN_INTERVAL == 0 && this.tickCount > 20) {
            spawnFallingArrows();
        }

        if (this.tickCount % 100 == 0) { // Every 5 seconds (20 ticks per second)
            applySlownessEffect();
        }

        if (this.tickCount > 20) {
            summonParticleRing();
            summonBlizzardParticles();
        }

        if (this.tickCount >= LIFETIME) {
            this.discard();
        }
    }

    private void spawnFallingArrows() {
        List<LivingEntity> enemies = findNearbyEnemies();
        Entity companionTarget = getCompanionTarget();

        for (int i = 0; i < 1; i++) {
            boolean shouldTarget = Math.random() < 0.5;

            if (shouldTarget && (!enemies.isEmpty() || companionTarget != null)) {
                LivingEntity target = !enemies.isEmpty()
                        ? enemies.get(this.level().random.nextInt(enemies.size()))
                        : (LivingEntity) companionTarget;

                Snowball projectile = new Snowball(this.level(), target.getX(), target.getY() + 10, target.getZ());
                projectile.setDeltaMovement(0, -1.0, 0);
                this.level().addFreshEntity(projectile);
            } else {
                double angle = Math.random() * 2 * Math.PI;
                double distance = Math.sqrt(Math.random()) * PARTICLE_RADIUS;
                double xOffset = Math.cos(angle) * distance;
                double zOffset = Math.sin(angle) * distance;

                Snowball projectile = new Snowball(this.level(), this.getX() + xOffset, this.getY() + 10, this.getZ() + zOffset);
                projectile.setDeltaMovement(0, -1.0, 0);
                this.level().addFreshEntity(projectile);
            }
        }
    }

    private void applySlownessEffect() {
        List<LivingEntity> affectedEntities = findNearbyEnemies();

        for (LivingEntity entity : affectedEntities) {
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 140, 1));
        }
    }

    private List<LivingEntity> findNearbyEnemies() {
        return this.level().getEntitiesOfClass(LivingEntity.class,
                new AABB(this.getX() - PARTICLE_RADIUS, this.getY(), this.getZ() - PARTICLE_RADIUS,
                        this.getX() + PARTICLE_RADIUS, this.getY() + 10, this.getZ() + PARTICLE_RADIUS),
                entity -> entity.isAlive() && entity instanceof Enemy);
    }

    private Entity getCompanionTarget() {
        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class,
                new AABB(this.getX() - PARTICLE_RADIUS, this.getY(), this.getZ() - PARTICLE_RADIUS,
                        this.getX() + PARTICLE_RADIUS, this.getY() + 10, this.getZ() + PARTICLE_RADIUS))) {
            if (entity instanceof Companion companion && companion.getTarget() != null) {
                return companion.getTarget();
            }
        }
        return null;
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

    private void summonBlizzardParticles() {
        double blizzardSpeedX = 0.2;
        int snowFlakeDensity = 7;
        int frostDensity = 1;

        for (int i = 0; i < snowFlakeDensity; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double distance = Math.sqrt(Math.random()) * PARTICLE_RADIUS;
            double xOffset = Math.cos(angle) * distance;
            double zOffset = Math.sin(angle) * distance;

            double yOffset = Math.random() * 6;

            this.level().addParticle(
                    ParticleTypes.SNOWFLAKE,
                    this.getX() + xOffset, this.getY() + yOffset, this.getZ() + zOffset,
                    blizzardSpeedX, 0.0, 0.0
            );
        }

        for (int i = 0; i < frostDensity; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double distance = Math.sqrt(Math.random()) * PARTICLE_RADIUS;
            double xOffset = Math.cos(angle) * distance;
            double zOffset = Math.sin(angle) * distance;

            double yOffset = Math.random() * 4;

            this.level().addParticle(
                    ModParticleTypes.FROST_GLINT.get(),
                    this.getX() + xOffset, this.getY() + yOffset, this.getZ() + zOffset,
                    blizzardSpeedX, 0.0, 0.0
            );
        }
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D);
    }
}