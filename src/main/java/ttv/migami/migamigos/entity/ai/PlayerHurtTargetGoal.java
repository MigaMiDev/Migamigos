package ttv.migami.migamigos.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import ttv.migami.migamigos.entity.Companion;

import java.util.EnumSet;

public class PlayerHurtTargetGoal extends TargetGoal {
    private final Companion companion;
    private LivingEntity ownerLastHurt;
    private int timestamp;

    public PlayerHurtTargetGoal(Companion companion) {
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
                this.ownerLastHurt = livingentity.getLastHurtMob();
                if (this.ownerLastHurt instanceof Companion) {
                    return false;
                }
                if (this.ownerLastHurt != null && this.ownerLastHurt.isDeadOrDying()) {
                    return false;
                }

                if (!this.companion.isFocusingOnMainTarget() && this.companion.getTarget() != null) {
                    return false;
                }

                int i = livingentity.getLastHurtMobTimestamp();
                return i != this.timestamp && this.canAttack(this.ownerLastHurt, TargetingConditions.DEFAULT) && this.companion.wantsToAttack(this.ownerLastHurt, livingentity);
            }
        } else {
            return false;
        }
    }

    public void start() {
        this.companion.setTarget(this.ownerLastHurt);
        LivingEntity livingentity = this.companion.getPlayer();
        if (livingentity != null) {
            this.timestamp = livingentity.getLastHurtMobTimestamp();
        }
        this.companion.setAttacking(true);

        super.start();
    }
}