package me.ziyun.ctnhchangelog;

import com.mmyddd.mcmod.changelog.ModConfig; // 确保 import 了正确的配置类
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CTNHChangelogClient implements ClientModInitializer {
    public static final String MOD_ID = "ctnhchangelog";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("CTNH Changelog Fabric 版正在初始化...");

        // 必须在这里注册，否则 ModMenu 打开设置时会崩溃
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);

        LOGGER.info("Cloth Config 注册完成！");

        // 这里可以放置你原本在 Forge 版里的逻辑，比如：
        // ChangelogLoader.load();
    }
}