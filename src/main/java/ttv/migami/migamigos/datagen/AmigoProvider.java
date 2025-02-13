package ttv.migami.migamigos.datagen;

import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import ttv.migami.migamigos.Reference;
import ttv.migami.migamigos.common.Amigo;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Author: MrCrayfish
 */
public abstract class AmigoProvider implements DataProvider
{
    protected final PackOutput.PathProvider pathProvider;
    private final CompletableFuture<HolderLookup.Provider> registries;
    private final Map<ResourceLocation, Amigo> amigos = new HashMap<>();

    protected AmigoProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "amigos");
        this.registries = registries;
    }

    protected abstract void registerAmigos();

    protected final void addAmigo(ResourceLocation id, Amigo amigo)
    {
        this.amigos.put(id, amigo);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache)
    {
        return this.registries.thenCompose(provider ->
        {
            this.amigos.clear();
            this.registerAmigos();
            return CompletableFuture.allOf(this.amigos.entrySet().stream().map(entry -> {
                ResourceLocation key = entry.getKey();
                Amigo amigo = entry.getValue();
                Path path = this.pathProvider.json(key);
                JsonObject object = amigo.toJsonObject();
                return DataProvider.saveStable(cache, object, path);
            }).toArray(CompletableFuture[]::new));
        });
    }

    @Override
    public String getName()
    {
        return "Amigos: " + Reference.MOD_ID;
    }

}
