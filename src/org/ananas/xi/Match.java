package org.ananas.xi;

import java.util.*;
import java.util.regex.*;

public class Match
   extends QName
{
   private Pattern pattern;
   private Matcher matcher = null;
   private String input = null;
   private List groups = new ArrayList();

   public Match(String namespaceURI,
                String localName,
                String prefix,
                String pattern)
   {
      super(namespaceURI,localName,prefix);
      this.pattern = Pattern.compile(pattern);
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

   public synchronized String getGroupValueAt(int index)
      throws IllegalStateException, IllegalArgumentException
   {
      if(matcher == null)
         throw new IllegalStateException("Call matches() first");
      return getGroupNameAt(index).isText() ?
             matcher.group(0) : matcher.group(index);
   }

   public synchronized int getGroupCount()
   {
      return groups.size();
   }

   public boolean matches(String st)
   {
      input = st;
      if(matcher == null)
         matcher = pattern.matcher(st);
      else
         matcher.reset(st);
      return matcher.lookingAt();
   }

   public String rest()
   {
      if(matcher == null)
         throw new IllegalStateException("Call matches() first");
      int end = matcher.end(),
          length = input.length();
      if(end < length)
         return input.substring(end,length);
      else
         return null;
   }
}
