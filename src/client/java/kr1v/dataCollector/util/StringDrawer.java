package kr1v.dataCollector.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class StringDrawer {
	int x;
	int y;
	int colour;
	DrawContext context;
	boolean shadow;
	TextRenderer textRenderer;
	MinecraftClient client;

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getColour() {
		return colour;
	}

	public void setColour(int colour) {
		this.colour = colour;
	}

	public boolean isShadow() {
		return shadow;
	}

	public void setShadow(boolean shadow) {
		this.shadow = shadow;
	}

	public StringDrawer(int x, int y, int colour, DrawContext context, boolean shadow, TextRenderer textRenderer) {
		this.x = x;
		this.y = y;
		this.colour = colour;
		this.context = context;
		this.shadow = shadow;
		this.textRenderer = textRenderer;
		this.client = MinecraftClient.getInstance();
	}

	public StringDrawer(int x, int y, int colour, DrawContext context, boolean shadow) {
		this(x, y, colour, context, shadow, MinecraftClient.getInstance().textRenderer);
	}

	public void drawString(String text) {
		var split = text.split("\\R");
		for (String s : split) {
			context.drawText(textRenderer, s, x, y, colour, shadow);
			y += client.textRenderer.fontHeight + 2;
		}
	}

	public void drawTimeString(String prefix, Double seconds, String suffix) {
		String text = prefix + formatDuration(seconds) + suffix;
		var split = text.split("\\R");
		for (String s : split) {
			context.drawText(textRenderer, s, x, y, colour, shadow);
			y += client.textRenderer.fontHeight + 2;
		}
	}

	public void drawTimeString(String prefix, Double seconds) {
		drawTimeString(prefix, seconds, "");
	}

	public static String formatDuration(Double seconds) {
		int totalSeconds = (int)Math.round(seconds);
		int hours = totalSeconds / 3600;
		int minutes = (totalSeconds % 3600) / 60;
		int secondsRounded = totalSeconds % 60;
		if (hours > 0) {
			return String.format("%dh %dm %ds", hours, minutes, secondsRounded);
		} else if (minutes > 0) {
			return String.format("%dm %ds", minutes, secondsRounded);
		} else {
			return String.format("%ds", secondsRounded);
		}
	}
}
