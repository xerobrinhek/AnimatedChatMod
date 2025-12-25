package com.xerobrinhek.animatedchat.mixin;

import com.xerobrinhek.animatedchat.client.AnimatedChatConfig;
import net.minecraft.util.StringUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StringUtil.class)
public abstract class StringUtilMixin {

    @Inject(
            method = "trimChatMessage",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void animatedchat$overrideTrim(String text, CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(
                StringUtil.truncateStringIfNecessary(
                        text,
                        AnimatedChatConfig.getMaxLength(),
                        true
                )
        );
    }
}
