package ttv.migami.migamigos.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.npc.Villager;
import ttv.migami.migamigos.entity.AmigoEntity;

import java.util.EnumSet;

public class PlayerHurtTargetGoal extends TargetGoal {
    private final AmigoEntity amigoEntity;
    private LivingEntity ownerLastHurt;
    private int timestamp;

    public PlayerHurtTargetGoal(AmigoEntity amigoEntity) {
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
                this.ownerLastHurt = livingentity.getLastHurtMob();
                if (this.ownerLastHurt instanceof TamableAnimal tamable) {
                    if (tamable.isTame() && tamable.getOwner() != null) {
                        return tamable.getOwner().getUUID() != livingentity.getUUID();
                    }
                }
                if (this.ownerLastHurt instanceof AmigoEntity) {
                    return false;
                }
                if (this.ownerLastHurt != null && this.ownerLastHurt.isDeadOrDying()) {
                    return false;
                }
                if (this.ownerLastHurt instanceof Villager) {
                    return false;
                }
                if (!this.amigoEntity.isFocusingOnMainTarget() && this.amigoEntity.getTarget() != null) {
                    return false;
                }

                int i = livingentity.getLastHurtMobTimestamp();
                return i != this.timestamp && this.canAttack(this.ownerLastHurt, TargetingConditions.DEFAULT) && this.amigoEntity.wantsToAttack(this.ownerLastHurt, livingentity);
            }
        } else {
            return false;
        }
    }

    public void start() {
        this.amigoEntity.setTarget(this.ownerLastHurt);
        LivingEntity livingentity = this.amigoEntity.getPlayer();
        if (livingentity != null) {
            this.timestamp = livingentity.getLastHurtMobTimestamp();
        }
        this.amigoEntity.setAttacking(true);

        super.start();
    }
}