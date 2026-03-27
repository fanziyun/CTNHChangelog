package com.mmyddd.mcmod.changelog;

import com.mmyddd.mcmod.changelog.client.ChangelogEntry;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CTNHChangelog implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("CTNH-Changelog");

    // 静态变量，供全局调用
    public static ModConfig config;

    @Override
    public void onInitializeClient() {
        LOGGER.info("***********************************************");
        LOGGER.info("正在初始化 Changelog Fabric 版...");

        // 1. 核心步骤：向 Cloth Config 注册配置类
        // 注意：必须引用 com.mmyddd.mcmod.changelog 下的 ModConfig
        ConfigHolder<ModConfig> holder = AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);

        // 2. 强制加载磁盘上的 JSON 文件
        holder.load();
        config = holder.getConfig();

        LOGGER.info("[Config] 配置文件加载成功！当前配置版本: {}", config.modpackVersion);
        LOGGER.info("[Config] 远程 URL: {}", config.changelogUrl);

        // 3. 初始化更新日志加载器
        try {
            LOGGER.info("[Loader] 正在启动异步更新日志下载器...");
            ChangelogEntry.initLoader();

            // 根据配置决定是否触发
            if (config.enableVersionCheck) {
                ChangelogEntry.loadAfterConfig();
            }
        } catch (Exception e) {
            LOGGER.error("[Loader] 初始化加载器时发生异常: ", e);
        }

        // 4. 注册主界面监听
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            // 如果需要进入主界面自动弹窗，逻辑写这里
        });

        LOGGER.info("CTNH Changelog 初始化流程执行完毕！");
        LOGGER.info("***********************************************");
    }
}