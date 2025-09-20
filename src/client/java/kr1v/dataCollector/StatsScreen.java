package kr1v.dataCollector;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsScreen extends Screen {
	private StringListWidget stringListWidget;

	protected StatsScreen() {
		super(Text.of(""));
	}

	@Override
	protected void init() {
		this.stringListWidget = this.addDrawableChild(new StringListWidget(MinecraftClient.getInstance(), this.width, this.height - 100, 100, 10, 2));
		Map<String, Integer> map = new HashMap<>(); // item to count
		var listOfGames = DataCollectorClient.data.PoFListOfGames;
		for (Game game : listOfGames) {
			for (String item : game.items) {
				map.put(item, map.getOrDefault(item, 0) + 1);
			}
		}
		List<String> sortedKeys = map.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()).thenComparing(Map.Entry.comparingByKey()) // optional tie-breaker by key
		).map(Map.Entry::getKey).toList();
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
			var listOfGames = DataCollectorClient.data.PoFListOfGames;

			for (var game : listOfGames) {
				totalGames++;
				if (game.place == 1) totalWins++;
				totalKills += game.kills;
				totalTimeInSeconds += game.lengthSeconds;
				totalItems += game.items.size();
			}

// put this where you render the HUD (you already have the totals calculated above)
			String totalWinsText = "Wins: " + totalWins;
			String totalGamesText = "Games: " + totalGames;
			String totalKillsText = "Kills: " + totalKills;
			String totalItemsText = "Items: " + totalItems;

			String totalTimeText = "Play time: " + formatDuration(totalTimeInSeconds);

// some optional useful derived stats
			double winRate = totalGames == 0 ? 0.0 : (100.0 * totalWins) / totalGames;
			String winRateText = String.format("Win rate: %.1f%%", winRate);

			double avgKills = totalGames == 0 ? 0.0 : ((double) totalKills) / totalGames;
			String avgKillsText = String.format("Avg kills/game: %.2f", avgKills);

			double avgItems = totalGames == 0 ? 0.0 : ((double) totalItems) / totalGames;
			String avgItemsText = String.format("Avg items/game: %.2f", avgItems);

			int x = 5;
			int y = 5;
// prefer using the font height for spacing; add a couple pixels so lines don't touch
			int lineHeight = client.textRenderer.fontHeight + 2;

// draw the core totals
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

// draw the optional derived stats
			context.drawTextWithShadow(client.textRenderer, winRateText, x, y, 0xFFAAAAFF);
			y += lineHeight;
			context.drawTextWithShadow(client.textRenderer, avgKillsText, x, y, 0xFFFFFFFF);
			y += lineHeight;
			context.drawTextWithShadow(client.textRenderer, avgItemsText, x, y, 0xFFFFFFFF);
		}

		super.render(context, mouseX, mouseY, deltaTicks);
	}

	// helper to format durations nicely
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
