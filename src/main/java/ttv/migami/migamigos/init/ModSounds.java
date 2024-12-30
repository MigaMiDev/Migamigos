package ttv.migami.migamigos.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ttv.migami.migamigos.Reference;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> REGISTER = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Reference.MOD_ID);

    public static final RegistryObject<SoundEvent> BACKPACK_OPENING = register("companion.backpack_opening");
    public static final RegistryObject<SoundEvent> BACKPACK_CLOSING = register("companion.backpack_closing");

    private static RegistryObject<SoundEvent> register(String key) {
        return REGISTER.register(key, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Reference.MOD_ID, key)));
    }
}
