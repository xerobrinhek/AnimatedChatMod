package com.xerobrinhek.animatedchat.core;

import com.xerobrinhek.animatedchat.utils.AnimationUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import static com.xerobrinhek.animatedchat.utils.AnimationUtils.StyledCharacter;

public class MiniMessageParser {
    public static Component parse(Component original) {
        List<StyledCharacter> originalChars = new ArrayList<>();
        original.getVisualOrderText().accept((idx, style, cp) -> {
            originalChars.add(new StyledCharacter((char) cp, style));
            return true;
        });
        List<StyledCharacter> overlay = parseMiniMessage(original.getString());

        List<StyledCharacter> result = applyDiff(originalChars, overlay);

        return AnimationUtils.createStyledComponent(result);
    }

    public static List<StyledCharacter> applyDiff(List<StyledCharacter> base, List<StyledCharacter> overlay) {
        int limit = Math.min(base.size(), overlay.size());

        for (int i = 0; i < limit; i++) {
            StyledCharacter baseChar = base.get(i);
            StyledCharacter overlayChar = overlay.get(i);

            boolean charDiff = baseChar.character != overlayChar.character;

            if (charDiff) {
                base.set(i, overlayChar);
            }
        }

        for (int i = base.size() - 1; i >= overlay.size(); i--) {
            base.remove(i);
        }

        for (int i = limit; i < overlay.size(); i++) {
            base.add(overlay.get(i));
        }
        return base;
    }

    private static List<StyledCharacter> parseMiniMessage(String text) {
        try {
            net.kyori.adventure.text.Component adventure = MiniMessage.miniMessage().deserialize(text);
            String json = GsonComponentSerializer.gson().serialize(adventure);
            List<StyledCharacter> parsed = new ArrayList<>();
            Component.Serializer.fromJson(json).getVisualOrderText().accept((idx, style, cp) -> {
                parsed.add(new StyledCharacter((char) cp, style));
                return true;
            });
            return parsed;

        } catch (Exception e) {
            List<StyledCharacter> originalChars = new ArrayList<>();
            Component.literal(text).getVisualOrderText().accept((idx, style, cp) -> {
                originalChars.add(new StyledCharacter((char) cp, style));
                return true;
            });
            return originalChars;
        }
    }
}