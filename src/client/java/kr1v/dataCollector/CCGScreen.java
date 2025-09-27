package kr1v.dataCollector;

import kr1v.dataCollector.games.CCGGame;
import kr1v.dataCollector.games.PoFGame;
import kr1v.dataCollector.util.StringDrawer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.*;

public class CCGScreen extends Screen {
	private StringListWidget stringListWidget;
	private TextFieldWidget filterWidget;

	protected CCGScreen() {
		super(Text.of(""));
	}

	@Override
	protected void init() {
		this.stringListWidget = this.addDrawableChild(new StringListWidget(MinecraftClient.getInstance(), this.width / 2, this.height - 50, 50, 55, 0, this.width / 2));
		assert this.client != null;
		this.filterWidget = this.addDrawableChild(new TextFieldWidget(this.client.textRenderer, this.width / 2, 5, this.width / 2, 20, Text.of("")));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		if (client != null) {
			int totalGames = 0;
			int totalKills = 0;
			int totalTimeInSeconds = 0;
			int maxWinStreak = 0;
			int currentWinStreak = 0;

			int[] placements = new int[32];

			List<CCGGame> listOfGames = new ArrayList<>();
			listOfGames.addAll(DataCollectorClient.data.ListOfPoFGames);
			listOfGames.addAll(DataCollectorClient.data.ListOfLuckyIslandsGames);
			listOfGames.sort(Comparator.comparing(
				g -> g.timeStampGameStart,
				Comparator.nullsLast(Comparator.reverseOrder())
			));


			for (var game : listOfGames) {
				if (game.place == null) continue;
				placements[game.place]++;

				totalGames++;
				if (game.place == 1) {
					currentWinStreak++;
				} else {
					currentWinStreak = 0;
				}

				totalKills += game.kills;
				totalTimeInSeconds += game.lengthSeconds;
				if (maxWinStreak <= currentWinStreak)
					maxWinStreak = currentWinStreak;
			}

			double winRate = totalGames == 0 ? 0.0 : (100.0 * placements[1]) / totalGames;
			double avgKills = totalGames == 0 ? 0.0 : ((double) totalKills) / totalGames;
			double avgTimePerGame = totalGames == 0 ? 0.0 : ((double) totalTimeInSeconds) / totalGames;

			int x = 5;
			int y = 5;

			StringDrawer sd = new StringDrawer(x, y, 0xFFFFFFFF, context, true);

			sd.drawTimeString("Play time: ", (double) totalTimeInSeconds);
			sd.drawString("Wins: " + placements[1]);
			sd.drawString("Games: " + totalGames);
			sd.drawString(String.format("Win rate: %.1f%%", winRate));
			sd.drawString("Kills: " + totalKills);
			sd.drawString(String.format("Average kills/game: %.2f", avgKills));
			sd.drawTimeString("Average time/game: ", avgTimePerGame, " (%.2fs)".formatted(avgTimePerGame));
			sd.drawString("Best win streak: " + maxWinStreak);
			sd.drawString("Current streak: " + currentWinStreak);
			sd.drawString("Placements: ");
			for (int i = 0; i < placements.length; i++) {
				if (i == 0 || placements[i] == 0) continue;
				String toAdd = "th";
				if (i == 1) toAdd = "st";
				else if (i == 2) toAdd = "nd";
				else if (i == 3) toAdd = "rd";
				sd.drawString("    " + i + toAdd + " - " + placements[i] + " times");
			}

			stringListWidget.clearStrings();
			String filterText = filterWidget.getText();
			int i = 0;
			for (CCGGame game : listOfGames) {
				String s =
					"Game mode: " + (game.getClass() == PoFGame.class ? "Pillars of Fortune" : "Lucky Islands") +
					"\n    Kills: " + game.kills +
					"\n    Place: " + game.place +
					"\n    Length: " + StringDrawer.formatDuration((double) game.lengthSeconds) +
					"\n    Points: " + game.pointsEarned;

				if (s.contains(filterText)) {
					stringListWidget.addString(s);
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
