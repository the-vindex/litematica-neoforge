package fi.dy.masa.litematica.util;

import fi.dy.masa.malilib.gui.Message.MessageType;
import fi.dy.masa.malilib.util.InfoUtils;

/**
 * Audited fork (Overseer, local-minecraft-mods-forks/litematica): world-editing tools that send commands, paste into the
 * integrated server or use a server-side data channel are removed from this build. Their entry points call {@link #refuse} instead.
 * {@link #MARKER} lets other mods (the printer, Overseer) tell this build from a stock jar with the same version string.
 */
public final class AuditedBuild
{
    /** Capability revision of the audited fork; bumped when its contract for other mods changes. */
    public static final String MARKER = "overseer-audited-1";

    private AuditedBuild() {}

    public static void refuse(String what)
    {
        InfoUtils.showGuiOrInGameMessage(MessageType.ERROR, "Litematica (audited build): " + what + " is disabled in this build");
    }
}
