package ttv.migami.migamigos.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Author: MrCrayfish
 */
public class CustomAmigo implements INBTSerializable<CompoundTag>
{
    public Amigo amigo;

    public Amigo getAmigo()
    {
        return this.amigo;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag compound = new CompoundTag();
        compound.put("Amigo", this.amigo.serializeNBT());
        return compound;
    }

    @Override
    public void deserializeNBT(CompoundTag compound)
    {
        this.amigo = Amigo.create(compound.getCompound("Amigo"));
    }
}
