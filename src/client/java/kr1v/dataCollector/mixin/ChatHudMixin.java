package kr1v.dataCollector.mixin;

import kr1v.dataCollector.games.LuckyIslandsGame;
import kr1v.dataCollector.games.PoFGame;
import kr1v.dataCollector.GameMode;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static kr1v.dataCollector.DataCollectorClient.*;

@Mixin(ChatHud.class)
public class ChatHudMixin {
	@Inject(method = "addMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V", at = @At("HEAD"))
	private void injected(ChatHudLine message, CallbackInfo ci) {
		String content = message.content().getString();
		if (content.contains("Pillars of Fortune is starting in 1 second")) {
			currentGame = GameMode.CCG_PILLARS_OF_FORTUNE;
			data.ListOfPoFGames.add(new PoFGame());
		} else if (content.contains("Lucky Islands is starting in 1 second")) {
			currentGame = GameMode.CCG_PILLARS_OF_FORTUNE;
			data.ListOfLuckyIslandsGames.add(new LuckyIslandsGame());
		} else if (content.contains("Welcome to CubeCraft!")) {
			currentGame = GameMode.CCG_LOBBY;
		}
		else if (currentGame == GameMode.CCG_PILLARS_OF_FORTUNE) {
			if (content.contains("● You got ") && content.contains(" place.")) {
				data.ListOfPoFGames.getLast().place = getPlace(content);
			} else if (content.contains("● PoFGame length: ")) {
				data.ListOfPoFGames.getLast().lengthSeconds = getLength(content);
			} else if (content.contains("● Kills: ")) {
				data.ListOfPoFGames.getLast().kills = getKills(content);
			} else if (content.contains("● Points earned: ")) {
				data.ListOfPoFGames.getLast().pointsEarned = getPoints(content);
			}
		} else if (currentGame == GameMode.CCG_LUCKY_ISLANDS) {
			if (content.contains("● You got ") && content.contains(" place.")) {
				data.ListOfLuckyIslandsGames.getLast().place = getPlace(content);
			} else if (content.contains("● PoFGame length: ")) {
				data.ListOfLuckyIslandsGames.getLast().lengthSeconds = getLength(content);
			} else if (content.contains("● Kills: ")) {
				data.ListOfLuckyIslandsGames.getLast().kills = getKills(content);
			} else if (content.contains("● Lucky blocks opened: ")) {
				data.ListOfLuckyIslandsGames.getLast().luckyBlocksOpened = getLuckyBlocksOpened(content);
			} else if (content.contains("● Points earned: ")) {
				data.ListOfLuckyIslandsGames.getLast().pointsEarned = getPoints(content);
			}
		}
		else if (content.contains("Thank you for playing Pillars of Fortune")) {
			currentGame = null;
		} else if (content.contains("Thank you for playing Lucky Islands")) {
			currentGame = null;
		}
	}

	@Unique
	private static int getPlace(String content) {
		String placeStr;
		placeStr = content.replace("● You got", "").trim();
		placeStr = placeStr.replace(" place.", "");
		placeStr = placeStr.replace("st", "");
		placeStr = placeStr.replace("nd", "");
		placeStr = placeStr.replace("rd", "");
		placeStr = placeStr.replace("th", "");
		return Integer.parseInt(placeStr);
	}

	@Unique
	private static int getKills(String content) {
		return Integer.parseInt(content.replace("● Kills:", "").trim());
	}

	@Unique
	private static int getPoints(String content) {
		return Integer.parseInt(content.replace("● Points earned:", "").trim());
	}

	@Unique
	private static int getLuckyBlocksOpened(String content) {
		return Integer.parseInt(content.replace("● Lucky blocks opened:", "").trim());
	}

	@Unique
	private static int getLength(String content) {
		String lengthStr = content.replace("● PoFGame length:", "").trim();

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
		return minutes * 60 + seconds;
	}
}
