package com.mmyddd.mcmod.changelog;

import com.mmyddd.mcmod.changelog.client.ChangelogEntry;
import com.mmyddd.mcmod.changelog.client.Config;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.TitleScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 适配 Fabric 1.21.1 的 CTNH 更新日志主类
 * 负责模组启动时的配置加载与数据初始化
 */
public class CTNHChangelog implements ClientModInitializer {
    public static final String MOD_ID = "ctnhchangelog";
    public static final Logger LOGGER = LoggerFactory.getLogger("CTNH-Changelog");

    @Override
    public void onInitializeClient() {
        LOGGER.info("***********************************************");
        LOGGER.info("正在初始化 CTNH Changelog Fabric 版...");

        // 1. 加载本地配置文件
        // 这一步必须最先执行，否则后续加载器拿到的 URL 永远是默认值
        try {
            Config.load();
            LOGGER.info("[Config] 配置文件加载成功！当前配置版本: {}", Config.getModpackVersion());
            LOGGER.info("[Config] 远程 URL: {}", Config.getChangelogUrl());
        } catch (Exception e) {
            LOGGER.error("[Config] 配置文件加载失败，将使用代码内置默认值: ", e);
        }

        // 2. 初始化更新日志加载器
        // 触发异步网络请求去获取 JSON 数据
        try {
            LOGGER.info("[Loader] 正在启动异步更新日志下载器...");
            ChangelogEntry.initLoader();

            // 如果配置了启动时自动加载，可以在这里预触发
            if (Config.isEnableVersionCheck()) {
                ChangelogEntry.loadAfterConfig();
            }
        } catch (Exception e) {
            LOGGER.error("[Loader] 初始化加载器时发生异常: ", e);
        }

        // 3. 注册屏幕初始化事件 (可选逻辑)
        // 可以在这里监听玩家是否进入了主界面
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen) {
                // 如果你想在进入主菜单时做些什么（比如检测到新版本弹出提醒），代码写在这里
            }
        });

        LOGGER.info("CTNH Changelog 初始化流程执行完毕！");
        LOGGER.info("***********************************************");
    }
}