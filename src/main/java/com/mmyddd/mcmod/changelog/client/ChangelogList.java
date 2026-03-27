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

        if (ChangelogEntry.isLoaded()) {
            for (ChangelogEntry entry : ChangelogEntry.getAllEntries()) {
                this.addEntry(new Entry(entry));
            }
        }
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {}

    public class Entry extends EntryListWidget.Entry<Entry> {
        private final ChangelogEntry entry;

        public Entry(ChangelogEntry entry) {
            this.entry = entry;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean isSelected, float delta) {
            MinecraftClient client = MinecraftClient.getInstance();
            int x = this.getX();
            int y = this.getY();
            int entryWidth = ChangelogList.this.getRowWidth();
            int entryHeight = ChangelogList.this.itemHeight;

            // --- 核心高亮判定逻辑 ---
            // 判断鼠标是否在当前这一行的矩形范围内
            boolean isHovered = mouseX >= x && mouseX <= x + entryWidth && mouseY >= y && mouseY <= y + entryHeight;

            // 定义颜色：指上去白色 (0xFFFFFFFF)，没指上去灰色 (0xFFAAAAAA)
            int mainColor = isHovered ? 0xFFFFFFFF : 0xFFAAAAAA;
            // 判定背景：指上去时铺一层淡淡的白光遮罩 (约 12% 透明度)
            if (isHovered) {
                context.fill(x - 2, y, x + entryWidth + 2, y + entryHeight, 0x20FFFFFF);
            }

            // 1. 绘制版本号 (高亮时为青色，常态为暗青色)
            Formatting vFormat = isHovered ? Formatting.AQUA : Formatting.DARK_AQUA;
            Text vText = Text.literal(entry.getVersion()).formatted(vFormat, Formatting.BOLD);
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

                // 标签背景：高亮时完全不透明，常态时带一点透明度
                int alpha = isHovered ? 0xFF000000 : 0xAA000000;
                context.fill(currentX - 2, y + 3, currentX + labelWidth + 2, y + 13, (labelColor & 0xFFFFFF) | alpha);
                context.drawText(client.textRenderer, displayLabel, currentX, y + 4, 0xFFFFFFFF, false);

                currentX += labelWidth + 8;
            }

            // 3. 绘制日期 (随悬停变色)
            Text dText = Text.literal(entry.getDate());
            int dateWidth = client.textRenderer.getWidth(dText);
            context.drawTextWithShadow(client.textRenderer, dText, x + entryWidth - dateWidth - 5, y + 4, mainColor);

            // 4. 绘制标题预览 (随悬停变色)
            String preview = entry.getTitle();
            if (preview == null || preview.isEmpty()) {
                preview = !entry.getChanges().isEmpty() ? entry.getChanges().get(0) : "No description";
            }
            if (preview.length() > 40) {
                preview = preview.substring(0, 37) + "...";
            }
            context.drawTextWithShadow(client.textRenderer, Text.literal(preview), x + 5, y + 18, isHovered ? 0xFFFFFFFF : 0xFF999999);
        }

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

    @Override
    protected int getScrollbarX() {
        return this.width - 10;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderList(context, mouseX, mouseY, delta);
    }

    @Override
    protected void drawHeaderAndFooterSeparators(DrawContext context) {}
}