package kr1v.dataCollector;

public enum GameMode {
	CCG_PILLARS_OF_FORTUNE("Pillars of Fortune"),
	CCG_LUCKY_ISLANDS("Lucky islands"),
	CCG_LOBBY("Lobby"),
	CCG_SKY_WARS("SkyWars"), CCG_EGG_WARS("EggWars");
	public final String name;

	GameMode(String s) {
		this.name = s;
	}

	public boolean isCCGGame() {
		return this == CCG_PILLARS_OF_FORTUNE || this == CCG_LUCKY_ISLANDS || this == CCG_SKY_WARS || this == CCG_EGG_WARS;
	}
}
