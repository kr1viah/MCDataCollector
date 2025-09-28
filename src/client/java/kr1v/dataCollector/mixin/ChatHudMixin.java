package kr1v.dataCollector.mixin;

import kr1v.dataCollector.GameMode;
import kr1v.dataCollector.games.CCGGame;
import kr1v.dataCollector.games.LuckyIslandsGame;
import kr1v.dataCollector.games.PoFGame;
import kr1v.dataCollector.games.SkyWarsGame;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static kr1v.dataCollector.DataCollectorClient.currentGame;
import static kr1v.dataCollector.DataCollectorClient.data;

@Mixin(ChatHud.class)
public class ChatHudMixin {
	@Inject(method = "addMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V", at = @At("HEAD"))
	private void injected(ChatHudLine message, CallbackInfo ci) {
		String content = message.content().getString();
		if (content.contains("Pillars of Fortune is starting in 1 second")) {
			currentGame = GameMode.CCG_PILLARS_OF_FORTUNE;
			data.listOfPoFGames.add(new PoFGame());
		} else if (content.contains("Lucky Islands is starting in 1 second")) {
			currentGame = GameMode.CCG_LUCKY_ISLANDS;
			data.listOfLuckyIslandsGames.add(new LuckyIslandsGame());
		} else if (content.contains("SkyWars is starting in 1 second")) {
			currentGame = GameMode.CCG_SKY_WARS;
			data.listOfSkyWarsGames.add(new SkyWarsGame());
		} else if (content.contains("Welcome to CubeCraft!")) {
			currentGame = GameMode.CCG_LOBBY;
		} else if (currentGame != null && currentGame.isCCGGame()) {
			if (!checkBasic(content)) {
				switch (currentGame) {
					case CCG_PILLARS_OF_FORTUNE -> {
					}
					case CCG_SKY_WARS -> {
						if (content.contains("● Accuracy: ")) {
							System.out.println("Accuracy");
							data.listOfSkyWarsGames.getLast().accuracy = getAccuracy(content);
						}
					}
					case CCG_LUCKY_ISLANDS -> {
						if (content.contains("● Lucky blocks opened: ")) {
							data.listOfLuckyIslandsGames.getLast().luckyBlocksOpened = getLuckyBlocksOpened(content);
						}
					}
				}
			}
		} else if (content.contains("Thank you for playing Pillars of Fortune")) {
			currentGame = null;
		} else if (content.contains("Thank you for playing Lucky Islands")) {
			currentGame = null;
		}else if (content.contains("Thank you for playing SkyWars")) {
			currentGame = null;
		}
	}

	@Unique
	private float getAccuracy(String content) {
		return Float.parseFloat(content.replace("● Accuracy:", "").replace("%", "").trim());
	}

	@Unique
	private boolean checkBasic(String content) {
		List<? extends CCGGame> currentGameList = List.of(new CCGGame());
		if (currentGame == GameMode.CCG_SKY_WARS) currentGameList = data.listOfSkyWarsGames;
		if (currentGame == GameMode.CCG_LUCKY_ISLANDS) currentGameList = data.listOfLuckyIslandsGames;
		if (currentGame == GameMode.CCG_PILLARS_OF_FORTUNE) currentGameList = data.listOfPoFGames;
		if (content.contains("● You got ") && content.contains(" place.")) {
			currentGameList.getLast().place = getPlace(content);
			return true;
		} else if (content.contains("● Game length: ")) {
			currentGameList.getLast().lengthSeconds = getLength(content);
			return true;
		} else if (content.contains("● Kills: ")) {
			currentGameList.getLast().kills = getKills(content);
			return true;
		} else if (content.contains("● Points earned: ")) {
			currentGameList.getLast().pointsEarned = getPoints(content);
			return true;
		}
		return false;
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
		return minutes * 60 + seconds;
	}
}
