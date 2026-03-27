package com.mmyddd.mcmod.changelog.client;

import com.mmyddd.mcmod.changelog.CTNHChangelog;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Util;

public class ChangelogOverviewScreen extends Screen {
    private final Screen parent;

    public ChangelogOverviewScreen(Screen parent) {
        super(Component.translatable("menu.ctnhchangelog.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(Component.literal("↻").withStyle(ChatFormatting.BOLD), button -> {
            ChangelogEntry.resetLoaded();
            if (this.minecraft != null) {
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2F));
                ChangelogEntry.loadAfterConfig().thenRun(() -> this.minecraft.execute(this::rebuildWidgets));
            }
        }).bounds(this.width - 30, 10, 20, 20).build());

        int totalWidth = 204;
        int spacing = 4;
        int linkWidth = totalWidth / 3;
        int backWidth = totalWidth - linkWidth - spacing;
        int startX = this.width / 2 - totalWidth / 2;
        int y = this.height - 30;

        // 从 Config 读取按钮名字和 URL
        String btnName = CTNHChangelog.config.getExternalLinkName();
        String url = CTNHChangelog.config.getExternalLinkUrl();

        this.addRenderableWidget(Button.builder(Component.literal(btnName), button -> {
            if (url != null && !url.isEmpty()) {
                Util.getPlatform().openUri(url);
            }
        }).bounds(startX, y, linkWidth, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> this.onClose())
                .bounds(startX + linkWidth + spacing, y, backWidth, 20).build());

        if (ChangelogEntry.isLoaded()) {
            ChangelogList list = new ChangelogList(this.minecraft, this.width, this.height, 40, 48, this);
            this.addRenderableWidget(list);
        } else if (!ChangelogEntry.isLoading()) {
            ChangelogEntry.loadAfterConfig().thenRun(() -> {
                if (this.minecraft != null) {
                    this.minecraft.execute(this::rebuildWidgets);
                }
            });
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        this.extractPanorama(context, delta);
        this.extractTransparentBackground(context);
    }

    // 这里是关键修改 1：改名并替换了 context 类型
    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        this.drawHeaderAndFooter(context);
    }

    // 这里是关键修改 2：替换了 context 类型
    private void drawHeaderAndFooter(GuiGraphicsExtractor context) {
        context.centeredText(this.font, this.getTitle(), this.width / 2, 15, 0xFFFFFFFF);
        if (ChangelogEntry.isLoading()) {
            context.centeredText(this.font, Component.literal("正在获取最新内容..."), this.width / 2, this.height / 2, 0xFFFFAA00);
        }
        context.centeredText(this.font, Component.literal(ChangelogEntry.getFooterText()), this.width / 2, this.height - 45, 0xAAAAAA);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}