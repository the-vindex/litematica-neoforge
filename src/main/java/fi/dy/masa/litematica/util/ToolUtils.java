package fi.dy.masa.litematica.util;

import javax.annotation.Nullable;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.scheduler.TaskScheduler;
import fi.dy.masa.litematica.scheduler.tasks.TaskDeleteArea;
import fi.dy.masa.litematica.scheduler.tasks.TaskDeleteBlocksByPlacement;
import fi.dy.masa.litematica.scheduler.tasks.TaskFillArea;
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
        // Audited fork (Task 14): restored behind CommandToolGate (opt-in commandToolsEnabled, creative, op).
        if (CommandToolGate.allowedNow(mc, "fill") == false) return;
        if (AuditedBuild.OPERATOR_IDS.contains(net.minecraft.registry.Registries.BLOCK.getId(state.getBlock()).toString()))
        {
            InfoUtils.showGuiOrInGameMessage(MessageType.ERROR, "fill with an operator block is refused");
            return;
        }

        final AreaSelection area = DataManager.getSelectionManager().getCurrentSelection();

        if (area == null)
        {
            InfoUtils.showGuiOrInGameMessage(MessageType.ERROR, "litematica.message.error.no_area_selected");
            return;
        }

        if (area.getAllSubRegionBoxes().size() > 0)
        {
            Box currentBox = area.getSelectedSubRegionBox();
            final ImmutableList<Box> boxes = currentBox != null ? ImmutableList.of(currentBox) : ImmutableList.copyOf(area.getAllSubRegionBoxes());

            TaskFillArea task = new TaskFillArea(boxes, state, stateToReplace, false);
            scheduler(mc).scheduleTask(task, Configs.Generic.COMMAND_TASK_INTERVAL.getIntegerValue());
            InfoUtils.showGuiOrInGameMessage(MessageType.INFO, "litematica.message.scheduled_task_added");
        }
        else
        {
            InfoUtils.showGuiOrInGameMessage(MessageType.ERROR, "litematica.message.error.empty_area_selection");
        }
    }

    /**
     * Single player: the integrated server's scheduler (the task writes that world on its own thread, as upstream);
     * on a server: the client's (the task sends vanilla fill/setblock commands through CommandToolGate).
     */
    private static TaskScheduler scheduler(MinecraftClient mc)
    {
        return mc.isIntegratedServerRunning() ? TaskScheduler.getInstanceServer() : TaskScheduler.getInstanceClient();
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
        if (CommandToolGate.allowedNow(mc, "delete") == false) return;

        if (area == null)
        {
            InfoUtils.showGuiOrInGameMessage(MessageType.ERROR, "litematica.message.error.no_area_selected");
            return;
        }

        if (area.getAllSubRegionBoxes().size() > 0)
        {
            Box currentBox = area.getSelectedSubRegionBox();
            final ImmutableList<Box> boxes = currentBox != null ? ImmutableList.of(currentBox) : ImmutableList.copyOf(area.getAllSubRegionBoxes());

            TaskDeleteArea task = new TaskDeleteArea(boxes, removeEntities);

            if (listener != null)
            {
                task.setCompletionListener(listener);
            }

            scheduler(mc).scheduleTask(task, Configs.Generic.COMMAND_TASK_INTERVAL.getIntegerValue());
            InfoUtils.showGuiOrInGameMessage(MessageType.INFO, "litematica.message.scheduled_task_added");
        }
        else
        {
            InfoUtils.showGuiOrInGameMessage(MessageType.ERROR, "litematica.message.error.empty_area_selection");
        }
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
        MinecraftClient mc = MinecraftClient.getInstance();
        if (CommandToolGate.allowedNow(mc, "delete") == false) return;
        TaskDeleteBlocksByPlacement task = new TaskDeleteBlocksByPlacement(ImmutableList.of(placement), mode, DataManager.getRenderLayerRange());

        if (listener != null)
        {
            task.setCompletionListener(listener);
        }

        scheduler(mc).scheduleTask(task, Configs.Generic.COMMAND_TASK_INTERVAL.getIntegerValue());
        InfoUtils.showGuiOrInGameMessage(MessageType.INFO, "litematica.message.scheduled_task_added");
    }
}
