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

    @Inject(method = "init", at = @At("HEAD"))
    private void onInitHead(CallbackInfo ci) {
        if (CTNHChangelog.config != null && CTNHChangelog.config.enableVersionCheck) {
            VersionCheckService.checkForUpdate();
        }
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInitTail(CallbackInfo ci) {
        if (CTNHChangelog.config == null) return;

        int buttonY = this.height / 4 + 48 + 72 + 12 + 24;

        this.ctnhChangelogButton = ButtonWidget.builder(
                Text.translatable("menu.ctnhchangelog.button"),
                button -> {
                    ChangelogEntry.loadAfterConfig();
                    if (this.client != null) {
                        this.client.setScreen(new ChangelogOverviewScreen(this));
                    }
                }
        ).dimensions(this.width / 2 - 100, buttonY, 200, 20).build();

        this.addDrawableChild(ctnhChangelogButton);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (CTNHChangelog.config == null) return;

        String localVer = CTNHChangelog.config.modpackVersion;
        StringBuilder info = new StringBuilder("363 Modpack: v").append(localVer);

        if (VersionCheckService.isCheckDone()) {
            if (VersionCheckService.hasUpdate()) {
                info.append(" §6(发现新版本: v").append(VersionCheckService.getLatestChangelogVersion()).append("!)");

                if (ctnhChangelogButton != null) {
                    boolean blink = (System.currentTimeMillis() / 500 & 1) == 1;
                    if (blink) {
                        context.drawTextWithShadow(this.textRenderer, "!",
                                ctnhChangelogButton.getX() + ctnhChangelogButton.getWidth() - 12,
                                ctnhChangelogButton.getY() + 6, 0xFF5555);
                    }
                }
            } else {
                info.append(" §a(已是最新)");
            }
        } else {
            info.append(" §7(正在检查更新...)");
        }

        context.drawTextWithShadow(this.textRenderer, info.toString(), 2, this.height - 20, 0xFFFFFF);
    }
}