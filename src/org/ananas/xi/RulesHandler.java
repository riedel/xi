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

   private Ruleset getLastRuleset()
   {
      return (Ruleset)rulesets.get(rulesets.size() - 1);
   }

   /**
    * @xpath xi:rules
    */
   public void init(Attributes attributes)
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
      if(name != null && pattern != null)
      {
         Ruleset ruleset = getLastRuleset();
         ruleset.addMatch(new Match(namespaceURI,
                                    name,
                                    prefix,
                                    pattern));
      }
      else
         throw new SAXException("name and pattern attributes required for xi:match");
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
      String name = attributes.getValue("name");
      if(name != null)
      {
         Ruleset ruleset = getLastRuleset();
         Match match = ruleset.getLastMatch();
         match.addGroup(new Group(namespaceURI,
                                  name,
                                  prefix));
      }
      else
         throw new SAXException("name attribute required for xi:group");
   }

   public Ruleset[] getRulesets()
   {
      Ruleset[] array = new Ruleset[rulesets.size()];
      return (Ruleset[])rulesets.toArray(array);
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
