package ttv.migami.migamigos.entity.ai;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import ttv.migami.migamigos.entity.AmigoEntity;
import ttv.migami.migamigos.entity.amigo.Shybroom;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Predicate;

public class NearestAttackableAmigoTargetGoal<T extends LivingEntity> extends TargetGoal {
    private static final int DEFAULT_RANDOM_INTERVAL = 10;
    protected final Class<T> targetType;
    protected final int randomInterval;
    @Nullable
    protected LivingEntity target;
    protected TargetingConditions targetConditions;

    public NearestAttackableAmigoTargetGoal(Mob pMob, Class<T> pTargetType, boolean pMustSee) {
        this(pMob, pTargetType, 10, pMustSee, false, null);
    }

    public NearestAttackableAmigoTargetGoal(Mob pMob, Class<T> pTargetType, boolean pMustSee, Predicate<LivingEntity> pTargetPredicate) {
        this(pMob, pTargetType, 10, pMustSee, false, pTargetPredicate);
    }

    public NearestAttackableAmigoTargetGoal(Mob pMob, Class<T> pTargetType, boolean pMustSee, boolean pMustReach) {
        this(pMob, pTargetType, 10, pMustSee, pMustReach, null);
    }

    public NearestAttackableAmigoTargetGoal(Mob pMob, Class<T> pTargetType, int pRandomInterval, boolean pMustSee, boolean pMustReach, @Nullable Predicate<LivingEntity> pTargetPredicate) {
        super(pMob, pMustSee, pMustReach);
        this.targetType = pTargetType;
        this.randomInterval = reducedTickDelay(pRandomInterval);
        this.setFlags(EnumSet.of(Flag.TARGET));
        this.targetConditions = TargetingConditions.forCombat().range(this.getFollowDistance()).selector(pTargetPredicate);
    }

    public boolean canUse() {
        if (!(this.mob instanceof AmigoEntity amigoEntity)) {
            return false;
        }
        if (amigoEntity instanceof Shybroom shybroom) {
            if(!shybroom.hasPlayer() && shybroom.getPlayer() == null) return false;
        }
        if (amigoEntity.getHealth() < amigoEntity.getMaxHealth() / 3 && !amigoEntity.isHeartless() && !amigoEntity.isEnemigo()) {
            return false;
        }
        if (!amigoEntity.isAttackingAnyEnemy() && !amigoEntity.isHeartless() && !amigoEntity.isEnemigo()) {
            return false;
        }
        if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0) {
            return false;
        }

        Predicate<LivingEntity> pTargetPredicate;
        if (amigoEntity.isEnemigo() || amigoEntity.isHeartless()) {
            pTargetPredicate = (livingEntity) -> !(livingEntity instanceof Enemy) && (
                    //livingEntity instanceof Animal ||
                            livingEntity instanceof Villager ||
                            (livingEntity instanceof Player && !livingEntity.isSpectator() && !((Player) livingEntity).isCreative()) ||
                            livingEntity instanceof IronGolem || livingEntity instanceof SnowGolem ||
                            (livingEntity instanceof AmigoEntity amigoEntity1 && (!amigoEntity1.isHeartless() && !amigoEntity1.isEnemigo()))
            );
        } else {
            pTargetPredicate = (livingEntity) ->
                    (livingEntity instanceof Enemy || (livingEntity instanceof AmigoEntity amigoEntity1 && (amigoEntity1.isHeartless() || amigoEntity1.isEnemigo()))) &&
                            !(livingEntity instanceof Creeper) &&
                            !(livingEntity instanceof EnderMan) &&
                            !(livingEntity instanceof Ravager);
        }

        this.targetConditions = TargetingConditions.forCombat()
                .range(this.getFollowDistance())
                .selector(pTargetPredicate);

        this.findTarget();
        return this.target != null;
    }

    protected AABB getTargetSearchArea(double pTargetDistance) {
        return this.mob.getBoundingBox().inflate(pTargetDistance, 4.0, pTargetDistance);
    }

    protected void findTarget() {
        if (this.targetType != Player.class && this.targetType != ServerPlayer.class) {
            this.target = this.mob.level().getNearestEntity(this.mob.level().getEntitiesOfClass(this.targetType, this.getTargetSearchArea(this.getFollowDistance()), (p_148152_) -> {
                return true;
            }), this.targetConditions, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        } else {
            this.target = this.mob.level().getNearestPlayer(this.targetConditions, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        }

    }

    public void start() {
        this.mob.setTarget(this.target);
        super.start();
    }

    public void setTarget(@Nullable LivingEntity pTarget) {
        this.target = pTarget;
    }
}