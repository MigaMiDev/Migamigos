package ttv.migami.migamigos.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ttv.migami.migamigos.Reference;
import ttv.migami.migamigos.entity.StunEntity;

@OnlyIn(Dist.CLIENT)
public class StunEntityRenderer extends EntityRenderer<StunEntity> {
   private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(Reference.MOD_ID, "textures/projectile/cocogoat/ice_cone.png");

   public StunEntityRenderer(EntityRendererProvider.Context pContext) {
      super(pContext);
   }

   protected int getBlockLightLevel(StunEntity pEntity, BlockPos pPos) {
      return 15;
   }

   public void render(StunEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
      super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(StunEntity pEntity) {
      return TEXTURE_LOCATION;
   }
}