package com.xerobrinhek.animatedchat.mixin;

import com.xerobrinhek.animatedchat.client.AnimatedChatConfig;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {

    @Shadow
    protected EditBox input;

    @Inject(
            method = "init",
            at = @At("TAIL")
    )
    private void animatedchat$overrideMaxLength(CallbackInfo ci) {
        this.input.setMaxLength(AnimatedChatConfig.getMaxLength());
    }
}

