package ttv.migami.migamigos.client.network;

import ttv.migami.migamigos.client.CustomAmigoManager;
import ttv.migami.migamigos.common.NetworkAmigoManager;
import ttv.migami.migamigos.network.message.S2CMessageUpdateAmigos;

public class ClientPlayHandler {
    public static void handleUpdateAmigos(S2CMessageUpdateAmigos message)
    {
        NetworkAmigoManager.updateRegisteredAmigos(message);
        CustomAmigoManager.updateCustomAmigos(message);
    }
}