package org.ananas.xi;

import java.util.*;

public abstract class Match
   extends QName
{
   private List groups = new ArrayList();

   public Match(String namespaceURI,
                String localName,
                String prefix)
   {
      super(namespaceURI,localName,prefix);
   }

   public synchronized void addGroup(Group group)
   {
      groups.add(group);
   }

   public synchronized Group getGroupNameAt(int index)
   {
      if(index < 1 || index > groups.size())
         throw new IndexOutOfBoundsException("index out of bounds");
      return (Group)groups.get(index - 1);
   }

   public synchronized int getGroupCount()
   {
      return groups.size();
   }

   abstract public boolean matches(String st);
   abstract public String getGroupValueAt(int index)
      throws IllegalStateException, IllegalArgumentException;
   abstract public String rest();
   abstract public void setPattern(String pattern)
      throws InvalidPatternException, IllegalStateException;
}