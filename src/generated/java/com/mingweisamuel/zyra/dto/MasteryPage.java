package com.mingweisamuel.zyra.dto;

import java.lang.String;
import java.util.List;

/**
 * MasteryPage - This object contains mastery page information.
 *
 * This class is automagically generated from the <a href="https://developer.riotgames.com/api/methods">Riot API reference</a> using {@link com.mingweisamuel.zyra.build.RiotDtoGenerator}. */
public class MasteryPage {
  /**
   * Indicates if the mastery page is the current mastery page. */
  public boolean current;

  /**
   * Mastery page ID. */
  public long id;

  /**
   * Collection of masteries associated with the mastery page. */
  public List<Mastery> masteries;

  /**
   * Mastery page name. */
  public String name;
}
