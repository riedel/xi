package org.ananas.xi;

import java.io.*;
import java.nio.*;
import java.util.*;
import org.xml.sax.*;
import java.util.regex.*;
import java.nio.charset.*;
import java.nio.channels.*;
import org.xml.sax.helpers.*;    
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;
import org.apache.xalan.serialize.*;
import org.apache.xalan.templates.*;

public class Test
{
   public static void main(String[] params)
      throws TransformerException, TransformerConfigurationException,
             SAXException, IOException
   {
      five(params);
   }

   public static void five(String[] params)
      throws TransformerException, TransformerConfigurationException,
             SAXException, IOException
   {
      InputSource inputSource = new InputSource(new FileInputStream(params[0]));
      inputSource.setSystemId(params[0]);
      XMLReader xmlReader = XMLReaderFactory.createXMLReader("org.ananas.xi.XIReader");
      xmlReader.setProperty(XIReader.RULESETS_URI,new InputSource("rules.xml"));
      xmlReader.setErrorHandler(new DefaultHandler()
      {
         public void error(SAXParseException e)
         {
            System.err.print(e.getMessage());
            System.err.print(" (");
            System.err.print(e.getColumnNumber());
            System.err.print(", ");
            System.err.print(e.getLineNumber());
            System.err.print(") ");
            System.err.print(e.getSystemId());
            System.err.print(' ');
            System.err.println(e.getPublicId());
         }
      });
      TransformerFactory factory = TransformerFactory.newInstance();
      Transformer transformer = factory.newTransformer();
      transformer.transform(new SAXSource(xmlReader,inputSource),new StreamResult("result.xml"));
   }

   public static void four(String[] params)
      throws IOException, SAXException
   {
      XMLReader xmlReader = XMLReaderFactory.createXMLReader("org.ananas.xi.XIReader");
      xmlReader.setProperty(XIReader.RULESETS_URI,new InputSource("rules.xml"));
      Properties properties = OutputProperties.getDefaultMethodProperties("xml");
      properties.setProperty("indent","yes");
      Serializer serializer = SerializerFactory.getSerializer(properties);
      serializer.setOutputStream(new FileOutputStream("result.xml"));
      xmlReader.setContentHandler(serializer.asContentHandler());
//      InputSource inputSource = new InputSource("file:" + params[0]);
//      InputSource inputSource = new InputSource(new FileReader(params[0]));
      InputSource inputSource = new InputSource(new FileInputStream(params[0]));
      xmlReader.parse(inputSource);
   }

   public static void three(String[] params)
      throws IOException
   {
      Ruleset[] rulesets = getRulesets();
      BufferedReader reader = new BufferedReader(new FileReader(params[0]));
      String st = reader.readLine();
      while(st != null)
      {
         read(rulesets,st);
         st = reader.readLine();
      }
   }

   public static Ruleset[] getRulesets()
   {
      Ruleset[] rulesets = new Ruleset[2];
      rulesets[0] = new Ruleset("http://ananas.org/2002/sample",
                                "address-book",
                                "an:address-book");
      rulesets[1] = new Ruleset("http://ananas.org/2002/sample",
                                "fields",
                                "an:fields");
      Match match = new Match("http://ananas.org/2002/sample",
                              "alias",
                              "an:alias",
                              "^alias (.*):(.*)$");
      Group group = new Group("http://ananas.org/2002/sample",
                              "id",
                              "an:id");
      match.addGroup(group);
      group = new Group("http://ananas.org/2002/sample",
                        "email",
                        "an:email");
      match.addGroup(group);
      rulesets[0].addMatch(match);
      match = new Match("http://ananas.org/2002/sample",
                        "note",
                        "an:note",
                        "^note .*:(.*)$");
      group = new Group("http://ananas.org/2002/sample",
                        "fields",
                        "an:fields");
/*      group = new Group(null,
                        "text()",
                        "text()");*/
      match.addGroup(group);
      rulesets[0].addMatch(match);
      match = new Match("http://ananas.org/2002/sample",
                        "fields",
                        "an:fields",
                        "[\\s]*<([^<]*)>");
      group = new Group("http://ananas.org/2002/sample",
                        "field",
                        "an:field");
      match.addGroup(group);
      rulesets[1].addMatch(match);
      return rulesets;
   }

   public static void read(Ruleset[] rulesets,String st)
   {
      // BUGBUG -- I could move printing of the ruleset name one
      // level above and be done with the next flag maybe...
      read(rulesets,rulesets[0],st,false);
   }

