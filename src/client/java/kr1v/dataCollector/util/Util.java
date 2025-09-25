package kr1v.dataCollector.util;

import kr1v.dataCollector.Game;
import kr1v.dataCollector.PoFIndividualGamesScreen;
import kr1v.dataCollector.PoFIndividualGamesScreen.TypeOfSort;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class Util {
	public static void sortGames(List<Game> games, PoFIndividualGamesScreen.Sort sort, TypeOfSort direction) {
		if (games == null || games.size() <= 1 || sort == null || direction == null) return;

		// stable tie-breaker: remember original index
		Map<Game, Integer> originalIndex = new IdentityHashMap<>();
		for (int i = 0; i < games.size(); i++) {
			originalIndex.put(games.get(i), i);
		}

		switch (sort) {
			case RECENT:
				if (direction != TypeOfSort.DESCENDING) {
					Collections.reverse(games);
				}
				return;

			case RANDOM:
				Collections.shuffle(games, new Random());
				return;

			case PLACE: {
				java.util.Comparator<Game> comp = java.util.Comparator
					.comparingInt((Game g) -> g.place == null ? Integer.MAX_VALUE : g.place)
					.thenComparingInt((Game g) -> originalIndex.getOrDefault(g, 0));
				if (direction == TypeOfSort.DESCENDING) comp = comp.reversed();
				games.sort(comp);
				return;
			}

			case TIME: {
				java.util.Comparator<Game> comp = java.util.Comparator
					.comparingInt((Game g) -> g.lengthSeconds)
					.thenComparingInt((Game g) -> originalIndex.getOrDefault(g, 0));
				if (direction == TypeOfSort.DESCENDING) comp = comp.reversed();
				games.sort(comp);
				return;
			}

			case KILLS: {
				java.util.Comparator<Game> comp = java.util.Comparator
					.comparingInt((Game g) -> g.kills)
					.thenComparingInt((Game g) -> originalIndex.getOrDefault(g, 0));
				if (direction == TypeOfSort.DESCENDING) comp = comp.reversed();
				games.sort(comp);
				return;
			}

			case TOP_ITEM: {
				// For each game: compute the count of the game's most common item (e.g. 4 iron -> score 4).
				Comparator<Game> comp = getGameComparator(direction, originalIndex);
				games.sort(comp);
			}
		}
	}

	private static @NotNull Comparator<Game> getGameComparator(TypeOfSort direction, Map<Game, Integer> originalIndex) {
		Function<Game, Integer> topScore = (Game g) -> {
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

		Comparator<Game> comp = Comparator
			.comparingInt(topScore::apply)
			.thenComparingInt((Game g) -> originalIndex.getOrDefault(g, 0));

		if (direction == TypeOfSort.DESCENDING) comp = comp.reversed();
		return comp;
	}

}
