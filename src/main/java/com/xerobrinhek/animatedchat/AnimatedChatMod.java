package com.xerobrinhek.animatedchat;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.xerobrinhek.animatedchat.client.AnimatedChatConfig;
import com.xerobrinhek.animatedchat.core.MiniMessageParser;
import com.xerobrinhek.animatedchat.utils.AnimationUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.*;

@Mod("animatedchat")
public class AnimatedChatMod {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_CHAT_HISTORY = AnimatedChatConfig.getHistorySize();
    private static final int TIME_BEFORE_MESSAGE_DELETION = 60;
    private static final Component DELETED_CHAT_MESSAGE = Component.translatable("chat.deleted_marker").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);

    private final Minecraft minecraft;
    private final List<String> recentChat = Lists.newArrayList();
    private final static List<GuiMessage> allMessages = Lists.newArrayList();
    private final static List<GuiMessage.Line> trimmedMessages = Lists.newArrayList();
    private int chatScrollbarPos;
    private boolean newMessageSinceScroll;
    private final List<DelayedMessageDeletion> messageDeletionQueue = new ArrayList<>();
    private static AnimatedChatMod instance;

    public static AnimatedChatMod getInstance() {
        if (instance == null) {
            instance = new AnimatedChatMod();
        }
        return instance;
    }

    public int globalTick = 0;

    public AnimatedChatMod() {
        this.minecraft = Minecraft.getInstance();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        globalTick++;

        if (AnimatedChatConfig.isAnimationsEnabled() && globalTick % 3 == 0) {
            this.rescaleChat();
        }
    }

    public void render(GuiGraphics guiGraphics, int tickCount, int mouseX, int mouseY) {
        if (!this.isChatHidden()) {
            int i = this.getLinesPerPage();
            int j = this.trimmedMessages.size();
            if (j > 0) {
                boolean flag = this.isChatFocused();
                float f = (float)this.getScale();
                int k = Mth.ceil((float)this.getWidth() / f);
                int l = guiGraphics.guiHeight();
                guiGraphics.pose().pushPose();
                guiGraphics.pose().scale(f, f, 1.0F);
                guiGraphics.pose().translate(4.0F, 0.0F, 0.0F);
                int i1 = Mth.floor((float)(l - 40) / f);
                int j1 = this.getMessageEndIndexAt(this.screenToChatX((double)mouseX), this.screenToChatY((double)mouseY));
                double d0 = this.minecraft.options.chatOpacity().get() * (double)0.9F + (double)0.1F;
                double d1 = this.minecraft.options.textBackgroundOpacity().get();
                double d2 = this.minecraft.options.chatLineSpacing().get();
                int k1 = this.getLineHeight();
                int l1 = (int)Math.round(-8.0D * (d2 + 1.0D) + 4.0D * d2);
                int i2 = 0;

                for(int j2 = 0; j2 + this.chatScrollbarPos < this.trimmedMessages.size() && j2 < i; ++j2) {
                    int k2 = j2 + this.chatScrollbarPos;
                    GuiMessage.Line guimessage$line = this.trimmedMessages.get(k2);
                    if (guimessage$line != null) {
                        int l2 = tickCount - guimessage$line.addedTime();
                        if (l2 < 200 || flag) {
                            double d3 = flag ? 1.0D : getTimeFactor(l2);
                            int j3 = (int)(255.0D * d3 * d0);
                            int k3 = (int)(255.0D * d3 * d1);
                            ++i2;
                            if (j3 > 3) {
                                int i4 = i1 - j2 * k1;
                                int j4 = i4 + l1;
                                guiGraphics.pose().pushPose();
                                guiGraphics.pose().translate(0.0F, 0.0F, 50.0F);
                                guiGraphics.fill(-4, i4 - k1, 0 + k + 4 + 4, i4, k3 << 24);
                                GuiMessageTag guimessagetag = guimessage$line.tag();
                                if (guimessagetag != null) {
                                    int k4 = guimessagetag.indicatorColor() | j3 << 24;
                                    guiGraphics.fill(-4, i4 - k1, -2, i4, k4);
                                    if (k2 == j1 && guimessagetag.icon() != null) {
                                        int l4 = this.getTagIconLeft(guimessage$line);
                                        int i5 = j4 + 9;
                                        this.drawTagIcon(guiGraphics, l4, i5, guimessagetag.icon());
                                    }
                                }

                                guiGraphics.pose().translate(0.0F, 0.0F, 50.0F);
                                guiGraphics.drawString(this.minecraft.font, guimessage$line.content(), 0, j4, 16777215 + (j3 << 24));
                                guiGraphics.pose().popPose();
                            }
                        }
                    }
                }

                long j5 = this.minecraft.getChatListener().queueSize();
                if (j5 > 0L) {
                    int k5 = (int)(128.0D * d0);
                    int i6 = (int)(255.0D * d1);
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(0.0F, (float)i1, 50.0F);
                    guiGraphics.fill(-2, 0, k + 4, 9, i6 << 24);
                    guiGraphics.pose().translate(0.0F, 0.0F, 50.0F);
                    guiGraphics.drawString(this.minecraft.font, Component.translatable("chat.queue", j5), 0, 1, 16777215 + (k5 << 24));
                    guiGraphics.pose().popPose();
                }

                if (flag) {
                    int l5 = this.getLineHeight();
                    int j6 = j * l5;
                    int k6 = i2 * l5;
                    int i3 = this.chatScrollbarPos * k6 / j - i1;
                    int l6 = k6 * k6 / j6;
                    if (j6 != k6) {
                        int i7 = i3 > 0 ? 170 : 96;
                        int j7 = this.newMessageSinceScroll ? 13382451 : 3355562;
                        int k7 = k + 4;
                        guiGraphics.fill(k7, -i3, k7 + 2, -i3 - l6, j7 + (i7 << 24));
                        guiGraphics.fill(k7 + 2, -i3, k7 + 1, -i3 - l6, 13421772 + (i7 << 24));
                    }
                }
                guiGraphics.pose().popPose();
            }
        }
    }

    private void drawTagIcon(GuiGraphics guiGraphics, int x, int y, GuiMessageTag.Icon icon) {
        int i = y - icon.height - 1;
        icon.draw(guiGraphics, x, i);
    }

    private int getTagIconLeft(GuiMessage.Line line) {
        return this.minecraft.font.width(line.content()) + 4;
    }

    public boolean isChatHidden() {
        return this.minecraft.options.chatVisibility().get() == ChatVisiblity.HIDDEN;
    }

    private static double getTimeFactor(int timeSinceAdded) {
        double d0 = (double)timeSinceAdded / 200.0D;
        d0 = 1.0D - d0;
        d0 *= 10.0D;
        d0 = Mth.clamp(d0, 0.0D, 1.0D);
        return d0 * d0;
    }

    public void clearMessages(boolean clearHistory) {
        this.minecraft.getChatListener().clearQueue();
        this.messageDeletionQueue.clear();
        this.trimmedMessages.clear();
        this.allMessages.clear();
        if (clearHistory) {
            this.recentChat.clear();
        }
    }

    public void addMessage(Component message) {
        this.addMessage(message, null, this.minecraft.isSingleplayer() ? GuiMessageTag.systemSinglePlayer() : GuiMessageTag.system());
    }

    public void addMessage(Component message, @Nullable MessageSignature signature, @Nullable GuiMessageTag tag) {
        this.logChatMessage(message, tag);
        this.addMessage(message, signature, this.minecraft.gui.getGuiTicks(), tag, false);
    }

    private void logChatMessage(Component message, @Nullable GuiMessageTag tag) {
        String s = message.getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n");
        String s1 = Optionull.map(tag, GuiMessageTag::logTag);
        if (s1 != null) {
            LOGGER.info("[{}] [CHAT] {}", s1, s);
        } else {
            LOGGER.info("[CHAT] {}", s);
        }
    }

    private void addMessage(Component message, @Nullable MessageSignature signature, int addedTime, @Nullable GuiMessageTag tag, boolean refreshOnly) {
        if (!refreshOnly) {
            this.allMessages.add(0, new GuiMessage(addedTime, message, signature, tag));
            while(this.allMessages.size() > MAX_CHAT_HISTORY) {
                this.allMessages.remove(this.allMessages.size() - 1);
            }
            if (!AnimatedChatConfig.isAnimationsEnabled()) {
                this.refreshTrimmedMessage();
            }
        }
    }

    private void processMessageDeletionQueue() {
        int i = this.minecraft.gui.getGuiTicks();
        this.messageDeletionQueue.removeIf((p_250713_) -> {
            if (i >= p_250713_.deletableAfter()) {
                return this.deleteMessageOrDelay(p_250713_.signature()) == null;
            } else {
                return false;
            }
        });
    }

    public void deleteMessage(MessageSignature signature) {
        DelayedMessageDeletion chatcomponent$delayedmessagedeletion = this.deleteMessageOrDelay(signature);
        if (chatcomponent$delayedmessagedeletion != null) {
            this.messageDeletionQueue.add(chatcomponent$delayedmessagedeletion);
        }
    }

    @Nullable
    private DelayedMessageDeletion deleteMessageOrDelay(MessageSignature signature) {
        int i = this.minecraft.gui.getGuiTicks();
        ListIterator<GuiMessage> listiterator = this.allMessages.listIterator();

        while(listiterator.hasNext()) {
            GuiMessage guimessage = listiterator.next();
            if (signature.equals(guimessage.signature())) {
                int j = guimessage.addedTime() + TIME_BEFORE_MESSAGE_DELETION;
                if (i >= j) {
                    listiterator.set(this.createDeletedMarker(guimessage));
                    this.refreshTrimmedMessage();
                    return null;
                }
                return new DelayedMessageDeletion(signature, j);
            }
        }
        return null;
    }

    private GuiMessage createDeletedMarker(GuiMessage message) {
        return new GuiMessage(message.addedTime(), DELETED_CHAT_MESSAGE, null, GuiMessageTag.system());
    }

    public List<String> getRecentChat() {
        return this.recentChat;
    }

    public void addRecentChat(String message) {
        if (this.recentChat.isEmpty() || !this.recentChat.get(this.recentChat.size() - 1).equals(message)) {
            this.recentChat.add(message);
        }
    }

    public boolean handleChatQueueClicked(double mouseX, double mouseY) {
        if (this.isChatFocused() && !this.minecraft.options.hideGui && !this.isChatHidden()) {
            ChatListener chatlistener = this.minecraft.getChatListener();
            if (chatlistener.queueSize() == 0L) {
                return false;
            } else {
                double d0 = mouseX - 2.0D;
                double d1 = (double)this.minecraft.getWindow().getGuiScaledHeight() - mouseY - 40.0D;
                if (d0 <= (double)Mth.floor((double)this.getWidth() / this.getScale()) && d1 < 0.0D && d1 > (double)Mth.floor(-9.0D * this.getScale())) {
                    chatlistener.acceptNextDelayedMessage();
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    @Nullable
    public Style getClickedComponentStyleAt(double mouseX, double mouseY) {
        double d0 = this.screenToChatX(mouseX);
        double d1 = this.screenToChatY(mouseY);

        int i = this.getMessageLineIndexAt(d0, d1);

        if (i >= 0 && i < this.trimmedMessages.size()) {
            GuiMessage.Line guimessage$line = this.trimmedMessages.get(i);
            return this.minecraft.font.getSplitter().componentStyleAtWidth(guimessage$line.content(), Mth.floor(d0));
        } else {
            return null;
        }
    }

    @Nullable
    public GuiMessageTag getMessageTagAt(double mouseX, double mouseY) {
        double d0 = this.screenToChatX(mouseX);
        double d1 = this.screenToChatY(mouseY);
        int i = this.getMessageEndIndexAt(d0, d1);
        if (i >= 0 && i < this.trimmedMessages.size()) {
            GuiMessage.Line guimessage$line = this.trimmedMessages.get(i);
            GuiMessageTag guimessagetag = guimessage$line.tag();
            if (guimessagetag != null && this.hasSelectedMessageTag(d0, guimessage$line, guimessagetag)) {
                return guimessagetag;
            }
        }
        return null;
    }

    private boolean hasSelectedMessageTag(double mouseX, GuiMessage.Line line, GuiMessageTag tag) {
        if (mouseX < 0.0D) {
            return true;
        } else {
            GuiMessageTag.Icon guimessagetag$icon = tag.icon();
            if (guimessagetag$icon == null) {
                return false;
            } else {
                int i = this.getTagIconLeft(line);
                int j = i + guimessagetag$icon.width;
                return mouseX >= (double)i && mouseX <= (double)j;
            }
        }
    }

    private double screenToChatX(double mouseX) {
        return mouseX / this.getScale() - 4.0D;
    }

    private double screenToChatY(double mouseY) {
        double d0 = (double)this.minecraft.getWindow().getGuiScaledHeight() - mouseY - 40.0D;
        return d0 / (this.getScale() * (double)this.getLineHeight());
    }

    private int getMessageEndIndexAt(double mouseX, double mouseY) {
        int i = this.getMessageLineIndexAt(mouseX, mouseY);
        if (i == -1) {
            return -1;
        } else {
            while(i >= 0) {
                if (this.trimmedMessages.get(i).endOfEntry()) {
                    return i;
                }
                --i;
            }
            return i;
        }
    }

    private int getMessageLineIndexAt(double mouseX, double mouseY) {
        if (this.isChatFocused() && !this.minecraft.options.hideGui && !this.isChatHidden()) {
            if (!(mouseX < -4.0D) && !(mouseX > (double)Mth.floor((double)this.getWidth() / this.getScale()))) {
                int i = Math.min(this.getLinesPerPage(), this.trimmedMessages.size());
                if (mouseY >= 0.0D && mouseY < (double)i) {
                    int j = Mth.floor(mouseY + (double)this.chatScrollbarPos);
                    if (j >= 0 && j < this.trimmedMessages.size()) {
                        return j;
                    }
                }
                return -1;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    public static double defaultUnfocusedPct() {
        return 70.0D / (double)(getHeight(1.0D) - 20);
    }

    public void rescaleChat() {
        this.refreshTrimmedMessage();
    }

    private void refreshTrimmedMessage() {
        this.trimmedMessages.clear();

        for(int i = this.allMessages.size() - 1; i >= 0; --i) {
            GuiMessage guimessage = this.allMessages.get(i);
            Component message = guimessage.content();

            message = AnimationUtils.processAnimations(message, guimessage.addedTime());

            message = MiniMessageParser.parse(message);

            int width = Mth.floor((double)this.getWidth() / this.getScale());
            if (guimessage.tag() != null && guimessage.tag().icon() != null) {
                width -= guimessage.tag().icon().width + 4 + 2;
            }

            List<FormattedCharSequence> list = ComponentRenderUtils.wrapComponents(message, width, this.minecraft.font);

            for(int j = 0; j < list.size(); ++j) {
                boolean isLast = j == list.size() - 1;
                this.trimmedMessages.add(0, new GuiMessage.Line(guimessage.addedTime(), list.get(j), guimessage.tag(), isLast));
            }
        }

        while(this.trimmedMessages.size() > MAX_CHAT_HISTORY) {
            this.trimmedMessages.remove(this.trimmedMessages.size() - 1);
        }
    }

    public void resetChatScroll() {
        this.chatScrollbarPos = 0;
        this.newMessageSinceScroll = false;
    }

    public void scrollChat(int lines) {
        this.chatScrollbarPos += lines;

        int i = this.trimmedMessages.size();
        if (this.chatScrollbarPos > i - this.getLinesPerPage()) {
            this.chatScrollbarPos = i - this.getLinesPerPage();
        }

        if (this.chatScrollbarPos <= 0) {
            this.chatScrollbarPos = 0;
            this.newMessageSinceScroll = false;
        }
    }

    public boolean isChatFocused() {
        return this.minecraft.screen instanceof ChatScreen;
    }

    public int getWidth() {
        return getWidth(this.minecraft.options.chatWidth().get());
    }

    public int getHeight() {
        return getHeight(this.isChatFocused() ? this.minecraft.options.chatHeightFocused().get() : this.minecraft.options.chatHeightUnfocused().get());
    }

    public double getScale() {
        return this.minecraft.options.chatScale().get();
    }

    public static int getWidth(double chatWidth) {
        return Mth.floor(chatWidth * 280.0D + 40.0D);
    }

    public static int getHeight(double chatHeight) {
        return Mth.floor(chatHeight * 160.0D + 20.0D);
    }

    public int getLinesPerPage() {
        return this.getHeight() / this.getLineHeight();
    }

    private int getLineHeight() {
        return (int)(9.0D * (this.minecraft.options.chatLineSpacing().get() + 1.0D));
    }

    private record DelayedMessageDeletion(MessageSignature signature, int deletableAfter) {}
}
