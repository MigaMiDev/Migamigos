package ttv.migami.migamigos.common;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.ForgeRegistries;
import ttv.migami.migamigos.annotation.Optional;

import javax.annotation.Nullable;

public class Amigo implements INBTSerializable<CompoundTag>
{
    protected General general = new General();
    protected AttackCombo attackCombo = new AttackCombo();
    protected AttackSpecial attackSpecial = new AttackSpecial();
    protected AttackUltimate attackUltimate = new AttackUltimate();

    public General getGeneral() { return this.general; }
    public AttackCombo getAttackCombo() { return this.attackCombo; }
    public AttackSpecial getAttackSpecial() { return this.attackSpecial; }
    public AttackUltimate getAttackUltimate() { return this.attackUltimate; }

    public static class General implements INBTSerializable<CompoundTag>
    {
        private String name = "amigo";
        private ResourceLocation favoriteItem = new ResourceLocation("poppy");
        private float health = 20.0F;
        private int healingTimer = 30;
        private float armor = 4.0F;
        private boolean customWeapon = false;
        private boolean doesAttack = true;
        private boolean hasSpecial = true;
        private boolean hasUltimate = true;
        private float recruitingChance = 0.25F;
        private int tolerance = 3;
        private float speed = 0.2F;
        private float renderSize = 1.0F;
        @Optional
        @Nullable
        private ResourceLocation chime;

        @Override
        public CompoundTag serializeNBT()
        {
            CompoundTag tag = new CompoundTag();
            tag.putString("Name", this.name);
            tag.putString("FavoriteItem", this.favoriteItem.toString());
            tag.putFloat("Health", this.health);
            tag.putInt("HealingTimer", this.healingTimer);
            tag.putFloat("Armor", this.armor);
            tag.putBoolean("CustomWeapon", this.customWeapon);
            tag.putBoolean("Attacks", this.doesAttack);
            tag.putBoolean("HasSpecial", this.hasSpecial);
            tag.putBoolean("HasUltimate", this.hasUltimate);
            tag.putFloat("RecruitingChance", this.recruitingChance);
            tag.putInt("Tolerance", this.tolerance);
            tag.putFloat("Speed", this.speed);
            tag.putFloat("RenderSize", this.renderSize);
            if(this.chime != null)
            {
                tag.putString("Chime", this.chime.toString());
            }
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag)
        {
            if(tag.contains("Name", Tag.TAG_STRING))
            {
                this.name = tag.getString("Name");
            }
            if(tag.contains("FavoriteItem", Tag.TAG_STRING))
            {
                this.favoriteItem = new ResourceLocation(tag.getString("FavoriteItem"));
            }
            if(tag.contains("Health", Tag.TAG_ANY_NUMERIC))
            {
                this.health = tag.getFloat("Health");
            }
            if(tag.contains("HealingTimer", Tag.TAG_ANY_NUMERIC))
            {
                this.healingTimer = tag.getInt("HealingTimer");
            }
            if(tag.contains("Armor", Tag.TAG_ANY_NUMERIC))
            {
                this.armor = tag.getFloat("Armor");
            }
            if(tag.contains("CustomWeapon", Tag.TAG_ANY_NUMERIC))
            {
                this.customWeapon = tag.getBoolean("CustomWeapon");
            }
            if(tag.contains("Attacks", Tag.TAG_ANY_NUMERIC))
            {
                this.doesAttack = tag.getBoolean("Attacks");
            }
            if(tag.contains("HasSpecial", Tag.TAG_ANY_NUMERIC))
            {
                this.hasSpecial = tag.getBoolean("HasSpecial");
            }
            if(tag.contains("HasUltimate", Tag.TAG_ANY_NUMERIC))
            {
                this.hasUltimate = tag.getBoolean("HasUltimate");
            }
            if(tag.contains("RecruitingChance", Tag.TAG_ANY_NUMERIC))
            {
                this.recruitingChance = tag.getFloat("RecruitingChance");
            }
            if(tag.contains("Tolerance", Tag.TAG_ANY_NUMERIC))
            {
                this.tolerance = tag.getInt("Tolerance");
            }
            if(tag.contains("Speed", Tag.TAG_ANY_NUMERIC))
            {
                this.speed = tag.getFloat("Speed");
            }
            if(tag.contains("RenderSize", Tag.TAG_ANY_NUMERIC))
            {
                this.renderSize = tag.getFloat("RenderSize");
            }
        }

        public JsonObject toJsonObject()
        {
            JsonObject object = new JsonObject();
            object.addProperty("name", this.name);
            object.addProperty("favoriteItem", this.favoriteItem.toString());
            object.addProperty("health", this.health);
            object.addProperty("healingTimer", this.healingTimer);
            object.addProperty("armor", this.armor);
            object.addProperty("customWeapon", this.customWeapon);
            object.addProperty("doesAttack", this.doesAttack);
            object.addProperty("hasSpecial", this.hasSpecial);
            object.addProperty("hasUltimate", this.hasUltimate);
            object.addProperty("recruitingChance", this.recruitingChance);
            object.addProperty("tolerance", this.tolerance);
            object.addProperty("speed", this.speed);
            object.addProperty("renderSize", this.renderSize);
            return object;
        }

