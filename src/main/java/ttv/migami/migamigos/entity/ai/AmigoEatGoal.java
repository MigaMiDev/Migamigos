package ttv.migami.migamigos.entity.ai;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import ttv.migami.migamigos.entity.AmigoEntity;

public class AmigoEatGoal extends Goal {
    private final AmigoEntity amigoEntity;
    private int randomEatTimer = 0;
    private int eatCooldown = 0;
    private int eatingProcess = 0;
    private Item favoriteItem = null;
    private boolean panicEating = false;

    private final static int EAT_COOLDOWN = 200;
    private final static int EAT_TIMER = 35;

    public AmigoEatGoal(AmigoEntity amigoEntity) {
        this.amigoEntity = amigoEntity;
        this.randomEatTimer = randomEatTimer();
    }

    private int randomEatTimer() {
        return this.amigoEntity.getRandom().nextInt(2400) + 1200;
    }

    @Override
    public boolean canUse() {
        return !this.amigoEntity.isDeadOrDying();
    }

    @Override
    public void start() {
        this.randomEatTimer = randomEatTimer();
        this.favoriteItem = ForgeRegistries.ITEMS.getValue(this.amigoEntity.getAmigo().getGeneral().getFavoriteItem());
    }

    @Override
    public void tick() {
        //PanicFood
        if (--this.eatCooldown <= 0 && this.amigoEntity.getHealth() <= this.amigoEntity.getMaxHealth() / 2)
        {
            this.panicEating = true;
            this.amigoEntity.invulnerableTime = EAT_TIMER * 2;
            this.eatingProcess = EAT_TIMER;
            this.randomEatTimer = randomEatTimer();
            this.eatCooldown = EAT_COOLDOWN;
        }
        if (--this.randomEatTimer <= 0 && !this.amigoEntity.isAttacking())
        {
            this.amigoEntity.invulnerableTime = EAT_TIMER * 2;
            this.eatingProcess = EAT_TIMER;
            this.randomEatTimer = randomEatTimer();
        }
        --this.eatingProcess;
        if (this.shouldTriggerItemUseEffects()) {
            this.amigoEntity.level().playSound(null, this.amigoEntity, SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 0.5F + 0.5F * (float)this.amigoEntity.getRandom().nextInt(2), (this.amigoEntity.getRandom().nextFloat() - this.amigoEntity.getRandom().nextFloat()) * 0.2F + 1.0F);
            this.spawnItemParticles(this.amigoEntity.getMainHandItem(), 5);
        }
        if (this.eatingProcess != 0 && this.eatingProcess > 0) {
            if (this.panicEating) {
                if (hasGoldenApple()) {
                    this.amigoEntity.setItemSlot(EquipmentSlot.MAINHAND, Items.GOLDEN_APPLE.getDefaultInstance());
                }
            } else {
                if (this.favoriteItem != null) {
                    this.amigoEntity.setItemSlot(EquipmentSlot.MAINHAND, this.favoriteItem.getDefaultInstance());
                }
            }
            this.amigoEntity.setIsEating(true);
            this.amigoEntity.setIsFarming(false);
            this.amigoEntity.getNavigation().stop();
        }
        else {
            this.amigoEntity.setIsEating(false);
        }

        if (this.eatingProcess == 10) {
            this.amigoEntity.level().playSound(null, this.amigoEntity, SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F, this.amigoEntity.getRandom().nextFloat() * 0.1F + 0.9F);
        }
        if (this.eatingProcess == 1) {
            this.completeMeal();
        }
    }

    private boolean hasGoldenApple() {
        SimpleContainer container = this.amigoEntity.getInventory();
        if (container == null) return false;

        for (int i = 0; i < container.getContainerSize(); i++) {
            if (container.getItem(i).getItem() == Items.GOLDEN_APPLE) {
                return true;
            }
        }
        return false;
    }

    private void consumeGoldenApple() {
        SimpleContainer container = this.amigoEntity.getInventory();
        if (container == null) return;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.getItem() == Items.GOLDEN_APPLE) {
                stack.shrink(1);
                this.amigoEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
                this.amigoEntity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 0));
                break;
            }
        }
    }

    private void completeMeal() {
        if (this.panicEating) {
            if (hasGoldenApple()) {
                consumeGoldenApple();
            }
        }
        this.amigoEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1, false, false));
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
        this.amigoEntity.setItemSlot(EquipmentSlot.MAINHAND, this.amigoEntity.getDefaultItem().getDefaultInstance());
        this.amigoEntity.setIsEating(false);
    }

    private boolean shouldTriggerItemUseEffects() {
        return this.eatingProcess < (EAT_TIMER - 3) && this.eatingProcess > 10 && this.eatingProcess % 2 == 0;
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