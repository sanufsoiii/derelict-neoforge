package dev.mim1q.derelict.port.assets;

import dev.mim1q.derelict.Derelict;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;

/** Client wiring for the original assets pack and the missing assets notice. */
public final class OriginalAssetsClient {
    private static boolean noticeHandled = false;

    private OriginalAssetsClient() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(AddPackFindersEvent.class, OriginalAssetsPack::onAddPackFinders);
        // LOW: the port's smoke check sees the TitleScreen first
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, false, ScreenEvent.Opening.class, OriginalAssetsClient::onScreenOpening);
    }

    private static void onScreenOpening(ScreenEvent.Opening event) {
        if (noticeHandled || !(event.getNewScreen() instanceof TitleScreen title)) return;
        noticeHandled = true;
        if (OriginalAssetsLocator.isPresent() || AssetNoticeSettings.dontShowAgain()) return;
        if (Boolean.getBoolean("derelict.port.exitOnTitle")) {
            Derelict.LOGGER.info("[derelict-port] ASSET_NOTICE_WOULD_SHOW");
            return;
        }
        Derelict.LOGGER.info("[derelict-port] ASSET_NOTICE_SHOWN");
        event.setNewScreen(new MissingAssetsScreen(title));
    }
}