   public static void read(Ruleset[] rulesets,Ruleset ruleset,String st,boolean next)
   {
      boolean found = false;
      for(int i = 0;i < ruleset.getMatchCount() && !found;i++)
      {
         if(ruleset.getMatchAt(i).matches(st))
         {
            found = true;
            Match match = ruleset.getMatchAt(i);
            if(!next)
            {
               System.out.print(ruleset.getMatchAt(i).getQualifiedName());
               System.out.print(' ');
            }
            for(int j = 1;j <= match.getGroupCount();j++)
            {
               String qname = match.getGroupNameAt(j).getQualifiedName();
               boolean deep = false;
               for(int k = 0;k < rulesets.length && !deep;k++)
                  // BUGBUG -- test on something else than qualified name!
                  if(rulesets[k].getQualifiedName().equals(qname))
                  {
                     System.out.print("\n >> \"");
                     System.out.print(match.getGroupValueAt(j));
                     System.out.print("\" >> ");
                     read(rulesets,rulesets[k],match.getGroupValueAt(j),false);
                     deep = true;
                  }
               if(!deep)
               {
                  System.out.print(match.getGroupNameAt(j).getQualifiedName());
                  System.out.print(' ');
                  System.out.print(match.getGroupValueAt(j));
                  System.out.print(' ');
               }
            }
            String rest = match.rest();
            if(rest != null)
               read(rulesets,ruleset,rest,true);
         }
      }
      System.out.println();
/*      Pattern pattern = Pattern.compile("^(.*):(.*)$"); // ,Pattern.MULTILINE);
      Matcher matcher = pattern.matcher(in);
      if(matcher.matches())
      {
         System.out.print("Found: ");
         System.out.print(matcher.group(1));
         System.out.print(" - ");
         System.out.println(matcher.group(2));
      }*/
   }

   public static void two(String[] params)
      throws IOException
   {
      BufferedReader reader = new BufferedReader(new FileReader(params[0]));
      String st = reader.readLine();
      while(st != null)
      {
         read(st);
         st = reader.readLine();
      }
   }

/*
      // one of two options: either check if it's a FileInputStream and refuse
      // anything else or use the .readLine() to pre-break the element
      // in strings...
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      String st = br.readLine();
      while(st != null)
      {
         matcher.
         st = br.readLine();
      }
   }*/

/*
      InputSource src = new InputSource(new FileInputStream(params[0]));
      InputStream is = src.getByteStream();
      ReadableByteChannel rbc = Channels.newChannel(is);
      ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
      CharBuffer charBuffer = CharBuffer.allocate(1024);
      Charset charset = Charset.forName("ISO-8859-1");
      CharsetDecoder decoder = charset.newDecoder();
      System.out.println(1);
      Pattern pattern = Pattern.compile("^(.*):(.*)$",Pattern.MULTILINE);
      int read = 0;
      read = rbc.read(byteBuffer);
//      while((read = rbc.read(byteBuffer)) != -1)
      {
         System.out.println("Read: " + read + " " + 2);
         byteBuffer.flip();
         decoder.decode(byteBuffer,charBuffer,false);
         charBuffer.flip();
         Matcher matcher = pattern.matcher(charBuffer);
         while(matcher.find())
         {
            System.out.println(3);
            System.out.print("Found: ");
            System.out.print(matcher.group(1));
            System.out.print(" - ");
            System.out.println(matcher.group(2));
         }
      }
   }*/

   public static void one(String[] params)
      throws IOException
   {
      FileInputStream input = new FileInputStream(params[0]);
      FileChannel channel = input.getChannel();
      int fileLength = (int)channel.size();
      MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY,0,fileLength);

      // Convert to character buffer
      Charset charset = Charset.forName("ISO-8859-1");
      CharsetDecoder decoder = charset.newDecoder();
      CharBuffer charBuffer = decoder.decode(buffer);

      read(charBuffer);
   }

   public static void read(CharSequence in)
   {
      Pattern pattern = Pattern.compile("^(.*):(.*)$"); // ,Pattern.MULTILINE);
      Matcher matcher = pattern.matcher(in);
      if(matcher.matches())
      {
         System.out.print("Found: ");
         System.out.print(matcher.group(1));
         System.out.print(" - ");
         System.out.println(matcher.group(2));
      }
   }
}
