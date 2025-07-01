package ttv.migami.migamigos.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import ttv.migami.migamigos.entity.AmigoEntity;
import ttv.migami.migamigos.init.ModEntities;

import javax.annotation.Nullable;
import java.util.List;

public class AmigoFishingHook extends Projectile {
    private int nibbleTime;
    private int timeUntilLured;
    private int timeUntilHooked;
    private float fishAngle;
    private boolean openWater;
    private final RandomSource random = RandomSource.create();
    private final int luck;
    private final int lureSpeed;
    private int outOfWaterTime;
    private int life;

    public AmigoFishingHook(EntityType<? extends AmigoFishingHook> type, Level level) {
        super(type, level);
        this.luck = 0;
        this.lureSpeed = 0;
        this.noCulling = true;
    }

    public AmigoFishingHook(LivingEntity caster, Level level, int luck, int lureSpeed) {
        super(ModEntities.AMIGO_FISHING_HOOK.get(), level);
        this.setOwner(caster);
        this.luck = Math.max(0, luck);
        this.lureSpeed = Math.max(0, lureSpeed);
        this.noCulling = true;

        float xRot = caster.getXRot();
        float yRot = caster.getYRot();
        float f2 = Mth.cos(-yRot * ((float)Math.PI / 180F) - (float)Math.PI);
        float f3 = Mth.sin(-yRot * ((float)Math.PI / 180F) - (float)Math.PI);
        float f4 = -Mth.cos(-xRot * ((float)Math.PI / 180F));
        float f5 = Mth.sin(-xRot * ((float)Math.PI / 180F));

        double x = caster.getX() - f3 * 0.3;
        double y = caster.getEyeY();
        double z = caster.getZ() - f2 * 0.3;

        this.moveTo(x, y, z, yRot, xRot);

        Vec3 motion = new Vec3(-f3, Mth.clamp(-(f5 / f4), -5F, 5F), -f2).normalize();
        motion = motion.scale(0.6 + random.triangle(0.5, 0.01));
        this.setDeltaMovement(motion);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            BlockPos pos = this.blockPosition();
            FluidState fluid = level().getFluidState(pos);
            boolean inWater = fluid.is(FluidTags.WATER);

            // Bobbing behavior
            if (inWater) {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.9).add(0, -0.03, 0));
                this.outOfWaterTime = Math.max(0, this.outOfWaterTime - 1);
            } else {
                this.setDeltaMovement(this.getDeltaMovement().add(0, -0.03, 0));
                this.outOfWaterTime = Math.min(10, this.outOfWaterTime + 1);
            }

            // Transition to idle (bobbing) state
            if (this.timeUntilHooked > 0) {
                this.timeUntilHooked--;
                if (this.timeUntilHooked <= 0) {
                    // Biting FX
                    this.level().broadcastEntityEvent(this, (byte)31); // optionally trigger splash particles
                    this.nibbleTime = Mth.nextInt(this.random, 20, 40);
                }
            } else if (this.timeUntilLured > 0) {
                this.timeUntilLured--;
                if (this.timeUntilLured <= 0) {
                    this.fishAngle = Mth.nextFloat(this.random, 0.0F, 360.0F);
                    this.timeUntilHooked = Mth.nextInt(this.random, 20, 80);
                }
            } else {
                this.timeUntilLured = Mth.nextInt(this.random, 100, 600) - (lureSpeed * 100);
            }

            this.life++;
            if (this.life > 1200) {
                this.discard();
            }

            this.move(MoverType.SELF, this.getDeltaMovement());
            this.updateRotation();
            if (this.onGround() || this.horizontalCollision) {
                this.setDeltaMovement(Vec3.ZERO);
            }

            this.setDeltaMovement(this.getDeltaMovement().scale(0.92));
            this.reapplyPosition();
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        super.handleEntityEvent(id);
        if (id == 31 && this.level().isClientSide) {
            // Optional splash particles
            for (int i = 0; i < 5; i++) {
                double x = this.getX() + random.nextGaussian() * 0.2;
                double y = this.getY() + 0.5;
                double z = this.getZ() + random.nextGaussian() * 0.2;
                level().addParticle(ParticleTypes.FISHING, x, y, z, 0, 0, 0);
            }
        }
    }

    public void reelIn() {
        // Call this manually when your Amigo wants to retrieve loot
        if (!this.level().isClientSide) {
            LootParams.Builder builder = new LootParams.Builder((ServerLevel)level())
                    .withParameter(LootContextParams.ORIGIN, this.position())
                    .withParameter(LootContextParams.THIS_ENTITY, this)
                    .withLuck(this.luck);

            LootTable table = level().getServer().getLootData().getLootTable(BuiltInLootTables.FISHING);
            List<ItemStack> loot = table.getRandomItems(builder.create(LootContextParamSets.FISHING));

            for (ItemStack stack : loot) {
                ItemEntity drop = new ItemEntity(level(), getX(), getY(), getZ(), stack);
                level().addFreshEntity(drop);
            }

            this.discard();
        }
    }

    @Nullable
    public AmigoEntity getAmigoOwner() {
        Entity entity = this.getOwner();
        return entity instanceof AmigoEntity ? (AmigoEntity)entity : null;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}

    @Override
    public boolean canChangeDimensions() {
        return false;
    }
}