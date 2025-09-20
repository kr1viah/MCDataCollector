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
		assert this.client != null;
		this.filterWidget = this.addDrawableChild(new TextFieldWidget(this.client.textRenderer, this.width / 2, 5, this.width / 2, 20, Text.of("")));
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
			int maxWinStreak = 0;
			int currentWinStreak = 0;

			Map<String, Integer> map = new HashMap<>(); // item to count
			var listOfGames = DataCollectorClient.data.PoFListOfGames;

			for (var game : listOfGames) {
				if (game.place == null) continue;

				totalGames++;
				if (game.place == 1) totalWins++;

				totalKills += game.kills;
				totalTimeInSeconds += game.lengthSeconds;
				totalItems += game.items.size();

				if (fastestWinInSeconds >= game.lengthSeconds && game.place == 1)
					fastestWinInSeconds = game.lengthSeconds;
				for (String item : game.items) {
					map.put(item, map.getOrDefault(item, 0) + 1);
				}

				if (game.place == 1)
					currentWinStreak++;
				else
					currentWinStreak = 0;
				if (maxWinStreak <= currentWinStreak)
					maxWinStreak = currentWinStreak;
			}

			double winRate = totalGames == 0 ? 0.0 : (100.0 * totalWins) / totalGames;
			double avgKills = totalGames == 0 ? 0.0 : ((double) totalKills) / totalGames;
			double avgItems = totalGames == 0 ? 0.0 : ((double) totalItems) / totalGames;
			double avgTimePerGame = totalGames == 0 ? 0.0 : ((double) totalTimeInSeconds) / totalGames;

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
			int uniqueItems = map.size();

			int x = 5;
			int y = 5;

			y = drawString(context, "Play time: " + formatDuration(totalTimeInSeconds), x, y);
			y = drawString(context, "Wins: " + totalWins, x, y);
			y = drawString(context, "Games: " + totalGames, x, y);
			y = drawString(context, String.format("Win rate: %.1f%%", winRate), x, y);
			y = drawString(context, "Kills: " + totalKills, x, y);
			y = drawString(context, "Items: " + totalItems, x, y);
			y = drawString(context, "Unique items: " + uniqueItems, x, y);
			y = drawString(context, fastestWinInSeconds == Integer.MAX_VALUE ? "Fastest win: -" : "Fastest win: " + formatDuration(fastestWinInSeconds), x, y);
			y = drawString(context, String.format("Average kills/game: %.2f", avgKills), x, y);
			y = drawString(context, String.format("Average items/game: %.2f", avgItems), x, y);
			y = drawString(context, String.format("Average time/game: %s (%.2fs)", formatDuration((int)Math.round(avgTimePerGame)), avgTimePerGame), x, y);
			y = drawString(context, winsConsidered == 0 ? "Average time/win: -" : String.format("Average time/win: %s (%.2fs)", formatDuration((int)Math.round(avgTimePerWin)), avgTimePerWin), x, y);
			y = drawString(context, winsConsidered == 0 ? "Average items/win: -" : String.format("Average items/win: %.2f", avgItemsPerWin), x, y);
			y = drawString(context, "Best win streak: " + maxWinStreak, x, y);
			y = drawString(context, "Current streak: " + currentWinStreak, x, y);

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
				int color = i == 0 ? 0xFFFF5555 : 0xFFFFFFFF;
				context.drawTextWithShadow(client.textRenderer, "Results: " + i, this.width / 2, 26, color);
			}
		}

		super.render(context, mouseX, mouseY, deltaTicks);
	}

	private int drawString(DrawContext context, String text, int x, int y) {
		context.drawTextWithShadow(client.textRenderer, text, x, y, 0xFFFFFFFF);
		return y + (client.textRenderer.fontHeight + 2);
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
