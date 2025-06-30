package ttv.migami.migamigos.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import ttv.migami.migamigos.Reference;
import ttv.migami.migamigos.common.Amigo;

import java.util.concurrent.CompletableFuture;

/**
 * Author: MigaMi
 */
public class AmigoGen extends AmigoProvider
{
    public AmigoGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries);
    }

    @Override
    protected void registerAmigos()
    {

        this.addAmigo(new ResourceLocation(Reference.MOD_ID, "cocogoat"), Amigo.Builder.create()

                // General
                .setName("cocogoat")
                .setFavoriteItem(Items.AZURE_BLUET)
                .setHealth(30.0F)
                .setHealingTimer(60)
                .setArmor(3.0F)
                .setRecruitingChance(0.25F)
                .setTolerance(3)
                .setSpeed(0.2F)
                .setRenderSize(0.9F)

                .setAttackCombo("frost_arrow_flurry", 3.5F, 160, -0.5F)
                .setAttackSpecial("ice_lotus", 5F, 400, -2.25F)
                .setAttackUltimate("hail_shower", 4F, 2400, 0.0F)

                .build());

        this.addAmigo(new ResourceLocation(Reference.MOD_ID, "wavelyn"), Amigo.Builder.create()

                // General
                .setName("wavelyn")
                .setFavoriteItem(Items.COOKIE)
                .setHealth(25.0F)
                .setHealingTimer(40)
                .setArmor(2.0F)
                .setRecruitingChance(0.25F)
                .setTolerance(5)
                .setSpeed(0.225F)
                .setRenderSize(0.9F)

                .setAttackCombo("soul_fire_blast", 5F, 100, -0.8F)
                .setAttackSpecial("warmth_push", 5F, 100, 0.0F)
                .setAttackUltimate("smite_storm", 8F, 1800, -0.5F)

                .build());

        this.addAmigo(new ResourceLocation(Reference.MOD_ID, "claymore"), Amigo.Builder.create()

                // General
                .setName("claymore")
                .setFavoriteItem(Items.COOKED_SALMON)
                .setHealth(40.0F)
                .setHealingTimer(70)
                .setArmor(10.0F)
                .setRecruitingChance(0.25F)
                .setTolerance(3)
                .setSpeed(0.19F)
                .setRenderSize(0.95F)

                .setAttackCombo("epic_sword_combo", 4F, 100, 0.5F)
                .setAttackSpecial("rock_armor", 3F, 400, 0.0F)
                .setAttackUltimate("ground_them", 4F, 1200, 0.0F)

                .build());

        this.addAmigo(new ResourceLocation(Reference.MOD_ID, "shysaw"), Amigo.Builder.create()

                // General
                .setName("shysaw")
                .setFavoriteItem(Items.COOKED_CHICKEN)
                .setHealth(30.0F)
                .setHealingTimer(30)
                .setArmor(4.0F)
                .setCustomWeapon(true)
                .setHasSpecial(false)
                .setRecruitingChance(0.25F)
                .setTolerance(6)
                .setSpeed(0.225F)
                .setRenderSize(0.85F)

                .setAttackCombo("saw_blade_combo", 4F, 95, 0.5F)
                .setAttackSpecial("saw_slam", 5F, 0, 0.0F)
                .setAttackUltimate("saw_spin", 3F, 1000, 0.5F)

                .build());

        this.addAmigo(new ResourceLocation(Reference.MOD_ID, "pollypounce"), Amigo.Builder.create()

                // General
                .setName("pollypounce")
                .setFavoriteItem(Items.COD)
                .setHealth(20.0F)
                .setHealingTimer(20)
                .setArmor(3.0F)
                .setCustomWeapon(false)
                .setHasSpecial(false)
                .setRecruitingChance(0.25F)
                .setTolerance(6)
                .setSpeed(0.225F)
                .setRenderSize(0.85F)

                .setAttackCombo("parrot_flurry", 2F, 95, 0.0F)
                .setAttackSpecial("saw_slam", 5F, 0, 0.0F)
                .setAttackUltimate("saw_spin", 3F, 1000, 0.5F)

                .build());

    }
}