        /**
         * @return A copy of the general get
         */
        public General copy()
        {
            General general = new General();
            general.name = this.name;
            general.favoriteItem = this.favoriteItem;
            general.health = this.health;
            general.healingTimer = this.healingTimer;
            general.armor = this.armor;
            general.customWeapon = this.customWeapon;
            general.doesAttack = this.doesAttack;
            general.hasSpecial = this.hasSpecial;
            general.hasUltimate = this.hasUltimate;
            general.recruitingChance = this.recruitingChance;
            general.tolerance = this.tolerance;
            general.speed = this.speed;
            general.renderSize = this.renderSize;
            return general;
        }

        public String getName()
        {
            return this.name;
        }

        public ResourceLocation getFavoriteItem()
        {
            return this.favoriteItem;
        }

        public float getHealth()
        {
            return this.health;
        }

        public int getHealingTimer()
        {
            return this.healingTimer;
        }

        public float getArmor()
        {
            return this.armor;
        }

        public boolean hasCustomWeapon()
        {
            return this.customWeapon;
        }

        public boolean doesAttack()
        {
            return this.doesAttack;
        }

        public boolean hasSpecial()
        {
            return this.hasSpecial;
        }

        public boolean hasUltimate()
        {
            return this.hasUltimate;
        }

        public float getRecruitingChance()
        {
            return this.recruitingChance;
        }

        public int getTolerance()
        {
            return this.tolerance;
        }

        public float getSpeed()
        {
            return this.speed;
        }

        public float getRenderSize()
        {
            return this.renderSize;
        }
    }

    public static class Action implements INBTSerializable<CompoundTag> {
        protected String name = "";
        protected float power;
        protected int cooldown;
        @Optional
        protected float recoil = 0.0F;

        public Action(String name, float power, int cooldown, float recoil) {
            this.name = name;
            this.power = power;
            this.cooldown = cooldown;
            this.recoil = recoil;
        }

        public Action() {
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putString("Name", this.name);
            tag.putFloat("Power", this.power);
            tag.putInt("Cooldown", this.cooldown);
            tag.putFloat("Recoil", this.recoil);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if(tag.contains("Name", Tag.TAG_STRING))
            {
                this.name = tag.getString("Name");
            }
            if(tag.contains("Power", Tag.TAG_ANY_NUMERIC))
            {
                this.power = tag.getFloat("Power");
            }
            if(tag.contains("Cooldown", Tag.TAG_ANY_NUMERIC))
            {
                this.cooldown = tag.getInt("Cooldown");
            }
            if(tag.contains("Recoil", Tag.TAG_ANY_NUMERIC))
            {
                this.recoil = tag.getFloat("Recoil");
            }
        }

        public JsonObject toJsonObject() {
            Preconditions.checkArgument(this.power >= 0, "Power must be equal or greater than zero");
            JsonObject object = new JsonObject();
            object.addProperty("name", this.name);
            object.addProperty("power", this.power);
            if (this.cooldown != 0) object.addProperty("cooldown", this.cooldown);
            if(this.recoil != 0.0F) object.addProperty("recoil", this.recoil);
            return object;
        }

        public Action copy() {
            Action action = new Action();
            action.name = this.name;
            action.power = this.power;
            action.cooldown = this.cooldown;
            action.recoil = this.recoil;
            return action;
        }

        public String getName() {
            return this.name.toString();
        }

        public float getPower() {
            return this.power;
        }

        public int getCooldown() {
            return this.cooldown;
        }

        /**
         * @return The amount of pushback applied to the Amigo!
         */
        public float getRecoil()
        {
            return this.recoil;
        }
    }

    public static class AttackCombo extends Action {
        public AttackCombo(String name, float power, int cooldown, float recoil) {
            super(name, power, cooldown, recoil);
        }

        public AttackCombo() {
        }

        public AttackCombo copy() {
            AttackCombo action = new AttackCombo();
            action.name = this.name;
            action.power = this.power;
            action.cooldown = this.cooldown;
            action.recoil = this.recoil;
            return action;
        }

        public void setValues(String name, float power, int cooldown, float recoil) {
            this.name = name;
            this.power = power;
            this.cooldown = cooldown;
            this.recoil = recoil;
        }
    }

    public static class AttackSpecial extends Action {
        public AttackSpecial(String name, float power, int cooldown, float recoil) {
            super(name, power, cooldown, recoil);
        }

        public AttackSpecial() {
        }

        public AttackSpecial copy() {
            AttackSpecial action = new AttackSpecial();
            action.name = this.name;
            action.power = this.power;
            action.cooldown = this.cooldown;
            action.recoil = this.recoil;
            return action;
        }

        public void setValues(String name, float power, int cooldown, float recoil) {
            this.name = name;
            this.power = power;
            this.cooldown = cooldown;
            this.recoil = recoil;
        }
    }

