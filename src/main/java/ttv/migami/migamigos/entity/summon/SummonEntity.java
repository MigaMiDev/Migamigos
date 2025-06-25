package ttv.migami.migamigos.entity.summon;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import ttv.migami.migamigos.common.network.ServerPlayHandler;
import ttv.migami.migamigos.entity.AmigoEntity;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class SummonEntity extends Mob implements IEntityAdditionalSpawnData {
    private static final Predicate<Entity> ATTACK_TARGETS = input -> input != null && input.isPickable() && !input.isSpectator();
    private static final EntityDataAccessor<Optional<UUID>> DATA_PLAYER_UUID = SynchedEntityData.defineId(SummonEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    protected UUID playerUUID;
    protected int ownerID;
    protected LivingEntity owner;
    protected float power;

    public SummonEntity(EntityType<? extends Mob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public SummonEntity(EntityType<? extends Mob> pEntityType, Level pLevel, LivingEntity owner, float power) {
        this(pEntityType, pLevel);
        if (owner instanceof AmigoEntity amigoEntity && amigoEntity.getPlayer() != null) {
            this.playerUUID = amigoEntity.getPlayer().getUUID();
            this.setPlayerUUID(Optional.of(this.playerUUID));
        }
        this.ownerID = owner.getId();
        this.owner = owner;
        this.power = power;
    }

    protected void hurt(LivingEntity entity, float damage, DamageSource damageSource) {
        if(entity instanceof SummonEntity) {
            return;
        }
        if(entity.getId() == this.ownerID)
        {
            return;
        }
        if (entity.getUUID() == this.playerUUID) {
            return;
        }

        if (this.owner instanceof AmigoEntity amigoEntity) {
            if (!ServerPlayHandler.shouldHurt(amigoEntity, entity)) {
                return;
            }
        }

        entity.hurt(damageSource, damage);
        entity.invulnerableTime = 0;
    }

    protected void mobEffect(LivingEntity entity, MobEffect effect, int duration, int amplifier, boolean ambient, boolean hideParticles) {
        if(entity == this) {
            return;
        }
        if(entity.getId() == this.ownerID)
        {
            return;
        }
        if (entity instanceof AmigoEntity target &&
                target.getPlayer() != null && target.getPlayer().getUUID() == this.playerUUID) {
            return;
        }
        if (this.getPlayerUUID().isPresent() && entity.getUUID() == this.getPlayerUUID().get()) {
            return;
        }
        entity.addEffect(new MobEffectInstance(effect, duration, amplifier, ambient, hideParticles));
    }

    public Optional<UUID> getPlayerUUID() {
        return this.entityData.get(DATA_PLAYER_UUID);
    }

    public void setPlayerUUID(Optional<UUID> uuid) {
        this.entityData.set(DATA_PLAYER_UUID, uuid);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_PLAYER_UUID, Optional.empty());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound)
    {
        this.playerUUID = compound.getUUID("Owner");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound)
    {
        compound.putUUID("Owner", this.playerUUID);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.ownerID);
        buffer.writeUUID(this.playerUUID);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer)
    {
        this.ownerID = buffer.readInt();
        this.playerUUID = buffer.readUUID();
    }
}
