package org.ananas.xi;

import org.apache.oro.text.regex.*;

public class OROMatch
   extends Match
{
   // the class is not thread-safe because we share those 2 instances
   // of the compiler and the matcher, not a problem in practice with XI
   private static Perl5Compiler compiler = new Perl5Compiler();
   private static Perl5Matcher matcher = new Perl5Matcher();
   private Pattern pattern = null;
   private MatchResult result = null;
   private String input = null;

   public OROMatch(String namespaceURI,
                   String localName,
                   String prefix,
                   String pattern)
      throws InvalidPatternException
   {
      super(namespaceURI,localName,prefix);
      if(pattern != null)
         try
         {
            this.pattern = compiler.compile(pattern);
         }
         catch(MalformedPatternException x)
         {
            throw new InvalidPatternException(x);
         }
   }

   public boolean matches(String st)
   {
      input = st;
      if(matcher.contains(st,pattern))
      {
         result = matcher.getMatch();
         return true;
      }
      else
         return false;
   }

   public synchronized String getGroupValueAt(int index)
      throws IllegalStateException, IllegalArgumentException
   {
      if(result == null)
         throw new IllegalStateException("matches() must have returned true");
      return getGroupNameAt(index).isText() ?
         result.group(0) : result.group(index);
   }

   public String rest()
   {
      if(result == null)
         throw new IllegalStateException("matches() must have returned true");
      int end = result.length(),  // BUGBUG -1?
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
            this.pattern = compiler.compile(pattern);
         }
         catch(MalformedPatternException x)
         {
            throw new InvalidPatternException(x);
         }
      }
      else
         throw new IllegalStateException("Cannot change pattern in OROMatch");
   }
}