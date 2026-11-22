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

    /** Ids of operator-only blocks/items whose data must never travel to the server on an item. */
    public static final java.util.Set<String> OPERATOR_IDS = java.util.Set.of("minecraft:command_block",
        "minecraft:chain_command_block", "minecraft:repeating_command_block", "minecraft:command_block_minecart",
        "minecraft:structure_block", "minecraft:jigsaw");

    /**
     * Whether a block entity's data may be copied onto an item (creative pick-block with Ctrl). Never for operator-only
     * blocks (command, structure, jigsaw), and never when the data CONTAINS operator content at any depth (e.g. a chest
     * or shulker holding a command block item with its command): the copied data would reach the server in a creative
     * slot packet.
     */
    public static boolean mayCopyBlockEntityData(net.minecraft.block.entity.BlockEntity te, net.minecraft.registry.RegistryWrapper.WrapperLookup registries)
    {
        if (te == null
            || te instanceof net.minecraft.block.entity.CommandBlockBlockEntity
            || te instanceof net.minecraft.block.entity.StructureBlockBlockEntity
            || te instanceof net.minecraft.block.entity.JigsawBlockEntity)
        {
            return false;
        }
        try
        {
            return containsOperatorData(te.createNbtWithIdentifyingData(registries), 0) == false;
        }
        catch (RuntimeException e)
        {
            return false;   // data that cannot be inspected is not copied
        }
    }

    /** True when the NBT holds operator content anywhere: an operator id, or command-block keys (Command, LastOutput). */
    public static boolean containsOperatorData(net.minecraft.nbt.NbtElement e, int depth)
    {
        if (e == null || depth > 64) return depth > 64;
        if (e instanceof net.minecraft.nbt.NbtCompound c)
        {
            if (c.contains("Command") || c.contains("LastOutput")) return true;
            for (String k : c.getKeys())
            {
                net.minecraft.nbt.NbtElement v = c.get(k);
                if (v instanceof net.minecraft.nbt.NbtString str && OPERATOR_IDS.contains(str.asString())) return true;
                if (containsOperatorData(v, depth + 1)) return true;
            }
            return false;
        }
        if (e instanceof net.minecraft.nbt.NbtList l)
        {
            for (net.minecraft.nbt.NbtElement v : l) if (containsOperatorData(v, depth + 1)) return true;
            return false;
        }
        if (e instanceof net.minecraft.nbt.NbtString str) return OPERATOR_IDS.contains(str.asString());
        return false;
    }

    public static void refuse(String what)
    {
        InfoUtils.showGuiOrInGameMessage(MessageType.ERROR, "Litematica (audited build): " + what + " is disabled in this build");
    }
}
