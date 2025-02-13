package ttv.migami.migamigos.network.message;

import com.google.common.collect.ImmutableMap;
import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;
import ttv.migami.migamigos.client.network.ClientPlayHandler;
import ttv.migami.migamigos.common.Amigo;
import ttv.migami.migamigos.common.CustomAmigo;
import ttv.migami.migamigos.common.CustomAmigoLoader;
import ttv.migami.migamigos.common.NetworkAmigoManager;

/**
 * Author: MrCrayfish
 */
public class S2CMessageUpdateAmigos extends PlayMessage<S2CMessageUpdateAmigos>
{
    private ImmutableMap<ResourceLocation, Amigo> registeredAmigos;
    private ImmutableMap<ResourceLocation, CustomAmigo> customAmigos;

    public S2CMessageUpdateAmigos() {}

    @Override
    public void encode(S2CMessageUpdateAmigos message, FriendlyByteBuf buffer)
    {
        Validate.notNull(NetworkAmigoManager.get());
        Validate.notNull(CustomAmigoLoader.get());
        NetworkAmigoManager.get().writeRegisteredAmigos(buffer);
        CustomAmigoLoader.get().writeCustomAmigos(buffer);
    }

    @Override
    public S2CMessageUpdateAmigos decode(FriendlyByteBuf buffer)
    {
        S2CMessageUpdateAmigos message = new S2CMessageUpdateAmigos();
        message.registeredAmigos = NetworkAmigoManager.readRegisteredAmigos(buffer);
        message.customAmigos = CustomAmigoLoader.readCustomAmigos(buffer);
        return message;
    }

    @Override
    public void handle(S2CMessageUpdateAmigos message, MessageContext context)
    {
        context.execute(() -> ClientPlayHandler.handleUpdateAmigos(message));
        context.setHandled(true);
    }

    public ImmutableMap<ResourceLocation, Amigo> getRegisteredAmigos()
    {
        return this.registeredAmigos;
    }

    public ImmutableMap<ResourceLocation, CustomAmigo> getCustomAmigos()
    {
        return this.customAmigos;
    }
}