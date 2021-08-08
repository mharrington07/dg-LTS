/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.gamesdk;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import kr.syeyoung.dungeonsguide.gamesdk.jna.GameSDKTypeMapper;
import kr.syeyoung.dungeonsguide.gamesdk.jna.NativeGameSDK;
import kr.syeyoung.dungeonsguide.gamesdk.jna.datastruct.*;
import kr.syeyoung.dungeonsguide.gamesdk.jna.enumuration.*;
import kr.syeyoung.dungeonsguide.gamesdk.jna.interfacestruct.IDiscordActivityEvents;
import kr.syeyoung.dungeonsguide.gamesdk.jna.interfacestruct.IDiscordActivityManager;
import kr.syeyoung.dungeonsguide.gamesdk.jna.interfacestruct.IDiscordCore;
import kr.syeyoung.dungeonsguide.gamesdk.jna.typedef.*;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;

public class GameSDK {
    @Getter
    private static NativeGameSDK nativeGameSDK;

    static {
        try {
            extractLibrary();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void extractLibrary() throws IOException {
        String libName = System.mapLibraryName("discord_game_sdk");
        String dir = "";
        switch(Platform.getOSType()) {
            case Platform.MAC:
                dir = "darwin";
                break;
            case Platform.LINUX:
                dir = "linux";
                break;
            case Platform.WINDOWS:
                if (Platform.is64Bit()) dir = "win-x64";
                else dir = "win-x86";
                break;
            default:
                throw new IllegalStateException("Unsupported OS Type");
        }

        String resourceLoc = "/gamesdk/"+dir+"/"+libName;
        File targetExtractionPath = new File("native/"+libName);
        targetExtractionPath.getParentFile().mkdirs();
        try (InputStream is = GameSDK.class.getResourceAsStream(resourceLoc)) {
            Files.copy(is, targetExtractionPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
            targetExtractionPath.deleteOnExit();
        }

        nativeGameSDK = (NativeGameSDK) Native.loadLibrary(targetExtractionPath.getAbsolutePath(), NativeGameSDK.class,
                Collections.singletonMap(Library.OPTION_TYPE_MAPPER, GameSDKTypeMapper.INSTANCE));
    }

    public static void writeString(byte[] bts, String str) {
        System.arraycopy(str.getBytes(), 0, bts, 0, str.getBytes().length);
    }
    public static String readString(byte[] bts) {
        int i;
        for (i = 0; i < bts.length && bts[i] != 0; i++);
        byte[] asdasd = new byte[i+1];
        System.arraycopy(bts, 0, asdasd, 0, i);
        return new String(asdasd);
    }

    public static void main(String args[]) throws InterruptedException {
        NativeGameSDK nativeGameSDK = getNativeGameSDK();
        DiscordCreateParams discordCreateParams = new DiscordCreateParams();
        discordCreateParams.client_id = new DiscordClientID(816298079732498473L);

        IDiscordActivityEvents activityEvents = new IDiscordActivityEvents();
        activityEvents.OnActivityInvite = new IDiscordActivityEvents.OnActivityInviteCallback() {
            @Override
            public void onActivityInvite(Pointer eventData, EDiscordActivityActionType type, DiscordUser user, DiscordActivity activity) {

            }
        };
        activityEvents.OnActivityJoin = new IDiscordActivityEvents.OnActivityJoinCallback() {
            @Override
            public void onActivityJoin(Pointer eventData, String secret) {

            }
        };
        activityEvents.OnActivityJoinRequest = new IDiscordActivityEvents.OnActivityJoinRequestCallback() {
            @Override
            public void onActivityJoinRequest(Pointer eventData, DiscordUser user) {

            }
        };
        activityEvents.OnActivitySpectate = new IDiscordActivityEvents.OnActivitySpectateCallback() {
            @Override
            public void onActivitySpectate(Pointer eventData, String secret) {

            }
        };
        activityEvents.write();
        discordCreateParams.activity_events = new IDiscordActivityEvents.ByReference(activityEvents.getPointer());


        PointerByReference pointerByReference = new PointerByReference();
        nativeGameSDK.DiscordCreate(new DiscordVersion(NativeGameSDK.DISCORD_VERSION), discordCreateParams, pointerByReference);
        IDiscordCore iDiscordCore = new IDiscordCore(pointerByReference.getValue());

        iDiscordCore.SetLogHook.setLogHook(iDiscordCore, EDiscordLogLevel.DiscordLogLevel_Debug, Pointer.NULL, new IDiscordCore.LogHook() {
            @Override
            public void hook(Pointer hookData, EDiscordLogLevel level, String message) {
                System.out.println(message+" - "+level+" - "+hookData);
            }
        });

        DiscordActivity discordActivity = new DiscordActivity();
        discordActivity.activityType = EDiscordActivityType.DiscordActivityType_Playing;
        writeString(discordActivity.details, "Dungeons Guide RPC Test Det");
        writeString(discordActivity.state, "Dungeons Guide RPC Test Sta");
        discordActivity.party = new DiscordActivityParty();
        writeString(discordActivity.party.id, "partyid");
        discordActivity.party.discordActivityParty = new DiscordPartySize();
        discordActivity.party.discordActivityParty.current_size = new Int32(4);
        discordActivity.party.discordActivityParty.max_size = new Int32(10);
        discordActivity.instance = false;
        discordActivity.timestamps = new DiscordActivityTimestamps();
        discordActivity.timestamps.start = new DiscordTimestamp(System.currentTimeMillis());
        discordActivity.timestamps.end = new DiscordTimestamp(System.currentTimeMillis()+1000*60*60);
        discordActivity.secrets = new DiscordActivitySecrets();
        writeString(discordActivity.secrets.join, "thisisjoinsecret");
        writeString(discordActivity.secrets.spectate, "thisisspectatesecret");
        writeString(discordActivity.secrets.match, "thisismatchsecret");
        discordActivity.assets = new DiscordActivityAssets();
        writeString(discordActivity.assets.large_text, "thisislargetext");
        writeString(discordActivity.assets.large_image, "mort");



        IDiscordActivityManager iDiscordActivityManager = iDiscordCore.GetActivityManager.getActivityManager(iDiscordCore);
        iDiscordActivityManager.UpdateActivity.updateActivity(iDiscordActivityManager, discordActivity, Pointer.NULL, new NativeGameSDK.DiscordCallback() {
            @Override
            public void callback(Pointer callbackData, EDiscordResult result) {
                System.out.println("Callback: "+callbackData+" - "+result);
            }
        });

        while(true) {
            iDiscordCore.RunCallbacks.runCallbacks(iDiscordCore);
            Thread.sleep(100);
        }
    }
}
