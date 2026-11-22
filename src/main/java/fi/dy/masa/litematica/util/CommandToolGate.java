package fi.dy.masa.litematica.util;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.client.MinecraftClient;
import fi.dy.masa.malilib.gui.Message.MessageType;
import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.litematica.Litematica;
import fi.dy.masa.litematica.config.Configs;

/**
 * Audited fork (Overseer, Task 14): the ONLY place this build sends a command. Litematica's creative command tools
 * (paste, fill, delete) were restored on the player's decision; every command they build goes through {@link #send},
 * which allows it only when
 * <ul>
 *   <li>the tools are switched on (config {@code commandToolsEnabled}, off by default; one switch for all three),</li>
 *   <li>the player is in creative and has operator permission (level 2+) as the server told the client,</li>
 *   <li>the command is one of the vanilla {@code setblock, fill, clone, summon, data modify block/entity},</li>
 *   <li>and it carries no operator content (command/structure/jigsaw blocks, command-block keys, sign click commands).</li>
 * </ul>
 * A context refusal (off, not creative, not op, another command) stops the paste task; an operator-content command is
 * dropped (that block stays unpasted) and counted. Never called by the printer or Overseer: they place with clicks. {@code scripts/check-client-only.py} fails on command sending in any
 * other class.
 */
public final class CommandToolGate
{
    private CommandToolGate() {}

    public static final Set<String> ALLOWED = Set.of("setblock", "fill", "clone", "summon", "data");
    private static final Pattern OPERATOR_IDS = Pattern.compile("command_block|structure_block|jigsaw|clickEvent|click_event|run_command",
        Pattern.CASE_INSENSITIVE);
    /** Command-block NBT keys (case-sensitive, as NBT keys are). */
    private static final Pattern OPERATOR_KEYS = Pattern.compile("\\bCommand:|\\bLastOutput:|\\bTrackOutput:");
    private static int sent, dropped;

    /** Why a command tool ("paste", "fill", "delete") may not run now (null = it may): off, not creative, not op. */
    public static String refusal(MinecraftClient mc, String tool)
    {
        if (Configs.Generic.COMMAND_TOOLS_ENABLED.getBooleanValue() == false) return tool + " is disabled (Litematica config: commandToolsEnabled)";
        if (mc.player == null || mc.world == null) return tool + " needs a world";
        if (EntityUtils.isCreativeMode(mc.player) == false) return tool + " works only in creative mode";
        if (mc.player.hasPermissionLevel(2) == false)
            return mc.isIntegratedServerRunning() ? "enable cheats (or Open to LAN → Allow Cheats) to use paste/fill/delete"
                : tool + " needs operator permission (level 2) on this server";
        return null;
    }

    /** Shows the refusal (if any) and returns whether the tool may run. */
    public static boolean allowedNow(MinecraftClient mc, String tool)
    {
        String r = refusal(mc, tool);
        if (r != null) InfoUtils.showGuiOrInGameMessage(MessageType.ERROR, r);
        return r == null;
    }

    /** Whether a command is an allowed vanilla paste command (first word and, for data, its form). */
    public static boolean allowedCommand(String command)
    {
        String c = command.startsWith("/") ? command.substring(1) : command;
        String first = c.split(" ", 2)[0].toLowerCase(Locale.ROOT);
        if (ALLOWED.contains(first) == false) return false;
        return first.equals("data") == false || c.startsWith("data modify block ") || c.startsWith("data modify entity ");
    }

    /** Operator content that must never be copied, also not through a paste command. */
    public static boolean hasOperatorContent(String command)
    {
        return OPERATOR_IDS.matcher(command).find() || OPERATOR_KEYS.matcher(command).find();
    }

    /** Sends one paste command if every condition holds; false = stop the paste task. */
    public static boolean send(String command)
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        String reason = refusal(mc, "the command tool");
        if (reason == null && allowedCommand(command) == false) reason = "refused a non-paste command";
        if (reason != null)
        {
            InfoUtils.showGuiOrInGameMessage(MessageType.ERROR, "stopped: " + reason);
            return false;
        }
        if (hasOperatorContent(command))
        {
            ++dropped;
            Litematica.LOGGER.warn("command tool: skipped a block with operator data (never copied)");
            return true;
        }
        String c = command.startsWith("/") ? command.substring(1) : command;
        mc.player.networkHandler.sendCommand(c);
        ++sent;
        return true;
    }

    /** Commands sent and operator-content commands dropped since the game started (tests, status). */
    public static int[] counts() { return new int[] { sent, dropped }; }
}
