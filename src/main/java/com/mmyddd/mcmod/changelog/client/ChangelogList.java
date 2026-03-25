package com.mmyddd.mcmod.changelog.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
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
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        // 1.21.1 抽象方法实现
    }

    public class Entry extends EntryListWidget.Entry<Entry> {
        private final ChangelogEntry entry;

        public Entry(ChangelogEntry entry) {
            this.entry = entry;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            // 1. 绘制版本号
            Text vText = Text.literal(entry.getVersion()).formatted(Formatting.AQUA, Formatting.BOLD);
            context.drawTextWithShadow(client.textRenderer, vText, x + 5, y + 5, 0xFFFFFFFF);

            // 2. 动态计算标签位置
            int currentX = x + 5 + client.textRenderer.getWidth(vText) + 8;

            // --- 核心修复：合并 JSON 中的 type 和 tags ---
            List<String> allLabels = new ArrayList<>();
            if (entry.getTypes() != null) allLabels.addAll(entry.getTypes());
            if (entry.getTags() != null) allLabels.addAll(entry.getTags());

            for (String label : allLabels) {
                int labelWidth = client.textRenderer.getWidth(label);
                int labelColor = ChangelogEntry.getTagColor(label);

                // 绘制背景矩形
                context.fill(currentX - 2, y + 4, currentX + labelWidth + 2, y + 14, labelColor);

                // 绘制标签文字 (白色文字兼容性最好)
                context.drawText(client.textRenderer, label, currentX, y + 5, 0xFFFFFFFF, false);

                // 累加 X 坐标，防止重叠
                currentX += labelWidth + 8;
            }

            // 3. 绘制日期 (靠右对齐)
            Text dText = Text.literal(entry.getDate()).formatted(Formatting.GRAY);
            context.drawTextWithShadow(client.textRenderer, dText,
                    x + entryWidth - client.textRenderer.getWidth(dText) - 5, y + 5, 0xFFFFFFFF);

            // 4. 绘制预览 (Title 或 Changes 第一行)
            String preview = entry.getTitle();
            if (preview.isEmpty() && !entry.getChanges().isEmpty()) {
                preview = entry.getChanges().get(0);
            }

            if (preview.length() > 30) preview = preview.substring(0, 27) + "...";
            context.drawTextWithShadow(client.textRenderer, Text.literal(preview).formatted(Formatting.WHITE), x + 5, y + 22, 0xFFFFFFFF);

            // 5. 悬停高亮
            if (hovered) {
                context.fill(x, y, x + entryWidth, y + entryHeight, 0x22FFFFFF);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                MinecraftClient.getInstance().getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.0F));
                MinecraftClient.getInstance().setScreen(new ChangelogDetailScreen(this.entry, parentScreen));
                return true;
            }
            return false;
        }
    }

    @Override
    public int getRowWidth() { return 280; }
}