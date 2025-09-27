package kr1v.dataCollector;

import kr1v.dataCollector.games.PoFGame;
import kr1v.dataCollector.util.StringDrawer;
import kr1v.dataCollector.util.Util;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class PoFIndividualGamesScreen extends Screen {
	GameListWidget gameListWidget;
	StringListWidget stringListWidget;
	static Sort sortingBy = Sort.RECENT;
	static TypeOfSort howToSort = TypeOfSort.ASCENDING;
	private TextFieldWidget itemFilterField;
	private TextFieldWidget minKillsField;
	private TextFieldWidget maxKillsField;
	private TextFieldWidget minPlaceField;
	private TextFieldWidget maxPlaceField;

	protected PoFIndividualGamesScreen() {
		super(Text.of(""));
	}

	protected void init() {
		this.gameListWidget = addDrawableChild(new GameListWidget(this.client, this.width / 2, this.height, 0, 50, 0, this.width / 2));
		for (PoFGame poFGame : DataCollectorClient.data.ListOfPoFGames) {
			gameListWidget.addGame(poFGame);
		}
		int quarter = width / 4;
		int oneEights = width / 8;

		this.stringListWidget = addDrawableChild(new StringListWidget(this.client, this.width / 2, this.height / 2, this.height / 2, 11, 0, 0));

		addDrawableChild(new ButtonWidget.Builder(
			Text.of("Sort by: " + sortingBy.name().toLowerCase().replace("_", " ")),
			(button) -> {
				sortingBy = PoFIndividualGamesScreen.next(sortingBy);
				button.setMessage(Text.of("Sort by: " + sortingBy.name().toLowerCase().replace("_", " ")));
				this.applyFilters();
			})
			.dimensions(quarter, 22, quarter - 5, 20)
			.build());

		addDrawableChild(new ButtonWidget.Builder(
			Text.of(howToSort.name().substring(0, 1).concat(howToSort.name().toLowerCase().substring(1))),
			(button) -> {
				if (howToSort == TypeOfSort.ASCENDING) {
					howToSort = TypeOfSort.DESCENDING;
				} else {
					howToSort = TypeOfSort.ASCENDING;
				}
				button.setMessage(Text.of(howToSort.name().substring(0, 1).concat(howToSort.name().toLowerCase().substring(1))));
				this.applyFilters();
			})
			.dimensions(quarter, 44, quarter - 5, 20)
			.build());

		int y = 84;

		this.itemFilterField = addDrawableChild(new TextFieldWidget(
			this.textRenderer, quarter, y, quarter - 5, 20, Text.of("Enter item name...")));
		y += 40;
		this.itemFilterField.setChangedListener(s -> this.applyFilters());

		this.minKillsField = addDrawableChild(new TextFieldWidget(
			this.textRenderer, quarter, y, oneEights - 5, 20, Text.of("Min kills")));
		this.minKillsField.setChangedListener(s -> this.applyFilters());

		this.maxKillsField = addDrawableChild(new TextFieldWidget(
			this.textRenderer, quarter + oneEights, y, oneEights - 5, 20, Text.of("Max kills")));
		this.maxKillsField.setChangedListener(s -> this.applyFilters());
		y += 40;

		this.minPlaceField = addDrawableChild(new TextFieldWidget(
			this.textRenderer, quarter, y, oneEights - 5, 20, Text.of("Min place")));
		this.minPlaceField.setChangedListener(s -> this.applyFilters());

		this.maxPlaceField = addDrawableChild(new TextFieldWidget(
			this.textRenderer, quarter + oneEights, y, oneEights - 5, 20, Text.of("Max place")));
		this.maxPlaceField.setChangedListener(s -> this.applyFilters());
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		if (client != null) {
			StringDrawer sd = new StringDrawer(2, 2, 0xFFFFFFFF, context, true);
			GameListWidget.GameEntry selectedGame = this.gameListWidget.getSelectedOrNull();
			sd.drawString("Total games: " + DataCollectorClient.data.ListOfPoFGames.size());

			this.stringListWidget.clearStrings();
			if (selectedGame != null) {
				PoFGame poFGame = selectedGame.poFGame;
				sd.drawString("In this game");
				sd.setX(sd.getX() + 6);
				sd.drawString("Total items: " + poFGame.items.size());
				int i = 0;
				for (String s : poFGame.items) {
					String newStr = ++i + ": " + s.replace("_", " ");
					stringListWidget.addString(newStr);
				}
			}
			int quarter = width / 4;
			sd.setX(quarter);
			sd.setY(10);
			sd.drawString("Sorting Options");
			sd.setY(sd.getY() + 52);
			sd.drawString("Item Filter");
			sd.setY(sd.getY() + 30);
			sd.drawString("Kills Range");
			sd.setY(sd.getY() + 30);
			sd.drawString("Place Range");
		}
		super.render(context, mouseX, mouseY, deltaTicks);
	}

	public enum TypeOfSort {
		ASCENDING,
		DESCENDING,
	}

	public enum Sort {
		RECENT,
		PLACE,
		TIME,
		KILLS,
		TOP_ITEM,
		RANDOM,
	}
	static Sort next(Sort s) {
		Sort[] vals = Sort.values();
		int nextIndex = (s.ordinal() + 1) % vals.length;
		return vals[nextIndex];
	}

	private void applyFilters() {
		List<PoFGame> filteredPoFGames = new ArrayList<>(DataCollectorClient.data.ListOfPoFGames);

		String itemFilter = itemFilterField.getText().toLowerCase();
		if (!itemFilter.isEmpty()) {
			filteredPoFGames.removeIf(game ->
				game.items.stream().noneMatch(item ->
					item.toLowerCase().contains(itemFilter)));
		}

		try {
			if (!minKillsField.getText().isEmpty()) {
				int minKills = Integer.parseInt(minKillsField.getText());
				filteredPoFGames.removeIf(game -> game.kills < minKills);
			}
			if (!maxKillsField.getText().isEmpty()) {
				int maxKills = Integer.parseInt(maxKillsField.getText());
				filteredPoFGames.removeIf(game -> game.kills > maxKills);
			}
		} catch (NumberFormatException ignored) {}

		try {
			if (!minPlaceField.getText().isEmpty()) {
				int minPlace = Integer.parseInt(minPlaceField.getText());
				filteredPoFGames.removeIf(game -> game.place != null && game.place < minPlace);
			}
			if (!maxPlaceField.getText().isEmpty()) {
				int maxPlace = Integer.parseInt(maxPlaceField.getText());
				filteredPoFGames.removeIf(game -> game.place != null && game.place > maxPlace);
			}
		} catch (NumberFormatException ignored) {}

		Util.sortGames(filteredPoFGames, sortingBy, howToSort);

		gameListWidget.clearGames();
		for (PoFGame poFGame : filteredPoFGames) {
			gameListWidget.addGame(poFGame);
		}
	}
}
