package kr1v.dataCollector.util;

import kr1v.dataCollector.games.PoFGame;
import kr1v.dataCollector.PoFIndividualGamesScreen;
import kr1v.dataCollector.PoFIndividualGamesScreen.TypeOfSort;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class Util {
	public static void sortGames(List<PoFGame> poFGames, PoFIndividualGamesScreen.Sort sort, TypeOfSort direction) {
		if (poFGames == null || poFGames.size() <= 1 || sort == null || direction == null) return;

		Map<PoFGame, Integer> originalIndex = new IdentityHashMap<>();
		for (int i = 0; i < poFGames.size(); i++) {
			originalIndex.put(poFGames.get(i), i);
		}

		switch (sort) {
			case RECENT:
				if (direction != TypeOfSort.DESCENDING) {
					Collections.reverse(poFGames);
				}
				return;

			case RANDOM:
				Collections.shuffle(poFGames, new Random());
				return;

			case PLACE: {
				java.util.Comparator<PoFGame> comp = java.util.Comparator
					.comparingInt((PoFGame g) -> g.place == null ? Integer.MAX_VALUE : g.place)
					.thenComparingInt((PoFGame g) -> originalIndex.getOrDefault(g, 0));
				if (direction == TypeOfSort.DESCENDING) comp = comp.reversed();
				poFGames.sort(comp);
				return;
			}

			case TIME: {
				java.util.Comparator<PoFGame> comp = java.util.Comparator
					.comparingInt((PoFGame g) -> g.lengthSeconds)
					.thenComparingInt((PoFGame g) -> originalIndex.getOrDefault(g, 0));
				if (direction == TypeOfSort.DESCENDING) comp = comp.reversed();
				poFGames.sort(comp);
				return;
			}

			case KILLS: {
				java.util.Comparator<PoFGame> comp = java.util.Comparator
					.comparingInt((PoFGame g) -> g.kills)
					.thenComparingInt((PoFGame g) -> originalIndex.getOrDefault(g, 0));
				if (direction == TypeOfSort.DESCENDING) comp = comp.reversed();
				poFGames.sort(comp);
				return;
			}

			case TOP_ITEM: {
				Comparator<PoFGame> comp = getGameComparator(direction, originalIndex);
				poFGames.sort(comp);
			}
		}
	}

	private static @NotNull Comparator<PoFGame> getGameComparator(TypeOfSort direction, Map<PoFGame, Integer> originalIndex) {
		Function<PoFGame, Integer> topScore = (PoFGame g) -> {
			if (g == null || g.items == null || g.items.isEmpty()) return 0;
			Map<String, Integer> counts = new HashMap<>();
			int max = 0;
			for (String it : g.items) {
				if (it == null) continue;
				int c = counts.merge(it, 1, Integer::sum);
				if (c > max) max = c;
			}
			return max;
		};

		Comparator<PoFGame> comp = Comparator
			.comparingInt(topScore::apply)
			.thenComparingInt((PoFGame g) -> originalIndex.getOrDefault(g, 0));

		if (direction == TypeOfSort.DESCENDING) comp = comp.reversed();
		return comp;
	}

}
