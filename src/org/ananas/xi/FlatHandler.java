package org.ananas.xi;

import java.io.*;
import org.xml.sax.*;

/**
 * @xmlns flat http://ananas.org/2003/xi/flat
 */

public class FlatHandler
   implements org.ananas.hc.HCHandler
{
   private StringBuffer buffer;
   private int width,
               align,
               defaultWidth,
               defaultAlign;
   private char paddingChar,
                defaultPaddingChar;
   private static final int LEFT = 0,
                            RIGHT = 1;
   private PrintWriter writer = null;

   public void setWriter(PrintWriter writer)
   {
      this.writer = writer;
   }

   /**
    * @xpath flat:root
    */
   public void startRoot(Attributes attributes)
   {
      buffer = new StringBuffer(1024);
      String value = attributes.getValue("default-width");
      try
      {
         defaultWidth = Integer.parseInt(value);
      }
      catch(NumberFormatException e)
      {
         defaultWidth = -1;
      }
      value = attributes.getValue("default-align");
      if(value != null && value.equalsIgnoreCase("right"))
         defaultAlign = RIGHT;
      else
         defaultAlign = LEFT;
      value = attributes.getValue("default-padding");
      if(value != null && value.length() > 0)
         defaultPaddingChar = value.charAt(0);
      else
         defaultPaddingChar = ' ';
   }
   
   /**
    * @xpath flat:root/flat:field
    */
   public void startField(Attributes attributes)
   {
      buffer.setLength(0);
      String value = attributes.getValue("width");
      try
      {
         width = Integer.parseInt(value);
      }
      catch(NumberFormatException e)
      {
         width = defaultWidth;
      }
      value = attributes.getValue("align");
      if(value != null && value.equalsIgnoreCase("left"))
         align = LEFT;
      else if(value != null && value.equalsIgnoreCase("right"))
         align = RIGHT;
      else
         align = defaultAlign;
      value = attributes.getValue("padding");
      if(value != null && value.length() > 0)
         paddingChar = value.charAt(0);
      else
         paddingChar = defaultPaddingChar;
   }
   
   /**
    * @xpath flat:root/flat:field
    */
   public void characters(char[] ch,int offset,int len)
   {
      buffer.append(ch,offset,len);
   }

   /**
    * @xpath flat:root/flat:field
    */
   public void endField()
   {
      if(width != -1)
      {
         int padding = width - buffer.length();
         if(padding > 0)
         {
            if(align == LEFT)
               for(int i = 0;i < padding;i++)
                  writer.print(paddingChar);
            writer.print(buffer.toString());
            if(align == RIGHT)
               for(int i = 0;i < padding;i++)
                  writer.print(paddingChar);
         }
         else
            writer.print(buffer.substring(0,width));
      }
      else
         writer.print(buffer.toString());
   }

   /**
    * @xpath flat:root/flat:br
    */
   public void endBR()
   {
      writer.println();
   }

   /**
    * @xpath flat:root
    */
   public void endRoot()
   {
      writer.close();
      writer = null;
   }
}
