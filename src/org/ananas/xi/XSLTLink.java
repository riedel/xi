package org.ananas.xi;

import java.io.*;
import java.util.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import org.ananas.hc.*;
import javax.xml.parsers.*;
import org.xml.sax.helpers.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

public class XSLTLink
{
   private String name;
   private File stylesheet,
                output;
   private String suffix;
   private XMLReader xiReader;
   private Transformer transformer;
   private FlatHandler flatHandler = null;
   private static final String TEXT_METHOD = "{http://ananas.org/2002/xi/rules}flat",
                               SYSTEM_ID = "{http://ananas.org/2002/xi/rules}system-id",
                               PUBLIC_ID = "{http://ananas.org/2002/xi/rules}public-id";
   private boolean loaded = false;

   public XSLTLink(File stylesheet,File output)
      throws IOException, SAXException,
             TransformerConfigurationException
   {
      this.stylesheet = stylesheet;
      this.output = output;
      name = stylesheet.getName();
      int pos = name.lastIndexOf('.');
      if(pos != -1)
         name = name.substring(0,pos);
   }

   public XSLTLink(File stylesheet)
      throws IOException, SAXException,
             TransformerConfigurationException
   {
      this(stylesheet,null);
   }

   public synchronized void reload()
      throws IOException, SAXException,
             TransformerConfigurationException
   {
      InputSource input =
         new InputSource(stylesheet.toURL().toExternalForm());
      xiReader =
         XMLReaderFactory.createXMLReader("org.ananas.xi.XIReader");
      xiReader.setProperty(XIReader.RULESETS_URI,input);
      TransformerFactory factory = TransformerFactory.newInstance();
      transformer =
         factory.newTransformer(new StreamSource(stylesheet));
      suffix = transformer.getOutputProperty(OutputKeys.METHOD);
      if(suffix.equals(TEXT_METHOD))
      {
         suffix = "txt";
         flatHandler = new FlatHandler();
      }
      else
         flatHandler = null;
      loaded = true;
   }

   private synchronized void applyTo(InputSource input,OutputStream os)
      throws IOException, SAXException, TransformerException
   {
      if(!loaded)
         reload();
      Source tsource = null;
      if(xiReader.getFeature(XIReader.RULESETS_VALID_URI))
         tsource = new SAXSource(xiReader,input);
      else
         tsource = new SAXSource(input);
      Result tresult = null;
      if(flatHandler == null)
         tresult = new StreamResult(os);
      else
      {
         String encoding = transformer.getOutputProperty(OutputKeys.ENCODING);
         OutputStreamWriter osw = encoding == null
                                  ? new OutputStreamWriter(os)
                                  : new OutputStreamWriter(os,encoding);
         flatHandler.setWriter(new PrintWriter(osw));
         tresult = new SAXResult(new XPathHandler(flatHandler));
      }
      transformer.clearParameters();
      if(input.getSystemId() != null)
         transformer.setParameter(SYSTEM_ID,input.getSystemId());
      if(input.getPublicId() != null)
         transformer.setParameter(PUBLIC_ID,input.getSystemId());
      transformer.transform(tsource,tresult);
   }

   public void applyTo(InputStream is,String systemId,OutputStream os)
      throws IOException, SAXException, TransformerException
   {
      if(output != null)
         throw new IllegalStateException("XSLTLink has an output directory.");
      InputSource input = new InputSource(is);
      input.setSystemId(systemId);
      applyTo(input,os);
   }

   public synchronized File applyTo(File source,boolean overwrite)
      throws IOException, SAXException, TransformerException
   {
      if(!loaded)   // needed for prefix..., synchronized for the same reason
         reload();
      if(output == null)
         throw new IllegalStateException("XSLTLink has no output directory.");
      if(!source.isFile())
         throw new IOException(source.getPath() + " is not a file");
      InputSource input = new InputSource(source.toURL().toExternalForm());
      String name = source.getName();
      int pos = name.lastIndexOf('.');
      String base = pos != -1 ? name.substring(0,pos + 1)
                              : name + '.';
      File resultFile = new File(output,adaptFilename(name));
      int index = 1;
      while(resultFile.exists() && !overwrite)
      {
         resultFile = new File(output,base + index + "." + suffix);
         index++;
      }
      OutputStream os = new FileOutputStream(resultFile);
      try
      {
         applyTo(input,os);
      }
      finally
      {
         os.close();
      }
      return resultFile;
   }

   public synchronized String getDisplayName()
   {
      return name;
   }

   public static String getMimeType(Properties outputProperties)
   {
      if(outputProperties == null)
         return null;
      String st = outputProperties.getProperty(OutputKeys.MEDIA_TYPE);
      if(st != null)
         return st;
      st = outputProperties.getProperty(OutputKeys.METHOD);
      if(st == null)
         return "text/xml";
      if(st.equals("html"))
         return "text/html";
      else if(st.equals("text") || st.equals(TEXT_METHOD))
         return "text/plain";
      else
         return "text/xml";
   }

   public synchronized String getMimeType()
   {
      return getMimeType(transformer.getOutputProperties());
   }
   
   public synchronized String adaptFilename(String fname)
      throws IOException, SAXException,
             TransformerConfigurationException
   {
      if(fname == null)
         return null;
      if(!loaded)
         reload();
      int pos = fname.lastIndexOf('.');
      String base = pos != -1 ? fname.substring(0,pos + 1)
                              : fname + '.';
      return base + suffix;
   }
}
