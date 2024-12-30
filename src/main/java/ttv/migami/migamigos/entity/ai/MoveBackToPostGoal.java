package ttv.migami.migamigos.entity.ai;

import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import ttv.migami.migamigos.entity.Companion;

import javax.annotation.Nullable;

public class MoveBackToPostGoal extends RandomStrollGoal {
    private static final int MAX_XZ_DIST = 10;
    private static final int MAX_Y_DIST = 7;

    public MoveBackToPostGoal(PathfinderMob pMob, double pSpeedModifier, boolean pCheckNoActionTime) {
        super(pMob, pSpeedModifier, 10, pCheckNoActionTime);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.mob instanceof Companion companion) {
            if (companion.getPostPos() == null) {
                this.stop();
            }
            if (companion.isAttacking()) {
                this.stop();
            }
            if (companion.isContainerOpen()) {
                this.stop();
            }
            if (companion.isFollowing()) {
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
        Vector3f postPos = ((Companion) this.mob).getPostPos();
        Vec3i actualPos = new Vec3i((int) postPos.x, (int) postPos.y, (int) postPos.z);
        return DefaultRandomPos.getPosTowards(this.mob, MAX_XZ_DIST, MAX_Y_DIST, Vec3.atBottomCenterOf(actualPos), 1.5707963705062866);
    }
}