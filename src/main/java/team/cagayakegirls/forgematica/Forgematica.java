package team.cagayakegirls.forgematica;

import fi.dy.masa.litematica.Litematica;
import fi.dy.masa.litematica.compat.modmenu.ModMenuImpl;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Forgematica.MOD_ID, dist = Dist.CLIENT)
public class Forgematica {
    public static final String MOD_ID = "forgematica";

    public Forgematica(ModContainer modContainer) {
        if (FMLLoader.getDist().isClient()) {
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, new ModMenuImpl().getModConfigScreenFactory());
            new Litematica().onInitialize();
            // Audited fork (Task 14): single-player creative paste writes the integrated world on its own thread; its
            // tasks run only on the player's integrated server, never on any other (a dedicated server never loads
            // this client-only mod at all).
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.tick.ServerTickEvent.Post.class, e -> {
                if (e.getServer() == net.minecraft.client.MinecraftClient.getInstance().getServer())
                    fi.dy.masa.litematica.scheduler.TaskScheduler.getInstanceServer().runTasks();
            });
        }
    }
}
