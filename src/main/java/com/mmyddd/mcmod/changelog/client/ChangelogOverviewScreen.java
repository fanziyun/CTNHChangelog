package com.mmyddd.mcmod.changelog.client;

import com.mmyddd.mcmod.changelog.CTNHChangelog;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

public class ChangelogOverviewScreen extends Screen {
    private final Screen parent;

    public ChangelogOverviewScreen(Screen parent) {
        super(Text.translatable("menu.ctnhchangelog.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(Text.literal("↻").formatted(Formatting.BOLD), button -> {
            ChangelogEntry.resetLoaded();
            if (this.client != null) {
                this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.2F));
                ChangelogEntry.loadAfterConfig().thenRun(() -> this.client.execute(this::clearAndInit));
            }
        }).dimensions(this.width - 30, 10, 20, 20).build());

        int totalWidth = 204;
        int spacing = 4;
        int linkWidth = totalWidth / 3;
        int backWidth = totalWidth - linkWidth - spacing;
        int startX = this.width / 2 - totalWidth / 2;
        int y = this.height - 30;

        // 从 Config 读取按钮名字和 URL
        String btnName = CTNHChangelog.config.getExternalLinkName();
        String url = CTNHChangelog.config.getExternalLinkUrl();

        this.addDrawableChild(ButtonWidget.builder(Text.literal(btnName), button -> {
            if (url != null && !url.isEmpty()) {
                Util.getOperatingSystem().open(url);
            }
        }).dimensions(startX, y, linkWidth, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.back"), button -> this.close())
                .dimensions(startX + linkWidth + spacing, y, backWidth, 20).build());

        if (ChangelogEntry.isLoaded()) {
            ChangelogList list = new ChangelogList(this.client, this.width, this.height, 40, 48, this);
            this.addDrawableChild(list);
        } else if (!ChangelogEntry.isLoading()) {
            ChangelogEntry.loadAfterConfig().thenRun(() -> {
                if (this.client != null) {
                    this.client.execute(this::clearAndInit);
                }
            });
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderPanoramaBackground(context, delta);
        this.renderInGameBackground(context);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawHeaderAndFooter(context);
    }

    private void drawHeaderAndFooter(DrawContext context) {
        context.drawCenteredTextWithShadow(this.textRenderer, this.getTitle(), this.width / 2, 15, 0xFFFFFFFF);
        if (ChangelogEntry.isLoading()) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("正在获取最新内容..."), this.width / 2, this.height / 2, 0xFFFFAA00);
        }
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(ChangelogEntry.getFooterText()), this.width / 2, this.height - 45, 0xAAAAAA);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}