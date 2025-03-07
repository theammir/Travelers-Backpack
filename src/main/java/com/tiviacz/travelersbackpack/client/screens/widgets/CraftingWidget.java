package com.tiviacz.travelersbackpack.client.screens.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.tiviacz.travelersbackpack.TravelersBackpack;
import com.tiviacz.travelersbackpack.client.screens.TravelersBackpackScreen;
import com.tiviacz.travelersbackpack.config.TravelersBackpackConfig;
import com.tiviacz.travelersbackpack.inventory.SettingsManager;
import com.tiviacz.travelersbackpack.network.ServerboundSettingsPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public class CraftingWidget extends WidgetBase
{
    public CraftingWidget(TravelersBackpackScreen screen, int x, int y, int width, int height)
    {
        super(screen, x, y, width, height);
        this.isVisible = !TravelersBackpackConfig.disableCrafting;
        this.showTooltip = true;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;

        if(zOffset != 0)
        {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, zOffset);
        }

        RenderSystem.enableDepthTest();
        renderBg(guiGraphics, Minecraft.getInstance(), mouseX, mouseY);

        if(zOffset != 0)
        {
            guiGraphics.pose().popPose();
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, Minecraft minecraft, int mouseX, int mouseY)
    {
        if(isVisible())
        {
            guiGraphics.blit(TravelersBackpackScreen.SETTINGS_TRAVELERS_BACKPACK, isWidgetActive ? x - 3 : x, y, isWidgetActive ? 29 : 48, isWidgetActive ? 41 : 0, width, height);

            if(isWidgetActive())
            {
                if(in(mouseX, mouseY, x + 14, y + 16, 10, 10))
                {
                    guiGraphics.blit(TravelersBackpackScreen.SETTINGS_TRAVELERS_BACKPACK, x + 14, y + 16, 11, 83, 10, 10);
                }

                if(screen.container.getSettingsManager().isCraftingGridLocked())
                {
                    guiGraphics.blit(TravelersBackpackScreen.SETTINGS_TRAVELERS_BACKPACK, x + 15, y + 17, 1, 73, 8, 8);
                }

                if(in(mouseX, mouseY, x + 14, y + 28, 10, 10))
                {
                    guiGraphics.blit(TravelersBackpackScreen.SETTINGS_TRAVELERS_BACKPACK, x + 14, y + 28, 11, 83, 10, 10);
                }

                if(screen.container.getSettingsManager().renderOverlay())
                {
                    guiGraphics.blit(TravelersBackpackScreen.SETTINGS_TRAVELERS_BACKPACK, x + 15, y + 29, 1, 73, 8, 8);
                }
            }
        }
    }

    @Override
    public void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY)
    {
        if(isHovered && showTooltip && isVisible)
        {
            if(!isWidgetActive())
            {
                guiGraphics.renderTooltip(screen.getFont(), Component.translatable("screen.travelersbackpack.crafting"), mouseX, mouseY);
            }
            else
            {
                if(in(mouseX, mouseY, x, y + 3, 13, 11))
                {
                    guiGraphics.renderTooltip(screen.getFont(), Component.translatable("screen.travelersbackpack.crafting"), mouseX, mouseY);
                }

                if(in(mouseX, mouseY, x, y + 15, 12, 11))
                {
                    guiGraphics.renderComponentTooltip(screen.getFont(), List.of(Component.translatable("screen.travelersbackpack.crafting_lock"), Component.translatable("screen.travelersbackpack.crafting_lock_description")), mouseX, mouseY);
                }

                if(in(mouseX, mouseY, x, y + 27, 12, 11))
                {
                    guiGraphics.renderComponentTooltip(screen.getFont(), List.of(Component.translatable("screen.travelersbackpack.crafting_overlay"), Component.translatable("screen.travelersbackpack.crafting_overlay_description")), mouseX, mouseY);
                }
            }
        }
    }

    @Override
    public void setWidgetStatus(boolean status)
    {
        super.setWidgetStatus(status);
        //screen.craftingWidget.setTooltipVisible(!status);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(isHovered)
        {
            if(this.isWidgetActive)
            {
                if(mouseX >= x && mouseY >= y + 3 && mouseX < x + 13 && mouseY < y + 15)
                {
                    setWidgetStatus(false);
                }

                if(mouseX >= x + 14 && mouseY >= y + 16 && mouseX < x + 24 && mouseY < y + 26)
                {
                    boolean isCraftingLocked = screen.container.getSettingsManager().isCraftingGridLocked();
                    screen.container.getSettingsManager().set(SettingsManager.CRAFTING, SettingsManager.LOCK_CRAFTING_GRID, (byte)(isCraftingLocked ? 0 : 1));
                    TravelersBackpack.NETWORK.sendToServer(new ServerboundSettingsPacket(screen.container.getScreenID(), SettingsManager.CRAFTING, SettingsManager.LOCK_CRAFTING_GRID, (byte)(isCraftingLocked ? 0 : 1)));
                }

                if(mouseX >= x + 14 && mouseY >= y + 28 && mouseX < x + 24 && mouseY < y + 38)
                {
                    boolean renderOverlay = screen.container.getSettingsManager().renderOverlay();
                    screen.container.getSettingsManager().set(SettingsManager.CRAFTING, SettingsManager.RENDER_OVERLAY, (byte)(renderOverlay ? 0 : 1));
                    TravelersBackpack.NETWORK.sendToServer(new ServerboundSettingsPacket(screen.container.getScreenID(), SettingsManager.CRAFTING, SettingsManager.RENDER_OVERLAY, (byte)(renderOverlay ? 0 : 1)));
                }
            }
            else
            {
                setWidgetStatus(true);
            }

            if(this.isWidgetActive)
            {
                this.height = 42;
                this.width = 31;
                this.zOffset = 0;
            }

            if(!this.isWidgetActive)
            {
                this.height = 18;
                this.width = 15;
                this.zOffset = 0;
            }

            this.screen.playUIClickSound();
            return true;
        }
        return false;
    }

    @Override
    public void setFocused(boolean p_265728_) {

    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public boolean isSettingsChild()
    {
        return false;
    }

    @Override
    public int[] getWidgetSizeAndPos()
    {
        int[] size = new int[4];
        size[0] = x;
        size[1] = y;
        size[2] = width;
        size[3] = height;
        return size;
    }
}