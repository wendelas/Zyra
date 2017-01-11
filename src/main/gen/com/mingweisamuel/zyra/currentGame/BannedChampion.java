package com.mingweisamuel.zyra.currentGame;

/**
 * BannedChampion
 *
 * This class is automagically generated from the <a href="https://developer.riotgames.com/api/methods">Riot API reference</a>.
 *
 * @version current-game-v1.0 */
public class BannedChampion {
  /**
   * The ID of the banned champion */
  public int championId;

  /**
   * The turn during which the champion was banned */
  public int pickTurn;

  /**
   * The ID of the team that banned the champion */
  public long teamId;
}
