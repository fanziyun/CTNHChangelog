package com.mmyddd.mcmod.changelog.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class ChangelogList extends AbstractSelectionList<ChangelogList.Entry> implements ChangelogListse {
    private final Screen parentScreen;

    public ChangelogList(Minecraft client, int width, int height, int top, int itemHeight, Screen parentScreen) {
        super(client, width, height, top, itemHeight);
        this.parentScreen = parentScreen;

        if (ChangelogEntry.isLoaded()) {
            for (ChangelogEntry entry : ChangelogEntry.getAllEntries()) {
                this.addEntry(new com.mmyddd.mcmod.changelog.client.ChangelogList.Entry(entry));
            }
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput builder) {}

    public class Entry extends AbstractSelectionList.Entry<com.mmyddd.mcmod.changelog.client.ChangelogList.Entry> implements com.mmyddd.mcmod.changelog.client.Entry {
        private final ChangelogEntry entry;

        public Entry(ChangelogEntry entry) {
            this.entry = entry;
        }

        @Override
        public void  renderContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean isSelected, float delta) {
            Minecraft client = Minecraft.getInstance();
            int x = this.getX();
            int y = this.getY();
            int entryWidth = ChangelogList.this.getRowWidth();
            int entryHeight = ChangelogList.this.defaultEntryHeight;

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
            ChatFormatting vFormat = isHovered ? ChatFormatting.AQUA : ChatFormatting.DARK_AQUA;
            Component vText = Component.literal(entry.getVersion()).withStyle(vFormat, ChatFormatting.BOLD);
            context.text(client.font, vText, x + 5, y + 4, 0xFFFFFFFF);

            // 2. 动态绘制标签 (Types + Tags)
            int currentX = x + 5 + client.font.width(vText) + 8;
            List<String> allLabels = new ArrayList<>();
            if (entry.getTypes() != null) allLabels.addAll(entry.getTypes());
            if (entry.getTags() != null) allLabels.addAll(entry.getTags());

            for (String label : allLabels) {
                String displayLabel = label.toUpperCase();
                int labelWidth = client.font.width(displayLabel);
                int labelColor = ChangelogEntry.getTagColor(label);

                // 标签背景：高亮时完全不透明，常态时带一点透明度
                int alpha = isHovered ? 0xFF000000 : 0xAA000000;
                context.fill(currentX - 2, y + 3, currentX + labelWidth + 2, y + 13, (labelColor & 0xFFFFFF) | alpha);
                context.text(client.font, displayLabel, currentX, y + 4, 0xFFFFFFFF, false);

                currentX += labelWidth + 8;
            }

            // 3. 绘制日期 (随悬停变色)
            Component dText = Component.literal(entry.getDate());
            int dateWidth = client.font.width(dText);
            context.text(client.font, dText, x + entryWidth - dateWidth - 5, y + 4, mainColor);

            // 4. 绘制标题预览 (随悬停变色)
            String preview = entry.getTitle();
            if (preview == null || preview.isEmpty()) {
                preview = !entry.getChanges().isEmpty() ? entry.getChanges().getFirst() : "No description";
            }
            if (preview.length() > 40) {
                preview = preview.substring(0, 37) + "...";
            }
            context.text(client.font, Component.literal(preview), x + 5, y + 18, isHovered ? 0xFFFFFFFF : 0xFF999999);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent click, boolean doubleClick) {
            if (click.button() == 0) {
                Minecraft.getInstance().getSoundManager().play(
                        net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                                net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F
                        )
                );
                Minecraft.getInstance().setScreen(new ChangelogDetailScreen(this.entry, parentScreen));
                return true;
            }
            return super.mouseClicked(click, doubleClick);
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {

        }
    }

    @Override
    public int getRowWidth() {
        return 300;
    }

    @Override
    protected int scrollBarX() {
        return this.width - 10;
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        this.renderListItems(context, mouseX, mouseY, delta);
    }

    private void renderListItems(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    }


}