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

package kr.syeyoung.dungeonsguide.gamesdk.jna.interfacestruct;

import com.sun.jna.Callback;
import com.sun.jna.Structure;
import kr.syeyoung.dungeonsguide.gamesdk.jna.datastruct.DiscordStruct;
import com.sun.jna.Pointer;

import java.util.Arrays;
import java.util.List;

public class IDiscordOverlayEvents extends DiscordStruct { public IDiscordOverlayEvents() {super();} public IDiscordOverlayEvents(Pointer pointer) {super(pointer);}
    public interface OnToggleCallback extends GameSDKCallback { void onToggle(Pointer eventData, boolean locked); }
    public OnToggleCallback OnToggle;



    public static class ByReference extends IDiscordOverlayEvents implements Structure.ByReference { public ByReference() {super();} public ByReference(Pointer pointer) {super(pointer);}}
    public static class ByValue extends IDiscordOverlayEvents implements Structure.ByValue { public ByValue() {super();} public ByValue(Pointer pointer) {super(pointer);}}

    @Override protected List getFieldOrder() { return Arrays.asList("OnToggle"); }
}
