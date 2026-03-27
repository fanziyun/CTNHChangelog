package com.mmyddd.mcmod.changelog.mixin;

import com.mmyddd.mcmod.changelog.CTNHChangelog;
import com.mmyddd.mcmod.changelog.client.ChangelogEntry;
import com.mmyddd.mcmod.changelog.client.ChangelogOverviewScreen;
import com.mmyddd.mcmod.changelog.client.VersionCheckService;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    @Unique
    private ButtonWidget ctnhChangelogButton;

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInitTail(CallbackInfo ci) {
        if (CTNHChangelog.config == null || !CTNHChangelog.config.showOnTitle) return;

        // 从配置读取按钮 Y 轴偏移量
        int buttonY = this.height / 4 + CTNHChangelog.config.buttonYOffset;

        this.ctnhChangelogButton = ButtonWidget.builder(
                Text.translatable("menu.ctnhchangelog.button"),
                button -> {
                    ChangelogEntry.loadAfterConfig();
                    if (this.client != null) {
                        this.client.setScreen(new ChangelogOverviewScreen(this));
                    }
                }
        ).dimensions(this.width / 2 - 100, buttonY, 200, 20).build();

        this.addDrawableChild(this.ctnhChangelogButton);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (CTNHChangelog.config == null) return;

        // 1. 读取配置中的整合包名称和版本号
        String displayName = CTNHChangelog.config.modpackName;
        String text = CTNHChangelog.config.modpackVersion + " " + displayName;

        // 2. 拼接更新状态提示
        StringBuilder info = new StringBuilder(text);
        if (VersionCheckService.isCheckDone()) {
            if (VersionCheckService.hasUpdate()) {
                info.append(" §6(有更新!)");
            }
        }

        String finalString = info.toString();
        int textWidth = this.textRenderer.getWidth(finalString);

        // 3. 计算坐标 (x=2 为左边距，y 根据配置偏移量上移)
        int x = 2;
        int y = this.height - CTNHChangelog.config.versionYOffset;

        // 4. 鼠标悬停逻辑与颜色设置
        boolean isHovered = mouseX >= x && mouseX <= x + textWidth && mouseY >= y && mouseY <= y + 9;

        // 修改点：平时使用 0xFFFFFFFF (纯白)，悬停时使用 0xFFFFFF55 (淡黄)
        int color = isHovered ? 0xFFFFFF55 : 0xFFFFFFFF;

        // 5. 渲染文字
        context.drawTextWithShadow(this.textRenderer, finalString, x, y, color);
    }
}