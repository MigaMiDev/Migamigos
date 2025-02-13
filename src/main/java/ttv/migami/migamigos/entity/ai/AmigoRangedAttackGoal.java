package ttv.migami.migamigos.entity.ai;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import ttv.migami.migamigos.entity.AmigoEntity;

import java.util.EnumSet;
import java.util.List;

public class AmigoRangedAttackGoal<T extends Mob & RangedAttackMob> extends Goal {
    private int specialCooldown = 400;
    private int ultimateCooldown = 2400;
    private int comboCooldown = 100;

    private final AmigoEntity amigo;
    private final double speedModifier;
    private int seeTime;

    public AmigoRangedAttackGoal(AmigoEntity amigo, double speedModifier) {
        this.amigo = amigo;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.amigo.hasPlayer() && this.amigo.getPlayer() != null && this.amigo.distanceToSqr(this.amigo.getPlayer()) >= 720.0) {
            return false;
        }
        if (this.amigo.isContainerOpen()) {
            return false;
        }
        if (this.amigo.isEating() || this.amigo.isEmoting()) {
            return false;
        }
        return this.amigo.getTarget() != null && !this.amigo.getTarget().isDeadOrDying();
    }

    @Override
    public boolean canContinueToUse() {
        return (this.canUse());
    }

    @Override
    public void start() {
        super.start();
        this.comboCooldown = this.amigo.getAmigo().getAttackCombo().getCooldown();
        this.specialCooldown = this.amigo.getAmigo().getAttackSpecial().getCooldown();
        this.ultimateCooldown = this.amigo.getAmigo().getAttackUltimate().getCooldown();
        this.amigo.setIsFarming(false);
        this.amigo.setAggressive(true);
    }

    @Override
    public void stop() {
        super.stop();
        this.amigo.setAggressive(false);
        this.seeTime = 0;
        this.amigo.stopUsingItem();
        this.amigo.setAttacking(false);
        this.amigo.setSpecialAttacking(false);
        this.amigo.setUltimateAttacking(false);
        this.amigo.setComboAttacking(false);
        this.amigo.setComboCooldown(20);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.amigo.getTarget();
        if (target != null) {
            double distanceToTarget = this.amigo.distanceToSqr(target.getX(), target.getY(), target.getZ());
            boolean canSeeTarget = this.amigo.getSensing().hasLineOfSight(target);
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
            if (this.amigo.getUltimateCooldown() <= 0 && !this.amigo.isSpecialAttacking() && !this.amigo.isComboAttacking()) {
                List<Entity> nearbyEntities = this.amigo.level().getEntities(this.amigo, this.amigo.getBoundingBox().inflate(6));
                long enemyCount = nearbyEntities.stream().filter(e -> e instanceof Enemy).count();
                if (enemyCount >= 5) {
                    this.amigo.setPlayingAnimation(true);
                    this.amigo.setAttacking(true);
                    this.amigo.setUltimateAttacking(true);
                    this.amigo.setSpecialAttacking(false);
                    this.amigo.setComboAttacking(false);
                    this.amigo.setUltimateCooldown(this.ultimateCooldown);
                    this.amigo.startAction(this.amigo.ultimateAction());
                    this.amigo.getNavigation().stop();
                }
            }

            // Special Attack
            if (this.amigo.getSpecialCooldown() <= 0 && !this.amigo.isUltimateAttacking() && !this.amigo.isComboAttacking()) {
                if (distanceToTarget <= 3 * 3) {
                    this.amigo.setPlayingAnimation(true);
                    this.amigo.setAttacking(true);
                    this.amigo.setSpecialAttacking(true);
                    this.amigo.setUltimateAttacking(false);
                    this.amigo.setComboAttacking(false);
                    this.amigo.setSpecialCooldown(this.specialCooldown);
                    this.amigo.startAction(this.amigo.specialAction());
                    this.amigo.getNavigation().stop();
                }
            }

            // Normal/Combo Attack
            if (distanceToTarget > 17 * 17) {
                this.amigo.setAttacking(false);
                this.amigo.setSpecialAttacking(false);
                this.amigo.setUltimateAttacking(false);
                this.amigo.setComboAttacking(false);
                this.amigo.getNavigation().moveTo(target, this.speedModifier);
            } else if (!this.amigo.isSpecialAttacking() &&
                    !this.amigo.isUltimateAttacking() &&
                    this.amigo.getComboCooldown() <= 0 &&
                    distanceToTarget <= 7 * 7) {
                this.amigo.setPlayingAnimation(true);
                this.amigo.setAttacking(true);
                this.amigo.setComboAttacking(true);
                this.amigo.getNavigation().stop();
                this.amigo.setComboCooldown(this.comboCooldown);
                this.amigo.startAction(this.amigo.basicAction());
            } else if (!this.amigo.isAttacking()) {
                this.amigo.getNavigation().moveTo(target, this.speedModifier);
            }

            double targetEyeY = target.getEyeY();
            this.amigo.getLookControl().setLookAt(target.getX(), targetEyeY, target.getZ());
            this.amigo.lookAt(EntityAnchorArgument.Anchor.FEET, target.getBoundingBox().getCenter());
        }
    }
}