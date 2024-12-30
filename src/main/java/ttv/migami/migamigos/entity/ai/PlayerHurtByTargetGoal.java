package ttv.migami.migamigos.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import ttv.migami.migamigos.entity.Companion;

import java.util.EnumSet;

public class PlayerHurtByTargetGoal extends TargetGoal {
    private final Companion companion;
    private LivingEntity playerLastHurtBy;
    private int timestamp;

    public PlayerHurtByTargetGoal(Companion companion) {
        super(companion, false);
        this.companion = companion;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    public boolean canUse() {
        if (this.companion.hasPlayer()) {
            LivingEntity livingentity = this.companion.getPlayer();
            if (livingentity == null) {
                return false;
            } else {
                this.playerLastHurtBy = livingentity.getLastHurtByMob();
                if (this.playerLastHurtBy != null && this.playerLastHurtBy.isDeadOrDying()) {
                    return false;
                }

                if (!this.companion.isFocusingOnMainTarget() && this.companion.getTarget() != null) {
                    return false;
                }

                int i = livingentity.getLastHurtByMobTimestamp();
                return i != this.timestamp && this.canAttack(this.playerLastHurtBy, TargetingConditions.DEFAULT) && this.companion.wantsToAttack(this.playerLastHurtBy, livingentity);
            }
        } else {
            return false;
        }
    }

    public void start() {
        this.companion.setTarget(this.playerLastHurtBy);
        LivingEntity livingentity = this.companion.getPlayer();
        if (livingentity != null) {
            this.timestamp = livingentity.getLastHurtByMobTimestamp();
        }
        this.companion.setAttacking(true);
        super.start();
    }
}