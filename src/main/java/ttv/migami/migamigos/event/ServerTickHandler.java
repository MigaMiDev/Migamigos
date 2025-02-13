package ttv.migami.migamigos.event;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ttv.migami.migamigos.amigo.AmigoRealmVisitor;

public class ServerTickHandler {
    private static final AmigoRealmVisitor amigoVisitor = new AmigoRealmVisitor();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            //amigoVisitor.tick(event.getServer().overworld(), false, true);
        }
    }
}