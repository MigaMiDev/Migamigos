package ttv.migami.migamigos.entity.ai;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import ttv.migami.migamigos.entity.Companion;

import java.util.EnumSet;
import java.util.List;

public class CompanionRangedAttackGoal<T extends Mob & RangedAttackMob> extends Goal {
    private static final int ULTIMATE_COOLDOWN = 2400;
    private static final int SPECIAL_COOLDOWN = 400;

    private final Companion mob;
    private final double speedModifier;
    private int seeTime;

    public CompanionRangedAttackGoal(Companion mob, double speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.mob.hasPlayer() && this.mob.getPlayer() != null && this.mob.distanceToSqr(this.mob.getPlayer()) >= 720.0) {
            return false;
        }
        if (this.mob.isContainerOpen()) {
            return false;
        }
        return this.mob.getTarget() != null && !this.mob.getTarget().isDeadOrDying();
    }

    @Override
    public boolean canContinueToUse() {
        return (this.canUse());
    }

    @Override
    public void start() {
        super.start();
        this.mob.setAggressive(true);
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.setAggressive(false);
        this.seeTime = 0;
        this.mob.stopUsingItem();
        this.mob.setAttacking(false);
        this.mob.setSpecialAttacking(false);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target != null) {
            double distanceToTarget = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
            boolean canSeeTarget = this.mob.getSensing().hasLineOfSight(target);
            boolean sawTargetPreviously = this.seeTime > 0;

            if (canSeeTarget != sawTargetPreviously) {
                this.seeTime = 0;
            }

            if (canSeeTarget) {
                ++this.seeTime;
            } else {
                --this.seeTime;
            }

            // Ultimate Attack
            if (this.mob.getUltimateCooldown() <= 0 && !this.mob.isSpecialAttacking()) {
                List<Entity> nearbyEntities = this.mob.level().getEntities(this.mob, this.mob.getBoundingBox().inflate(6));
                long enemyCount = nearbyEntities.stream().filter(e -> e instanceof Enemy).count();
                if (enemyCount >= 5) {
                    this.mob.setUltimateAttacking(true);
                    this.mob.setSpecialAttacking(false);
                    this.mob.setUltimateCooldown(ULTIMATE_COOLDOWN);
                }
            }

            // Special Attack
            if (this.mob.getSpecialCooldown() <= 0 && !this.mob.isUltimateAttacking()) {
                if (distanceToTarget <= 3 * 3) {
                    this.mob.setSpecialAttacking(true);
                    this.mob.setUltimateAttacking(false);
                    this.mob.setSpecialCooldown(SPECIAL_COOLDOWN);
                }
            }

            // Normal/Combo Attack
            if (distanceToTarget > 17 * 17) {
                this.mob.setAttacking(false);
                this.mob.setSpecialAttacking(false);
                this.mob.setUltimateAttacking(false);
                this.mob.getNavigation().moveTo(target, this.speedModifier);
            } else if (distanceToTarget <= 7 * 7) {
                this.mob.setAttacking(true);
                this.mob.getNavigation().stop();
            } else if (!this.mob.isAttacking()) {
                this.mob.getNavigation().moveTo(target, this.speedModifier);
            }

            double targetEyeY = target.getEyeY();
            this.mob.getLookControl().setLookAt(target.getX(), targetEyeY, target.getZ());
            this.mob.lookAt(EntityAnchorArgument.Anchor.FEET, target.getBoundingBox().getCenter());
        }
    }
}