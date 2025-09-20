package kr1v.dataCollector.mixin;

import kr1v.dataCollector.DataCollectorClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static kr1v.dataCollector.DataCollectorClient.isInPoF;

@Mixin(ChatHud.class)
public class ChatHudMixin {
	@Inject(method = "addMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V", at = @At("HEAD"))
	private void injected(ChatHudLine message, CallbackInfo ci) {
		String content = message.content().getString();
		if (content.contains("Pillars of Fortune is starting in 1 second")) {
			isInPoF = true;
		} else if (content.contains("● You got ") && content.contains(" place.") && isInPoF) {
			String placeStr;
			placeStr = content.replace("● You got", "").trim();
			placeStr = placeStr.replace(" place.", "");
			placeStr = placeStr.replace("st", "");
			placeStr = placeStr.replace("nd", "");
			placeStr = placeStr.replace("rd", "");
			placeStr = placeStr.replace("th", "");

			final int place = Integer.parseInt(placeStr);
			DataCollectorClient.data.PoFListOfGames.getLast().place = place;
		} else if (content.contains("● Game length: ") && isInPoF) {
			String lengthStr = content.replace("● Game length:", "").trim();

			int minutes = 0;
			int seconds = 0;

			String[] parts = lengthStr.split(",");
			for (String part : parts) {
				part = part.trim();
				if (part.contains("minute")) {
					minutes = Integer.parseInt(part.split(" ")[0]);
				} else if (part.contains("second")) {
					seconds = Integer.parseInt(part.split(" ")[0]);
				}
			}

			final int length = minutes * 60 + seconds;
			DataCollectorClient.data.PoFListOfGames.getLast().lengthSeconds = length;
		} else if (content.contains("● Kills: ") && isInPoF) {
			String killsStr = content.replace("● Kills:", "").trim();
			final int kills = Integer.parseInt(killsStr);
			DataCollectorClient.data.PoFListOfGames.getLast().kills = kills;
		} else if (content.contains("Thank you for playing Pillars of Fortune")) {
			isInPoF = false;
		}
//		System.out.println("\n" + message.content().getString() + "\n");
	}
}
