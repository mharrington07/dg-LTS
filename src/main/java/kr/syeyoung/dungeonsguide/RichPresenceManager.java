/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.events.SkyblockJoinedEvent;
import kr.syeyoung.dungeonsguide.events.SkyblockLeftEvent;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.gamesdk.GameSDK;
import kr.syeyoung.dungeonsguide.gamesdk.jna.NativeGameSDK;
import kr.syeyoung.dungeonsguide.gamesdk.jna.datastruct.*;
import kr.syeyoung.dungeonsguide.gamesdk.jna.enumuration.EDiscordActivityActionType;
import kr.syeyoung.dungeonsguide.gamesdk.jna.enumuration.EDiscordLogLevel;
import kr.syeyoung.dungeonsguide.gamesdk.jna.enumuration.EDiscordResult;
import kr.syeyoung.dungeonsguide.gamesdk.jna.interfacestruct.IDiscordActivityEvents;
import kr.syeyoung.dungeonsguide.gamesdk.jna.interfacestruct.IDiscordActivityManager;
import kr.syeyoung.dungeonsguide.gamesdk.jna.interfacestruct.IDiscordCore;
import kr.syeyoung.dungeonsguide.gamesdk.jna.typedef.DiscordClientID;
import kr.syeyoung.dungeonsguide.gamesdk.jna.typedef.DiscordTimestamp;
import kr.syeyoung.dungeonsguide.gamesdk.jna.typedef.DiscordVersion;
import kr.syeyoung.dungeonsguide.gamesdk.jna.typedef.Int32;
import kr.syeyoung.dungeonsguide.party.PartyInviteViewer;
import kr.syeyoung.dungeonsguide.party.PartyJoinRequest;
import kr.syeyoung.dungeonsguide.party.PartyManager;
import kr.syeyoung.dungeonsguide.stomp.StompHeader;
import kr.syeyoung.dungeonsguide.stomp.StompPayload;
import lombok.Getter;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.json.JSONObject;

public class RichPresenceManager implements Runnable {
    public static RichPresenceManager INSTANCE = new RichPresenceManager();
    private Thread t = new Thread(this);

    public RichPresenceManager() {
        t.start();
    }
    @Getter
    private NativeGameSDK nativeGameSDK;
    @Getter
    private IDiscordCore iDiscordCore;
    private IDiscordActivityManager activityManager;
    private IDiscordActivityEvents.ByReference callbacks;

    @Getter
    private boolean setupSuccessful = false;
    public boolean setup() {
        nativeGameSDK = GameSDK.getNativeGameSDK();
        if (nativeGameSDK == null) return false;
        DiscordCreateParams discordCreateParams = new DiscordCreateParams();
        discordCreateParams.client_id = new DiscordClientID(816298079732498473L);

        callbacks = new IDiscordActivityEvents.ByReference();
        callbacks.OnActivityInvite = (eventData, type, user, activity) -> {

        };
        callbacks.OnActivityJoin = (eventData, secret) -> DungeonsGuide.getDungeonsGuide().getStompConnection().send(new StompPayload().method(StompHeader.SEND)
                .header("destination", "/app/party.askedtojoin")
                .payload(new JSONObject().put("token", secret).toString()));
        callbacks.OnActivityJoinRequest = (eventData, user) -> {
                PartyJoinRequest partyJoinRequest = new PartyJoinRequest();
                partyJoinRequest.setDiscordUser(user);
                partyJoinRequest.setExpire(System.currentTimeMillis() + 30000);

                PartyInviteViewer.INSTANCE.joinRequests.add(partyJoinRequest);
        };
        callbacks.OnActivitySpectate = (eventData, secret) -> {

        };
        callbacks.write();
        discordCreateParams.activity_events = callbacks;

        PointerByReference pointerByReference = new PointerByReference();
        nativeGameSDK.DiscordCreate(new DiscordVersion(NativeGameSDK.DISCORD_VERSION), discordCreateParams, pointerByReference);
        if (pointerByReference.getValue() == Pointer.NULL) return false;
        iDiscordCore = new IDiscordCore(pointerByReference.getValue());

        iDiscordCore.SetLogHook.setLogHook(iDiscordCore, EDiscordLogLevel.DiscordLogLevel_Debug, Pointer.NULL, new IDiscordCore.LogHook() {
            @Override
            public void hook(Pointer hookData, EDiscordLogLevel level, String message) {
                System.out.println(message+" - "+level+" - "+hookData);
            }
        });

        activityManager = iDiscordCore.GetActivityManager.getActivityManager(iDiscordCore);
        latestDiscordActivity = new DiscordActivity();
        latestDiscordActivity.assets = new DiscordActivityAssets();
        GameSDK.writeString(latestDiscordActivity.assets.large_image, "mort");
        GameSDK.writeString(latestDiscordActivity.assets.large_text, "mort");
        return true;
    }

