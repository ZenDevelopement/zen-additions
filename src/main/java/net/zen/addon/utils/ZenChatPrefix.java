package net.zen.addon.utils;

import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

public class ZenChatPrefix {
    private static final String PREFIX_TEXT = "Zen Additions";
    private static final int PREFIX_COLOR = 0xFCB1F4;
    private static boolean registered = false;

    public static void register() {
        if (!registered) {
            ChatUtils.registerCustomPrefix("net.zen.addon.features.modules", ZenChatPrefix::getPrefix);
            registered = true;
        }
    }

    public static void unregister() {
        if (registered) {
            ChatUtils.unregisterCustomPrefix("net.zen.addon.features.modules");
            registered = false;
        }
    }

    public static Text getPrefix() {
        MutableText value = Text.literal(PREFIX_TEXT);
        MutableText prefix = Text.literal("");
        value.setStyle(value.getStyle().withColor(TextColor.fromRgb(PREFIX_COLOR)));
        prefix.setStyle(prefix.getStyle().withFormatting(Formatting.GRAY))
            .append(Text.literal("["))
            .append(value)
            .append(Text.literal("] "));
        return prefix;
    }
}