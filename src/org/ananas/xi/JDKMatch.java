package org.ananas.xi;

import java.util.regex.*;

public class JDKMatch
   extends Match
{
   private String input = null;
   private Pattern pattern = null;
   private Matcher matcher = null;

   public JDKMatch(String namespaceURI,
                   String localName,
                   String prefix,
                   String pattern)
      throws InvalidPatternException
   {
      super(namespaceURI,localName,prefix);
      if(pattern != null)
         try
         {
            this.pattern = Pattern.compile(pattern);
         }
         catch(PatternSyntaxException x)
         {
            throw new InvalidPatternException(x);
         }
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

   public synchronized String getGroupValueAt(int index)
      throws IllegalStateException, IllegalArgumentException
   {
      if(matcher == null)
         throw new IllegalStateException("Call matches() first");
      return getGroupNameAt(index).isText() ?
             matcher.group(0) : matcher.group(index);
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


   public void setPattern(String pattern)
      throws InvalidPatternException, IllegalStateException
   {
      if(this.pattern == null)
      {
         try
         {
            this.pattern = Pattern.compile(pattern);
         }
         catch(PatternSyntaxException x)
         {
            throw new InvalidPatternException(x);
         }
      }
      else
         throw new IllegalStateException("Cannot change pattern in JDKMatch");
   }
}