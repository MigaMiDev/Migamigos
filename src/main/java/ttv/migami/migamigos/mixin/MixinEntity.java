package ttv.migami.migamigos.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ttv.migami.migamigos.Config;
import ttv.migami.migamigos.entity.AmigoEntity;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow public abstract List<Entity> getPassengers();

    @Shadow public abstract EntityType<?> getType();

    @Shadow @Nullable public abstract LivingEntity getControllingPassenger();

    @Inject(at = @At("HEAD"), method = "canAddPassenger", cancellable = true)
    private void migamigos$allowAmigoOnHorse(Entity pPassenger, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.COMMON.gameplay.rideWithPlayer.get())
            return;

        if (((Object) this) instanceof AbstractHorse) {
            if (getPassengers().size() > 1)
                return;

            if (getControllingPassenger() instanceof Player && pPassenger instanceof AmigoEntity) {
                cir.setReturnValue(true);
            }
        }
    }
}
