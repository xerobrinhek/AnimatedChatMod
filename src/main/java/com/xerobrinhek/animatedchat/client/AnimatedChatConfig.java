package com.xerobrinhek.animatedchat.client;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class AnimatedChatConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLE_ANIMATIONS;

    public static final ForgeConfigSpec.IntValue HISTORY_SIZE;

    public static final ForgeConfigSpec.IntValue CHAT_INPUT_MAX_LENGTH;

    static {
        BUILDER.comment("Animated Chat Mod Configuration")
                .push("general");

        ENABLE_ANIMATIONS = BUILDER
                .comment("[English]",
                        "Enable or disable chat animations",
                        "If set to false, all animations will be disabled",
                        "and replaced with static text only once",
                        "Default: true",
                        "",
                        "[Русский]",
                        "Включает или выключает анимации в чате",
                        "Если установить false, все анимации отключатся",
                        "и заменятся статичным текстом только один раз",
                        "По умолчанию: true")
                .define("enable-animations", true);

        HISTORY_SIZE = BUILDER
                .comment("",
                        "[English]",
                        "Maximum amount of messages stored in chat history",
                        "Older messages will be removed automatically",
                        "Default: 100",
                        "[Русский]",
                        "Максимальное количество сообщений в истории чата",
                        "Старые сообщения будут автоматически удаляться",
                        "По умолчанию: 100"
                )
                .defineInRange("chat-history-size", 100, 1, 100000);

        CHAT_INPUT_MAX_LENGTH = BUILDER
                .comment("",
                        "[English]",
                        "Maximum length of chat input",
                        "Default: 256",
                        "[Русский]",
                        "Максимальная длина строки ввода чата",
                        "По умолчанию: 256"
                )
                .defineInRange("chat-input-max-length", 256, 1, 32767);

        BUILDER.pop();
        SPEC = BUILDER.build();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SPEC);
    }

    public static boolean isAnimationsEnabled() {
        return ENABLE_ANIMATIONS.get();
    }

    public static int getHistorySize() {
        return HISTORY_SIZE.get();
    }

    public static int getMaxLength() {
        return CHAT_INPUT_MAX_LENGTH.get();
    }
}