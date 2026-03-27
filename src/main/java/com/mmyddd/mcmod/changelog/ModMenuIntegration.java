package com.mmyddd.mcmod.changelog;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        // 核心改动：不再弹出链接询问，而是弹出由 Cloth Config 自动生成的配置界面
        // 它会自动关联到你在 ModConfig 类里定义的那些字段
        return parent -> AutoConfig.getConfigScreen(ModConfig.class, parent).get();
    }
}