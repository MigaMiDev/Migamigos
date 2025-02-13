package ttv.migami.migamigos;

import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import ttv.migami.migamigos.entity.AmigoEntity;

public class AmigoAnimations {
    public static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    public static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    public static final RawAnimation SIT = RawAnimation.begin().thenLoop("sit");
    public static final RawAnimation EAT = RawAnimation.begin().thenPlay("eat");
    public static final RawAnimation ATTACK_COMBO = RawAnimation.begin().thenLoop("attack_combo");
    public static final RawAnimation ATTACK_SPECIAL = RawAnimation.begin().thenLoop("attack_special");
    public static final RawAnimation ATTACK_ULTIMATE = RawAnimation.begin().thenLoop("attack_ultimate");
    public static final RawAnimation FARM = RawAnimation.begin().thenPlay("farm");

    public static final RawAnimation EMOTE_WAVE = RawAnimation.begin().thenPlay("emote_wave");

    public static <T extends GeoAnimatable> AnimationController<AmigoEntity> genericEmoteController(AmigoEntity animatable) {
        return new AnimationController<>(animatable, "Emote", 0, state -> {
            if (animatable.isEmoting() && !state.isMoving()) {
                switch (animatable.getEmote()) {
                    case 1: {
                        return state.setAndContinue(EMOTE_WAVE);
                    }
                }
            }

            return PlayState.STOP;
        });
    }

    public static <T extends GeoAnimatable> AnimationController<AmigoEntity> genericEatController(AmigoEntity animatable) {
        return new AnimationController<>(animatable, "Eat", 0, state -> {
            if (animatable.isEating()) {
                return state.setAndContinue(EAT);
            }

            return PlayState.STOP;
        });
    }

    /**
     * Generic {@link DefaultAnimations#WALK walk} + {@link DefaultAnimations#IDLE idle} controller.<br>
     * Will play the walk animation if the animatable is considered moving, or idle if not
     */
    public static <T extends GeoAnimatable> AnimationController<AmigoEntity> genericWalkIdleController(AmigoEntity animatable) {
        return new AnimationController<>(animatable, "Walk/Idle", 0, state -> {
            if (!animatable.isEating()) {
                if (animatable.isSitting()) {
                    state.setAndContinue(SIT);
                } else if (!animatable.isAttacking()) {
                    if(state.isMoving()) {
                        state.getController().setAnimationSpeed(1.6);
                    } else {
                        state.getController().setAnimationSpeed(1.0);
                    }
                    state.setAndContinue(state.isMoving() ? WALK : IDLE);
                }
            }

            return PlayState.CONTINUE;
        });
    }

    /**
     * Will play the farm animation if the animatable is not considered moving
     */
    public static <T extends GeoAnimatable> AnimationController<AmigoEntity> genericFarmController(AmigoEntity animatable) {
        return new AnimationController<>(animatable, "Farm", 0, state -> {
            if (!animatable.isEmoting() && !animatable.isEating() && !state.isMoving() && !animatable.isAttacking() && animatable.isHarvesting() && animatable.isFarming()) {
                return state.setAndContinue(FARM);
            }

            return PlayState.STOP;
        });
    }

    /**
     * Generic attack controller.<br>
     * Plays an attack animation if the animatable is {@link net.minecraft.world.entity.LivingEntity#swinging}.<br>
     * Resets the animation each time it stops, ready for the next swing
     * @param animatable The entity that should swing
     * @return A new {@link AnimationController} instance to use
     */
    public static <T extends LivingEntity & GeoAnimatable> AnimationController<AmigoEntity> genericAttackAnimation(AmigoEntity animatable) {
        return new AnimationController<>(animatable, "Attack", 0, state -> {
            if (animatable.isUltimateAttacking()) {
                return state.setAndContinue(ATTACK_ULTIMATE);
            }
            if (animatable.isSpecialAttacking()) {
                return state.setAndContinue(ATTACK_SPECIAL);
            }
            if (animatable.isComboAttacking() && animatable.isAttacking()) {
                    return state.setAndContinue(ATTACK_COMBO);
            }

            state.getController().forceAnimationReset();

            return PlayState.STOP;
        });
    }
}
