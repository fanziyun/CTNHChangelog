package com.mmyddd.mcmod.changelog.mixin;

import com.mmyddd.mcmod.changelog.CTNHChangelog;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SelectWorldScreen.class)
public abstract class SelectWorldScreenMixin extends Screen {
    protected SelectWorldScreenMixin(Component title) { super(title); }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        // 核心修复：改用 CTNHChangelog.config 访问变量
        if (CTNHChangelog.config == null || !CTNHChangelog.config.showOnSelectWorld) return;
        // 这里可以添加你在选择世界界面的按钮逻辑
    }
}