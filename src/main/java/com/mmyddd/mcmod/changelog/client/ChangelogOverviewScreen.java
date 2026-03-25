package com.mmyddd.mcmod.changelog.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ChangelogOverviewScreen extends Screen {
    private final Screen parent;
    private ChangelogList list;
    private boolean listInitialized = false;

    public ChangelogOverviewScreen(Screen parent) {
        super(Text.literal("整合包更新日志"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // 1. 刷新按钮 (↻)
        this.addDrawableChild(ButtonWidget.builder(Text.literal("↻").formatted(Formatting.BOLD), button -> {
            ChangelogEntry.resetLoaded();
            this.listInitialized = false;
            // 异步加载并重启 Screen
            ChangelogEntry.loadAfterConfig().thenRun(() -> {
                if (this.client != null) {
                    this.client.execute(this::clearAndInit);
                }
            });
            if (this.client != null) {
                this.client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(
                        net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.2F));
            }
        }).dimensions(this.width - 30, 10, 20, 20).build());

        // 2. 返回按钮
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.back"), button -> this.close())
                .dimensions(this.width / 2 - 100, this.height - 30, 200, 20).build());

        // 3. 核心逻辑：只要数据好了，立刻创建 UI
        if (ChangelogEntry.isLoaded()) {
            this.createList();
        } else if (!ChangelogEntry.isLoading()) {
            ChangelogEntry.loadAfterConfig();
        }
    }

    private void createList() {
        // 修复：直接重新赋值并添加，不再调用找不到的 removeInternal
        this.list = new ChangelogList(this.client, this.width, this.height, 40, 40, this);
        this.addDrawableChild(this.list);
        this.listInitialized = true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        // 如果在渲染期间数据才加载完，补全 UI
        if (ChangelogEntry.isLoaded() && !listInitialized) {
            this.createList();
        }

        super.render(context, mouseX, mouseY, delta);

        if (ChangelogEntry.isLoading()) {
            context.drawCenteredTextWithShadow(this.textRenderer, "正在从服务器获取最新内容...", this.width / 2, this.height / 2, 0xFFFFAA00);
        }

        context.drawCenteredTextWithShadow(this.textRenderer, this.getTitle(), this.width / 2, 15, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(ChangelogEntry.getFooterText()), this.width / 2, this.height - 45, 0xAAAAAA);
    }

    @Override
    public void close() {
        if (this.client != null) this.client.setScreen(this.parent);
    }
}