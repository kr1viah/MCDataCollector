package kr1v.dataCollector;

import kr1v.dataCollector.util.StringDrawer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class StringListWidget extends ElementListWidget<StringListWidget.StringEntry> {
	public StringListWidget(MinecraftClient client, int width, int height, int y, int itemHeight, int headerHeight, int x) {
		super(client, width, height, y, itemHeight, headerHeight);
		this.setX(x);
	}

	public void addString(String s) {
		this.addEntry(new StringEntry(s));
	}

	public void clearStrings() {
		this.clearEntries();
	}

	public class StringEntry extends ElementListWidget.Entry<StringEntry> {
		private final String value;
		private final Text text;

		public StringEntry(String value) {
			this.value = value;
			this.text = Text.literal(value);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			StringListWidget.this.setSelected(this);
			return true;
		}

		@Override
		public List<? extends Element> children() {
			return Collections.emptyList();
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return Collections.emptyList();
		}

		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickProgress) {
			StringDrawer sd = new StringDrawer(x + 4, y, 0xFFFFFFFF, context, true);
			sd.drawString(this.text.getString());
		}
	}
}
