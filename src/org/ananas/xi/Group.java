package org.ananas.xi;

public class Group
   extends QName
{
   private boolean trimSpace,
                   ignoreEmpty;

   public Group(String namespaceURI,
                String localName,
                String prefix,
                boolean trimSpace,
                boolean ignoreEmpty)
   {
      super(namespaceURI,localName,prefix);
      this.trimSpace = trimSpace;
      this.ignoreEmpty = ignoreEmpty;
   }
   
   public boolean isTrimSpace()
   {
      return trimSpace;
   }
   
   public boolean isIgnoreEmpty()
   {
      return ignoreEmpty;
   }
}