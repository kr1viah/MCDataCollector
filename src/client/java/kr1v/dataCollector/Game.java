package kr1v.dataCollector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Game {
	public List<String> items = new ArrayList<>();
	public Integer place = null;
	public int lengthSeconds = -1;
	public int kills = 0;

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Game game)) return false;
		return lengthSeconds == game.lengthSeconds && kills == game.kills && Objects.equals(items, game.items) && Objects.equals(place, game.place);
	}

	@Override
	public int hashCode() {
		return Objects.hash(items, place, lengthSeconds, kills);
	}

	@Override
	public String toString() {
		return "Game{" +
			"items=" + items +
			", place=" + place +
			", lengthSeconds=" + lengthSeconds +
			", kills=" + kills +
			'}';
	}
}
