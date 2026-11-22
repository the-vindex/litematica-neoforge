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
    /**
     * Capability revision of the audited fork; bumped when its contract for other mods changes. Deliberately not a
     * compile-time constant (String.valueOf): other mods read it at runtime through {@link #marker()}, never inlined.
     */
    public static final String MARKER = String.valueOf("overseer-audited-2");

    /** The marker of the installed build, read at runtime (reflection target for the printer and Overseer). */
    public static String marker()
    {
        return MARKER;
    }

    private AuditedBuild() {}

    /**
     * Whether a block entity's data may be copied onto an item (creative pick-block with Ctrl). Never for operator-only
     * blocks: command blocks (their command NBT would reach the server in a creative slot packet), structure blocks and
     * jigsaws.
     */
    public static boolean mayCopyBlockEntityData(net.minecraft.block.entity.BlockEntity te)
    {
        return te != null
            && !(te instanceof net.minecraft.block.entity.CommandBlockBlockEntity)
            && !(te instanceof net.minecraft.block.entity.StructureBlockBlockEntity)
            && !(te instanceof net.minecraft.block.entity.JigsawBlockEntity);
    }

    public static void refuse(String what)
    {
        InfoUtils.showGuiOrInGameMessage(MessageType.ERROR, "Litematica (audited build): " + what + " is disabled in this build");
    }
}