    public static class AttackUltimate extends Action {
        public AttackUltimate(String name, float power, int cooldown, float recoil) {
            super(name, power, cooldown, recoil);
        }

        public AttackUltimate() {
        }

        public AttackUltimate copy() {
            AttackUltimate action = new AttackUltimate();
            action.name = this.name;
            action.power = this.power;
            action.cooldown = this.cooldown;
            action.recoil = this.recoil;
            return action;
        }

        public void setValues(String name, float power, int cooldown, float recoil) {
            this.name = name;
            this.power = power;
            this.cooldown = cooldown;
            this.recoil = recoil;
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("General", this.general.serializeNBT());
        tag.put("AttackCombo", this.attackCombo.serializeNBT());
        tag.put("AttackSpecial", this.attackSpecial.serializeNBT());
        tag.put("AttackUltimate", this.attackUltimate.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if(tag.contains("General", Tag.TAG_COMPOUND))
        {
            this.general.deserializeNBT(tag.getCompound("General"));
        }
        if (tag.contains("AttackCombo", Tag.TAG_COMPOUND)) {
            this.attackCombo.deserializeNBT(tag.getCompound("AttackCombo"));
        }
        if (tag.contains("AttackSpecial", Tag.TAG_COMPOUND)) {
            this.attackSpecial.deserializeNBT(tag.getCompound("AttackSpecial"));
        }
        if (tag.contains("AttackUltimate", Tag.TAG_COMPOUND)) {
            this.attackUltimate.deserializeNBT(tag.getCompound("AttackUltimate"));
        }
    }

    public JsonObject toJsonObject() {
        JsonObject object = new JsonObject();
        object.add("general", this.general.toJsonObject());
        object.add("attackCombo", this.attackCombo.toJsonObject());
        object.add("attackSpecial", this.attackSpecial.toJsonObject());
        object.add("attackUltimate", this.attackUltimate.toJsonObject());
        return object;
    }

    public static Amigo create(CompoundTag tag) {
        Amigo amigo = new Amigo();
        amigo.deserializeNBT(tag);
        return amigo;
    }

    public Amigo copy() {
        Amigo amigo = new Amigo();
        amigo.general = this.general.copy();
        amigo.attackCombo = this.attackCombo.copy();
        amigo.attackSpecial = this.attackSpecial.copy();
        amigo.attackUltimate = this.attackUltimate.copy();
        return amigo;
    }

    public static class Builder {
        private final Amigo amigo;

        public Builder() {
            this.amigo = new Amigo();
        }

        public static Builder create() {
            return new Builder();
        }

        public Amigo build() {
            return this.amigo.copy(); // Copy since the builder could be used again
        }

        public Builder setName(String name)
        {
            this.amigo.general.name = name;
            return this;
        }

        public Builder setFavoriteItem(Item favoriteItem)
        {
            this.amigo.general.favoriteItem = ForgeRegistries.ITEMS.getKey(favoriteItem);
            return this;
        }

        public Builder setHealth(float health)
        {
            this.amigo.general.health = health;
            return this;
        }

        public Builder setHealingTimer(int healingTimer)
        {
            this.amigo.general.healingTimer = healingTimer;
            return this;
        }

        public Builder setArmor(float armor)
        {
            this.amigo.general.armor = armor;
            return this;
        }

        public Builder setCustomWeapon(boolean customWeapon)
        {
            this.amigo.general.customWeapon = customWeapon;
            return this;
        }

        public Builder setDoesAttack(boolean doesAttack)
        {
            this.amigo.general.doesAttack = doesAttack;
            return this;
        }

        public Builder setHasSpecial(boolean hasSpecial)
        {
            this.amigo.general.hasSpecial = hasSpecial;
            return this;
        }

        public Builder setHasUltimate(boolean hasUltimate)
        {
            this.amigo.general.hasUltimate = hasUltimate;
            return this;
        }

        public Builder setRecruitingChance(float recruitingChance)
        {
            this.amigo.general.recruitingChance = recruitingChance;
            return this;
        }

        public Builder setTolerance(int tolerance)
        {
            this.amigo.general.tolerance = tolerance;
            return this;
        }

        public Builder setSpeed(float speed)
        {
            this.amigo.general.speed = speed;
            return this;
        }

        public Builder setRenderSize(float renderSize)
        {
            this.amigo.general.renderSize = renderSize;
            return this;
        }

        public Builder setAttackCombo(String name, float power, int cooldown, float recoil) {
            this.amigo.attackCombo.setValues(name, power, cooldown, recoil);
            return this;
        }

        public Builder setAttackSpecial(String name, float power, int cooldown, float recoil) {
            this.amigo.attackSpecial.setValues(name, power, cooldown, recoil);
            return this;
        }

        public Builder setAttackUltimate(String name, float power, int cooldown, float recoil) {
            this.amigo.attackUltimate.setValues(name, power, cooldown, recoil);
            return this;
        }
    }
}
