package ttv.migami.migamigos.mixin;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(AbstractHorse.class)
public abstract class MixinAbstractHorse extends Animal {

    @Shadow private float standAnimO;

    protected MixinAbstractHorse(EntityType<? extends Animal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(at = @At("HEAD"), method = "positionRider", cancellable = true)
    private void migamigos$positionRider(Entity pPassenger, MoveFunction pCallback, CallbackInfo ci) {
        List<Entity> passengers = getPassengers();
        boolean isControlling = Math.max(passengers.indexOf(pPassenger), 0) == 0;

        double offsetX = 0.0F;
        double offsetY = 0.0F;

        double v1 = Mth.sin(this.yBodyRot * ((float) Math.PI / 180F));
        double v2 = Mth.cos(this.yBodyRot * ((float) Math.PI / 180F));
        double v3 = 0.6f * standAnimO;
        double v4 = 0.175f * standAnimO;

        if (passengers.size() > 1) {
            // This values may be tweaked later for better positioning
            offsetX = isControlling ? 0.14 : -0.5 + 0.3 * standAnimO;
            offsetY = isControlling ? 0.0 : -0.2 * standAnimO;
        }

        Vec3 rotation = (new Vec3(0.0F, 0.0F, offsetX)).yRot(-this.yBodyRot * (float)Math.PI / 180.0F);
        double pHeight = this.getY() + this.getPassengersRidingOffset() + pPassenger.getMyRidingOffset();

        pCallback.accept(pPassenger, this.getX() + (v3 * v1) + rotation.x, pHeight + v4 + offsetY, this.getZ() - (v3 * v2) + rotation.z);
        ci.cancel();
    }
}
