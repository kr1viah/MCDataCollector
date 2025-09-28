package kr1v.dataCollector.mixin;

import kr1v.dataCollector.GameMode;
import kr1v.dataCollector.games.*;
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
		} else if (content.contains("EggWars is starting in 1 second")) {
			currentGame = GameMode.CCG_EGG_WARS;
			if (data.listOfEggWarsGames.getLast().lengthSeconds == -1) return;
			data.listOfEggWarsGames.add(new EggWarsGame());
		} else if (content.contains("Welcome to CubeCraft!")) {
			currentGame = GameMode.CCG_LOBBY;
		} else if (currentGame != null && currentGame.isCCGGame()) {
			System.out.println(content);
			if (!checkBasicCCG(content)) {
				switch (currentGame) {
					case CCG_PILLARS_OF_FORTUNE -> {
					}
					case CCG_EGG_WARS -> {
						if (content.contains("● Eggs broken: ")) {
							data.listOfEggWarsGames.getLast().eggsBroken = getInt(content, "● Eggs broken: ");
						}
						if (content.contains("● Deaths: ")) {
							data.listOfEggWarsGames.getLast().deaths = getInt(content, "● Deaths: ");
						}
						if (content.contains("● K/D: ")) {
							data.listOfEggWarsGames.getLast().KDRatio = getFloat(content, "● K/D: ");
						}
						if (content.contains("● Eliminations: ")) {
							data.listOfEggWarsGames.getLast().finalKills = getInt(content, "● Eliminations: ");
						}
					}
					case CCG_SKY_WARS -> {
						if (content.contains("● Accuracy: ")) {
							data.listOfSkyWarsGames.getLast().accuracy = getInt(content, "● Accuracy: ", "%");
						}
					}
					case CCG_LUCKY_ISLANDS -> {
						if (content.contains("● Lucky blocks opened: ")) {
							data.listOfLuckyIslandsGames.getLast().luckyBlocksOpened = getInt(content, "● Lucky blocks opened: ");
						}
					}
				}
			}
		} else if (content.contains("Thank you for playing Pillars of Fortune")) {
			currentGame = null;
		} else if (content.contains("Thank you for playing Lucky Islands")) {
			currentGame = null;
		} else if (content.contains("Thank you for playing SkyWars")) {
			currentGame = null;
		} else if (content.contains("Thank you for playing EggWars")) {
			currentGame = null;
		}
	}

	@Unique
	private static float getFloat(String content, String prefix) {
		return getFloat(content, prefix, "");
	}

	@Unique
	private static float getFloat(String content, String prefix, String suffix) {
		return Float.parseFloat(content.replace(prefix, "").replace(suffix, ""));
	}

	@Unique
	private boolean checkBasicCCG(String content) {
		List<? extends CCGGame> currentGameList = List.of(new CCGGame());
		if (currentGame == GameMode.CCG_SKY_WARS) currentGameList = data.listOfSkyWarsGames;
		if (currentGame == GameMode.CCG_LUCKY_ISLANDS) currentGameList = data.listOfLuckyIslandsGames;
		if (currentGame == GameMode.CCG_PILLARS_OF_FORTUNE) currentGameList = data.listOfPoFGames;
		if (currentGame == GameMode.CCG_EGG_WARS) currentGameList = data.listOfEggWarsGames;
		if (content.contains("● You got ") && content.contains(" place.")) {
			currentGameList.getLast().place = getPlace(content);
			return true;
		} else if (content.contains("● Game length: ")) {
			currentGameList.getLast().lengthSeconds = getLength(content);
			return true;
		} else if (content.contains("● Kills: ")) {
			currentGameList.getLast().kills = getInt(content, "● Kills: ");
			return true;
		} else if (content.contains("● Points earned: ")) {
			currentGameList.getLast().pointsEarned = getInt(content, "● Points earned: ");
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
	private static int getInt(String content, String prefix) {
		return getInt(content, prefix, "");
	}

	@Unique
	private static int getInt(String content, String prefix, String suffix) {
		return Integer.parseInt(content.replace(prefix, "").replace(suffix, ""));
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
