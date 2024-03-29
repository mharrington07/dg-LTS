package kr.syeyoung.dungeonsguide.mod.whosonline;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.WhosOnlineApi;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.WhosOnlineCache;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.WhosOnlineRest;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.WhosOnlineWebSocket;
import lombok.Getter;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.*;

public class WhosOnlineManager {

    public static final Gson gson = new Gson();

    static final Logger logger = LogManager.getLogger("WhosOnlineManager");

    private final ExecutorService ex;
    private final ScheduledExecutorService se;

    final String remoteHost;
    @Getter
    private WhosOnlineWebSocket webSocket;
    @Getter
    private WhosOnlineApi websocketClient;
    @Getter
    private WhosOnlineCache cache;
    private WhosOnlineRest restClient;

    boolean useDebugServers = true;

    public WhosOnlineManager(String host) {
        remoteHost = host;
        MinecraftForge.EVENT_BUS.register(this);

        val namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("Dg WhosOnline pool").build();

        ex = Executors.newCachedThreadPool(namedThreadFactory);
        se = Executors.newScheduledThreadPool(2, namedThreadFactory);
    }

    public void init() {
        this.cache = new WhosOnlineCache();

        val websocketUri = useDebugServers ? "ws://" + remoteHost + "/ws" : "wss://" + remoteHost + "/ws";
        val restUri = useDebugServers ? "http://" + remoteHost : "https://" + remoteHost;

        this.webSocket = new WhosOnlineWebSocket(websocketUri, se, cache, Minecraft.getMinecraft().getSession().getPlayerID());
        this.restClient = new WhosOnlineRest(cache, ex, restUri);


        webSocket.connect();
        this.websocketClient = new WhosOnlineApi(webSocket, cache, ex);

    }

    boolean closed = false;

    public void close(){
        closed = true;
        try {
            this.webSocket.close();
            this.webSocket = null;
            this.websocketClient = null;
            this.restClient = null;
            ex.awaitTermination(1,TimeUnit.SECONDS);
            se.awaitTermination(1,TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void websocketdied(WhosOnlineDied e){
        if(closed) return;
        logger.info("Who'sOnline websocket died, trying again in 4 seconds");
        se.schedule(this::init, 4, TimeUnit.SECONDS);
    }

    public static class WhosOnlineDied extends Event {

    }

}
