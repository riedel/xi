package org.ananas.xi;

public class MatchFactory
{
   public static final String JDK_PACKAGE = "java.util.regex",
                              ORO_PACKAGE = "org.apache.oro.text.regex";
   private static boolean ignoreJDK = false;
   
   static
   {
      String ignore = System.getProperty("org.ananas.xi.ignoreJDK");
      if(Boolean.valueOf(ignore).booleanValue())
         ignoreJDK = true;
   }

   public static Match createMatch(String packageID,
                                   String namespaceURI,
                                   String localName,
                                   String prefix,
                                   String pattern)
      throws InvalidPatternException
   {
      if(packageID.equals(JDK_PACKAGE))
         return new JDKMatch(namespaceURI,localName,prefix,pattern);
      else if(packageID.equals(ORO_PACKAGE))
         return new OROMatch(namespaceURI,localName,prefix,pattern);
      else
         throw new IllegalArgumentException("Unknown package in org.ananas.xi.MatchFactory");
   }

   public static Match createMatch(String namespaceURI,
                                   String localName,
                                   String prefix,
                                   String pattern)
      throws InvalidPatternException
   {
      return createMatch(isJDKLibraryAvailable() ? JDK_PACKAGE : ORO_PACKAGE,
                         namespaceURI,
                         localName,
                         prefix,
                         pattern);
   }
   
   public static Match createMatch(String namespaceURI,
                                   String localName,
                                   String prefix)
      throws InvalidPatternException
   {
      return createMatch(namespaceURI,
                         localName,
                         prefix,
                         null);
   }

   private static boolean isJDKLibraryAvailable()
   {
      try
      {
         Class.forName("java.util.regex.Pattern");
         return !ignoreJDK;
      }
      catch(Exception x)
      {
         return false;
      }
      catch(LinkageError x)
      {
         return false;
      }
   }
}