package com.xerobrinhek.animatedchat.utils;

import com.xerobrinhek.animatedchat.AnimatedChatMod;
import com.xerobrinhek.animatedchat.core.AnimationParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;

public class AnimationUtils {
    public static class StyledCharacter {
        public final char character;
        public final Style style;

        public StyledCharacter(char character, Style style) {
            this.character = character;
            this.style = style;
        }
    }

    public static Component processAnimations(Component original, int addedTime) {
        List<StyledCharacter> styledChars = new ArrayList<>();
        original.getVisualOrderText().accept((idx, style, cp) -> {
            styledChars.add(new StyledCharacter((char) cp, style));
            return true;
        });

        List<AnimationParser.AnimNode> nodes = AnimationParser.parse(styledChars);

        MutableComponent result = Component.empty();
        for (AnimationParser.AnimNode node : nodes) {
            result.append(node.result(AnimatedChatMod.getInstance().globalTick, addedTime));
        }

        return result;
    }

    public static Component createStyledComponent(List<StyledCharacter> styledChars) {
        if (styledChars.isEmpty()) {
            return Component.empty();
        }

        MutableComponent result = Component.empty();

        for (StyledCharacter sc : styledChars) {
            MutableComponent charComp = Component.literal(String.valueOf(sc.character));

            charComp.setStyle(sc.style);

            result.append(charComp);
        }

        return result;
    }
}
