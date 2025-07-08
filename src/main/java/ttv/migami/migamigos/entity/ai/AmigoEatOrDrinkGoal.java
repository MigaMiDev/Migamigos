package ttv.migami.migamigos.entity.ai;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import ttv.migami.migamigos.entity.AmigoEntity;
import ttv.migami.migamigos.entity.AmigoState;

import java.util.List;

public class AmigoEatOrDrinkGoal extends Goal {
    private final AmigoEntity amigoEntity;
    private int randomEatTimer = 0;
    private int selfAidCooldown = 0;
    private int eatingProgress = 0;
    private Item favoriteItem = null;
    private boolean panicEating = false;
    private boolean milkDrinking = false;
    private boolean hasEvilEffect = false;
    private List<MobEffectInstance> currentEffects = null;

    private final static int SELF_AID_COOLDOWN = 300;
    private final static int EAT_TIMER = 35;

    private static final List<Item> PREFERRED_FOODS = List.of(
            Items.ENCHANTED_GOLDEN_APPLE,
            Items.GOLDEN_APPLE,
            Items.POTION
    );

    public AmigoEatOrDrinkGoal(AmigoEntity amigoEntity) {
        this.amigoEntity = amigoEntity;
        this.randomEatTimer = randomEatTimer();
    }

    private int randomEatTimer() {
        return this.amigoEntity.getRandom().nextInt(2400) + 1200;
    }

    @Override
    public boolean canUse() {
        if (this.amigoEntity.isHeartless()) {
            return false;
        }

        return !this.amigoEntity.isDeadOrDying();
    }

    @Override
    public void start() {
        this.randomEatTimer = randomEatTimer();
        this.favoriteItem = ForgeRegistries.ITEMS.getValue(this.amigoEntity.getAmigo().getGeneral().getFavoriteItem());
    }

    public void tryToEat() {
        this.amigoEntity.invulnerableTime = EAT_TIMER * 2;
        this.eatingProgress = EAT_TIMER;
        this.randomEatTimer = randomEatTimer();
    }

