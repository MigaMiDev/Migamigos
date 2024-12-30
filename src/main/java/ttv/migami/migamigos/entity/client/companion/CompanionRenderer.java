package ttv.migami.migamigos.entity.client.companion;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;
import software.bernie.geckolib.renderer.layer.ItemArmorGeoLayer;
import ttv.migami.migamigos.Reference;
import ttv.migami.migamigos.entity.Companion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CompanionRenderer extends GeoEntityRenderer<Companion> {
    private static final String LEFT_HAND = "left_hand";
    private static final String RIGHT_HAND = "right_hand";

    private static final String LEFT_BOOT = "armor_left_foot";
    private static final String RIGHT_BOOT = "armor_right_foot";
    private static final String LEFT_ARMOR_LEG = "armor_left_leg";
    private static final String RIGHT_ARMOR_LEG = "armor_right_leg";
    private static final String CHESTPLATE = "armor_body";
    private static final String BREAST = "armor_breasts";
    private static final String RIGHT_SLEEVE = "armor_right_arm";
    private static final String LEFT_SLEEVE = "armor_left_arm";
    private static final String HELMET = "armor_head";

    protected ItemStack mainHandItem;
    protected ItemStack offhandItem;

    public CompanionRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CompanionModel());
        this.shadowRadius = 0.5f;

        // Armor Renderer
        addRenderLayer(new ItemArmorGeoLayer<>(this) {
            @Nullable
            @Override
            protected ItemStack getArmorItemForBone(GeoBone bone, Companion animatable) {
                return switch (bone.getName()) {
                    case LEFT_BOOT, RIGHT_BOOT -> this.bootsStack;
                    case LEFT_ARMOR_LEG, RIGHT_ARMOR_LEG -> this.leggingsStack;
                    case CHESTPLATE, BREAST, RIGHT_SLEEVE, LEFT_SLEEVE -> this.chestplateStack;
                    case HELMET -> this.helmetStack;
                    default -> null;
                };
            }

            @Nonnull
            @Override
            protected EquipmentSlot getEquipmentSlotForBone(GeoBone bone, ItemStack stack, Companion animatable) {
                return switch (bone.getName()) {
                    case LEFT_BOOT, RIGHT_BOOT -> EquipmentSlot.FEET;
                    case LEFT_ARMOR_LEG, RIGHT_ARMOR_LEG -> EquipmentSlot.LEGS;
                    case RIGHT_SLEEVE -> !animatable.isLeftHanded() ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                    case LEFT_SLEEVE -> animatable.isLeftHanded() ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
                    case CHESTPLATE, BREAST -> EquipmentSlot.CHEST;
                    case HELMET -> EquipmentSlot.HEAD;
                    default -> super.getEquipmentSlotForBone(bone, stack, animatable);
                };
            }

            @Nonnull
            @Override
            protected ModelPart getModelPartForBone(GeoBone bone, EquipmentSlot slot, ItemStack stack, Companion animatable, HumanoidModel<?> baseModel) {
                return switch (bone.getName()) {
                    case LEFT_BOOT, LEFT_ARMOR_LEG -> baseModel.leftLeg;
                    case RIGHT_BOOT, RIGHT_ARMOR_LEG, BREAST -> baseModel.rightLeg;
                    case RIGHT_SLEEVE -> baseModel.rightArm;
                    case LEFT_SLEEVE -> baseModel.leftArm;
                    case CHESTPLATE -> baseModel.body;
                    case HELMET -> baseModel.head;
                    default -> super.getModelPartForBone(bone, slot, stack, animatable, baseModel);
                };
            }
        });

        // Item Renderer
        addRenderLayer(new BlockAndItemGeoLayer<>(this) {
            @Nullable
            @Override
            protected ItemStack getStackForBone(GeoBone bone, Companion animatable) {
                boolean isMainHandBow = CompanionRenderer.this.mainHandItem.getItem() instanceof BowItem;

                return switch (bone.getName()) {
                    case LEFT_HAND -> isMainHandBow ?
                            CompanionRenderer.this.mainHandItem :
                            (animatable.isLeftHanded() ? CompanionRenderer.this.mainHandItem : CompanionRenderer.this.offhandItem);
                    case RIGHT_HAND -> isMainHandBow ?
                            null :
                            (animatable.isLeftHanded() ? CompanionRenderer.this.offhandItem : CompanionRenderer.this.mainHandItem);
                    default -> null;
                };
            }

            @Override
            protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, Companion animatable) {
                return switch (bone.getName()) {
                    case LEFT_HAND, RIGHT_HAND -> ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
                    default -> ItemDisplayContext.NONE;
                };
            }

            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, Companion animatable,
                                              MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
                if (stack == CompanionRenderer.this.mainHandItem) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(-90f));

                    if (stack.getItem() instanceof BowItem && bone.getName().equals(LEFT_HAND)) {
                        poseStack.translate(0.125, 0.0, 0);
                    }

                    if (stack.getItem() instanceof ShieldItem)
                        poseStack.translate(0, 0.0, -0.05);
                }
                else if (stack == CompanionRenderer.this.offhandItem) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(-90f));

                    if (stack.getItem() instanceof ShieldItem) {
                        poseStack.translate(0, 0.0, 0.45);
                        poseStack.mulPose(Axis.YP.rotationDegrees(180));
                    }
                }

                super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(Companion animatable) {
        return new ResourceLocation(Reference.MOD_ID, "textures/animated/companion/cocogoat.png");
    }

    @Override
    public void render(Companion entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        poseStack.scale(0.9f, 0.9f, 0.9f);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public void preRender(PoseStack poseStack, Companion animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

        this.mainHandItem = animatable.getMainHandItem();
        this.offhandItem = animatable.getOffhandItem();
    }

    @Override
    public boolean shouldShowName(Companion companion) {
        double d0 = this.entityRenderDispatcher.distanceToSqr(companion);
        float f = companion.isDiscrete() ? 8.0F : 16.0F;
        if (d0 >= (double)(f * f)) {
            return false;
        }

        if (companion.getPlayer() != null && companion.getPlayer().isCrouching()) {
            return false;
        }

        return !companion.isContainerOpen();
    }
}
