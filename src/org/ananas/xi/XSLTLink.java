package org.ananas.xi;

import java.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;

public class XSLTLink
{
   private String name;
   private File stylesheet,
                output;
   private String suffix;
   private XMLReader xiReader;
   private Transformer transformer;

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
      reload();
   }

   public void reload()
      throws IOException, SAXException,
             TransformerConfigurationException
   {
      InputSource input =
         new InputSource(stylesheet.toURI().toString());
      xiReader =
         XMLReaderFactory.createXMLReader("org.ananas.xi.XIReader");
      xiReader.setProperty(XIReader.RULESETS_URI,input);
      TransformerFactory factory = TransformerFactory.newInstance();
      transformer =
         factory.newTransformer(new StreamSource(stylesheet));
      suffix = transformer.getOutputProperty("method");
   }

   public File applyTo(File source,boolean overwrite)
      throws IOException, SAXException, TransformerException
   {
      if(!source.isFile())
         throw new IOException(source.getPath() + " is not a file");
      InputSource input = new InputSource(source.toURI().toString());
      String name = source.getName();
      int pos = name.lastIndexOf('.');
      String base = pos != -1 ? name.substring(0,pos + 1)
                              : name + '.';
      File result = new File(output,base + suffix);
      int index = 1;
      while(result.exists() && !overwrite)
      {
         result = new File(output,base + index + "." + suffix);
         index++;
      }
      transformer.transform(new SAXSource(xiReader,input),
                            new StreamResult(result));
      return result;
   }

   public String getDisplayName()
   {
      return name;
   }
}
