package com.mmyddd.mcmod.changelog.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

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
        super(Text.literal(entry.getVersion() + " - " + entry.getTitle()));
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
            List<OrderedText> lines = this.textRenderer.wrapLines(Text.literal("• " + change), listRight - listLeft - 20);
            this.contentHeight += lines.size() * 12;
        }

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.back"), button -> this.close())
                .dimensions(this.width / 2 - 50, this.height - 30, 100, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // 注意：这里调用了 entry.getColor()，所以 ChangelogEntry 类里必须有这个方法
        int titleColor = (entry.getColor() & 0xFFFFFF) | 0xFF000000;
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, titleColor);

        if (!entry.getDate().isEmpty()) {
            Text dateText = Text.literal(entry.getDate());
            context.drawCenteredTextWithShadow(this.textRenderer, dateText, this.width / 2, 28, 0xFFAAAAAA);
        }

        renderTags(context, 40);

        context.enableScissor(listLeft - 5, listTop, listRight + 5, listBottom);

        int y = listTop - (int) this.scrollAmount;
        for (String change : entry.getChanges()) {
            List<OrderedText> lines = this.textRenderer.wrapLines(Text.literal("• " + change), listRight - listLeft - 20);
            for (OrderedText line : lines) {
                if (y + 12 > listTop && y < listBottom) {
                    context.drawTextWithShadow(this.textRenderer, line, listLeft, y, 0xFFDDDDDD);
                }
                y += 12;
            }
            y += 4;
        }

        context.disableScissor();

        int viewHeight = this.listBottom - this.listTop;
        if (this.contentHeight > viewHeight) {
            int scrollBarHeight = (int) ((float) viewHeight * viewHeight / this.contentHeight);
            int maxScroll = Math.max(0, this.contentHeight - viewHeight);
            int scrollBarY = (int) (this.scrollAmount * (viewHeight - scrollBarHeight) / maxScroll);
            scrollBarY = MathHelper.clamp(scrollBarY, 0, viewHeight - scrollBarHeight);

            context.fill(this.listRight + 2, this.listTop, this.listRight + 6, this.listBottom, 0x33AAAAAA);
            context.fill(this.listRight + 2, this.listTop + scrollBarY, this.listRight + 6, this.listTop + scrollBarY + scrollBarHeight, 0xFFAAAAAA);
        }
    }

    private void renderTags(DrawContext context, int y) {
        List<DisplayTag> allTags = new ArrayList<>();
        for (String type : entry.getTypes()) {
            allTags.add(new DisplayTag(type.toUpperCase(), getTypeColor(type)));
        }
        for (String tag : entry.getTags()) {
            allTags.add(new DisplayTag(tag, ChangelogEntry.getTagColor(tag)));
        }

        if (allTags.isEmpty()) return;

        int totalWidth = 0;
        for (DisplayTag tag : allTags) totalWidth += this.textRenderer.getWidth(tag.text) + 10;

        int currentX = (this.width - totalWidth) / 2;
        for (DisplayTag tag : allTags) {
            int tagWidth = this.textRenderer.getWidth(tag.text) + 6;
            context.fill(currentX, y - 1, currentX + tagWidth, y + 10, tag.color);
            context.drawText(this.textRenderer, tag.text, currentX + 3, y, 0xFFFFFFFF, false);
            currentX += tagWidth + 4;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int viewHeight = this.listBottom - this.listTop;
        if (this.contentHeight > viewHeight) {
            double maxScroll = Math.max(0, this.contentHeight - viewHeight);
            this.scrollAmount = MathHelper.clamp(this.scrollAmount - verticalAmount * 16, 0, maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int viewHeight = this.listBottom - this.listTop;
        if (this.contentHeight > viewHeight) {
            if (mouseX >= this.listRight + 2 && mouseX <= this.listRight + 6 &&
                    mouseY >= this.listTop && mouseY <= this.listBottom) {
                this.isScrolling = true;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.isScrolling = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.isScrolling) {
            int viewHeight = this.listBottom - this.listTop;
            int maxScroll = Math.max(0, this.contentHeight - viewHeight);
            if (maxScroll > 0) {
                int scrollBarHeight = (int) ((float) viewHeight * viewHeight / this.contentHeight);
                double scrollRatio = (mouseY - this.listTop) / (double) (viewHeight - scrollBarHeight);
                this.scrollAmount = MathHelper.clamp(scrollRatio * maxScroll, 0, maxScroll);
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parentScreen);
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