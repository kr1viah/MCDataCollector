package kr1v.dataCollector;

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
	public StringListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
		super(client, width, height, top, bottom, itemHeight);
	}

	/** Add one string to the list */
	public void addString(String s) {
		this.addEntry(new StringEntry(s));
	}

	public void setStrings(Collection<String> strings) {
		this.clearEntries();
		for (String s : strings) this.addString(s);
	}

	public String getSelectedString() {
		StringEntry e = this.getSelectedOrNull();
		return e == null ? null : e.value;
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
			context.drawText(MinecraftClient.getInstance().textRenderer, this.text, x + 4, y + (entryHeight - 8) / 4, 0xFFFFFFFF, true);
		}
	}
}
