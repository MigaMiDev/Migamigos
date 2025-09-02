package ttv.migami.migamigos.entity.ai;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import ttv.migami.migamigos.common.network.ServerPlayHandler;
import ttv.migami.migamigos.entity.AmigoEmotes;
import ttv.migami.migamigos.entity.AmigoEntity;
import ttv.migami.migamigos.entity.AmigoState;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class AmigoLookAtPlayerGoal extends Goal {
    public static final float DEFAULT_PROBABILITY = 0.02F;
    protected final AmigoEntity amigo;
    @Nullable
    protected Entity lookAt;
    protected final float lookDistance;
    private int lookTime;
    protected final float probability;
    private final boolean onlyHorizontal;
    protected final Class<? extends LivingEntity> lookAtType;
    protected final TargetingConditions lookAtContext;
    private boolean emote = true;

    public AmigoLookAtPlayerGoal(AmigoEntity pMob, Class<? extends LivingEntity> pLookAtType, float pLookDistance) {
        this(pMob, pLookAtType, pLookDistance, 0.02F);
    }

    public AmigoLookAtPlayerGoal(AmigoEntity pMob, Class<? extends LivingEntity> pLookAtType, float pLookDistance, float pProbability) {
        this(pMob, pLookAtType, pLookDistance, pProbability, false);
    }

    public AmigoLookAtPlayerGoal(AmigoEntity pMob, Class<? extends LivingEntity> pLookAtType, float pLookDistance, float pProbability, boolean pOnlyHorizontal) {
        this.amigo = pMob;
        this.lookAtType = pLookAtType;
        this.lookDistance = pLookDistance;
        this.probability = pProbability;
        this.onlyHorizontal = pOnlyHorizontal;
        this.setFlags(EnumSet.of(Flag.LOOK));
        if (pLookAtType == Player.class) {
            this.lookAtContext = TargetingConditions.forNonCombat().range((double)pLookDistance).selector((p_25531_) -> {
                return EntitySelector.notRiding(pMob).test(p_25531_);
            });
        } else {
            this.lookAtContext = TargetingConditions.forNonCombat().range((double)pLookDistance);
        }

    }

    public boolean canUse() {
        if (this.amigo.isAttacking() || this.amigo.isAggressive()) {
            return false;
        }
        if (this.amigo.getRandom().nextFloat() >= this.probability) {
            return false;
        } else {
            this.emote = true;
            if (this.amigo.getTarget() != null) {
                this.lookAt = this.amigo.getTarget();
            }

            if (this.lookAtType == Player.class) {
                this.lookAt = this.amigo.level().getNearestPlayer(this.lookAtContext, this.amigo, this.amigo.getX(), this.amigo.getEyeY(), this.amigo.getZ());
            } else {
                this.lookAt = this.amigo.level().getNearestEntity(this.amigo.level().getEntitiesOfClass(this.lookAtType, this.amigo.getBoundingBox().inflate((double)this.lookDistance, 3.0, (double)this.lookDistance), (p_148124_) -> {
                    return true;
                }), this.lookAtContext, this.amigo, this.amigo.getX(), this.amigo.getEyeY(), this.amigo.getZ());
            }

            return this.lookAt != null;
        }
    }

    public boolean canContinueToUse() {
        if (!this.lookAt.isAlive()) {
            return false;
        } else if (this.amigo.distanceToSqr(this.lookAt) > (double)(this.lookDistance * this.lookDistance)) {
            return false;
        } else {
            return this.lookTime > 0;
        }
    }

    public void start() {
        this.lookTime = this.adjustedTickDelay(40 + this.amigo.getRandom().nextInt(40));
    }

    public void stop() {
        this.lookAt = null;
    }

    public void tick() {
        if (this.lookAt.isAlive()) {
            double $$0 = this.onlyHorizontal ? this.amigo.getEyeY() : this.lookAt.getEyeY();
            this.amigo.getLookControl().setLookAt(this.lookAt.getX(), $$0, this.lookAt.getZ());
            if (this.emote && this.amigo.getAmigoState().equals(AmigoState.IDLE)) {
                if (this.lookAt instanceof Player player && !ServerPlayHandler.shouldHurt(this.amigo, player)) {
                    this.amigo.setActiveEmote(AmigoEmotes.WAVE);
                }
                if (!this.amigo.isHeartless() && !this.amigo.isEnemigo()) {
                    if (this.lookAt instanceof AmigoEntity amigoEntity && !ServerPlayHandler.shouldHurt(this.amigo, amigoEntity) && amigoEntity.getRandom().nextFloat() > 0.7F) {
                        this.amigo.setActiveEmote(AmigoEmotes.WAVE);
                        amigoEntity.setActiveEmote(AmigoEmotes.WAVE);
                        amigoEntity.lookAt(EntityAnchorArgument.Anchor.FEET, this.amigo.getEyePosition(1F));
                    }
                }
                this.emote = false;
            }
            --this.lookTime;
        }
    }
}
