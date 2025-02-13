package ttv.migami.migamigos.entity.client.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import ttv.migami.migamigos.Reference;
import ttv.migami.migamigos.entity.fx.GroundMarkEntity;

public class GroundMarkRenderer extends EntityRenderer<GroundMarkEntity> {
   private static ResourceLocation TEXTURE_LOCATION = new ResourceLocation(Reference.MOD_ID, "textures/ground/scorch_mark.png");
   private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TEXTURE_LOCATION);

   public GroundMarkRenderer(EntityRendererProvider.Context pContext) {
      super(pContext);
   }

   protected int getBlockLightLevel(GroundMarkEntity pEntity, BlockPos pPos) {
      return 15;
   }

   public void render(GroundMarkEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
      pPoseStack.pushPose();

      ResourceLocation registryName = ForgeRegistries.ENTITY_TYPES.getKey(pEntity.getType());
      if (registryName != null) {
         TEXTURE_LOCATION = new ResourceLocation(registryName.getNamespace(), "textures/ground/" + registryName.getPath() + ".png");
      }

      float scale = pEntity.getScale();
      pPoseStack.scale(scale, scale, scale);

      pPoseStack.mulPose(Axis.XP.rotationDegrees(90F));
      pPoseStack.mulPose(Axis.ZP.rotationDegrees(pEntity.getRandomRotation()));
      pPoseStack.translate(0.0, -0.25, -0.0075);
      PoseStack.Pose posestack$pose = pPoseStack.last();
      Matrix4f matrix4f = posestack$pose.pose();
      Matrix3f matrix3f = posestack$pose.normal();
      VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.entityTranslucent(TEXTURE_LOCATION));

      int remainingLife = pEntity.life;
      int alpha = 255;

      if (remainingLife <= 20) {
         alpha = (int) (255 * (remainingLife / 20.0f));
      }

      vertex(vertexconsumer, matrix4f, matrix3f, pPackedLight, 0.0F, 0, 0, 1, alpha);
      vertex(vertexconsumer, matrix4f, matrix3f, pPackedLight, 1.0F, 0, 1, 1, alpha);
      vertex(vertexconsumer, matrix4f, matrix3f, pPackedLight, 1.0F, 1, 1, 0, alpha);
      vertex(vertexconsumer, matrix4f, matrix3f, pPackedLight, 0.0F, 1, 0, 0, alpha);
      pPoseStack.popPose();
      super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
   }

   private static void vertex(VertexConsumer pConsumer, Matrix4f pPose, Matrix3f pNormal, int pLightmapUV, float pX, int pY, int pU, int pV, int alpha) {
      pConsumer.vertex(pPose, pX - 0.5F, (float) pY - 0.25F, 0.0F)
              .color(255, 255, 255, alpha)
              .uv((float) pU, (float) pV)
              .overlayCoords(OverlayTexture.NO_OVERLAY)
              .uv2(pLightmapUV)
              .normal(pNormal, 0.0F, 1.0F, 0.0F)
              .endVertex();
   }

   /**
    * Returns the location of an entity's texture.
    */
   @Override
   public ResourceLocation getTextureLocation(GroundMarkEntity entity) {
      ResourceLocation registryName = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
      if (registryName != null) {
         return new ResourceLocation(registryName.getNamespace(), "textures/ground/" + registryName.getPath() + ".png");
      }
      return new ResourceLocation(Reference.MOD_ID, "textures/ground/scorch_mark.png");
   }}