    private final SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();

    private DiscordActivity latestDiscordActivity;

    public void updatePresence() {
        if (!skyblockStatus.isOnHypixel() || !FeatureRegistry.ADVANCED_RICHPRESENCE.isEnabled() || (!skyblockStatus.isOnSkyblock() && FeatureRegistry.ADVANCED_RICHPRESENCE.<Boolean>getParameter("disablenotskyblock").getValue())) {
            activityManager.ClearActivity.clearActivity(activityManager, Pointer.NULL, new NativeGameSDK.DiscordCallback() {
                @Override
                public void callback(Pointer callbackData, EDiscordResult result) {
                    System.out.println("Discord Returned "+result+" For Clearing RPC");
                }
            });
        } else {
            String name = skyblockStatus.getDungeonName();
            if (name.trim().equals("Your Island")) name = "Private Island";

            GameSDK.writeString(latestDiscordActivity.state, name);


            if (PartyManager.INSTANCE.getPartyID() != null) {
                if (latestDiscordActivity.party == null) latestDiscordActivity.party = new DiscordActivityParty();
                GameSDK.writeString(latestDiscordActivity.party.id, PartyManager.INSTANCE.getPartyID());
                if (latestDiscordActivity.party.discordActivityParty == null) latestDiscordActivity.party.discordActivityParty = new DiscordPartySize();
                latestDiscordActivity.party.discordActivityParty.current_size = new Int32(PartyManager.INSTANCE.getMemberCount());
                latestDiscordActivity.party.discordActivityParty.max_size = new Int32(PartyManager.INSTANCE.getMaxParty());
            } else {
                latestDiscordActivity.party = null;
            }
            GameSDK.writeString(latestDiscordActivity.state, name);
            if (skyblockStatus.getContext() != null) {
                DungeonContext dungeonContext = skyblockStatus.getContext();
                long init = dungeonContext.getInit();
                if (latestDiscordActivity.timestamps == null)
                    latestDiscordActivity.timestamps = new DiscordActivityTimestamps();
                latestDiscordActivity.timestamps.start = new DiscordTimestamp(init);

                if (dungeonContext.getBossfightProcessor() != null) {
                    GameSDK.writeString(latestDiscordActivity.details, "Fighting "+dungeonContext.getBossfightProcessor().getBossName()+": "+dungeonContext.getBossfightProcessor().getCurrentPhase());
                } else {
                    GameSDK.writeString(latestDiscordActivity.details, "Clearing Rooms");
                }
            } else {
                latestDiscordActivity.timestamps = null;
                GameSDK.writeString(latestDiscordActivity.details, "Dungeons Guide");
            }
            if (PartyManager.INSTANCE.isAllowAskToJoin()) {
                if (latestDiscordActivity.secrets == null)
                    latestDiscordActivity.secrets = new DiscordActivitySecrets();
                GameSDK.writeString(latestDiscordActivity.secrets.join, PartyManager.INSTANCE.getAskToJoinSecret());
            } else {
                latestDiscordActivity.secrets = null;
            }
            activityManager.UpdateActivity.updateActivity(activityManager, latestDiscordActivity, Pointer.NULL, new NativeGameSDK.DiscordCallback() {
                @Override
                public void callback(Pointer callbackData, EDiscordResult result) {
                    System.out.println("Discord Returned "+result+" For Setting new RPC");
                }
            });
        }
    }

    @Override
    public void run() {
        setupSuccessful = setup();
        if (!setupSuccessful) return;
        while(!Thread.interrupted()) {
            iDiscordCore.RunCallbacks.runCallbacks(iDiscordCore);
            updatePresence();
            try {
                Thread.sleep(300L);
            } catch (Exception e) {e.printStackTrace();}
        }
    }
}
