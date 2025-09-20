package kr1v.dataCollector;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.*;

public class StatsScreen extends Screen {
	private StringListWidget stringListWidget;
	private TextFieldWidget filterWidget;

	protected StatsScreen() {
		super(Text.of(""));
	}

	@Override
	protected void init() {
		this.stringListWidget = this.addDrawableChild(new StringListWidget(MinecraftClient.getInstance(), this.width / 2, this.height - 50, 50, 10, 0));
		this.filterWidget = this.addDrawableChild(new TextFieldWidget(this.client.textRenderer, this.width / 2, 5, this.width / 2, 20, Text.of("")));
		Map<String, Integer> map = new HashMap<>(); // item to count
		var listOfGames = DataCollectorClient.data.PoFListOfGames;
		for (Game game : listOfGames) {
			for (String item : game.items) {
				map.put(item, map.getOrDefault(item, 0) + 1);
			}
		}
		List<String> sortedKeys = map.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()).thenComparing(Map.Entry.comparingByKey()) // optional tie-breaker by key
		).map(Map.Entry::getKey).toList();
		stringListWidget.clearStrings();
		for (String s : sortedKeys) {
			stringListWidget.addString(s.replace("_", " ") + ": " + map.get(s));
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		if (client != null) {
			int totalWins = 0;
			int totalGames = 0;
			int totalKills = 0;
			int totalTimeInSeconds = 0;
			int totalItems = 0;
			int fastestWinInSeconds = Integer.MAX_VALUE;
			int longestGameInSeconds = 0;

			Map<String, Integer> map = new HashMap<>(); // item to count
			var listOfGames = DataCollectorClient.data.PoFListOfGames;

			// collect item counts and build list of game lengths
			List<Integer> gameLengths = new ArrayList<>();
			for (Game game : listOfGames) {
				for (String item : game.items) {
					map.put(item, map.getOrDefault(item, 0) + 1);
				}
				// store length for median and longest game calculations (only when length known)
				if (game.lengthSeconds > 0) {
					gameLengths.add(game.lengthSeconds);
					if (game.lengthSeconds > longestGameInSeconds) longestGameInSeconds = game.lengthSeconds;
				}
			}

			for (var game : listOfGames) {
				if (game.place == null) continue;
				totalGames++;
				if (game.place == 1) totalWins++;
				totalKills += game.kills;
				totalTimeInSeconds += game.lengthSeconds;
				totalItems += game.items.size();
				if (game.lengthSeconds < fastestWinInSeconds) fastestWinInSeconds = game.lengthSeconds;
			}

			// extra computed stats
			double winRate = totalGames == 0 ? 0.0 : (100.0 * totalWins) / totalGames;
			double avgKills = totalGames == 0 ? 0.0 : ((double) totalKills) / totalGames;
			double avgItems = totalGames == 0 ? 0.0 : ((double) totalItems) / totalGames;
			double avgTimePerGame = totalGames == 0 ? 0.0 : ((double) totalTimeInSeconds) / totalGames;

			// average time for wins (only consider games that were wins)
			int totalTimeForWins = 0;
			int winsConsidered = 0;
			int totalItemsForWins = 0;
			for (var game : listOfGames) {
				if (game.place != null && game.place == 1) {
					winsConsidered++;
					totalTimeForWins += game.lengthSeconds;
					totalItemsForWins += game.items.size();
				}
			}
			double avgTimePerWin = winsConsidered == 0 ? 0.0 : ((double) totalTimeForWins) / winsConsidered;
			double avgItemsPerWin = winsConsidered == 0 ? 0.0 : ((double) totalItemsForWins) / winsConsidered;

			// median game length
			String medianTimeText = "Median game: -";
			if (!gameLengths.isEmpty()) {
				Collections.sort(gameLengths);
				int n = gameLengths.size();
				double median;
				if (n % 2 == 1) {
					median = gameLengths.get(n / 2);
				} else {
					median = (gameLengths.get(n/2 - 1) + gameLengths.get(n/2)) / 2.0;
				}
				medianTimeText = "Median game: " + formatDuration((int)Math.round(median));
			}

			// unique items count
			int uniqueItems = map.size();

			// format common stat strings
			String totalWinsText = "Wins: " + totalWins;
			String totalGamesText = "Games: " + totalGames;
			String totalKillsText = "Kills: " + totalKills;
			String totalItemsText = "Items: " + totalItems;
			String uniqueItemsText = "Unique items: " + uniqueItems;
			String totalTimeText = "Play time: " + formatDuration(totalTimeInSeconds);
			String fastestWinText = fastestWinInSeconds == Integer.MAX_VALUE ? "Fastest win: -" : "Fastest win: " + formatDuration(fastestWinInSeconds);
			String longestGameText = longestGameInSeconds == 0 ? "Longest game: -" : "Longest game: " + formatDuration(longestGameInSeconds);
			String winRateText = String.format("Win rate: %.1f%%", winRate);
			String avgKillsText = String.format("Avg kills/game: %.2f", avgKills);
			String avgItemsText = String.format("Avg items/game: %.2f", avgItems);
			String avgTimeText = String.format("Avg time/game: %s (%.2fs)", formatDuration((int)Math.round(avgTimePerGame)), avgTimePerGame);
			String avgTimePerWinText = winsConsidered == 0 ? "Avg time/win: -" : String.format("Avg time/win: %s (%.2fs)", formatDuration((int)Math.round(avgTimePerWin)), avgTimePerWin);
			String avgItemsPerWinText = winsConsidered == 0 ? "Avg items/win: -" : String.format("Avg items/win: %.2f", avgItemsPerWin);

			int x = 5;
			int y = 5;
			int lineHeight = client.textRenderer.fontHeight + 2;

			context.drawTextWithShadow(client.textRenderer, totalWinsText, x, y, 0xFFFFFFFF);
			y += lineHeight;
			context.drawTextWithShadow(client.textRenderer, totalGamesText, x, y, 0xFFFFFFFF);
			y += lineHeight;
			context.drawTextWithShadow(client.textRenderer, totalKillsText, x, y, 0xFFFFFFFF);
			y += lineHeight;
			context.drawTextWithShadow(client.textRenderer, totalTimeText, x, y, 0xFFFFFFFF);
			y += lineHeight;
			context.drawTextWithShadow(client.textRenderer, totalItemsText, x, y, 0xFFFFFFFF);
			y += lineHeight;
			context.drawTextWithShadow(client.textRenderer, uniqueItemsText, x, y, 0xFFFFFFFF);
			y += lineHeight;

			context.drawTextWithShadow(client.textRenderer, winRateText, x, y, 0xFFFFFFFF);
			y += lineHeight;
			context.drawTextWithShadow(client.textRenderer, avgKillsText, x, y, 0xFFFFFFFF);
			y += lineHeight;
			context.drawTextWithShadow(client.textRenderer, avgItemsText, x, y, 0xFFFFFFFF);
			y += lineHeight;
			context.drawTextWithShadow(client.textRenderer, avgTimeText, x, y, 0xFFFFFFFF);
			y += lineHeight;
			context.drawTextWithShadow(client.textRenderer, avgItemsPerWinText, x, y, 0xFFFFFFFF);
			y += lineHeight;
			context.drawTextWithShadow(client.textRenderer, avgTimePerWinText, x, y, 0xFFFFFFFF);
			y += lineHeight;
			context.drawTextWithShadow(client.textRenderer, medianTimeText, x, y, 0xFFFFFFFF);
			y += lineHeight;
			context.drawTextWithShadow(client.textRenderer, fastestWinText, x, y, 0xFFFFFFFF);
			y += lineHeight;
			context.drawTextWithShadow(client.textRenderer, longestGameText, x, y, 0xFFFFFFFF);

			List<String> sortedKeys = map.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()).thenComparing(Map.Entry.comparingByKey()) // optional tie-breaker by key
			).map(Map.Entry::getKey).toList();
			stringListWidget.clearStrings();
			String filterText = filterWidget.getText();
			int i = 0;
			for (String s : sortedKeys) {
				String newStr = s.replace("_", " ") + ": " + map.get(s);
				if (s.contains(filterText) || newStr.contains(filterText)) {
					stringListWidget.addString(newStr);
					i++;
				}
			}

			if (!filterText.trim().isEmpty()) {
				int colour = 0xFFFFFFFF;
				if (i == 0) colour -= 0xAAAA;
				context.drawTextWithShadow(client.textRenderer, "Results: " + i, this.width / 2, 26, colour);
			}
		}

		super.render(context, mouseX, mouseY, deltaTicks);
	}

	private static String formatDuration(int totalSeconds) {
		int hours = totalSeconds / 3600;
		int minutes = (totalSeconds % 3600) / 60;
		int seconds = totalSeconds % 60;
		if (hours > 0) {
			return String.format("%dh %dm %ds", hours, minutes, seconds);
		} else if (minutes > 0) {
			return String.format("%dm %ds", minutes, seconds);
		} else {
			return String.format("%ds", seconds);
		}
	}
}
