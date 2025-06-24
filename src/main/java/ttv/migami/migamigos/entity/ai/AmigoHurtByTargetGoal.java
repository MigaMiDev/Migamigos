package ttv.migami.migamigos.entity.ai;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;
import ttv.migami.migamigos.entity.AmigoEntity;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

public class AmigoHurtByTargetGoal extends TargetGoal {
    private static final TargetingConditions HURT_BY_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
    private static final int ALERT_RANGE_Y = 10;
    private boolean alertSameType;
    private int timestamp;
    private final Class<?>[] toIgnoreDamage;
    @Nullable
    private Class<?>[] toIgnoreAlert;

    public AmigoHurtByTargetGoal(PathfinderMob pMob, Class<?>... pToIgnoreDamage) {
        super(pMob, true);
        this.toIgnoreDamage = pToIgnoreDamage;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    public boolean canUse() {
        int i = this.mob.getLastHurtByMobTimestamp();
        LivingEntity livingentity = this.mob.getLastHurtByMob();

        if (!(this.mob instanceof AmigoEntity amigoEntity)) {
            return false;
        }

        if (livingentity instanceof AmigoEntity attackingAmigoEntity &&
                attackingAmigoEntity.getPlayer() == amigoEntity.getPlayer()) {
            if (attackingAmigoEntity.hasPlayer() && amigoEntity.hasPlayer()) {
                return false;
            }
        }

        if (livingentity instanceof TamableAnimal tamable) {
            if (tamable.isTame() && tamable.getOwner() != null) {
                return tamable.getOwner().getUUID() != amigoEntity.getPlayer().getUUID();
            }
        }

        if (i != this.timestamp && livingentity != null) {
            if (livingentity.getType() == EntityType.PLAYER && this.mob.level().getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                return false;
            } else {
                Class[] var3 = this.toIgnoreDamage;
                int var4 = var3.length;

                for(int var5 = 0; var5 < var4; ++var5) {
                    Class<?> oclass = var3[var5];
                    if (oclass.isAssignableFrom(livingentity.getClass())) {
                        return false;
                    }
                }

                return this.canAttack(livingentity, HURT_BY_TARGETING);
            }
        } else {
            return false;
        }
    }

    public AmigoHurtByTargetGoal setAlertOthers(Class<?>... pReinforcementTypes) {
        this.alertSameType = true;
        this.toIgnoreAlert = pReinforcementTypes;
        return this;
    }

    public void start() {
        this.mob.setTarget(this.mob.getLastHurtByMob());
        this.targetMob = this.mob.getTarget();
        this.timestamp = this.mob.getLastHurtByMobTimestamp();
        this.unseenMemoryTicks = 300;
        if (this.alertSameType) {
            this.alertOthers();
        }
        ((AmigoEntity) this.mob).setAttacking(true);

        super.start();
    }

    protected void alertOthers() {
        if (this.mob instanceof AmigoEntity amigoEntity && (amigoEntity.isHeartless() || amigoEntity.isEnemigo())) {
            return;
        }

        double d0 = this.getFollowDistance();
        AABB aabb = AABB.unitCubeFromLowerCorner(this.mob.position()).inflate(d0, 10.0, d0);
        List<AmigoEntity> nearbyEntities = this.mob.level().getEntitiesOfClass(
                AmigoEntity.class,
                aabb,
                amigoEntity -> {
                    if (!(this.mob instanceof AmigoEntity amigo)) return false;
                    if (amigoEntity.getTarget() != null) return false;

                    boolean isHeartlessOrEnemigo = (amigoEntity.isHeartless() || amigoEntity.isEnemigo()) && (amigo.isEnemigo() || amigo.isHeartless());

                    if (isHeartlessOrEnemigo) {
                        return true;
                    } else {
                        return !amigoEntity.isDefendingPlayerOnly() || !amigoEntity.hasPlayer();
                    }
                }
        );
        Iterator iterator = nearbyEntities.iterator();

        while(true) {
            Mob mob;
            boolean flag;
            do {
                do {
                    do {
                        do {
                            do {
                                if (!iterator.hasNext()) {
                                    return;
                                }

                                mob = (Mob)iterator.next();
                            } while(this.mob == mob);
                        } while(mob.getTarget() != null);
                    } while(this.mob instanceof AmigoEntity && ((AmigoEntity)this.mob).getPlayer() != ((AmigoEntity)mob).getPlayer());
                } while(mob.isAlliedTo(this.mob.getLastHurtByMob()));

                if (this.toIgnoreAlert == null) {
                    break;
                }

                flag = false;
                Class[] var8 = this.toIgnoreAlert;
                int var9 = var8.length;

                for(int var10 = 0; var10 < var9; ++var10) {
                    Class<?> oclass = var8[var10];
                    if (mob.getClass() == oclass) {
                        flag = true;
                        break;
                    }
                }
            } while(flag);

            this.alertOther(mob, this.mob.getLastHurtByMob());
        }
    }

    protected void alertOther(Mob pMob, LivingEntity pTarget) {
        pMob.setTarget(pTarget);
    }
}