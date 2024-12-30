package ttv.migami.migamigos.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import ttv.migami.migamigos.entity.Companion;

import java.util.EnumSet;

public class PersonalSpaceGoal extends Goal {
    private final Companion companion;
    private LivingEntity player;
    private final PathNavigation navigation;
    private static final double MIN_DISTANCE = 1.25;

    public PersonalSpaceGoal(Companion companion) {
        this.companion = companion;
        this.navigation = companion.getNavigation();
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity livingEntity = this.companion.getPlayer();
        if (livingEntity == null) {
            return false;
        } else if (livingEntity.isSpectator()) {
            return false;
        } else if (this.companion.distanceToSqr(livingEntity) > MIN_DISTANCE * MIN_DISTANCE) {
            return false;
        } else {
            this.player = livingEntity;
            return true;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.companion.distanceToSqr(this.player) < MIN_DISTANCE * MIN_DISTANCE;
    }

    @Override
    public void stop() {
        this.player = null;
        this.navigation.stop();
    }

    @Override
    public void tick() {
        this.companion.getLookControl().setLookAt(this.player, 10.0F, (float) this.companion.getMaxHeadXRot());

        double dx = this.companion.getX() - this.player.getX();
        double dz = this.companion.getZ() - this.player.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);

        if (distance < MIN_DISTANCE && distance > 0) {
            this.companion.lookAt(this.player, 30.0F, 30.0F);
            this.companion.getMoveControl().strafe(-1.0F, 0.0F);
        }
    }
}