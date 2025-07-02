package ttv.migami.migamigos;

import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import ttv.migami.migamigos.entity.AmigoEntity;
import ttv.migami.migamigos.entity.AmigoState;

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

    /* Epic Controller, it handles all animations, its easier to organize priorities. */
    public static <T extends GeoAnimatable> AnimationController<AmigoEntity> epicAnimationController(AmigoEntity animatable) {
        return new AnimationController<>(animatable, "Controller", 0, state -> {
            state.getController().setAnimationSpeed(1.0);

            if (animatable.getAmigoState().equals(AmigoState.SITTING)) {
                return state.setAndContinue(SIT);
            }
            else if (animatable.getAmigoState().equals(AmigoState.EATING)) {
                return state.setAndContinue(EAT);
            }
            else if (animatable.getAmigoState().equals(AmigoState.ULTIMATE_ATTACKING)) {
                return state.setAndContinue(ATTACK_ULTIMATE);
            }
            else if (animatable.getAmigoState().equals(AmigoState.SPECIAL_ATTACKING)) {
                return state.setAndContinue(ATTACK_SPECIAL);
            }
            else if (animatable.getAmigoState().equals(AmigoState.COMBO_ATTACKING)) {
                return state.setAndContinue(ATTACK_COMBO);
            }
            else if(state.isMoving()) {
                state.getController().setAnimationSpeed(1.6);
                return state.setAndContinue(WALK);
            }
            else if (!state.isMoving() && (animatable.getAmigoState().equals(AmigoState.IDLE) ||
                    animatable.getAmigoState().equals(AmigoState.EMOTING))) {
                switch (animatable.getEmote()) {
                    case 1: {
                        return state.setAndContinue(EMOTE_WAVE);
                    }
                }
            }
            state.setAndContinue(IDLE);

            return PlayState.CONTINUE;
        });
    }

    public static <T extends GeoAnimatable> AnimationController<AmigoEntity> genericEmoteController(AmigoEntity animatable) {
        return new AnimationController<>(animatable, "Emote", 0, state -> {
            if ((!animatable.getAmigoState().equals(AmigoState.SITTING) &&
                    !animatable.getAmigoState().equals(AmigoState.EATING)) &&
                    (animatable.getAmigoState().equals(AmigoState.IDLE) ||
                    animatable.getAmigoState().equals(AmigoState.EMOTING)) && !state.isMoving()) {
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
            if (animatable.getAmigoState().equals(AmigoState.EATING)) {
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
            if (animatable.getAmigoState().equals(AmigoState.SITTING)) {
                state.setAndContinue(SIT);
            } else if(state.isMoving()) {
                state.getController().setAnimationSpeed(1.6);
                state.setAndContinue(WALK);
            } else {
                state.getController().setAnimationSpeed(1.0);
                if (state.getController().getCurrentAnimation() != null) {
                    if (!state.getController().getCurrentAnimation().animation().name().startsWith("emote") &&
                            !state.getController().getCurrentAnimation().animation().name().startsWith("attack")) {
                        state.setAndContinue(IDLE);
                        return PlayState.CONTINUE;
                    }
                } else {
                    state.setAndContinue(IDLE);
                }
            }

            return PlayState.CONTINUE;
        });
    }

    /**
     * Will play the farm animation if the animatable is not considered moving
     */
    public static <T extends GeoAnimatable> AnimationController<AmigoEntity> genericFarmController(AmigoEntity animatable) {
        return new AnimationController<>(animatable, "Work", 0, state -> {
            if (animatable.getAmigoState().equals(AmigoState.WORKING) && !state.isMoving()) {
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
            if (animatable.getAmigoState().equals(AmigoState.ULTIMATE_ATTACKING)) {
                return state.setAndContinue(ATTACK_ULTIMATE);
            }
            else if (animatable.getAmigoState().equals(AmigoState.SPECIAL_ATTACKING)) {
                return state.setAndContinue(ATTACK_SPECIAL);
            }
            else if (animatable.getAmigoState().equals(AmigoState.COMBO_ATTACKING)) {
                    return state.setAndContinue(ATTACK_COMBO);
            }

            return PlayState.STOP;
        });
    }
}
