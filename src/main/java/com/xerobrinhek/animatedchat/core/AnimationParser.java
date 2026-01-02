package com.xerobrinhek.animatedchat.core;

import com.xerobrinhek.animatedchat.utils.AnimationUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Deque;
import java.util.ArrayDeque;

import static com.xerobrinhek.animatedchat.utils.AnimationUtils.StyledCharacter;

public class AnimationParser {

    public static List<AnimNode> parse(List<StyledCharacter> chars) {
        List<AnimNode> root = new ArrayList<>();
        Deque<AnimationNodeBuilder> stack = new ArrayDeque<>();

        List<StyledCharacter> buffer = new ArrayList<>();

        int i = 0;
        while (i < chars.size()) {
            if (match(chars, i, "<animation:interval:")) {

                flushText(buffer, stack, root);

                int interval = parseInterval(chars, i);
                i = skipTo(chars, i, ">") + 1;

                stack.push(new AnimationNodeBuilder(interval));
                continue;
            }

            if (match(chars, i, "<->")) {
                flushText(buffer, stack, root);
                stack.peek().nextFrame();
                i += 3;
                continue;
            }

            if (match(chars, i, "</->")) {
                flushText(buffer, stack, root);
                i += 4;
                continue;
            }

            if (match(chars, i, "</animation>")) {
                flushText(buffer, stack, root);
                AnimationNode node = stack.pop().build();

                if (stack.isEmpty()) {
                    root.add(node);
                } else {
                    stack.peek().currentFrame.add(node);
                }

                i += "</animation>".length();
                continue;
            }

            buffer.add(chars.get(i));
            i++;
        }

        flushText(buffer, stack, root);
        return root;
    }

    private static void flushText(
            List<StyledCharacter> buffer,
            Deque<AnimationNodeBuilder> stack,
            List<AnimNode> root
    ) {
        if (buffer.isEmpty()) return;

        TextNode node = new TextNode(new ArrayList<>(buffer));
        buffer.clear();

        if (stack.isEmpty()) {
            root.add(node);
        } else {
            stack.peek().currentFrame.add(node);
        }
    }

    private static boolean match(List<StyledCharacter> chars, int index, String text) {
        if (index + text.length() > chars.size()) return false;
        for (int i = 0; i < text.length(); i++) {
            if (chars.get(index + i).character != text.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    private static int parseInterval(List<StyledCharacter> chars, int index) {
        StringBuilder sb = new StringBuilder();
        index += "<animation:interval:".length();
        while (Character.isDigit(chars.get(index).character)) {
            sb.append(chars.get(index).character);
            index++;
        }
        return Integer.parseInt(sb.toString());
    }

    private static int skipTo(List<StyledCharacter> chars, int index, String token) {
        while (index < chars.size()) {
            if (match(chars, index, token)) return index;
            index++;
        }
        return index;
    }

    public interface AnimNode {
        Component result(int globalTick, int addedTime);
    }

    public static class TextNode implements AnimNode {

        private final List<StyledCharacter> chars;

        public TextNode(List<StyledCharacter> chars) {
            this.chars = chars;
        }

        @Override
        public Component result(int globalTick, int addedTime) {
            return AnimationUtils.createStyledComponent(chars);
        }
    }


    public static class AnimationNode implements AnimNode {

        private final int interval;
        private final List<List<AnimNode>> frames;

        private int lastFrameIndex = -1;
        private Component cachedComponent;

        public AnimationNode(int interval, List<List<AnimNode>> frames) {
            this.interval = interval;
            this.frames = frames;
        }

        @Override
        public Component result(int globalTick, int addedTime) {
            if (frames.isEmpty()) {
                return Component.empty();
            }

            int frameIndex = ((globalTick + addedTime) / interval) % frames.size();

            if (frameIndex == lastFrameIndex && cachedComponent != null) {
                return cachedComponent;
            }

            MutableComponent result = Component.empty();
            for (AnimNode node : frames.get(frameIndex)) {
                result.append(node.result(globalTick, addedTime));
            }

            lastFrameIndex = frameIndex;
            cachedComponent = result;
            return result;
        }
    }


    public static class AnimationNodeBuilder {

        public final int interval;
        public final List<List<AnimNode>> frames = new ArrayList<>();
        public List<AnimNode> currentFrame = new ArrayList<>();

        public AnimationNodeBuilder(int interval) {
            this.interval = interval;
        }

        public void nextFrame() {
            if (!currentFrame.isEmpty()) {
                frames.add(currentFrame);
            }
            currentFrame = new ArrayList<>();
        }

        public AnimationNode build() {
            if (!currentFrame.isEmpty()) {
                frames.add(currentFrame);
            }
            return new AnimationNode(interval, frames);
        }
    }
}
