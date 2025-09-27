package kr1v.dataCollector;

import kr1v.dataCollector.util.StringDrawer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.*;

public class PoFStatsScreen extends Screen {
	private StringListWidget stringListWidget;
	private TextFieldWidget filterWidget;

	protected PoFStatsScreen() {
		super(Text.of(""));
	}

	@Override
	protected void init() {
		this.stringListWidget = this.addDrawableChild(new StringListWidget(MinecraftClient.getInstance(), this.width / 2, this.height - 50, 50, 10, 0, this.width / 2));
		assert this.client != null;
		this.filterWidget = this.addDrawableChild(new TextFieldWidget(this.client.textRenderer, this.width / 2, 5, this.width / 2, 20, Text.of("")));
		this.addDrawableChild(ButtonWidget.builder(Text.literal("Individual stats"), button -> this.client.setScreen(new PoFIndividualGamesScreen())).dimensions(5, this.height - 25, 150, 20).build());
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		if (client != null) {
			int totalGames = 0;
			int totalKills = 0;
			int totalTimeInSeconds = 0;
			int totalItems = 0;
			int fastestWinInSeconds = Integer.MAX_VALUE;
			int maxWinStreak = 0;
			int currentWinStreak = 0;
			int mostKillsInOneGame = 0;
			int totalTimeForWins = 0;
			int totalItemsForWins = 0;

			int[] placements = new int[9];

			Map<String, Integer> map = new HashMap<>(); // item to count
			var listOfGames = DataCollectorClient.data.ListOfPoFGames;

			for (var game : listOfGames) {
				if (game.place == null) continue;
				placements[game.place]++;

				totalGames++;
				if (game.place == 1) {
					currentWinStreak++;

					if (fastestWinInSeconds > game.lengthSeconds)
						fastestWinInSeconds = game.lengthSeconds;

					totalTimeForWins += game.lengthSeconds;
					totalItemsForWins += game.items.size();
				} else {
					currentWinStreak = 0;
				}

				totalKills += game.kills;
				totalTimeInSeconds += game.lengthSeconds;
				totalItems += game.items.size();


				if (mostKillsInOneGame < game.kills)
					mostKillsInOneGame = game.kills;

				for (String item : game.items) {
					map.put(item, map.getOrDefault(item, 0) + 1);
				}

				if (maxWinStreak <= currentWinStreak)
					maxWinStreak = currentWinStreak;
			}

			double winRate = totalGames == 0 ? 0.0 : (100.0 * placements[1]) / totalGames;
			double avgKills = totalGames == 0 ? 0.0 : ((double) totalKills) / totalGames;
			double avgItems = totalGames == 0 ? 0.0 : ((double) totalItems) / totalGames;
			double avgTimePerGame = totalGames == 0 ? 0.0 : ((double) totalTimeInSeconds) / totalGames;
			double avgTimePerWin = placements[1] == 0 ? 0.0 : ((double) totalTimeForWins) / placements[1];
			double avgItemsPerWin = placements[1] == 0 ? 0.0 : ((double) totalItemsForWins) / placements[1];
			int uniqueItems = map.size();

			int x = 5;
			int y = 5;

			StringDrawer sd = new StringDrawer(x, y, 0xFFFFFFFF, context, true);

			sd.drawTimeString("Play time: ", (double) totalTimeInSeconds);
			sd.drawString("Wins: " + placements[1]);
			sd.drawTimeString("Fastest win: ", (double) fastestWinInSeconds);
			sd.drawString("Games: " + totalGames);
			sd.drawString(String.format("Win rate: %.1f%%", winRate));
			sd.drawString("Kills: " + totalKills);
			sd.drawString("Items: " + totalItems);
			sd.drawString("Unique items: " + uniqueItems);
			sd.drawString(String.format("Average kills/poFGame: %.2f", avgKills));
			sd.drawString(String.format("Average items/poFGame: %.2f", avgItems));
			sd.drawTimeString("Average time/poFGame: ", avgTimePerGame, " (%.2fs)".formatted(avgTimePerGame));
			sd.drawTimeString("Average time/win: ", avgTimePerWin, " (%.2fs)".formatted(avgTimePerWin));
			sd.drawString(String.format("Average items/win: %.2f", avgItemsPerWin));
			sd.drawString("Best win streak: " + maxWinStreak);
			sd.drawString("Current streak: " + currentWinStreak);
			sd.drawString("Most kills in one poFGame: " + mostKillsInOneGame);
			sd.drawString("Placements: ");
			for (int i = 0; i < placements.length; i++) {
				if (i == 0) continue;
				String toAdd = "th";
				if (i == 1) toAdd = "st";
				else if (i == 2) toAdd = "nd";
				else if (i == 3) toAdd = "rd";
				sd.drawString("    " + i + toAdd + " - " + placements[i] + " times");
			}

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
}
