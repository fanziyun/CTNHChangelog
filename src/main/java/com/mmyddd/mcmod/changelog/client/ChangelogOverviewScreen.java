package com.mmyddd.mcmod.changelog.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ChangelogOverviewScreen extends Screen {
    private final Screen parent;
    private ChangelogList list;

    public ChangelogOverviewScreen(Screen parent) {
        super(Text.translatable("menu.ctnhchangelog.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // 1. 刷新按钮
        this.addDrawableChild(ButtonWidget.builder(Text.literal("↻").formatted(Formatting.BOLD), button -> {
            ChangelogEntry.resetLoaded();
            if (this.client != null) {
                this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.2F));
                ChangelogEntry.loadAfterConfig().thenRun(() -> this.client.execute(this::clearAndInit));
            }
        }).dimensions(this.width - 30, 10, 20, 20).build());

        // 2. 返回按钮
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.back"), button -> this.close())
                .dimensions(this.width / 2 - 100, this.height - 30, 200, 20).build());

        // 3. 【核心修复 1】标准化的异步 UI 加载
        // 我们不再在 render 方法里动态创建列表，而是在 init 初始化时搞定。
        if (ChangelogEntry.isLoaded()) {
            this.list = new ChangelogList(this.client, this.width, this.height, 40, 48, this);
            // 将列表注册为可绘制组件，让 Minecraft 引擎负责它的渲染和状态隔离！
            this.addDrawableChild(this.list);
        } else if (!ChangelogEntry.isLoading()) {
            ChangelogEntry.loadAfterConfig().thenRun(() -> {
                if (this.client != null) {
                    // 数据下载完毕后，重新触发 init() 构建列表
                    this.client.execute(this::clearAndInit);
                }
            });
        }
    }

    // 【核心修复 2】覆写专属的背景渲染方法
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // 1. 画旋转背景
        this.renderPanoramaBackground(context, delta);
        // 2. 开启原版模糊
        this.renderInGameBackground(context);
        // 3. 强制结算！此时渲染引擎会把模糊层画死在底层，绝不会污染后续加入的 List
        context.draw();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 这一步会自动调用上面的 renderBackground，然后安全地绘制按钮和列表
        super.render(context, mouseX, mouseY, delta);

        // 绝对置顶的文字层
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