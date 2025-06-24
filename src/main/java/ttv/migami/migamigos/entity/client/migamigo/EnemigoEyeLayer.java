package ttv.migami.migamigos.entity.client.migamigo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import ttv.migami.migamigos.Reference;
import ttv.migami.migamigos.entity.AmigoEntity;

public class EnemigoEyeLayer extends GeoRenderLayer<AmigoEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/animated/enemigo/eyes_mask.png");

    public EnemigoEyeLayer(GeoRenderer<AmigoEntity> entityRenderer) {
        super(entityRenderer);
    }

    @Override
    public void render(PoseStack poseStack, AmigoEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (animatable.isEnemigo()) {
            RenderType armorRenderType = RenderType.energySwirl(TEXTURE, 1, 1);

            packedLight = 15728880;

            getRenderer().reRender(getDefaultBakedModel(animatable), poseStack, bufferSource, animatable, armorRenderType,
                    bufferSource.getBuffer(armorRenderType), partialTick, packedLight, OverlayTexture.NO_OVERLAY,
                    1, 1, 1, 1);
        }
    }
}