package kr1v.dataCollector.games;

import kr1v.dataCollector.GameMode;

public class EggWarsGame extends CCGGame {
	public int eggsBroken = 0;
	public int finalKills = 0;
	public int deaths = 0;
	public float KDRatio = 0;

	public EggWarsGame() {
		this.place = -1; // eggwars doesnt have places
		this.gameMode = GameMode.CCG_EGG_WARS;
	}
}
