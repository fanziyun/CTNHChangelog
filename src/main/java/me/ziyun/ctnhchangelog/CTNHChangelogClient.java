package me.ziyun.ctnhchangelog;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CTNHChangelogClient implements ClientModInitializer {
    public static final String MOD_ID = "ctnhchangelog";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("CTNH Changelog Fabric 版正在初始化...");
        // 原本 Forge 版 @Mod 构造函数里的逻辑，稍后我们要挪到这里
    }
}