    @Override
    public void tick() {
        --this.selfAidCooldown;

        // Actual Using of the item
        --this.eatingProgress;
        if (this.eatingProgress != 0 && this.eatingProgress > 0) {
            if (this.favoriteItem != null) {
                this.amigoEntity.setItemSlot(EquipmentSlot.MAINHAND, this.favoriteItem.getDefaultInstance());
            }
            if (this.panicEating) {
                ItemStack food = findBestFood();
                if (!food.isEmpty()) {
                    this.amigoEntity.setItemSlot(EquipmentSlot.MAINHAND, food);
                }
            } else if (this.milkDrinking) {
                this.amigoEntity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.MILK_BUCKET));
            }
            this.amigoEntity.setAmigoState(AmigoState.EATING);
            this.amigoEntity.setIsFarming(false);
            this.amigoEntity.getNavigation().stop();
        }
        else if (!this.amigoEntity.getAmigoState().equals(AmigoState.COMBO_ATTACKING) &&
                !this.amigoEntity.getAmigoState().equals(AmigoState.SPECIAL_ATTACKING) &&
                !this.amigoEntity.getAmigoState().equals(AmigoState.ULTIMATE_ATTACKING)) {
            this.amigoEntity.setAmigoState(AmigoState.IDLE);
        }

        if (this.shouldTriggerItemUseEffects()) {
            if (this.amigoEntity.getMainHandItem().getItem() instanceof PotionItem || this.amigoEntity.getMainHandItem().is(Items.MILK_BUCKET)) {
                this.amigoEntity.level().playSound(null, this.amigoEntity, SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.5F + 0.5F * (float)this.amigoEntity.getRandom().nextInt(2), (this.amigoEntity.getRandom().nextFloat() - this.amigoEntity.getRandom().nextFloat()) * 0.2F + 1.0F);
            } else {
                this.amigoEntity.level().playSound(null, this.amigoEntity, SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 0.5F + 0.5F * (float)this.amigoEntity.getRandom().nextInt(2), (this.amigoEntity.getRandom().nextFloat() - this.amigoEntity.getRandom().nextFloat()) * 0.2F + 1.0F);
                this.spawnItemParticles(this.amigoEntity.getMainHandItem(), 5);
            }
        }

        if (this.eatingProgress == 10) {
            this.amigoEntity.level().playSound(null, this.amigoEntity, SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F, this.amigoEntity.getRandom().nextFloat() * 0.1F + 0.9F);
        }
        if (this.eatingProgress == 1) {
            this.completeMeal();
        }

        // Panic Food
        if (this.eatingProgress <= 0) {
            if (this.amigoEntity.getHealth() <= this.amigoEntity.getMaxHealth() / 2)
            {
                //ItemStack food = findBestFood();
                //if (!food.isEmpty() || this.selfAidCooldown <= 0) {
                if (this.selfAidCooldown <= 0) {
                    this.milkDrinking = false;
                    this.panicEating = true;
                    this.selfAidCooldown = SELF_AID_COOLDOWN;
                    this.tryToEat();
                }
            }
            // Milk for negative effects
            else if (!this.panicEating) {
                this.currentEffects = this.amigoEntity.getActiveEffects().stream().toList();

                for (MobEffectInstance effect : this.currentEffects) {
                    if (checkForEvilEffect(effect)) {
                        this.hasEvilEffect = true;
                    }
                }

                if (this.hasEvilEffect) {
                    //ItemStack milk = findBestMilk();
                    //if ((!milk.isEmpty() || this.selfAidCooldown <= 0)) {
                    if (this.selfAidCooldown <= 0) {
                        this.milkDrinking = true;
                        this.selfAidCooldown = SELF_AID_COOLDOWN;
                        this.tryToEat();
                    }
                }
            }
            // Regular Food
            else if (--this.randomEatTimer <= 0 && !this.amigoEntity.isAttacking())
            {
                this.tryToEat();
            }
        }
    }

    private ItemStack findBestMilk() {
        SimpleContainer container = this.amigoEntity.getInventory();
        if (container == null) return ItemStack.EMPTY;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.getItem() == Items.MILK_BUCKET) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private ItemStack findBestFood() {
        SimpleContainer container = this.amigoEntity.getInventory();
        if (container == null) return ItemStack.EMPTY;

        for (Item food : PREFERRED_FOODS) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = container.getItem(i);
                if (stack.getItem() == food) {
                    if (stack.getItem() instanceof PotionItem) {
                        Potion potion = PotionUtils.getPotion(stack);
                        List<MobEffectInstance> effects = potion.getEffects();
                        for (MobEffectInstance effect : effects) {
                            if (checkForBeneficialEffect(effect)) {
                                return stack;
                            }
                        }
                    } else {
                        return stack;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private boolean checkForEvilEffect(MobEffectInstance effect) {
        return isEvilEffect(effect.getEffect());
    }

    private boolean checkForBeneficialEffect(MobEffectInstance effect) {
        return isBeneficialEffect(effect.getEffect());
    }

    private void consumePotion(ItemStack potionStack) {
        if (!(potionStack.getItem() instanceof PotionItem)) return;

        Potion potion = PotionUtils.getPotion(potionStack);
        List<MobEffectInstance> effects = potion.getEffects();

        for (MobEffectInstance effect : effects) {
            if (checkForBeneficialEffect(effect)) {
                this.amigoEntity.addEffect(new MobEffectInstance(effect));
            }
        }

        potionStack.shrink(1);
    }

    private void consumeMilk(ItemStack milkStack) {
        if (milkStack.isEmpty()) return;
        milkStack.shrink(1);
    }

    private void consumeFood(ItemStack foodStack) {
        if (foodStack.isEmpty()) return;

        Item item = foodStack.getItem();
        foodStack.shrink(1);

        if (item == Items.ENCHANTED_GOLDEN_APPLE) {
            this.amigoEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 1));
            this.amigoEntity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 0));
            this.amigoEntity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0));
            this.amigoEntity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 3));
        } else if (item == Items.GOLDEN_APPLE) {
            this.amigoEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
            this.amigoEntity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 0));
        } else if (item == Items.POTION) {
            Potion potion = PotionUtils.getPotion(foodStack);
            for (MobEffectInstance effect : potion.getEffects()) {
                this.amigoEntity.addEffect(new MobEffectInstance(effect));
            }
        }
    }

    private boolean isBeneficialEffect(MobEffect effect) {
        return effect == MobEffects.HEAL ||
                effect == MobEffects.HEALTH_BOOST ||
                effect == MobEffects.REGENERATION ||
                effect == MobEffects.DAMAGE_RESISTANCE ||
                effect == MobEffects.DAMAGE_BOOST;
    }

    private boolean isEvilEffect(MobEffect effect) {
        return effect == MobEffects.POISON ||
                effect == MobEffects.WITHER ||
                effect == MobEffects.WEAKNESS ||
                effect == MobEffects.MOVEMENT_SLOWDOWN;
    }

    private void completeMeal() {
        if (this.panicEating) {
            ItemStack food = findBestFood();
            if (!food.isEmpty()) {
                if (food.getItem() instanceof PotionItem) {
                    consumePotion(food);
                } else {
                    consumeFood(food);
                }
            }
        } else if (this.milkDrinking) {
            for (MobEffectInstance effect : this.currentEffects) {
                if (checkForEvilEffect(effect)) {
                    this.amigoEntity.removeEffect(effect.getEffect());
                }
            }
            ItemStack milk = findBestMilk();
            consumeMilk(milk);
            this.hasEvilEffect = false;
        }
        this.amigoEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1, false, false));
        if (this.amigoEntity.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 3; i++) {
                serverLevel.sendParticles(ParticleTypes.HEART,
                        this.amigoEntity.getX() + (this.amigoEntity.getRandom().nextDouble() - 0.5) * this.amigoEntity.getBbWidth(),
                        this.amigoEntity.getY() + this.amigoEntity.getRandom().nextDouble() * this.amigoEntity.getBbHeight(),
                        this.amigoEntity.getZ() + (this.amigoEntity.getRandom().nextDouble() - 0.5) * this.amigoEntity.getBbWidth(),
                        1, 0, 0, 0, 0.1);
            }
        }
        this.panicEating = false;
        this.milkDrinking = false;
        this.amigoEntity.setItemSlot(EquipmentSlot.MAINHAND, this.amigoEntity.getDefaultItem().getDefaultInstance());
        this.amigoEntity.setAmigoState(AmigoState.IDLE);
    }

    private boolean shouldTriggerItemUseEffects() {
        return this.eatingProgress < (EAT_TIMER - 3) && this.eatingProgress > 10 && this.eatingProgress % 2 == 0;
    }

    private void spawnItemParticles(ItemStack pStack, int pAmount) {
        for(int i = 0; i < pAmount; ++i) {
            Vec3 vec3 = new Vec3(((double)this.amigoEntity.getRandom().nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0);
            vec3 = vec3.xRot(-this.amigoEntity.getXRot() * 0.017453292F);
            vec3 = vec3.yRot(-this.amigoEntity.getYRot() * 0.017453292F);
            double d0 = (double)(-this.amigoEntity.getRandom().nextFloat()) * 0.6 - 0.3;
            Vec3 vec31 = new Vec3(((double)this.amigoEntity.getRandom().nextFloat() - 0.5) * 0.3, d0, 0.6);
            vec31 = vec31.xRot(-this.amigoEntity.getXRot() * 0.017453292F);
            vec31 = vec31.yRot(-this.amigoEntity.getYRot() * 0.017453292F);
            vec31 = vec31.add(this.amigoEntity.getX(), this.amigoEntity.getEyeY(), this.amigoEntity.getZ());
            if (this.amigoEntity.level() instanceof ServerLevel) {
                ((ServerLevel)this.amigoEntity.level()).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, pStack), vec31.x, vec31.y, vec31.z, 1, vec3.x, vec3.y + 0.05, vec3.z, 0.0);
            } else {
                this.amigoEntity.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, pStack), vec31.x, vec31.y, vec31.z, vec3.x, vec3.y + 0.05, vec3.z);
            }
        }

    }
}