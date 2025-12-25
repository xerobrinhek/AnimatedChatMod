package com.xerobrinhek.animatedchat.client;

import com.xerobrinhek.animatedchat.AnimatedChatMod;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.network.chat.Style;

import javax.annotation.Nullable;
import java.util.List;

public class ChatComponentWrapper extends ChatComponent {
    private final AnimatedChatMod animatedChat = AnimatedChatMod.getInstance();

    public ChatComponentWrapper() {
        super(Minecraft.getInstance());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int tickCount, int mouseX, int mouseY) {
        animatedChat.render(guiGraphics, tickCount, mouseX, mouseY);
    }

    @Override
    public void addMessage(Component message) {
        animatedChat.addMessage(message);
    }

    @Override
    public void addMessage(Component message, @Nullable MessageSignature signature, @Nullable GuiMessageTag tag) {
        animatedChat.addMessage(message, signature, tag);
    }

    @Override
    public void clearMessages(boolean clearHistory) {
        animatedChat.clearMessages(clearHistory);
    }

    @Override
    public List<String> getRecentChat() {
        return animatedChat.getRecentChat();
    }

    @Override
    public void addRecentChat(String message) {
        animatedChat.addRecentChat(message);
    }

    @Override
    public void deleteMessage(MessageSignature signature) {
        animatedChat.deleteMessage(signature);
    }

    @Override
    public boolean handleChatQueueClicked(double mouseX, double mouseY) {
        return animatedChat.handleChatQueueClicked(mouseX, mouseY);
    }

    @Nullable
    @Override
    public Style getClickedComponentStyleAt(double mouseX, double mouseY) {
        return animatedChat.getClickedComponentStyleAt(mouseX, mouseY);
    }

    @Nullable
    @Override
    public GuiMessageTag getMessageTagAt(double mouseX, double mouseY) {
        return animatedChat.getMessageTagAt(mouseX, mouseY);
    }

    @Override
    public void rescaleChat() {
        animatedChat.rescaleChat();
    }

    @Override
    public void resetChatScroll() {
        animatedChat.resetChatScroll();
    }

    @Override
    public void scrollChat(int lines) {
        animatedChat.scrollChat(lines);
    }

    @Override
    public int getWidth() {
        return animatedChat.getWidth();
    }

    @Override
    public int getHeight() {
        return animatedChat.getHeight();
    }

    @Override
    public double getScale() {
        return animatedChat.getScale();
    }

    @Override
    public int getLinesPerPage() {
        return animatedChat.getLinesPerPage();
    }
}