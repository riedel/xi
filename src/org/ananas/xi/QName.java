package org.ananas.xi;

import java.util.*;

public class QName
{
   private String namespaceURI,
                  localName,
                  qualifiedName,
                  prefix;

   public QName(String namespaceURI,
                String localName,
                String prefix)
   {
      this.localName = localName.trim().intern();
      if(localName.equals("text()"))
      {
         this.qualifiedName = "text()";
         this.prefix = "";
         this.namespaceURI = "";
      }
      else
      {
         this.namespaceURI = namespaceURI != null ? namespaceURI.trim() : "";
         this.prefix = prefix != null ? prefix.trim() : "";
         if(this.prefix.equals("") || this.namespaceURI.equals(""))
            this.qualifiedName = localName;
         else
         {
            StringBuffer buffer = new StringBuffer(this.prefix);
            buffer.append(':');
            buffer.append(this.localName);
            this.qualifiedName = buffer.toString();
         }
      }
      this.namespaceURI = this.namespaceURI.intern();
   }

   public String getNamespaceURI()
   {
      return namespaceURI;
   }

   public String getLocalName()
   {
      return localName;
   }

   public String getQualifiedName()
   {
      return qualifiedName;
   }

   public String getPrefix()
   {
      return prefix;
   }

   public boolean isText()
   {
      return localName.equals("text()");
   }

   public boolean equals(Object o)
   {
      if(o instanceof QName)
      {
         QName qName = (QName)o;
         return namespaceURI == qName.namespaceURI &&
                localName == qName.localName;
      }
      else
         return false;
   }

   public int hashCode()
   {
      return namespaceURI.hashCode() ^ localName.hashCode();
   }
}
