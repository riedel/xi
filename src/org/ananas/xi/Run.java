package org.ananas.xi;

import java.io.*;
import java.util.*;
import org.xml.sax.*;
import javax.xml.transform.*;

public class Run
{
   public static void main(String[] arguments)
   {
      try
      {
         XIFrame frame = new XIFrame();
         frame.show();
         try
         {
            frame.init(makeLinks());
            frame.click(parseArguments(frame,arguments));
         }
         catch(Exception x)
         {
            frame.display(x);
         }
      }
      catch(Exception x)
      {
         // since this exception occurs while opening the window,
         // it's impossible to display a proper error message
         // in most cases, the console won't be on screen
         // the best workaround is probably to store the exception
         // details in a file
         try
         {
            PrintWriter writer =
               new PrintWriter(new FileWriter("fatal.log"));
            writer.print("org.ananas.xi.Run - fatal error on ");
            writer.println(new Date().toString());
            x.printStackTrace(writer);
            writer.close();
            System.err.println("See fatal.log for details...");
         }
         catch(IOException x2)
            { /* total failure, what can I do? */ }
         System.exit(1);
      }
   }

   private static XSLTLink[] makeLinks()
      throws IOException, SAXException,
             TransformerConfigurationException
   {
      File rules = new File("rules"),
           output = new File("output");
      output.mkdirs();
      File[] ruleFiles = rules.listFiles(new FilenameFilter()
      {
         public boolean accept(File dir,String name)
         {
            int pos = name.lastIndexOf('.');
            if(pos != -1)
            {
               String suffix = name.substring(pos);
               return suffix.equalsIgnoreCase(".xsl");
            }
            return false;
         }
      }); 
      XSLTLink[] links = new XSLTLink[ruleFiles.length];
      for(int i = 0;i < ruleFiles.length;i++)
         links[i] = new XSLTLink(ruleFiles[i],output);
      return links;
   }

   private static Properties parseArguments(XIFrame frame,
                                            String[] arguments)
   {
      Properties properties = new Properties();
      boolean input = false,
              rule = false,
              overwrite = false,
              reload = false,
              autoclose = false;
      for(int i = 0;i < arguments.length;i++)
      {
         if(arguments[i].equals("-help"))
         {
            frame.display("org.ananas.xi.Run command-line options:");
            frame.display("  -input filename["
                          + File.pathSeparator + "filename"
                          + File.pathSeparator + "...]");
            frame.display("  -rule rulename");
            frame.display("  -overwrite true | false");
            frame.display("  -reload true | false");
            frame.display("  -autoclose true | false");
         }
         else if(input)
         {
            properties.setProperty("input",arguments[i]);
            input = false;
         }
         else if(rule)
         {
            properties.setProperty("rule",arguments[i]);
            rule = false;
         }
         else if(overwrite)
         {
            properties.setProperty("overwrite",arguments[i]);
            overwrite = false;
         }
         else if(reload)
         {
            properties.setProperty("reload",arguments[i]);
            reload = false;
         }
         else if(autoclose)
         {
            properties.setProperty("autoclose",arguments[i]);
            autoclose = false;
         }
         else if(arguments[i].equals("-input"))
            input = true;
         else if(arguments[i].equals("-rule"))
            rule = true;
         else if(arguments[i].equals("-overwrite"))
            overwrite = true;
         else if(arguments[i].equals("-reload"))
            reload = true;
         else if(arguments[i].equals("-autoclose"))
            autoclose = true;
      }
      return properties;
   }
}
