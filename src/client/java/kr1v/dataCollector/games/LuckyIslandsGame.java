package kr1v.dataCollector.games;

import kr1v.dataCollector.GameMode;

public class LuckyIslandsGame extends CCGGame {
	public int luckyBlocksOpened = 0;

	public LuckyIslandsGame() {
		this.gameMode = GameMode.CCG_LUCKY_ISLANDS;
	}
}
