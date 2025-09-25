package kr1v.dataCollector;

import kr1v.dataCollector.util.StringDrawer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;

import java.util.*;

public class GameListWidget extends ElementListWidget<GameListWidget.GameEntry> {
	public GameListWidget(MinecraftClient client, int width, int height, int y, int itemHeight, int headerHeight, int x) {
		super(client, width, height, y, itemHeight, headerHeight);
		this.setX(x);
	}

	public void addGame(Game game) {
		this.addEntry(new GameEntry(game));
	}

	public void clearGames() {
		this.clearEntries();
	}

	public class GameEntry extends ElementListWidget.Entry<GameEntry> {
		public final Game game;

		public GameEntry(Game game) {
			this.game = game;
		}

		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickProgress) {
			StringDrawer sd = new StringDrawer(x + 2, y + 2, 0xFFFFFFFF, context, true);

			Map<String, Integer> map = new HashMap<>();

			for (String item : game.items) {
				map.put(item, map.getOrDefault(item, 0) + 1);
			}

			List<String> sortedKeys = map.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()).thenComparing(Map.Entry.comparingByKey()) // optional tie-breaker by key
			).map(Map.Entry::getKey).toList();

			if (sortedKeys.size() >= 3)
				sd.drawString("Top item: " + sortedKeys.getFirst() + ": " + map.get(sortedKeys.getFirst()));
			sd.drawString("Place: " + game.place);
			sd.drawString("Kills: " + game.kills);
			sd.drawTimeString("Time: ", (double) game.lengthSeconds);

			if (GameListWidget.this.getSelectedOrNull() == this) {
				context.drawBorder(x-1, y-1, entryWidth+2, entryHeight+2, 0xFF000000);
				context.drawBorder(x, y, entryWidth, entryHeight, 0xFFFFFFFF);
			}
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (this.isMouseOver(mouseX, mouseY)) {
				GameListWidget.this.setSelected(this);
				return true;
			}
			return false;
		}

		@Override
		public List<? extends Element> children() {
			return Collections.emptyList();
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return Collections.emptyList();
		}
	}
}
