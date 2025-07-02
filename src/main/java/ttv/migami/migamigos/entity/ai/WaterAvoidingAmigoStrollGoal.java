package ttv.migami.migamigos.entity.ai;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import ttv.migami.migamigos.entity.AmigoEntity;

import javax.annotation.Nullable;

public class WaterAvoidingAmigoStrollGoal extends RandomStrollGoal {
    public static final float PROBABILITY = 0.001F;
    protected final float probability;

    public WaterAvoidingAmigoStrollGoal(PathfinderMob pMob, double pSpeedModifier) {
        this(pMob, pSpeedModifier, 0.001F);
    }

    public WaterAvoidingAmigoStrollGoal(PathfinderMob pMob, double pSpeedModifier, float pProbability) {
        super(pMob, pSpeedModifier);
        this.probability = pProbability;
    }

    @Override
    public boolean canUse() {
        return super.canUse();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.mob instanceof AmigoEntity amigoEntity) {
            amigoEntity.setIsFarming(false);
            if (amigoEntity.isAttacking()) {
                this.stop();
            }
            if (amigoEntity.isContainerOpen()) {
                this.stop();
            }
            if (!amigoEntity.canWander()) {
                this.stop();
            }
            if (!amigoEntity.isFollowing()) {
                this.stop();
            }
        } else {
            this.stop();
        }
    }

    @Nullable
    protected Vec3 getPosition() {
        if (this.mob.isInWaterOrBubble()) {
            Vec3 pos = LandRandomPos.getPos(this.mob, 15, 7);
            return pos == null ? super.getPosition() : pos;
        } else {
            return this.mob.getRandom().nextFloat() >= this.probability ? LandRandomPos.getPos(this.mob, 10, 7) : super.getPosition();
        }
    }
}