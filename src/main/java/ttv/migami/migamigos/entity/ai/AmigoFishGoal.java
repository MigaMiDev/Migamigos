package ttv.migami.migamigos.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import ttv.migami.migamigos.entity.AmigoEntity;
import ttv.migami.migamigos.entity.projectile.AmigoFishingHook;

import java.util.EnumSet;

;

public class AmigoFishGoal extends Goal {
    private final AmigoEntity amigo;
    private final double speed;
    private BlockPos waterPos;
    private int castTime = 0;
    private int fishingTime = 0;
    private boolean isFishing = false;
    private AmigoFishingHook bobber;

    public AmigoFishGoal(AmigoEntity amigo, double speed) {
        this.amigo = amigo;
        this.speed = speed;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse() {
        if (amigo.getTarget() != null || amigo.isPassenger()) return false;

        BlockPos origin = amigo.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-10, -2, -10), origin.offset(10, 2, 10))) {
            if (isValidFishingSpot(pos)) {
                waterPos = pos.immutable();
                return true;
            }
        }

        return false;
    }

    private boolean isValidFishingSpot(BlockPos pos) {
        return amigo.level().getBlockState(pos).is(Blocks.WATER)
            && amigo.level().getBlockState(pos.above()).isAir();
    }

    @Override
    public void start() {
        amigo.getNavigation().moveTo(waterPos.getX(), waterPos.getY(), waterPos.getZ(), speed);
    }

    @Override
    public void stop() {
        if (bobber != null && bobber.isAlive()) {
            bobber.discard();
        }
        bobber = null;
        isFishing = false;
        castTime = 0;
        fishingTime = 0;
    }

    @Override
    public void tick() {
        if (amigo.distanceToSqr(Vec3.atCenterOf(waterPos)) > 4.0D) {
            amigo.getNavigation().moveTo(waterPos.getX(), waterPos.getY(), waterPos.getZ(), speed);
            return;
        }

        amigo.getLookControl().setLookAt(waterPos.getX() + 0.5, waterPos.getY(), waterPos.getZ() + 0.5);
        amigo.getNavigation().stop();

        if (!isFishing) {
            if (castTime++ > 20) {
                castFishingLine();
                isFishing = true;
            }
        } else {
            fishingTime++;

            if (fishingTime % 60 == 0) {
                if (amigo.level() instanceof ServerLevel serverLevel && bobber != null) {
                    Vec3 splashPos = bobber.position().add((amigo.getRandom().nextDouble() - 0.5), 0, (amigo.getRandom().nextDouble() - 0.5));
                    serverLevel.sendParticles(ParticleTypes.FISHING, splashPos.x, splashPos.y, splashPos.z, 3, 0.1, 0.1, 0.1, 0.01);
                }
            }

            if (fishingTime > 200 + amigo.getRandom().nextInt(100)) {
                simulateCatch();
                stop();
            }
        }
    }

    private void castFishingLine() {
        Vec3 look = amigo.getLookAngle();
        Vec3 pos = amigo.position().add(0, amigo.getEyeHeight(), 0).add(look.scale(1.5));

        bobber = new AmigoFishingHook(amigo, amigo.level(), 0, 0);
        bobber.moveTo(pos);
        bobber.setDeltaMovement(look.scale(0.1));

        amigo.level().addFreshEntity(bobber);
        amigo.swing(InteractionHand.MAIN_HAND);
    }

    private void simulateCatch() {
        if (bobber != null && bobber.isAlive()) {
            bobber.discard();
            bobber = null;
            amigo.level().playSound(null, amigo, SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.NEUTRAL, 1.0F, 1.0F);

            ItemEntity fish = new ItemEntity(amigo.level(), amigo.getX(), amigo.getY(), amigo.getZ(), new ItemStack(Items.COD));
            fish.setDefaultPickUpDelay();
            amigo.level().addFreshEntity(fish);
        }
    }
}
