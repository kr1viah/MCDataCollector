package kr1v.dataCollector.games;

import kr1v.dataCollector.GameMode;

import java.util.ArrayList;
import java.util.List;

public class PoFGame extends CCGGame {
	public List<String> items = new ArrayList<>();

	public PoFGame() {
		this.gameMode = GameMode.CCG_PILLARS_OF_FORTUNE;
	}
}
