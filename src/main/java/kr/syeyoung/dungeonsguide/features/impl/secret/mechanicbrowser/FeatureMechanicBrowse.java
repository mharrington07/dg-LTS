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

package kr.syeyoung.dungeonsguide.features.impl.secret.mechanicbrowser;

import com.google.common.collect.Lists;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.guiconfig.location.GuiGuiLocationConfig;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.config.types.GUIRectangle;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.actions.tree.ActionRoute;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.*;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.features.impl.secret.mechanicbrowser.MGuiMechanicBrowser;
import kr.syeyoung.dungeonsguide.features.listener.GuiClickListener;
import kr.syeyoung.dungeonsguide.features.listener.GuiPreRenderListener;
import kr.syeyoung.dungeonsguide.features.listener.WorldRenderListener;
import kr.syeyoung.dungeonsguide.features.text.StyledTextRenderer;
import kr.syeyoung.dungeonsguide.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.gui.elements.MFloatSelectionButton;
import kr.syeyoung.dungeonsguide.gui.elements.MPassiveLabelAndElement;
import kr.syeyoung.dungeonsguide.gui.elements.MStringSelectionButton;
import kr.syeyoung.dungeonsguide.roomedit.gui.GuiDungeonAddSet;
import kr.syeyoung.dungeonsguide.roomedit.gui.GuiDungeonParameterEdit;
import kr.syeyoung.dungeonsguide.roomedit.gui.GuiDungeonRoomEdit;
import kr.syeyoung.dungeonsguide.roomedit.gui.GuiDungeonValueEdit;
import kr.syeyoung.dungeonsguide.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.utils.GlStateUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class FeatureMechanicBrowse extends GuiFeature implements GuiPreRenderListener, GuiClickListener, WorldRenderListener {
    public FeatureMechanicBrowse() {
        super("Dungeon Secret.Secret Pathfind","Secret Browser", "Browse and Pathfind secrets and mechanics in the current room", "secret.mechanicbrowse", false, 100, 300);
        parameters.put("linecolor2", new FeatureParameter<AColor>("linecolor2", "Color", "Color of Pathfind line", new AColor(0xFF00FF00, true), "acolor"));
        parameters.put("linethickness", new FeatureParameter<Float>("linethickness", "Thickness", "Thickness of Pathfind line", 1.0f, "float"));
        parameters.put("refreshrate", new FeatureParameter<Integer>("refreshrate", "Line Refreshrate", "How many ticks per line refresh?", 10, "integer"));
        parameters.put("scale", new FeatureParameter<Float>("scale", "Scale", "Scale", 1.0f, "float"));
        mGuiMechanicBrowser = new MGuiMechanicBrowser(this);
        mGuiMechanicBrowser.setWorldAndResolution(Minecraft.getMinecraft(), Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
        lastWidth = Minecraft.getMinecraft().displayWidth; lastHeight = Minecraft.getMinecraft().displayHeight;
    }

    public AColor getColor() {
        return this.<AColor>getParameter("linecolor2").getValue();
    }
    public float getThickness() {
        return this.<Float>getParameter("linethickness").getValue();
    }
    public int getRefreshRate() {
        return this.<Integer>getParameter("refreshrate").getValue();
    }
    public double getScale() {
        return this.<Float>getParameter("scale").getValue();
    }

    private MGuiMechanicBrowser mGuiMechanicBrowser;


    @Override
    public void drawDemo(float partialTicks) {
        super.drawDemo(partialTicks);
        double scale = FeatureMechanicBrowse.this.<Float>getParameter("scale").getValue();
        GlStateManager.scale(scale, scale, 1.0);

        Dimension bigDim = getFeatureRect().getRectangleNoScale().getSize();
        Dimension effectiveDim = new Dimension((int) (bigDim.width / scale),(int)( bigDim.height / scale));

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        Gui.drawRect(0, 0, effectiveDim.width, fr.FONT_HEIGHT + 4, 0xFF444444);
        Gui.drawRect(1, 1, effectiveDim.width - 1, fr.FONT_HEIGHT + 3, 0xFF262626);
        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        fr.drawString("Selected: ", 2,2, 0xFFAAAAAA);
        fr.drawString("Nothing", fr.getStringWidth("Selected: ") + 2,2, 0xFFAA0000);
        fr.drawString("Open Chat to Select Secrets", 2, fr.FONT_HEIGHT + 5, 0xFFAAAAAA);
    }

    private int lastWidth, lastHeight;

    @Override
    public void drawScreen(float partialTicks) {
        if (!isEnabled()) return;
        int i = Mouse.getEventX();
        int j = Minecraft.getMinecraft().displayHeight - Mouse.getEventY();
        if (Minecraft.getMinecraft().displayWidth != lastWidth || Minecraft.getMinecraft().displayHeight != lastHeight) mGuiMechanicBrowser.initGui();
        lastWidth = Minecraft.getMinecraft().displayWidth; lastHeight = Minecraft.getMinecraft().displayHeight;
        mGuiMechanicBrowser.drawScreen(i,j,partialTicks);
    }

    @Override
    public void setFeatureRect(GUIRectangle featureRect) {
        super.setFeatureRect(featureRect);
        mGuiMechanicBrowser.initGui();
    }

    @Override
    public void drawHUD(float partialTicks) { }

    @Override
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre mouseInputEvent) {
        try {
            mGuiMechanicBrowser.handleMouseInput();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onGuiPreRender(GuiScreenEvent.DrawScreenEvent.Pre rendered) {
        int i = Mouse.getEventX();
        int j = Minecraft.getMinecraft().displayHeight - Mouse.getEventY();
        mGuiMechanicBrowser.drawScreen(i, j, rendered.renderPartialTicks);
    }

    @Override
    public void drawWorld(float partialTicks) {
        if (!isEnabled()) return;
        SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
        if (!skyblockStatus.isOnDungeon()) return;
        if (skyblockStatus.getContext() == null || !skyblockStatus.getContext().getMapProcessor().isInitialized()) return;
        DungeonContext context = skyblockStatus.getContext();

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());
        DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
        if (dungeonRoom == null) return;
        if (!(dungeonRoom.getRoomProcessor() instanceof GeneralRoomProcessor)) return;
        String id = mGuiMechanicBrowser.getPanelMechanicBrowser().getSelectedID();
        if (id != null) {
            Optional.ofNullable(dungeonRoom.getMechanics().get(mGuiMechanicBrowser.getPanelMechanicBrowser().getSelectedID()))
                    .ifPresent(a -> {
                        a.highlight(new Color(0,255,255,50), id +" ("+(
                                dungeonRoom.getMechanics().get(id).getRepresentingPoint(dungeonRoom) != null ?
                                String.format("%.1f", MathHelper.sqrt_double((dungeonRoom.getMechanics().get(id)).getRepresentingPoint(dungeonRoom).getBlockPos(dungeonRoom).distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()))) : "")
                                +"m)", dungeonRoom, partialTicks);
                    });
        }
    }
    @Override
    public List<MPanel> getTooltipForEditor(GuiGuiLocationConfig guiGuiLocationConfig) {
        List<MPanel> mPanels = super.getTooltipForEditor(guiGuiLocationConfig);

            mPanels.add(new MPassiveLabelAndElement("Scale", new MFloatSelectionButton(FeatureMechanicBrowse.this.<Float>getParameter("scale").getValue()) {{
                setOnUpdate(() ->{
                    FeatureMechanicBrowse.this.<Float>getParameter("scale").setValue(this.getData());
                }); }
            }));

        return mPanels;
    }
}
