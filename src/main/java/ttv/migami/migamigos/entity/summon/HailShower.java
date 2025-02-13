package ttv.migami.migamigos.entity.summon;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import ttv.migami.migamigos.entity.AmigoEntity;
import ttv.migami.migamigos.entity.amigo.Cocogoat;
import ttv.migami.migamigos.init.ModEntities;
import ttv.migami.migamigos.init.ModParticleTypes;

import java.util.List;

import static ttv.migami.migamigos.common.network.ServerPlayHandler.sendParticlesToAll;

public class HailShower extends SummonEntity {
    private static final int PARTICLE_RADIUS = 10;
    private static final int SPAWN_INTERVAL = 8;
    private static final int LIFETIME = 400;

    public HailShower(EntityType<? extends HailShower> entityType, Level level) {
        super(entityType, level);
    }

    public HailShower(Level level, LivingEntity owner, float power) {
        super(ModEntities.ICE_SHOWER.get(), level, owner, power);

        this.noPhysics = true;
        this.noCulling = true;
        this.setNoGravity(true);
        this.setNoAi(true);
        this.setSilent(true);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount % SPAWN_INTERVAL == 0 && this.tickCount > 20 && this.owner instanceof AmigoEntity amigoEntity) {
            spawnFallingIceCones(amigoEntity);
        }

        if (this.tickCount % 100 == 0) { // Every 5 seconds (20 ticks per second)
            applySlownessEffect();
        }

        if (this.tickCount > 20) {
            summonParticleRing();
            summonBlizzardParticles();
            if (this.level() instanceof ServerLevel serverLevel) {
                sendParticlesToAll(
                        serverLevel,
                        ModParticleTypes.FREEZE_BREEZE.get(),
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

    private void spawnFallingIceCones(AmigoEntity amigoEntity) {
        List<LivingEntity> enemies = findNearbyEnemies();
        Entity amigoTarget = getAmigoTarget();

        if (amigoEntity instanceof Cocogoat cocogoat) {
            for (int i = 0; i < 1; i++) {
                boolean shouldTarget = Math.random() < 0.5;

                if (shouldTarget && (!enemies.isEmpty() || amigoTarget != null)) {
                    LivingEntity target = !enemies.isEmpty()
                            ? enemies.get(this.level().random.nextInt(enemies.size()))
                            : (LivingEntity) amigoTarget;

                    cocogoat.summonIceCone(new Vec3(target.getX(), target.getY() + 10, target.getZ()));
                } else {
                    double angle = Math.random() * 2 * Math.PI;
                    double distance = Math.sqrt(Math.random()) * PARTICLE_RADIUS;
                    double xOffset = Math.cos(angle) * distance;
                    double zOffset = Math.sin(angle) * distance;

                    cocogoat.summonIceCone(new Vec3(this.getX() + xOffset, this.getY() + 10, this.getZ() + zOffset));
                }
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

    private Entity getAmigoTarget() {
        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class,
                new AABB(this.getX() - PARTICLE_RADIUS, this.getY(), this.getZ() - PARTICLE_RADIUS,
                        this.getX() + PARTICLE_RADIUS, this.getY() + 10, this.getZ() + PARTICLE_RADIUS))) {
            if (entity instanceof AmigoEntity amigoEntity && amigoEntity.getTarget() != null) {
                return amigoEntity.getTarget();
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