package kr1v.dataCollector.mixin;

import kr1v.dataCollector.DataCollectorClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

import static kr1v.dataCollector.DataCollectorClient.DATA_PATH;
import static kr1v.dataCollector.DataCollectorClient.shouldStartNewGame;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Inject(method = "joinWorld", at = @At("HEAD"))
	private void injected(ClientWorld world, DownloadingTerrainScreen.WorldEntryReason worldEntryReason, CallbackInfo ci) {
		DataCollectorClient.isInPoF = false;
		if (!shouldStartNewGame) {
			try {
				DataCollectorClient.data.save(DATA_PATH);
			} catch (IOException ignored) {
			}
		}
		DataCollectorClient.shouldStartNewGame = true;
	}
}
