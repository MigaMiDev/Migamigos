package ttv.migami.migamigos.entity.ai;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import ttv.migami.migamigos.common.network.ServerPlayHandler;
import ttv.migami.migamigos.entity.AmigoEntity;
import ttv.migami.migamigos.entity.AmigoState;

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
        if (this.amigo.hasEffect(MobEffects.WEAKNESS)) return false;
        if (this.amigo.getTarget() == null) {
            return false;
        }
        if (this.amigo.hasPlayer() && this.amigo.getPlayer() != null && this.amigo.distanceToSqr(this.amigo.getPlayer()) >= 720.0) {
            return false;
        }
        if (this.amigo.isContainerOpen()) {
            return false;
        }
        if (this.amigo.isPassenger()) {
            return false;
        }
        if (this.amigo.getTarget().isDeadOrDying()) {
            return false;
        }
        return this.amigo.getTarget() != null;
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
        this.amigo.setAmigoState(AmigoState.IDLE);
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

            AABB attackRange = this.amigo.getBoundingBox().inflate(8.0);
            AABB specialRange = this.amigo.getBoundingBox().inflate(1.0);

            List<Entity> nearbyEntities = this.amigo.level().getEntities(this.amigo, this.amigo.getBoundingBox().inflate(6));
            long enemyCount = nearbyEntities.stream().filter(e ->
                    e instanceof LivingEntity livingEntity && ServerPlayHandler.shouldHurt(this.amigo, livingEntity)
                    ).count();
            if (this.amigo.isHeartless() || this.amigo.isEnemigo()) {
                nearbyEntities.stream().filter(e ->
                        e instanceof Player ||
                                (e instanceof AmigoEntity amigoEntity1 && (!amigoEntity1.isHeartless() && !amigoEntity1.isEnemigo()))
                ).count();
            }

            boolean ultimateConditions = (enemyCount >= 5 || this.amigo.getTarget().getHealth() >= this.amigo.getHealth() || this.amigo.getHealth() <= this.amigo.getMaxHealth() / 3);

            if (this.amigo.getAmigo().getGeneral().hasUltimate() && ultimateConditions && !this.amigo.isHeartless() &&
                    this.amigo.getUltimateCooldown() <= 0 &&
                    !this.amigo.getAmigoState().equals(AmigoState.COMBO_ATTACKING) &&
                    !this.amigo.getAmigoState().equals(AmigoState.SPECIAL_ATTACKING)) {
                {
                    this.amigo.getNavigation().stop();
                    this.amigo.setAmigoState(AmigoState.ULTIMATE_ATTACKING);
                    this.amigo.setUltimateCooldown(this.ultimateCooldown);
                    this.amigo.startAttacking(this.amigo.ultimateAction());
                }
            }
            else if (this.amigo.getAmigo().getGeneral().hasSpecial() && specialRange.intersects(this.amigo.getTarget().getBoundingBox()) && !this.amigo.isHeartless() &&
                    this.amigo.getSpecialCooldown() <= 0 &&
                    !this.amigo.getAmigoState().equals(AmigoState.COMBO_ATTACKING) &&
                    !this.amigo.getAmigoState().equals(AmigoState.ULTIMATE_ATTACKING)) {
                {
                    this.amigo.getNavigation().stop();
                    this.amigo.setAmigoState(AmigoState.SPECIAL_ATTACKING);
                    this.amigo.setSpecialCooldown(this.specialCooldown);
                    this.amigo.startAttacking(this.amigo.specialAction());
                }
            }
            else if (attackRange.intersects(this.amigo.getTarget().getBoundingBox()) &&
                    this.amigo.getComboCooldown() <= 0 &&
                    !this.amigo.getAmigoState().equals(AmigoState.SPECIAL_ATTACKING) &&
                    !this.amigo.getAmigoState().equals(AmigoState.ULTIMATE_ATTACKING)) {
                {
                    this.amigo.getNavigation().stop();
                    this.amigo.setAmigoState(AmigoState.COMBO_ATTACKING);
                    this.amigo.setComboCooldown(this.comboCooldown);
                    this.amigo.startAttacking(this.amigo.basicAction());
                }
            }
            else if ((!attackRange.intersects(this.amigo.getTarget().getBoundingBox()) && !specialRange.intersects(this.amigo.getTarget().getBoundingBox())) ||
                    (!this.amigo.getAmigoState().equals(AmigoState.COMBO_ATTACKING) &&
                            !this.amigo.getAmigoState().equals(AmigoState.SPECIAL_ATTACKING) &&
                            !this.amigo.getAmigoState().equals(AmigoState.ULTIMATE_ATTACKING))) {
                this.amigo.stopAttacks();
                this.amigo.getNavigation().moveTo(target, this.speedModifier);
            }

            double targetEyeY = target.getEyeY();
            this.amigo.getLookControl().setLookAt(target.getX(), targetEyeY, target.getZ());
            this.amigo.lookAt(EntityAnchorArgument.Anchor.FEET, target.getBoundingBox().getCenter());
        }
    }
}