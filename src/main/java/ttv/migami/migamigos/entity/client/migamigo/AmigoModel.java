package ttv.migami.migamigos.entity.client.migamigo;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;
import ttv.migami.migamigos.Reference;
import ttv.migami.migamigos.entity.AmigoEntity;

public class AmigoModel extends GeoModel<AmigoEntity> {

    @Override
    public ResourceLocation getModelResource(AmigoEntity animatable) {
        //return new ResourceLocation(Reference.MOD_ID, "geo/entity/amigo/" + animatable.getAmigo().getGeneral().getName() + ".geo.json");
        return new ResourceLocation(Reference.MOD_ID, "geo/entity/amigo/" + animatable.getAmigoName() + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AmigoEntity animatable) {
        //return new ResourceLocation(Reference.MOD_ID, "textures/animated/amigo/" + animatable.getAmigo().getGeneral().getName() + ".png");
        if (animatable.isHeartless()) {
            return new ResourceLocation(Reference.MOD_ID, "textures/animated/heartless/" + animatable.getAmigoName() + ".png");
        }
        return new ResourceLocation(Reference.MOD_ID, "textures/animated/amigo/" + animatable.getAmigoName() + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(AmigoEntity animatable) {
        //return new ResourceLocation(Reference.MOD_ID, "animations/entity/amigo/" + animatable.getAmigo().getGeneral().getName() + ".animation.json");
        return new ResourceLocation(Reference.MOD_ID, "animations/entity/amigo/" + animatable.getAmigoName() + ".animation.json");
    }

    @Override
    public void setCustomAnimations(AmigoEntity animatable, long instanceId, AnimationState<AmigoEntity> animationState) {
        CoreGeoBone head = getAnimationProcessor().getBone("head");
        if (head != null && !animatable.isAttacking() && !animatable.isEating()) {
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(head.getRotX() + entityData.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(head.getRotY() + entityData.netHeadYaw() * Mth.DEG_TO_RAD);
        }
    }
}
