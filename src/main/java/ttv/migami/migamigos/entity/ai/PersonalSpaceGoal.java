package ttv.migami.migamigos.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.level.levelgen.Heightmap;
import ttv.migami.migamigos.entity.AmigoEntity;

import java.util.EnumSet;

public class PersonalSpaceGoal extends Goal {
    private final AmigoEntity amigoEntity;
    private LivingEntity player;
    private final PathNavigation navigation;
    private static final double MIN_DISTANCE = 1.5;
    private static final int MAX_TICKS_IN_RANGE = 40; // 40 ticks = 2 seconds
    private int ticksWithinRange;

    public PersonalSpaceGoal(AmigoEntity amigoEntity) {
        this.amigoEntity = amigoEntity;
        this.navigation = amigoEntity.getNavigation();
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        this.player = this.amigoEntity.level().getNearestPlayer(
                this.amigoEntity.getX(),
                this.amigoEntity.getY(),
                this.amigoEntity.getZ(),
                MIN_DISTANCE,
                player -> !player.isSpectator()
        );

        if (this.player == null) {
            return false;
        }

        if (this.amigoEntity.isFarming()) {
            return false;
        }

        this.ticksWithinRange = 0;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.amigoEntity.distanceToSqr(this.player) < MIN_DISTANCE * MIN_DISTANCE;
    }

    @Override
    public void stop() {
        this.player = null;
        this.navigation.stop();
        this.ticksWithinRange = 0;
    }

    @Override
    public void tick() {
        this.amigoEntity.setIsFarming(false);
        this.amigoEntity.getLookControl().setLookAt(this.player, 10.0F, (float) this.amigoEntity.getMaxHeadXRot());

        if (this.amigoEntity.isVehicle() && !isPlayerInVehicle()) {
            this.amigoEntity.stopRiding();
        }

        double dx = this.amigoEntity.getX() - this.player.getX();
        double dz = this.amigoEntity.getZ() - this.player.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);

        if (distance < MIN_DISTANCE && distance > 0) {
            this.amigoEntity.lookAt(this.player, 30.0F, 30.0F);
            this.amigoEntity.getMoveControl().strafe(-2.0F, 0.0F);

            this.ticksWithinRange++;

            /*if (this.player.isCrouching()) {
                teleportAway();
            }*/

            if (this.ticksWithinRange > MAX_TICKS_IN_RANGE && !this.amigoEntity.isPassenger()) {
                teleportAway();
                this.ticksWithinRange = 0;
            }
        } else {
            this.ticksWithinRange = 0;
        }
    }

    private void teleportAway() {
        double newX = this.amigoEntity.getX() + (this.amigoEntity.getRandom().nextDouble() * 4 - 2);
        double newZ = this.amigoEntity.getZ() + (this.amigoEntity.getRandom().nextDouble() * 4 - 2);
        double newY = this.amigoEntity.level().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos((int) newX, (int) this.amigoEntity.getY(), (int) newZ)).getY();

        this.amigoEntity.teleportTo(newX, newY, newZ);
        this.amigoEntity.level().broadcastEntityEvent(this.amigoEntity, (byte) 46);
        this.amigoEntity.setIsSitting(false);
    }

    private boolean isPlayerInVehicle() {
        if (this.player != null) {
            return this.player.getVehicle() instanceof Boat || this.player.getVehicle() instanceof Minecart;
        }
        return false;
    }
}