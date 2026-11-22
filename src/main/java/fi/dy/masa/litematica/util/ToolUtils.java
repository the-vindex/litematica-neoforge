package fi.dy.masa.litematica.util;

import javax.annotation.Nullable;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.scheduler.TaskScheduler;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.selection.AreaSelection;
import fi.dy.masa.litematica.selection.Box;
import fi.dy.masa.litematica.tool.ToolMode;
import fi.dy.masa.litematica.tool.ToolModeData;
import fi.dy.masa.malilib.gui.Message.MessageType;
import fi.dy.masa.malilib.interfaces.ICompletionListener;
import fi.dy.masa.malilib.util.InfoUtils;

public class ToolUtils
{
    public static void fillSelectionVolumes(MinecraftClient mc, BlockState state, @Nullable BlockState stateToReplace)
    {
        fi.dy.masa.litematica.util.AuditedBuild.refuse("fill");
    }

    public static void deleteSelectionVolumes(boolean removeEntities, MinecraftClient mc)
    {
        AreaSelection area = null;

        if (DataManager.getToolMode() == ToolMode.DELETE && ToolModeData.DELETE.getUsePlacement())
        {
            SchematicPlacement placement = DataManager.getSchematicPlacementManager().getSelectedSchematicPlacement();

            if (placement != null)
            {
                area = AreaSelection.fromPlacement(placement);
            }
        }
        else
        {
            area = DataManager.getSelectionManager().getCurrentSelection();
        }

        deleteSelectionVolumes(area, removeEntities, mc);
    }

    public static void deleteSelectionVolumes(@Nullable final AreaSelection area, boolean removeEntities, MinecraftClient mc)
    {
        deleteSelectionVolumes(area, removeEntities, null, mc);
    }

    public static void deleteSelectionVolumes(@Nullable final AreaSelection area, boolean removeEntities,
            @Nullable ICompletionListener listener, MinecraftClient mc)
    {
        fi.dy.masa.litematica.util.AuditedBuild.refuse("delete");
    }

    public static void deleteBlocksByPlacement()
    {
        if (DataManager.getToolMode() == ToolMode.DELETE &&
            ToolModeData.DELETE.getUsePlacement())
        {
            SchematicPlacement placement = DataManager.getSchematicPlacementManager().getSelectedSchematicPlacement();

            if (placement != null)
            {
                PlacementDeletionMode mode = (PlacementDeletionMode) Configs.Generic.SCHEMATIC_VCS_DELETE_MODE.getOptionListValue();
                deleteBlocksByPlacement(placement, mode, null);
            }
            else
            {
                InfoUtils.showGuiOrInGameMessage(MessageType.ERROR, "litematica.message.error.no_placement_selected");
                InfoUtils.showGuiOrInGameMessage(MessageType.INFO, "Select a placement either by looking at one in the world, holding the tool item and using the §etoolSelectElements§r hotkey (middle click by default), or alternatively by clicking on a placement in the Schematic Placements menu, so that it has the white outline indicating it's selected. The selected placement's name will also appear on the Tool HUD.");
            }
        }
    }

    public static void deleteBlocksByPlacement(SchematicPlacement placement,
                                               PlacementDeletionMode mode,
                                               @Nullable ICompletionListener listener)
    {
        fi.dy.masa.litematica.util.AuditedBuild.refuse("delete by placement");
    }
}
