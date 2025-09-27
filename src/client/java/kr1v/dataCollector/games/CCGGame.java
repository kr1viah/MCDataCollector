package kr1v.dataCollector.games;

import java.time.Instant;

public class CCGGame {
	public Integer place = null;
	public int lengthSeconds = -1;
	public int kills = 0;
	public int pointsEarned = 0;
	public long timeStampGameStart = Instant.now().getEpochSecond();
}
