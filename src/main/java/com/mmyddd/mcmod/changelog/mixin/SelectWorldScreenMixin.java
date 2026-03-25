package com.mmyddd.mcmod.changelog.mixin;

import com.mmyddd.mcmod.changelog.client.ChangelogEntry;
import com.mmyddd.mcmod.changelog.client.ChangelogOverviewScreen;
import com.mmyddd.mcmod.changelog.client.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SelectWorldScreen.class)
public abstract class SelectWorldScreenMixin extends Screen {

    @Shadow
    private TextFieldWidget searchBox; // Fabric/Yarn 映射中 EditBox 叫 TextFieldWidget

    @Unique
    private ButtonWidget ctnhChangelogButton;

    protected SelectWorldScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (!Config.showButtonOnSelectWorld()) return;

        // 放在搜索框右侧或下方，这里演示放在搜索框右侧
        int buttonX = this.searchBox.getX() + this.searchBox.getWidth() + 5;
        int buttonY = this.searchBox.getY();

        this.ctnhChangelogButton = ButtonWidget.builder(
                Text.translatable("ctnhchangelog.button.changelog"),
                button -> {
                    ChangelogEntry.resetLoaded();
                    ChangelogEntry.loadAfterConfig();
                    MinecraftClient.getInstance().setScreen(new ChangelogOverviewScreen(this));
                }
        ).dimensions(buttonX, buttonY, 60, 20).build();

        this.addDrawableChild(ctnhChangelogButton);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // 如果需要绘制更新图标，逻辑同 TitleScreenMixin
    }
}