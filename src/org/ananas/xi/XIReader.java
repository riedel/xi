package org.ananas.xi;

import java.io.*;
import java.util.*;
import java.text.*;
import java.net.URL;    // spell it out to avoid conflict between
import org.xml.sax.*;   // java.net.* and org.xml.sax.*
import org.ananas.hc.*;
import javax.xml.parsers.*;
import org.xml.sax.helpers.*;
import java.net.URLConnection;   // spell it out against conflict

public class XIReader
   implements XMLReader, Locator
{
   protected ContentHandler contentHandler = null;
   protected DTDHandler dtdHandler = null;
   protected EntityResolver entityResolver = null;
   protected ErrorHandler errorHandler = null;
   protected int lineNumber = -1;
   protected boolean namespacePrefixes = false,
                     isParsing = false;
   protected Ruleset[] rulesets = null;
   protected Map rulesetsMap = null;
   protected String namespaceURI = null,
                    prefix = null,
                    systemId = null,
                    publicId = null;
   protected AttributesImpl attributes = new AttributesImpl();
   protected char[] chars = new char[1024];
   public static final String NAMESPACES_URI = "http://xml.org/sax/features/namespaces",
                              NAMESPACE_PREFIXES_URI = "http://xml.org/sax/features/namespace-prefixes",
                              RULESETS_URI = "http://ananas.org/xi/properties/rulesets",
                              RULESETS_VALID_URI = "http://ananas.org/xi/features/rulesets/valid";

   public ContentHandler getContentHandler()
   {
      return contentHandler;
   }

   public void setContentHandler(ContentHandler value)
      throws NullPointerException
   {
      if(value == null)
         throw new NullPointerException("ContentHandler");
      else
         contentHandler = value;
   }

   public DTDHandler getDTDHandler()
   {
      return dtdHandler;
   }

   public void setDTDHandler(DTDHandler value)
      throws NullPointerException
   {
      if(value == null)
         throw new NullPointerException("DTDHandler");
      else
         dtdHandler = value;
   }

   public ErrorHandler getErrorHandler()
   {
      return errorHandler;
   }

   public void setErrorHandler(ErrorHandler value)
      throws NullPointerException
   {
      if(value == null)
         throw new NullPointerException("ErrorHandler");
      else
         errorHandler = value;
   }

   public EntityResolver getEntityResolver()
   {
      return entityResolver;
   }

   public void setEntityResolver(EntityResolver value)
      throws NullPointerException
   {
      if(value == null)
         throw new NullPointerException("EntityResolver");
      else
         entityResolver = value;
   }

   public boolean isNamespaces()
   {
      // minimal conformance
      return true;
   }

   public void setNamespaces(boolean value)
      throws SAXNotSupportedException
   {
      // minimal conformance
      if(isParsing || !value)
         throw new SAXNotSupportedException(NAMESPACES_URI);
   }

   public boolean isNamespacePrefixes()
   {
      return namespacePrefixes;
   }

   public void setNamespacePrefixes(boolean value)
      throws SAXNotSupportedException
   {
      if(isParsing)
         throw new SAXNotSupportedException(NAMESPACE_PREFIXES_URI);
      else
         namespacePrefixes = value;
   }

   public Ruleset[] getRulesets()
   {
      return rulesets;
   }

   public void setRulesets(InputSource input)
      throws SAXNotSupportedException
   {
      if(isParsing)
         throw new SAXNotSupportedException(RULESETS_URI);
      else
         try
         {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            RulesHandler rulesHandler = new RulesHandler();
            reader.setContentHandler(new XPathHandler(rulesHandler));
            reader.parse(input);
            rulesets = rulesHandler.getRulesets();
            if(rulesets == null)
               return;
            namespaceURI = rulesHandler.getNamespaceURI();
            prefix = rulesHandler.getPrefix();
            rulesetsMap = new HashMap((int)(rulesets.length * 1.5));
            for(int i = 0;i < rulesets.length;i++)
               rulesetsMap.put((QName)rulesets[i],rulesets[i]);
         }
         catch(ParserConfigurationException e)
         {
            String msg = MessageFormat.format(
               "Cannot create parser (configuration exception: {0})",
               new String[] { e.getMessage() });
            throw new SAXNotSupportedException(msg);
         }
         catch(SAXException e)
         {
            String msg = null;
            if(e.getException() != null)
               msg = MessageFormat.format(
                        "Cannot create parser (Exception {1}: {0})",
                        new String[] { e.getException().getMessage(),
                                       e.getException().getClass().getName() });
            else
               msg = MessageFormat.format(
                        "Cannot create parser (Exception in SAX exception: {0})",
                        new String[] { e.getMessage() });
            throw new SAXNotSupportedException(msg);
         }
         catch(IOException e)
         {
            String msg = MessageFormat.format(
               "Cannot create parser (I/O exception: {0})",
               new String[] { e.getMessage() });
            throw new SAXNotSupportedException(msg);
         }
   }

   public boolean isRulesetsValid()
   {
      return rulesetsMap != null;
   }

   public boolean getFeature(String name)
      throws SAXNotRecognizedException
   {
      if(name.equals(NAMESPACES_URI))
         return isNamespaces();
      else if(name.equals(NAMESPACE_PREFIXES_URI))
         return isNamespacePrefixes();
      else if(name.equals(RULESETS_VALID_URI))
         return isRulesetsValid();
      else
         throw new SAXNotRecognizedException(name);
   }

   public void setFeature(String name,boolean value)
      throws SAXNotRecognizedException, SAXNotSupportedException
   {
      if(name.equals(NAMESPACES_URI))
         setNamespaces(value);
      else if(name.equals(NAMESPACE_PREFIXES_URI))
         setNamespacePrefixes(value);
      else
         throw new SAXNotRecognizedException(name);
   }

   public Object getProperty(String name)
      throws SAXNotRecognizedException, SAXNotSupportedException
   {
      if(name.equals(RULESETS_URI))
         return getRulesets();
      else
         throw new SAXNotRecognizedException(name);
   }

   public void setProperty(String name,Object value)
      throws SAXNotRecognizedException, SAXNotSupportedException
   {
      if(name.equals(RULESETS_URI))
         if(value instanceof InputSource)
            setRulesets((InputSource)value);
         else
            throw new SAXNotSupportedException(RULESETS_URI);
      else
         throw new SAXNotRecognizedException(name);
   }

   public void parse(String systemId)
      throws IOException, SAXException
   {
      parse(new InputSource(systemId));
   }

   public void parse(InputSource source)
      throws IOException, SAXException
   {
      if(isParsing)
         throw new SAXException("Parsing already in progress");
      isParsing = true;
      publicId = source.getPublicId();
      systemId = source.getSystemId();
      lineNumber = 1;
      try
      {
         BufferedReader reader = new BufferedReader(getReader(source));
         try
         {
            doParse(reader);
         }
         finally
         {
            reader.close();
         }
      }
      finally
      {
         isParsing = false;
      }
   }

   private void doParse(BufferedReader reader)
      throws IOException, SAXException
   {
      Ruleset ruleset = rulesets[0];
      if(contentHandler != null)
      {
         contentHandler.setDocumentLocator(this);
         contentHandler.startDocument();
         if(namespaceURI != null)
            if(prefix != null)
            {
               contentHandler.startPrefixMapping(prefix,namespaceURI);
               if(namespacePrefixes)
                  attributes.addAttribute("",
                                          prefix,
                                          "xmlns:" + prefix,
                                          "CDATA",
                                          namespaceURI);
            }
            else
            {
               contentHandler.startPrefixMapping("",namespaceURI);
               if(namespacePrefixes)
                  attributes.addAttribute("",
                                          "xmlns",
                                          "xmlns",
                                          "CDATA",
                                          namespaceURI);
            }
         contentHandler.startElement(ruleset.getNamespaceURI(),
                                     ruleset.getLocalName(),
                                     ruleset.getQualifiedName(),
                                     attributes);
      }
      String line = reader.readLine();
      while(line != null)
      {
         match(ruleset,line,true);
         line = reader.readLine();
         lineNumber++;
      }
      if(contentHandler != null)
      {
         contentHandler.endElement(ruleset.getNamespaceURI(),
                                   ruleset.getLocalName(),
                                   ruleset.getQualifiedName());
         if(namespaceURI != null)
            if(prefix != null)
               contentHandler.endPrefixMapping(prefix);
            else
               contentHandler.endPrefixMapping("");
         contentHandler.endDocument();
      }
   }

   public void match(Ruleset ruleset,String st,boolean firstMatch)
      throws SAXException
   {
      attributes.clear();
      int i = 0;
      while(i < ruleset.getMatchCount())
      {
         if(ruleset.getMatchAt(i).matches(st))
         {
            Match match = ruleset.getMatchAt(i);
            if(firstMatch && contentHandler != null)
               contentHandler.startElement(match.getNamespaceURI(),
                                           match.getLocalName(),
                                           match.getQualifiedName(),
                                           attributes);
            for(int j = 1;j <= match.getGroupCount();j++)
            {
               QName qname = match.getGroupNameAt(j);
               Ruleset nextRuleset = (Ruleset)rulesetsMap.get(qname);
               if(nextRuleset != null)
                  match(nextRuleset,match.getGroupValueAt(j),true);
               else
               {
                  Group group = match.getGroupNameAt(j);
                  if(contentHandler != null)
                  {
                     String value = match.getGroupValueAt(j);
                     if(group.isTrimSpace() && value != null)
                        value = value.trim();
                     if(!group.isIgnoreEmpty() || (value != null && value.length() != 0))
                     {
                        contentHandler.startElement(group.getNamespaceURI(),
                                                    group.getLocalName(),
                                                    group.getQualifiedName(),
                                                    attributes);
                        int begin = 0,
                            end = 0;
                        while(begin < value.length())
                        {
                           if(value.length() - begin < chars.length)
                              end = value.length();
                           else
                              end = begin + chars.length;
                           value.getChars(begin,end,chars,0);
                           contentHandler.characters(chars,0,end - begin);
                           begin = end;
                        }
                        contentHandler.endElement(group.getNamespaceURI(),
                                                  group.getLocalName(),
                                                  group.getQualifiedName());
                     }
                  }
               }
            }
            String rest = match.rest();
            if(rest != null)
               match(ruleset,rest,false);
            if(firstMatch && contentHandler != null)
               contentHandler.endElement(match.getNamespaceURI(),
                                         match.getLocalName(),
                                         match.getQualifiedName());
            break;
         }
         else
            i++;
      }
      if(i >= ruleset.getMatchCount()
         && ruleset.getError() != null
         && errorHandler != null)
         errorHandler.error(new SAXParseException(ruleset.getError(),
                                                  this));
   }

   protected Reader getReader(InputSource source)
      throws SAXException, IOException
   {
      if(source.getCharacterStream() != null)
         return source.getCharacterStream();
      else if(source.getByteStream() != null)
      {
         if(source.getEncoding() == null)
            return new InputStreamReader(source.getByteStream());
         else
            return new InputStreamReader(source.getByteStream(),
                                         source.getEncoding());
      }
      else if(source.getSystemId() != null)
      {
         URLConnection connection = new URL(source.getSystemId()).openConnection();
         connection.connect();
         InputStream is = connection.getInputStream();
         if(connection.getContentEncoding() == null)
            return new InputStreamReader(is);
         else
            return new InputStreamReader(is,connection.getContentEncoding());
      }
      throw new SAXException("No suitable input in InputSource");
   }

   public int getColumnNumber()
   {
      return -1;
   }

   public int getLineNumber()
   {
      return lineNumber;
   }

   public String getPublicId()
   {
      return publicId;
   }

   public String getSystemId()
   {
      return systemId;
   }
}
