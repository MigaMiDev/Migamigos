package ttv.migami.migamigos.entity.ai;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.GameRules;
import ttv.migami.migamigos.entity.AmigoEntity;

import java.util.EnumSet;
import java.util.Random;

public class AmigoHurtByPlayerGoal extends TargetGoal {
    private static final TargetingConditions HURT_BY_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
    private final Class<?>[] toIgnoreDamage;

    public AmigoHurtByPlayerGoal(PathfinderMob pMob, Class<?>... pToIgnoreDamage) {
        super(pMob, true);
        this.toIgnoreDamage = pToIgnoreDamage;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    public boolean canUse() {
        LivingEntity livingentity = this.mob.getLastHurtByMob();

        if (this.mob instanceof AmigoEntity amigoEntity && livingentity != amigoEntity.getPlayer()) {
            return false;
        }

        if (livingentity != null) {
            if (livingentity.getType() == EntityType.PLAYER && this.mob.level().getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                return false;
            } else {
                Class[] var3 = this.toIgnoreDamage;
                int var4 = var3.length;

                for(int var5 = 0; var5 < var4; ++var5) {
                    Class<?> oclass = var3[var5];
                    if (oclass.isAssignableFrom(livingentity.getClass())) {
                        return false;
                    }
                }

                return this.canAttack(livingentity, HURT_BY_TARGETING);
            }
        } else {
            return false;
        }
    }

    public void start() {
        LivingEntity livingentity = this.mob.getLastHurtByMob();
        Random random = new Random();

        if (this.mob instanceof AmigoEntity amigoEntity && livingentity == amigoEntity.getPlayer()) {
            amigoEntity.setTolerance(amigoEntity.getTolerance() - 1);

            if (this.mob.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                        this.mob.getX() + (random.nextDouble() - 0.5) * this.mob.getBbWidth(),
                        this.mob.getY() + random.nextDouble() * this.mob.getBbHeight(),
                        this.mob.getZ() + (random.nextDouble() - 0.5) * this.mob.getBbWidth(),
                        1, 0, 0, 0, 0.1);
            }

            if (amigoEntity.getTolerance() <= 0) {
                if (this.mob.level() instanceof ServerLevel serverLevel) {
                    for (int i = 0; i < 7; i++) {
                        serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                                this.mob.getX() + (random.nextDouble() - 0.5) * this.mob.getBbWidth(),
                                this.mob.getY() + random.nextDouble() * this.mob.getBbHeight(),
                                this.mob.getZ() + (random.nextDouble() - 0.5) * this.mob.getBbWidth(),
                                1, 0, 0, 0, 0.1);
                    }
                }

                amigoEntity.clearPlayer();
                amigoEntity.setTarget(livingentity);
            } else if (amigoEntity.getTolerance() > 0) {
                this.mob.setLastHurtByMob(null);
                this.stop();
                return;
            }
        }

        super.start();
    }
}