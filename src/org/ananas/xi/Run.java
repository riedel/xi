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
            Properties properties = parseArguments(frame,arguments);
            frame.init(makeLinks(frame,properties));
            frame.click(properties);
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
         // just in case there's a console somewhere, I put a
         // warning up
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
         {
            // total failure, try the console instead
            x2.printStackTrace();
         }
         System.exit(1);
      }
   }

   private static XSLTLink[] makeLinks(XIFrame frame,
                                       Properties properties)
      throws IOException
   {
      String rulesSt = properties.getProperty("rulesDir"),
             outputSt = properties.getProperty("outputDir");
      File rules = rulesSt == null ? new File("rules") :
                                     new File(rulesSt),
           output = outputSt == null ? new File("output") :
                                       new File(outputSt);
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
      if(ruleFiles == null)
         return new XSLTLink[0];
      XSLTLink[] links = new XSLTLink[ruleFiles.length];
      for(int i = 0;i < ruleFiles.length;i++)
         try
         {
            links[i] = new XSLTLink(ruleFiles[i],output);
         }
         catch(Exception x)
         {
            frame.display(x);
         }
      return links;
   }

   private static Properties parseArguments(XIFrame frame,
                                            String[] arguments)
   {
      Properties properties = new Properties();
      boolean input = false,
              rule = false,
              rulesDir = false,
              outputDir = false,
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
            frame.display("  -rulesdir directory");
            frame.display("  -outputdir directory");
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
         else if(rulesDir)
         {
            properties.setProperty("rulesDir",arguments[i]);
            rulesDir = false;
         }
         else if(outputDir)
         {
            properties.setProperty("outputDir",arguments[i]);
            outputDir = false;
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
         else if(arguments[i].equals("-rulesdir"))
            rulesDir = true;
         else if(arguments[i].equals("-outputdir"))
            outputDir = true;
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
