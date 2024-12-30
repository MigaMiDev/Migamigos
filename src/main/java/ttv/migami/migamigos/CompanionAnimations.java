package ttv.migami.migamigos;

import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import ttv.migami.migamigos.entity.Companion;

public class CompanionAnimations {
    public static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    public static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    public static final RawAnimation ATTACK_COMBO = RawAnimation.begin().thenLoop("attack_combo");
    public static final RawAnimation ATTACK_SPECIAL = RawAnimation.begin().thenLoop("attack_special");
    public static final RawAnimation ATTACK_ULTIMATE = RawAnimation.begin().thenLoop("attack_ultimate");

    /**
     * Generic {@link DefaultAnimations#WALK walk} + {@link DefaultAnimations#IDLE idle} controller.<br>
     * Will play the walk animation if the animatable is considered moving, or idle if not
     */
    public static <T extends GeoAnimatable> AnimationController<Companion> genericWalkIdleController(Companion animatable) {
        return new AnimationController<>(animatable, "Walk/Idle", 5, state -> {
            if (!animatable.isAttacking()) {
                if(state.isMoving()) {
                    state.getController().setAnimationSpeed(1.6);
                } else {
                    state.getController().setAnimationSpeed(1.0);
                }
                state.setAndContinue(state.isMoving() ? WALK : IDLE);
            }

            return PlayState.CONTINUE;
        });
    }

    /**
     * Generic attack controller.<br>
     * Plays an attack animation if the animatable is {@link net.minecraft.world.entity.LivingEntity#swinging}.<br>
     * Resets the animation each time it stops, ready for the next swing
     * @param animatable The entity that should swing
     * @param attackAnimation The attack animation to play (E.G. swipe, strike, stomp, swing, etc)
     * @return A new {@link AnimationController} instance to use
     */
    public static <T extends LivingEntity & GeoAnimatable> AnimationController<Companion> genericAttackAnimation(Companion animatable) {
        return new AnimationController<>(animatable, "Attack", 0, state -> {
            if (animatable.isUltimateAttacking()) {
                return state.setAndContinue(ATTACK_ULTIMATE);
            }
            if (animatable.isSpecialAttacking()) {
                return state.setAndContinue(ATTACK_SPECIAL);
            }
            if (animatable.isAttacking()) {
                    return state.setAndContinue(ATTACK_COMBO);
            }

            state.getController().forceAnimationReset();

            return PlayState.STOP;
        });
    }
}
