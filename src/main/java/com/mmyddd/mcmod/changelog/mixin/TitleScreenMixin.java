package com.mmyddd.mcmod.changelog.mixin;

import com.mmyddd.mcmod.changelog.CTNHChangelog;
import com.mmyddd.mcmod.changelog.client.ChangelogEntry;
import com.mmyddd.mcmod.changelog.client.ChangelogOverviewScreen;
import com.mmyddd.mcmod.changelog.client.VersionCheckService;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    @Unique
    private Button ctnhChangelogButton;

    // 渐变动画相关
    @Unique
    private static final int FADE_IN_DURATION = 40; // 淡入时长(帧)，约2秒
    @Unique
    private int ctnhFadeInTimer = 0;
    @Unique
    private boolean ctnhTextVisible = false;

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInitTail(CallbackInfo ci) {
        // 进入主界面时重置淡入动画
        ctnhFadeInTimer = 0;
        ctnhTextVisible = true;

        if (CTNHChangelog.config == null || !CTNHChangelog.config.showOnTitle) return;

        int buttonY = this.height / 4 + CTNHChangelog.config.buttonYOffset;

        this.ctnhChangelogButton = Button.builder(
                Component.translatable("menu.ctnhchangelog.button"),
                button -> {
                    ChangelogEntry.loadAfterConfig();
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new ChangelogOverviewScreen(this));
                    }
                }
        ).bounds(this.width / 2 - 100, buttonY, 200, 20).build();

        this.addRenderableWidget(this.ctnhChangelogButton);
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void onRender(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (CTNHChangelog.config == null) return;

        // 淡入动画：递增 timer
        if (ctnhTextVisible && ctnhFadeInTimer < FADE_IN_DURATION) {
            ctnhFadeInTimer++;
        }
        // 计算透明度 0~255
        int alpha = Math.min(255, (int) (ctnhFadeInTimer * 255.0f / FADE_IN_DURATION));
        if (alpha <= 0) return;

        // 1. 读取配置
        String displayName = CTNHChangelog.config.modpackName;
        String currentVersion = CTNHChangelog.config.modpackVersion;
        String text = currentVersion + " " + displayName;

        // 2. 拼接更新状态提示
        StringBuilder info = new StringBuilder(text);
        if (VersionCheckService.isCheckDone()) {
            if (VersionCheckService.hasUpdate()) {
                String latest = VersionCheckService.getLatestChangelogVersion();
                if (latest != null && !latest.isEmpty()) {
                    info.append(" §6(发现新版本v").append(latest).append("!)");
                } else {
                    info.append(" §6(有更新!)");
                }
            } else {
                info.append(" §a(已是最新版本)");
            }
        }

        String finalString = info.toString();
        int textWidth = this.font.width(finalString);

        // 3. 限制文字不超出屏幕右边界
        int maxX = this.width - 2;
        int x = 2;
        int y = this.height - CTNHChangelog.config.versionYOffset;

        if (x + textWidth > maxX) {
            int available = maxX - x;
            String ellipsis = "...";
            int ellipsisWidth = this.font.width(ellipsis);
            String truncated = finalString;
            while (this.font.width(truncated) + ellipsisWidth > available && truncated.length() > 0) {
                truncated = truncated.substring(0, truncated.length() - 1);
            }
            finalString = truncated + ellipsis;
            textWidth = this.font.width(finalString);
        }

        // 4. 渲染（带透明度渐变）
        boolean isHovered = mouseX >= x && mouseX <= x + textWidth
                && mouseY >= y && mouseY <= y + 9;
        int baseColor = isHovered ? 0xFFFFFF55 : 0xFFFFFFFF;
        // 把 alpha 混入颜色（ARGB 格式）
        int color = (alpha << 24) | (baseColor & 0x00FFFFFF);
        context.text(this.font, finalString, x, y, color);
    }
}
