package ttv.migami.migamigos.entity.client.migamigo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import org.joml.Vector3d;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;
import software.bernie.geckolib.renderer.layer.ItemArmorGeoLayer;
import ttv.migami.migamigos.entity.AmigoEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AmigoRenderer extends GeoEntityRenderer<AmigoEntity> {
    private int currentTick = -1;

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

    @Override
    public Color getRenderColor(AmigoEntity animatable, float partialTick, int packedLight) {
        if (animatable.isHeartless()) {
            return Color.BLACK;
        }
        else if (animatable.isEnemigo()) {
            return Color.DARK_GRAY;
        }
        return Color.WHITE;
    }

    public AmigoRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AmigoModel());
        this.shadowRadius = 0.5f;

        addRenderLayer(new EnemigoEyeLayer(this));

        // Armor Renderer
        addRenderLayer(new ItemArmorGeoLayer<>(this) {
            @Nullable
            @Override
            protected ItemStack getArmorItemForBone(GeoBone bone, AmigoEntity animatable) {
                return switch (bone.getName()) {
                    case LEFT_BOOT, RIGHT_BOOT -> {
                        if (animatable.isShowingBoots()) {
                            yield this.bootsStack;
                        }
                        yield null;
                    }
                    case LEFT_ARMOR_LEG, RIGHT_ARMOR_LEG -> {
                        if (animatable.isShowingLeggings()) {
                            yield this.leggingsStack;
                        }
                        yield null;
                    }
                    case CHESTPLATE, BREAST, RIGHT_SLEEVE, LEFT_SLEEVE -> {
                        if (animatable.isShowingChestplate()) {
                            yield this.chestplateStack;
                        }
                        yield null;
                    }
                    case HELMET -> {
                        if (animatable.isShowingHelmet()) {
                            yield this.helmetStack;
                        }
                        yield null;
                    }
                    default -> null;
                };
            }

            @Nonnull
            @Override
            protected EquipmentSlot getEquipmentSlotForBone(GeoBone bone, ItemStack stack, AmigoEntity animatable) {
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
            protected ModelPart getModelPartForBone(GeoBone bone, EquipmentSlot slot, ItemStack stack, AmigoEntity animatable, HumanoidModel<?> baseModel) {
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
            protected ItemStack getStackForBone(GeoBone bone, AmigoEntity animatable) {
                if ((bone.getName().equals(RIGHT_HAND) || bone.getName().equals(LEFT_HAND))) {
                    if (animatable.getAmigo().getGeneral().hasCustomWeapon() && animatable.getMainHandItem().is(animatable.getDefaultItem())) {
                        return null;
                    }
                    if (animatable.isEmoting() || animatable.isSitting()) {
                        return null;
                    }
                }

                boolean isMainHandBow = AmigoRenderer.this.mainHandItem.getItem() instanceof BowItem;

                if (isMainHandBow) {
                    return switch (bone.getName()) {
                        case LEFT_HAND -> isMainHandBow ?
                                AmigoRenderer.this.mainHandItem :
                                (animatable.isLeftHanded() ? AmigoRenderer.this.mainHandItem : AmigoRenderer.this.offhandItem);
                        case RIGHT_HAND -> isMainHandBow ?
                                null :
                                (animatable.isLeftHanded() ? AmigoRenderer.this.offhandItem : AmigoRenderer.this.mainHandItem);
                        default -> null;
                    };
                }

                if (bone.getName().equals(RIGHT_HAND)) {
                    return animatable.getMainHandItem();
                }
                if (bone.getName().equals(LEFT_HAND)) {
                    return animatable.getOffhandItem();
                }
                /*if (animatable.isEating()) {
                    Item food = Items.AIR;
                    if (animatable.getAmigo() != null) {
                        food = ForgeRegistries.ITEMS.getValue(animatable.getAmigo().getGeneral().getFavoriteItem());
                    }
                    if (bone.getName().equals(RIGHT_HAND) && food != null) {
                        return food.getDefaultInstance();
                    }
                } else if (animatable.isFarming()) {
                    if (bone.getName().equals(RIGHT_HAND)) {
                        return Items.IRON_HOE.getDefaultInstance();
                    }
                } else */
                return null;
            }

            @Override
            protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, AmigoEntity animatable) {
                return switch (bone.getName()) {
                    case LEFT_HAND, RIGHT_HAND -> ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
                    default -> ItemDisplayContext.NONE;
                };
            }

            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, AmigoEntity animatable,
                                              MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
                if (stack == AmigoRenderer.this.mainHandItem) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(-90f));

                    if (stack.getItem() instanceof BowItem && bone.getName().equals(LEFT_HAND)) {
                        poseStack.translate(0.125, 0.0, 0);
                    }

                    if (stack.getItem() instanceof ShieldItem)
                        poseStack.translate(0, 0.0, -0.05);
                }
                else if (stack == AmigoRenderer.this.offhandItem) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(-90f));

                    if (stack.getItem() instanceof ShieldItem) {
                        poseStack.translate(0, 0.0, 0.45);
                        poseStack.mulPose(Axis.YP.rotationDegrees(180));
                    }
                } else if (stack.getItem() instanceof HoeItem) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(-90f));
                    if (animatable.isHarvesting()) {
                        poseStack.mulPose(Axis.XP.rotationDegrees(-45f));
                    }
                }

                super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
            }
        });
    }

    @Override
    public void renderRecursively(PoseStack poseStack, AmigoEntity animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight,
                                  int packedOverlay, float red, float green, float blue, float alpha) {

        int eyeExpression = animatable.getEyeExpression();

        if (bone.getName().startsWith("glow")) {
            packedLight = 15728880;
        }
        if (bone.getName().equals("custom_item")) {
            bone.setHidden(!animatable.getMainHandItem().is(animatable.getDefaultItem()) || (animatable.isEmoting() || animatable.isSitting()));
        }
        if (bone.getName().startsWith("eyes_")) {
            switch (eyeExpression) {
                case 0: bone.setHidden(!bone.getName().equals("eyes_open")); break;
                case 1: bone.setHidden(!bone.getName().equals("eyes_semi_closed")); break;
                case 2: bone.setHidden(!bone.getName().equals("eyes_closed")); break;
                case 3: bone.setHidden(!bone.getName().equals("eyes_wink_right")); break;
                case 4: bone.setHidden(!bone.getName().equals("eyes_wink_left")); break;
            }
        }

        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public void render(AmigoEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        float size = entity.getAmigo().getGeneral().getRenderSize();
        poseStack.scale(size, size, size);

        if (entity.isHeartless()) {
            packedLight = 15728880;
        }

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public void preRender(PoseStack poseStack, AmigoEntity animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

        this.mainHandItem = animatable.getMainHandItem();
        this.offhandItem = animatable.getOffhandItem();
    }

    @Override
    public boolean shouldShowName(AmigoEntity amigoEntity) {
        if (amigoEntity.isEnemigo() || amigoEntity.isHeartless()) {
            return false;
        }

        double d0 = this.entityRenderDispatcher.distanceToSqr(amigoEntity);
        float f = amigoEntity.isDiscrete() ? 8.0F : 16.0F;
        if (d0 >= (double)(f * f)) {
            return false;
        }

        if (amigoEntity.getPlayer() != null && amigoEntity.getPlayer().isCrouching()) {
            return false;
        }

        return !amigoEntity.isContainerOpen();
    }

    @Override
    public void renderFinal(PoseStack poseStack, AmigoEntity animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (this.currentTick < 0 || this.currentTick != animatable.tickCount) {
            this.currentTick = animatable.tickCount;

            if (animatable.isEnemigo() || animatable.isHeartless()) {
                this.model.getBone("head").ifPresent(smoke -> {
                    Vector3d pos = smoke.getWorldPosition();

                    if (this.currentTick % 5 == 0) {
                        smokeParticles(pos);
                    }
                });

                this.model.getBone("right_hand").ifPresent(smoke -> {
                    Vector3d pos = smoke.getWorldPosition();

                    if (this.currentTick % 5 == 0) {
                        smokeParticles(pos);
                    }
                });

                this.model.getBone("left_hand").ifPresent(smoke -> {
                    Vector3d pos = smoke.getWorldPosition();

                    if (this.currentTick % 5 == 0) {
                        smokeParticles(pos);
                    }
                });
            }
        }

        super.renderFinal(poseStack, animatable, model, bufferSource, buffer, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    private void smokeParticles(Vector3d pos) {
        animatable.getCommandSenderWorld().addParticle(ParticleTypes.SMOKE,
                pos.x(),
                pos.y(),
                pos.z(),
                0,
                0,
                0);
    }
}
