package ttv.migami.migamigos.entity.client.companion;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;
import ttv.migami.migamigos.Reference;
import ttv.migami.migamigos.entity.Companion;

public class CompanionModel extends GeoModel<Companion> {

    @Override
    public ResourceLocation getModelResource(Companion animatable) {
        return new ResourceLocation(Reference.MOD_ID, "geo/entity/companion/female_companion.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Companion animatable) {
        return new ResourceLocation(Reference.MOD_ID, "textures/animated/companion/cocogoat.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Companion animatable) {
        return new ResourceLocation(Reference.MOD_ID, "animations/entity/companion/female_companion.animation.json");
    }

    @Override
    public void setCustomAnimations(Companion animatable, long instanceId, AnimationState<Companion> animationState) {
        CoreGeoBone head = getAnimationProcessor().getBone("head");
        if (head != null && !animatable.isAttacking()) {
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(head.getRotX() + entityData.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(head.getRotY() + entityData.netHeadYaw() * Mth.DEG_TO_RAD);
        }
    }
}
