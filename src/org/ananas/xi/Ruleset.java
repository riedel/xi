package org.ananas.xi;

import java.util.*;

public class Ruleset
   extends QName
{
   private List matches = new ArrayList();
   private String error = null;

   public Ruleset(String namespaceURI,
                  String localName,
                  String prefix)
   {
      super(namespaceURI,localName,prefix);
   }

   public void setError(String error)
   {
      this.error = error;
   }

   public String getError()
   {
      return error;
   }

   public synchronized void addMatch(Match match)
   {
      matches.add(match);
   }

   public synchronized Match getMatchAt(int index)
   {
      return (Match)matches.get(index);
   }

   public synchronized int getMatchCount()
   {
      return matches.size();
   }

   public synchronized Match getLastMatch()
   {
      return (Match)matches.get(matches.size() - 1);
   }
}
