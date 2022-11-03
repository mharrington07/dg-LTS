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

package kr.syeyoung.dungeonsguide.mod.cosmetics.replacers.chat.impl;

import kr.syeyoung.dungeonsguide.mod.cosmetics.data.ActiveCosmetic;
import kr.syeyoung.dungeonsguide.mod.cosmetics.data.CosmeticData;
import kr.syeyoung.dungeonsguide.mod.cosmetics.CosmeticsManager;
import kr.syeyoung.dungeonsguide.mod.cosmetics.replacers.chat.IChatReplacer;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Tuple;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class SelfChat implements IChatReplacer {
    Logger logger = LogManager.getLogger("SelfChat");
    @Override
    public boolean isApplyable(ClientChatReceivedEvent event) {
        for (IChatComponent sibling : event.message.getSiblings()) {
            logger.info("sibling.getUnformattedTextForChat() {}", sibling.getUnformattedTextForChat());
            if (sibling.getUnformattedTextForChat().startsWith(": ")) {
                logger.info("isApplyable() {}", sibling.getUnformattedTextForChat());
                return true;
            }
        }
        return false;
    }

    @Override
    public void transformIntoCosmeticsForm(ClientChatReceivedEvent event, CosmeticsManager cosmeticsManager) {
        List<IChatComponent> iChatComponents = new ArrayList<>( event.message.getSiblings() );
        List<IChatComponent> hasMsg = new ArrayList<>();
        for (IChatComponent sibling : iChatComponents) {
            if (sibling.getUnformattedTextForChat().startsWith(": ")) break;
            hasMsg.add(sibling);
        }
        iChatComponents.removeAll(hasMsg);

        ChatComponentText chatComponents = new ChatComponentText("");
        chatComponents.getSiblings().addAll(hasMsg);
        ChatStyle origStyle = hasMsg.get(0).getChatStyle();
        String name = chatComponents.getFormattedText();

        String[] splited = name.split(" ");
        String actualName = splited[splited.length-1];

        List<ActiveCosmetic> cDatas = cosmeticsManager.getActiveCosmeticByPlayerNameLowerCase().get(TextUtils.stripColor(actualName).toLowerCase());
        if (cDatas == null) return;
        CosmeticData color=null, prefix=null;
        for (ActiveCosmetic activeCosmetic : cDatas) {
            CosmeticData cosmeticData = cosmeticsManager.getCosmeticDataMap().get(activeCosmetic.getCosmeticData());
            if (cosmeticData !=null && cosmeticData.getCosmeticType().equals("color")) {
                color = cosmeticData;
            } else if (cosmeticData != null && cosmeticData.getCosmeticType().equals("prefix")) {
                prefix = cosmeticData;
            }
        }

        String building = "";
        if (prefix != null) building += prefix.getData().replace("&", "§") + " ";
        for (int i = 0; i < splited.length-1; i++) {
            building += splited[i] +" ";
        }

        if (color != null) {
            String nick = splited[splited.length-1];
            building += color.getData().replace("&","§");
            boolean foundLegitChar = false;
            boolean foundColor = false;
            for (char c : nick.toCharArray()) {
                if (foundColor) {
                    foundColor = false; continue;
                }
                if (c == '§' && !foundLegitChar) foundColor = true;
                else {
                    foundLegitChar = true;
                    building += c;
                }
            }
        } else {
            building += splited[splited.length-1] ;
        }

        ChatComponentText chatComponents1 = new ChatComponentText(building);
        chatComponents1.setChatStyle(origStyle);
        event.message.getSiblings().clear();
        event.message.getSiblings().add(chatComponents1);
        event.message.getSiblings().addAll(iChatComponents);
    }
}
