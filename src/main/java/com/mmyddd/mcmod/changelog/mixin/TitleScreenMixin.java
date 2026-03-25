package com.mmyddd.mcmod.changelog.mixin;

import com.mmyddd.mcmod.changelog.client.ChangelogEntry;
import com.mmyddd.mcmod.changelog.client.ChangelogOverviewScreen;
import com.mmyddd.mcmod.changelog.client.Config;
import com.mmyddd.mcmod.changelog.client.VersionCheckService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    @Unique
    private ButtonWidget ctnhChangelogButton;

    @Unique
    private boolean ctnhHasUpdate = false;

    @Unique
    private static final int BLINK_INTERVAL = 800;

    @Unique
    private static final Identifier VERSION_CHECK_ICONS =
            Identifier.of("minecraft", "textures/gui/sprites/hud/heart/container.png"); // 建议换成你自己的资源路径

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void onInitHead(CallbackInfo ci) {
        // 确保 Config 类里有这些静态方法
        if (Config.isChangelogTabEnabled() && !Config.getModpackVersion().isEmpty()) {
            VersionCheckService.reset();
            VersionCheckService.checkForUpdate();
        }
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (!Config.isChangelogTabEnabled() || Config.getModpackVersion().isEmpty()) return;
        if (!Config.showButtonOnTitleScreen()) return;

        int l = this.height / 4 + 48;
        int buttonY = l + 72 + 12 + 24;

        // Fabric 1.21.1 使用 ButtonWidget.builder
        this.ctnhChangelogButton = ButtonWidget.builder(
                Text.translatable("ctnhchangelog.button.changelog"),
                button -> {
                    ChangelogEntry.resetLoaded();
                    ChangelogEntry.loadAfterConfig();
                    MinecraftClient.getInstance().setScreen(
                            new ChangelogOverviewScreen(this)
                    );
                }
        ).dimensions(this.width / 2 - 100, buttonY, 200, 20).build();

        this.addDrawableChild(ctnhChangelogButton);
    }

    @Override
    public void tick() {
        super.tick();
        if (Config.isEnableVersionCheck() && VersionCheckService.isCheckDone()) {
            ctnhHasUpdate = VersionCheckService.hasUpdate();
        } else {
            ctnhHasUpdate = false;
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!ctnhHasUpdate || ctnhChangelogButton == null) return;

        int x = ctnhChangelogButton.getX();
        int y = ctnhChangelogButton.getY();
        int w = ctnhChangelogButton.getWidth();
        int h = ctnhChangelogButton.getHeight();

        int iconX = x + w - 15;
        int iconY = y + (h / 2 - 4);

        boolean blink = (System.currentTimeMillis() / BLINK_INTERVAL & 1) == 1;

        // 1.21.1 绘制纹理建议使用 DrawContext.drawTexture
        if (blink) {
            // 参数依次为: 纹理标识符, x, y, u, v, width, height, textureWidth, textureHeight
            context.drawTexture(VERSION_CHECK_ICONS, iconX, iconY, 0, 0, 8, 8, 8, 8);
        }
    }
}