package ttv.migami.migamigos.entity.ai;

import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import ttv.migami.migamigos.entity.AmigoEntity;
import ttv.migami.migamigos.entity.AmigoState;

import javax.annotation.Nullable;

public class MoveBackToPostGoal extends RandomStrollGoal {
    private static final int MAX_XZ_DIST = 10;
    private static final int MAX_Y_DIST = 7;

    public MoveBackToPostGoal(PathfinderMob pMob, double pSpeedModifier, boolean pCheckNoActionTime) {
        super(pMob, pSpeedModifier, 10, pCheckNoActionTime);
    }

    @Override
    public boolean canUse() {
        if (this.mob instanceof AmigoEntity amigoEntity) {
            if (!amigoEntity.getAmigoState().equals(AmigoState.IDLE) && !amigoEntity.getAmigoState().equals(AmigoState.WALKING)) {
                return false;
            }
        }
        return super.canUse();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.mob instanceof AmigoEntity amigoEntity) {
            if (amigoEntity.getPostPos() == null) {
                this.stop();
            }
            if (amigoEntity.isAttacking()) {
                this.stop();
            }
            if (amigoEntity.isContainerOpen()) {
                this.stop();
            }
            if (amigoEntity.isFollowing()) {
                this.stop();
            }
        } else {
            this.stop();
        }
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.getNavigation().stop();
    }

    @Nullable
    protected Vec3 getPosition() {
        Vector3f postPos = ((AmigoEntity) this.mob).getPostPos();
        Vec3i actualPos = new Vec3i((int) postPos.x, (int) postPos.y, (int) postPos.z);
        return DefaultRandomPos.getPosTowards(this.mob, MAX_XZ_DIST, MAX_Y_DIST, Vec3.atBottomCenterOf(actualPos), 1.5707963705062866);
    }
}