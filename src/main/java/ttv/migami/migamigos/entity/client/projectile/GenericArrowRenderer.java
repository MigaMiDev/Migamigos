package ttv.migami.migamigos.entity.client.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import ttv.migami.migamigos.entity.projectile.GenericArrow;

@OnlyIn(Dist.CLIENT)
public class GenericArrowRenderer<T extends GenericArrow> extends EntityRenderer<T> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft:textures/misc/white.png");

    public GenericArrowRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public ResourceLocation getTextureLocation(GenericArrow arrow) {
        return TEXTURE;
    }

    public void render(T pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        Minecraft mc = Minecraft.getInstance();
        pPoseStack.pushPose();

        pPoseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(pPartialTicks, pEntity.yRotO, pEntity.getYRot()) - 90.0F));
        pPoseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(pPartialTicks, pEntity.xRotO, pEntity.getXRot())));

        pPoseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        pPoseStack.scale(0.05625F, 0.05625F, 0.05625F);
        pPoseStack.translate(-4.0F, 0.0F, 0.0F);
        MultiBufferSource.BufferSource renderTypeBuffer = mc.renderBuffers().bufferSource();
        VertexConsumer vertexConsumer = renderTypeBuffer.getBuffer(RenderType.energySwirl(TEXTURE, 0.0F, 0.15625F));
        PoseStack.Pose posestack$pose = pPoseStack.last();
        Matrix4f matrix4f = posestack$pose.pose();
        Matrix3f matrix3f = posestack$pose.normal();
        // Makes the Arrow longer the longer airtime it has
        int size = Math.min((pEntity.tickCount + 0) * 15, 50);

        //int color = trail.getTrailColor();
        //int red = (color >> 16) & 0xFF;
        //int green = (color >> 8) & 0xFF;
        //int blue = color & 0xFF;
        int red = 255;
        int green = 255;
        int blue = 255;

        int light = 15728880;

        this.vertex(red, green, blue, matrix4f, matrix3f, vertexConsumer, -1 - size, -1, -1, 0.0F, 0.15625F, -1, 0, 0, light);
        this.vertex(red, green, blue, matrix4f, matrix3f, vertexConsumer, -1 - size, -1, 1, 0.15625F, 0.15625F, -1, 0, 0, light);
        this.vertex(red, green, blue, matrix4f, matrix3f, vertexConsumer, -1 - size, 1, 1, 0.15625F, 0.3125F, -1, 0, 0, light);
        this.vertex(red, green, blue, matrix4f, matrix3f, vertexConsumer, -1 - size, 1, -1, 0.0F, 0.3125F, -1, 0, 0, light);

        this.vertex(red, green, blue, matrix4f, matrix3f, vertexConsumer, 1, 1, -1, 0.0F, 0.15625F, 1, 0, 0, light);
        this.vertex(red, green, blue, matrix4f, matrix3f, vertexConsumer, 1, 1, 1, 0.15625F, 0.15625F, 1, 0, 0, light);
        this.vertex(red, green, blue, matrix4f, matrix3f, vertexConsumer, 1, -1, 1, 0.15625F, 0.3125F, 1, 0, 0, light);
        this.vertex(red, green, blue, matrix4f, matrix3f, vertexConsumer, 1, -1, -1, 0.0F, 0.3125F, 1, 0, 0, light);

        for(int j = 0; j < 4; ++j) {
            pPoseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            this.vertex(red, green, blue, matrix4f, matrix3f, vertexConsumer, -1 - size, -1, 1, 0.0F, 0.0F, 0, 1, 0, light);
            this.vertex(red, green, blue, matrix4f, matrix3f, vertexConsumer, 1, -1, 1, 0.5F, 0.0F, 0, 1, 0, light);
            this.vertex(red, green, blue, matrix4f, matrix3f, vertexConsumer, 1, 1, 1, 0.5F, 0.15625F, 0, 1, 0, light);
            this.vertex(red, green, blue, matrix4f, matrix3f, vertexConsumer, -1 - size, 1, 1, 0.0F, 0.15625F, 0, 1, 0, light);
        }

        pPoseStack.popPose();
        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
    }

    @Override
    protected int getBlockLightLevel(GenericArrow pEntity, BlockPos pPos) {
        return 15;
    }

    public void vertex(int red, int green, int blue, Matrix4f pMatrix, Matrix3f pNormal, VertexConsumer pConsumer, int pX, int pY, int pZ, float pU, float pV, int pNormalX, int pNormalZ, int pNormalY, int pPackedLight) {

        pConsumer.vertex(pMatrix, (float)pX, (float)pY, (float)pZ)
                .color(red, green, blue, 255)
                .uv(pU, pV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(pPackedLight)
                .normal(pNormal, (float)pNormalX, (float)pNormalY, (float)pNormalZ)
                .endVertex();
    }
}