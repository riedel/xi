package org.ananas.xi;

import java.util.*;
import org.xml.sax.*;

/**
 * @xmlns xi http://ananas.org/2002/xi/rules
 */

public class RulesHandler
   implements org.ananas.hc.HCHandler
{
   private String namespaceURI = null;
   private String prefix = null;
   private List rulesets = null;
   private StringBuffer buffer = null;
   private boolean defaultTrimSpace = false,
                   defaultIgnoreEmpty = false;

   private Ruleset getLastRuleset()
   {
      return (Ruleset)rulesets.get(rulesets.size() - 1);
   }

   /**
    * @xpath xi:rules
    */
   public void init(Attributes attributes)
      throws SAXException
   {
      rulesets = new ArrayList();
      namespaceURI = attributes.getValue("targetNamespace");
      prefix = attributes.getValue("defaultPrefix");
      if(namespaceURI != null)
      {
         namespaceURI = namespaceURI.trim();
         if(namespaceURI.equals(""))
            namespaceURI = null;
      }
      if(prefix != null)
      {
         prefix = prefix.trim();
         if(prefix.equals(""))
            prefix = null;
      }
      String space = attributes.getValue("defaultSpace");
      if(space != null)
      {
         space = space.trim();
         if(space.equalsIgnoreCase("trim"))
            defaultTrimSpace = true;
         else if(space.equalsIgnoreCase("preserve"))
            ;   // leaves it to default...
         else
            throw new SAXException("acceptable values for defaultSpace are trim and preserve");
      }
      String empty = attributes.getValue("defaultEmpty");
      if(empty != null)
      {
         empty = empty.trim();
         if(empty.equalsIgnoreCase("ignore"))
            defaultIgnoreEmpty = true;
         else if(empty.equalsIgnoreCase("preserve"))
            ;   // leaves it to default...
         else
            throw new SAXException("acceptable values for defaultEmpty are ignore and preserve");
      }
   }

   /**
    * @xpath xi:rules/xi:ruleset
    */
   public void doRuleset(Attributes attributes)
      throws SAXException
   {
      String name = attributes.getValue("name");
      if(name != null)
         rulesets.add(new Ruleset(namespaceURI,
                                  name,
                                  prefix));
      else
         throw new SAXException("name attribute required for xi:ruleset");
   }

   /**
    * @xpath xi:rules/xi:ruleset/xi:match
    */
   public void doMatch(Attributes attributes)
      throws SAXException
   {
      String name = attributes.getValue("name"),
             pattern = attributes.getValue("pattern");
      if(name != null)
      {
         Ruleset ruleset = getLastRuleset();
         ruleset.addMatch(MatchFactory.createMatch(namespaceURI,
                                                   name,
                                                   prefix,
                                                   pattern));
         if(pattern == null)
            buffer = new StringBuffer();
         else
            buffer = null;
      }
      else
         throw new SAXException("name attribute required for xi:match");
   }

   /**
    * @xpath xi:rules/xi:ruleset/xi:match
    */
   public void endMatch()
      throws SAXException
   {
      if(buffer != null)
      {
         Ruleset ruleset = getLastRuleset();
         Match match = ruleset.getLastMatch();
         match.setPattern(buffer.toString());
      }
   }

   /**
    * @xpath xi:rules/xi:ruleset/xi:error
    */
   public void doError(Attributes attributes)
      throws SAXException
   {
      String message = attributes.getValue("message");
      if(message != null)
      {
         Ruleset ruleset = getLastRuleset();
         if(ruleset.getError() == null)
            ruleset.setError(message);
         else
            throw new SAXException("no more than one error per xi:ruleset");
      }
      else
         throw new SAXException("message attribute required for xi:error");
   }

   /**
    * @xpath xi:rules/xi:ruleset/xi:match/xi:group
    */
   public void doGroup(Attributes attributes)
      throws SAXException
   {
      String name = attributes.getValue("name"),
             space = attributes.getValue("space"),
             empty = attributes.getValue("empty");
      boolean trimSpace = defaultTrimSpace;
      if(space != null)
      {
         space = space.trim();
         if(space.equalsIgnoreCase("trim"))
            trimSpace = true;
         else if(space.equalsIgnoreCase("preserve"))
            trimSpace = false;
         else
            throw new SAXException("acceptable values for space are trim and preserve");
      }
      boolean ignoreEmpty = defaultIgnoreEmpty;
      if(empty != null)
      {
         empty = empty.trim();
         if(empty.equalsIgnoreCase("ignore"))
            ignoreEmpty = true;
         else if(empty.equalsIgnoreCase("preserve"))
            ignoreEmpty = false;
         else
            throw new SAXException("acceptable values for empty are ignore and preserve");
      }
      if(name != null)
      {
         Ruleset ruleset = getLastRuleset();
         Match match = ruleset.getLastMatch();
         match.addGroup(new Group(namespaceURI,
                                  name,
                                  prefix,
                                  trimSpace,
                                  ignoreEmpty));
      }
      else
         throw new SAXException("name attribute required for xi:group");
      if(buffer != null)
      {
         String pattern = attributes.getValue("pattern");
         if(pattern != null)
         {
            buffer.append('(');
            buffer.append(pattern);
            buffer.append(')');
         }
         else
            throw new SAXException("pattern attribute required for xi:group if no pattern on the xi:match");
      }
   }

   /**
    * @xpath xi:rules/xi:ruleset/xi:match/xi:filler
    */
   public void doFiller(Attributes attributes)
      throws SAXException
   {
      if(buffer != null)
      {
         String pattern = attributes.getValue("pattern");
         if(pattern != null)
            buffer.append(pattern);
         else
            throw new SAXException("pattern attribute required for xi:filler");
      }
      else
         throw new SAXException("xi:filler can only appear if there's no pattern on the xi:match");
   }

   public Ruleset[] getRulesets()
   {
      if(rulesets != null && rulesets.size() > 0)
      {
         Ruleset[] array = new Ruleset[rulesets.size()];
         return (Ruleset[])rulesets.toArray(array);
      }
      else
         return null;
   }

   public String getNamespaceURI()
   {
      return namespaceURI;
   }

   public String getPrefix()
   {
      return prefix;
   }
}
