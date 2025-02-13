package ttv.migami.migamigos.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import ttv.migami.migamigos.entity.AmigoEntity;

import java.util.EnumSet;

public class FollowPlayerGoal extends Goal {
    public static final int TELEPORT_WHEN_DISTANCE_IS = 12;
    private static final int MIN_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 2;
    private static final int MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 3;
    private static final int MAX_VERTICAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 1;
    private final AmigoEntity amigoEntity;
    private LivingEntity player;
    private final LevelReader level;
    private final double speedModifier;
    private final PathNavigation navigation;
    private int timeToRecalcPath;
    private final float stopDistance;
    private final float startDistance;
    private float oldWaterCost;
    private final boolean canFly;

    public FollowPlayerGoal(AmigoEntity amigoEntity, double pSpeedModifier, float pStartDistance, float pStopDistance, boolean pCanFly) {
        this.amigoEntity = amigoEntity;
        this.level = amigoEntity.level();
        this.speedModifier = pSpeedModifier;
        this.navigation = amigoEntity.getNavigation();
        this.startDistance = pStartDistance;
        this.stopDistance = pStopDistance;
        this.canFly = pCanFly;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        if (!(amigoEntity.getNavigation() instanceof GroundPathNavigation) && !(amigoEntity.getNavigation() instanceof FlyingPathNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
        }
    }

    public boolean canUse() {
        LivingEntity livingentity = this.amigoEntity.getPlayer();
        if (livingentity == null) {
            return false;
        } else if (livingentity.isSpectator()) {
            return false;
        } else if (this.unableToMove()) {
            return false;
        } else if (!this.amigoEntity.isFollowing()) {
            return false;
        } else if (this.amigoEntity.distanceToSqr(livingentity) < (double)(this.startDistance * this.startDistance)) {
            return false;
        } else if (this.amigoEntity.isEating()) {
            return false;
        } else {
            this.player = livingentity;
            return true;
        }
    }

    public boolean canContinueToUse() {
        if (this.navigation.isDone()) {
            return false;
        } else if (this.unableToMove()) {
            return false;
        } else {
            return !(this.amigoEntity.distanceToSqr(this.player) <= (double)(this.stopDistance * this.stopDistance));
        }
    }

    private boolean unableToMove() {
        return this.amigoEntity.isPassenger();
    }

    public void start() {
        this.amigoEntity.setIsFarming(false);
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.amigoEntity.getPathfindingMalus(BlockPathTypes.WATER);
        this.amigoEntity.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
    }

    public void stop() {
        this.player = null;
        this.navigation.stop();
        this.amigoEntity.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
    }

    public void tick() {
        double finalSpeed = this.speedModifier;
        if (this.player.isSprinting()) {
            finalSpeed = finalSpeed * 1.25;
        }
        else {
            finalSpeed = this.speedModifier;
        }
        this.amigoEntity.getLookControl().setLookAt(this.player, 10.0F, (float)this.amigoEntity.getMaxHeadXRot());
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            if (this.amigoEntity.distanceToSqr(this.player) >= 144.0) {
                this.teleportToPlayer();
            } else {
                this.navigation.moveTo(this.player, finalSpeed);
            }
        }
    }

    private void teleportToPlayer() {
        BlockPos blockpos = this.player.blockPosition();
        this.amigoEntity.getNavigation().stop();

        for(int i = 0; i < 20; ++i) {
            int j = this.randomIntInclusive(-3, 3);
            int k = this.randomIntInclusive(-1, 1);
            int l = this.randomIntInclusive(-3, 3);
            boolean flag = this.maybeTeleportTo(blockpos.getX() + j, blockpos.getY() + k, blockpos.getZ() + l);
            if (flag) {
                return;
            }
        }

    }

    private boolean maybeTeleportTo(int pX, int pY, int pZ) {
        if (Math.abs((double)pX - this.player.getX()) < 2.0 && Math.abs((double)pZ - this.player.getZ()) < 2.0) {
            return false;
        } else if (!this.canTeleportTo(new BlockPos(pX, pY, pZ))) {
            return false;
        } else {
            this.amigoEntity.moveTo((double)pX + 0.5, (double)pY, (double)pZ + 0.5, this.amigoEntity.getYRot(), this.amigoEntity.getXRot());
            this.navigation.stop();
            return true;
        }
    }

    private boolean canTeleportTo(BlockPos pPos) {
        BlockPathTypes blockpathtypes = WalkNodeEvaluator.getBlockPathTypeStatic(this.level, pPos.mutable());
        if (blockpathtypes != BlockPathTypes.WALKABLE) {
            return false;
        } else {
            BlockState blockstate = this.level.getBlockState(pPos.below());
            if (!this.canFly && blockstate.getBlock() instanceof LeavesBlock) {
                return false;
            } else {
                BlockPos blockpos = pPos.subtract(this.amigoEntity.blockPosition());
                return this.level.noCollision(this.amigoEntity, this.amigoEntity.getBoundingBox().move(blockpos));
            }
        }
    }

    private int randomIntInclusive(int pMin, int pMax) {
        return this.amigoEntity.getRandom().nextInt(pMax - pMin + 1) + pMin;
    }
}