package com.mingweisamuel.zyra.lolStaticData;

import com.google.common.base.Objects;
import java.io.Serializable;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Map;

/**
 * RuneList - This object contains rune list data.
 *
 * This class is automagically generated from the <a href="https://developer.riotgames.com/api/methods">Riot API reference</a>.
 *
 * @version lol-static-data-v1.2 */
public class RuneList implements Serializable {
  public final BasicData basic;

  public final Map<Integer, Rune> data;

  public final String type;

  public final String version;

  public RuneList(final BasicData basic, final Map<Integer, Rune> data, final String type,
      final String version) {
    this.basic = basic;
    this.data = data;
    this.type = type;
    this.version = version;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) return true;
    if (!(obj instanceof RuneList)) return false;
    final RuneList other = (RuneList) obj;
    return true
        && Objects.equal(basic, other.basic)
        && Objects.equal(data, other.data)
        && Objects.equal(type, other.type)
        && Objects.equal(version, other.version);}

  @Override
  public int hashCode() {
    return Objects.hashCode(0,
        basic,
        data,
        type,
        version);}
}
