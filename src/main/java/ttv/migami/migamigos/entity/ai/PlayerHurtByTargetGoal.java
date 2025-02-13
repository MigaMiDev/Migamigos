package ttv.migami.migamigos.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import ttv.migami.migamigos.entity.AmigoEntity;

import java.util.EnumSet;

public class PlayerHurtByTargetGoal extends TargetGoal {
    private final AmigoEntity amigoEntity;
    private LivingEntity playerLastHurtBy;
    private int timestamp;

    public PlayerHurtByTargetGoal(AmigoEntity amigoEntity) {
        super(amigoEntity, false);
        this.amigoEntity = amigoEntity;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    public boolean canUse() {
        if (this.amigoEntity.hasPlayer()) {
            LivingEntity livingentity = this.amigoEntity.getPlayer();
            if (livingentity == null) {
                return false;
            } else {
                this.playerLastHurtBy = livingentity.getLastHurtByMob();
                if (this.playerLastHurtBy != null && this.playerLastHurtBy.isDeadOrDying()) {
                    return false;
                }

                if (!this.amigoEntity.isFocusingOnMainTarget() && this.amigoEntity.getTarget() != null) {
                    return false;
                }

                int i = livingentity.getLastHurtByMobTimestamp();
                return i != this.timestamp && this.canAttack(this.playerLastHurtBy, TargetingConditions.DEFAULT) && this.amigoEntity.wantsToAttack(this.playerLastHurtBy, livingentity);
            }
        } else {
            return false;
        }
    }

    public void start() {
        this.amigoEntity.setTarget(this.playerLastHurtBy);
        LivingEntity livingentity = this.amigoEntity.getPlayer();
        if (livingentity != null) {
            this.timestamp = livingentity.getLastHurtByMobTimestamp();
        }
        this.amigoEntity.setAttacking(true);
        super.start();
    }
}