package ttv.migami.migamigos.entity.ai;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import ttv.migami.migamigos.entity.Companion;

import javax.annotation.Nullable;

public class WaterAvoidingCompanionStrollGoal extends RandomStrollGoal {
    public static final float PROBABILITY = 0.001F;
    protected final float probability;

    public WaterAvoidingCompanionStrollGoal(PathfinderMob pMob, double pSpeedModifier) {
        this(pMob, pSpeedModifier, 0.001F);
    }

    public WaterAvoidingCompanionStrollGoal(PathfinderMob pMob, double pSpeedModifier, float pProbability) {
        super(pMob, pSpeedModifier);
        this.probability = pProbability;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.mob instanceof Companion companion) {
            if (companion.isAttacking()) {
                this.stop();
            }
            if (companion.isContainerOpen()) {
                this.stop();
            }
            if (!companion.canWander()) {
                this.stop();
            }
            if (!companion.isFollowing()) {
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