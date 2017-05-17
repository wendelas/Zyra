package com.mingweisamuel.zyra.matchlist;

import com.google.common.base.Objects;
import java.io.Serializable;
import java.lang.Object;
import java.lang.Override;
import java.util.List;

/**
 * MatchList.<br><br>
 *
 * This object contains match list information.<br><br>
 *
 * This class was automatically generated from the <a href="https://developer.riotgames.com/api-methods/#matchlist-v2.2/GET_getMatchList">Riot API reference</a> on Mon May 15 17:36:46 PDT 2017. */
public class MatchList implements Serializable {
  public final List<MatchReference> matches;

  public final int totalGames;

  public final int startIndex;

  public final int endIndex;

  public MatchList(final List<MatchReference> matches, final int totalGames, final int startIndex,
      final int endIndex) {
    this.matches = matches;
    this.totalGames = totalGames;
    this.startIndex = startIndex;
    this.endIndex = endIndex;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) return true;
    if (!(obj instanceof MatchList)) return false;
    final MatchList other = (MatchList) obj;
    return true
        && Objects.equal(matches, other.matches)
        && Objects.equal(totalGames, other.totalGames)
        && Objects.equal(startIndex, other.startIndex)
        && Objects.equal(endIndex, other.endIndex);}

  @Override
  public int hashCode() {
    return Objects.hashCode(0,
        matches,
        totalGames,
        startIndex,
        endIndex);}
}
