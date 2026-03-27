package com.mmyddd.mcmod.changelog.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.ArrayList;
import java.util.List;

public class ChangelogList extends EntryListWidget<ChangelogList.Entry> {
    private final Screen parentScreen;

    public ChangelogList(MinecraftClient client, int width, int height, int top, int itemHeight, Screen parentScreen) {
        super(client, width, height, top, itemHeight);
        this.parentScreen = parentScreen;

        // 加载数据到列表
        if (ChangelogEntry.isLoaded()) {
            for (ChangelogEntry entry : ChangelogEntry.getAllEntries()) {
                this.addEntry(new Entry(entry));
            }
        }
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        // 1.21 必需的抽象方法实现
    }

    public class Entry extends EntryListWidget.Entry<Entry> {
        private final ChangelogEntry entry;

        public Entry(ChangelogEntry entry) {
            this.entry = entry;
        }

        // --- 核心修复：1.21.9 的 5 参数签名，删除 super.render ---
        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean isSelected, float delta) {
            // 注意：这里没有 super.render(...) 这一行！
            // 这可以防止父类绘制默认的半透明黑色背景。

            MinecraftClient client = MinecraftClient.getInstance();

            // 在 1.21 新环境里，条目自己带有坐标
            int x = this.getX();
            int y = this.getY();
            int entryWidth = ChangelogList.this.getRowWidth();

            // 1. 绘制版本号 (青色加粗)
            Text vText = Text.literal(entry.getVersion()).formatted(Formatting.AQUA, Formatting.BOLD);
            context.drawTextWithShadow(client.textRenderer, vText, x + 5, y + 4, 0xFFFFFFFF);

            // 2. 动态绘制标签 (Types + Tags)
            int currentX = x + 5 + client.textRenderer.getWidth(vText) + 8;
            List<String> allLabels = new ArrayList<>();
            if (entry.getTypes() != null) allLabels.addAll(entry.getTypes());
            if (entry.getTags() != null) allLabels.addAll(entry.getTags());

            for (String label : allLabels) {
                String displayLabel = label.toUpperCase();
                int labelWidth = client.textRenderer.getWidth(displayLabel);
                int labelColor = ChangelogEntry.getTagColor(label);

                // 绘制标签背景和文字
                context.fill(currentX - 2, y + 3, currentX + labelWidth + 2, y + 13, (labelColor & 0xFFFFFF) | 0xFF000000);
                context.drawText(client.textRenderer, displayLabel, currentX, y + 4, 0xFFFFFFFF, false);

                currentX += labelWidth + 8;
            }

            // 3. 绘制日期 (靠右对齐)
            Text dText = Text.literal(entry.getDate()).formatted(Formatting.GRAY);
            int dateWidth = client.textRenderer.getWidth(dText);
            context.drawTextWithShadow(client.textRenderer, dText, x + entryWidth - dateWidth - 5, y + 4, 0xFFFFFFFF);

            // 4. 绘制标题预览 (第二行)
            String preview = entry.getTitle();
            if (preview == null || preview.isEmpty()) {
                preview = !entry.getChanges().isEmpty() ? entry.getChanges().get(0) : "No description";
            }
            if (preview.length() > 40) {
                preview = preview.substring(0, 37) + "...";
            }
            context.drawTextWithShadow(client.textRenderer, Text.literal(preview).formatted(Formatting.WHITE), x + 5, y + 18, 0xFFCCCCCC);

            // --- 核心修复：直接删除了原本的 fill 遮罩逻辑 ---
        }

        // 这里的 Click 签名在日志里没有报错，说明是正确的
        @Override
        public boolean mouseClicked(Click click, boolean doubleClick) {
            if (click.button() == 0) {
                MinecraftClient.getInstance().getSoundManager().play(
                        net.minecraft.client.sound.PositionedSoundInstance.master(
                                net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.0F
                        )
                );
                MinecraftClient.getInstance().setScreen(new ChangelogDetailScreen(this.entry, parentScreen));
                return true;
            }
            return super.mouseClicked(click, doubleClick);
        }
    }

    @Override
    public int getRowWidth() {
        return 300;
    }

    // --- 核心修复：1.21 方法改名 ---
    @Override
    protected int getScrollbarX() {
        return this.width - 10;
    }
    // 1. 去掉列表背景（那个深色的槽位）
    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // 我们只调用渲染条目的逻辑，跳过父类中渲染深色槽位的逻辑
        this.renderList(context, mouseX, mouseY, delta);
    }

    // 2. 1.21 新版：确保背景是透明的
    @Override
    protected void drawHeaderAndFooterSeparators(DrawContext context) {
        // 留空：去掉顶部和底部的两道深色分割线遮罩
    }




}