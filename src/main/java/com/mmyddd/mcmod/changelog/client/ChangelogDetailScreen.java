package com.mmyddd.mcmod.changelog.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public class ChangelogDetailScreen extends Screen {
    private final ChangelogEntry entry;
    private final Screen parentScreen;

    private double scrollAmount;
    private int contentHeight;
    private int listTop;
    private int listBottom;
    private int listLeft;
    private int listRight;
    private boolean isScrolling;

    public ChangelogDetailScreen(ChangelogEntry entry, Screen parentScreen) {
        super(Component.literal(entry.getVersion() + " - " + entry.getTitle()));
        this.entry = entry;
        this.parentScreen = parentScreen;
        this.scrollAmount = 0;
    }

    @Override
    protected void init() {
        this.listLeft = 30;
        this.listRight = this.width - 30;
        this.listTop = 60;
        this.listBottom = this.height - 50;

        this.contentHeight = 0;
        for (String change : entry.getChanges()) {
            List<FormattedCharSequence> lines = this.font.split(Component.literal("• " + change), listRight - listLeft - 20);
            this.contentHeight += lines.size() * 12;
        }

        this.addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> this.onClose())
                .bounds(this.width / 2 - 50, this.height - 30, 100, 20)
                .build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);

        int titleColor = (entry.getColor() & 0xFFFFFF) | 0xFF000000;
        context.centeredText(this.font, this.title, this.width / 2, 15, titleColor);

        if (!entry.getDate().isEmpty()) {
            Component dateText = Component.literal(entry.getDate());
            context.centeredText(this.font, dateText, this.width / 2, 28, 0xFFAAAAAA);
        }

        renderTags(context, 40);

        // 提示：如果文字不显示，请尝试注释掉下面这两行 Scissor
        context.enableScissor(listLeft - 5, listTop, listRight + 5, listBottom);

        int y = listTop - (int) this.scrollAmount;
        for (String change : entry.getChanges()) {
            List<FormattedCharSequence> lines = this.font.split(Component.literal("• " + change), listRight - listLeft - 20);
            for (FormattedCharSequence line : lines) {
                if (y + 12 > listTop && y < listBottom) {
                    context.text(this.font, line, listLeft, y, 0xFFDDDDDD);
                }
                y += 12;
            }
            y += 4;
        }

        context.disableScissor();

        // 绘制滚动条
        int viewHeight = this.listBottom - this.listTop;
        if (this.contentHeight > viewHeight) {
            int scrollBarHeight = (int) ((float) viewHeight * viewHeight / this.contentHeight);
            int maxScroll = Math.max(0, this.contentHeight - viewHeight);
            int scrollBarY = (int) (this.scrollAmount * (viewHeight - scrollBarHeight) / maxScroll);
            scrollBarY = Mth.clamp(scrollBarY, 0, viewHeight - scrollBarHeight);

            context.fill(this.listRight + 2, this.listTop, this.listRight + 6, this.listBottom, 0x33AAAAAA);
            context.fill(this.listRight + 2, this.listTop + scrollBarY, this.listRight + 6, this.listTop + scrollBarY + scrollBarHeight, 0xFFAAAAAA);
        }
    }

    private void renderTags(GuiGraphicsExtractor context, int y) {
        List<DisplayTag> allTags = new ArrayList<>();
        for (String type : entry.getTypes()) {
            allTags.add(new DisplayTag(type.toUpperCase(), getTypeColor(type)));
        }
        for (String tag : entry.getTags()) {
            allTags.add(new DisplayTag(tag, ChangelogEntry.getTagColor(tag)));
        }

        if (allTags.isEmpty()) return;

        int totalWidth = 0;
        for (DisplayTag tag : allTags) totalWidth += this.font.width(tag.text) + 10;

        int currentX = (this.width - totalWidth) / 2;
        for (DisplayTag tag : allTags) {
            int tagWidth = this.font.width(tag.text) + 6;
            context.fill(currentX, y - 1, currentX + tagWidth, y + 10, tag.color);
            context.text(this.font, tag.text, currentX + 3, y, 0xFFFFFFFF, false);
            currentX += tagWidth + 4;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int viewHeight = this.listBottom - this.listTop;
        if (this.contentHeight > viewHeight) {
            double maxScroll = Math.max(0, this.contentHeight - viewHeight);
            // 滚轮滚动，乘以16作为步长
            this.scrollAmount = Mth.clamp(this.scrollAmount - verticalAmount * 16, 0, maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubleClick) {
        // 使用 Minecraft 实例获取经过缩放的精确鼠标坐标，解决 Click 对象点不出方法的问题
        double mouseX = this.minecraft.mouseHandler.xpos() * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
        double mouseY = this.minecraft.mouseHandler.ypos() * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight();

        int viewHeight = this.listBottom - this.listTop;
        if (this.contentHeight > viewHeight) {
            // 判定是否点击在滚动条轨道上
            if (mouseX >= this.listRight + 2 && mouseX <= this.listRight + 6 &&
                    mouseY >= this.listTop && mouseY <= this.listBottom) {
                this.isScrolling = true;
                return true;
            }
        }
        return super.mouseClicked(click, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        this.isScrolling = false; // 停止拖动
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double dragX, double dragY) {
        if (this.isScrolling) {
            int viewHeight = this.listBottom - this.listTop;
            double maxScroll = Math.max(0, this.contentHeight - viewHeight);
            // 计算滚动比例并更新偏移量
            double scrollFactor = (double) this.contentHeight / viewHeight;
            this.scrollAmount = Mth.clamp(this.scrollAmount + dragY * scrollFactor, 0, maxScroll);
            return true;
        }
        return super.mouseDragged(click, dragX, dragY);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parentScreen);
        }
    }

    private int getTypeColor(String type) {
        return switch (type.toLowerCase()) {
            case "major" -> 0xFF5555FF;
            case "minor" -> 0xFF55FF55;
            case "patch" -> 0xFFFFFF55;
            case "hotfix", "danger" -> 0xFFFF5555;
            default -> 0xFF888888;
        };
    }

    private static class DisplayTag {
        final String text;
        final int color;
        DisplayTag(String text, int color) { this.text = text; this.color = color; }
    }
}