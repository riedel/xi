package org.ananas.xi;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.*;
import org.xml.sax.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.xml.transform.*;
import java.awt.datatransfer.*;
import java.text.MessageFormat;

import org.ananas.hc.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class XIFrame
   extends Frame
{
   private XSLTLink[] links;
   private Choice rules;
   private Checkbox overwrite,
                    reload,
                    autoclose;
   private java.awt.List messages;
   private Button open;
   private DataFlavor flavor;
   private FileDialog fileDialog;

   public XIFrame()
      throws ClassNotFoundException, UnsupportedFlavorException
   {
      super("XI - www.ananas.org/xi");
      links = null;
      fileDialog = new FileDialog(this);
      flavor = new DataFlavor("application/x-java-file-list;"
                              + " class=java.util.List");
      buildUI();
   }

   public void init(XSLTLink[] links)
   {
      this.links = links;
      for(int i = 0;i < links.length;i++)
        rules.add(links[i].getDisplayName());
      open.setEnabled(true);
      open.requestFocus();
   }

   public void display(String message)
   {
      if(messages.getItemCount() > 25)
         messages.remove(0);
      messages.add(message);
      messages.makeVisible(messages.getItemCount() - 1);
   }

   public void display(String pattern,Object[] arguments)
   {
      display(MessageFormat.format(pattern,arguments));
   }

   public void display(Throwable x)
   {
      if(x instanceof SAXParseException)
         display((SAXParseException)x);
      else if(x instanceof SAXException)
         display((SAXException)x);
      else if(x instanceof TransformerException)
         display((TransformerException)x);
      else if(x instanceof IOException)
         display((IOException)x);
      else
      {
         log(x);
         display("{0}: {1}",
                 new Object[] { x.getClass().getName(),
                                x.getMessage() });
      }
   }

   public void display(SAXException x)
   {
      if(x instanceof SAXParseException)
         display((SAXParseException)x);
      else if(x.getException() != null)
         display(x.getException());
      else
      {
         log(x);
         display("SAX: {0}",
                 new Object[] { x.getMessage() });
      }
   }

   public void display(SAXParseException x)
   {
      String pattern = x.getColumnNumber() != -1
                       ? "Parsing: {0} in {1} at line {2}, column {3}"
                       : "Parsing: {0} in {1} at line {2}",
             systemId = null;
      try
      {
         // translate into platform-specific file name
         // (more readable)
         URL url = new URL(x.getSystemId());                 
         if(url.getProtocol().equals("file"))
            systemId = new File(url.getPath(),url.getFile()).getPath();
      }
      catch(MalformedURLException ex)
         { }
      if(systemId == null)
         systemId = x.getSystemId();
      log(x);
      display(pattern,
              new Object[] { x.getMessage(),
                             systemId,
                             new Integer(x.getLineNumber()),
                             new Integer(x.getColumnNumber()) });
   }

   public void display(IOException x)
   {
      log(x);
      display("I/O: {0}",
              new Object[] { x.getMessage() });
   }

   public void display(TransformerException x)
   {
      if(x.getException() != null)
         display(x.getException());
      else
      {
         log(x);
         display("Transform: {0}",
                 new Object[] { x.getMessageAndLocation() });
      }
   }

   public void log(Throwable x)
   {
      x.printStackTrace();
   }

   public void click(Properties properties)
   {
      String value = properties.getProperty("rule");
      if(value != null)
         rules.select(value);
      value = properties.getProperty("overwrite");
      if(value != null)
         overwrite.setState(Boolean.valueOf(value).booleanValue());
      value = properties.getProperty("reload");
      if(value != null)
         reload.setState(Boolean.valueOf(value).booleanValue());
      value = properties.getProperty("autoclose");
      if(value != null)
         autoclose.setState(Boolean.valueOf(value).booleanValue());
      value = properties.getProperty("input");
      if(value != null)
      {
         StringTokenizer tokenizer =
            new StringTokenizer(value,File.pathSeparator);
         while(tokenizer.hasMoreTokens())
            applyTo(new File(tokenizer.nextToken()),false);
      }
   }

   private void buildUI()
   {
      setResizable(false);
      setSize(400,300);
      // load icon
      ClassLoader classLoader = getClass().getClassLoader();
      try
      {
         URL url = classLoader.getResource("etc/icon16.gif");
         Toolkit toolkit = Toolkit.getDefaultToolkit();
         Image image = toolkit.createImage((ImageProducer)url.getContent());
         setIconImage(image);
      }
      catch(Exception x)
         { /* no icon, too bad but that does not stop us */ }
      // add listeners
      addWindowListener(new WindowAdapter()
      {
         public void windowClosing(WindowEvent e)
            { doClose(); }
      });
      DropTarget dp = new DropTarget(this,new DropTargetListener()
      {
         public void dragEnter(DropTargetDragEvent e)
            { doDrag(e); }
         public void dragOver(DropTargetDragEvent e)
            { doDrag(e); }
         public void dropActionChanged(DropTargetDragEvent e)
            { doDrag(e); }
         public void dragExit(DropTargetEvent e)
            { }
         public void drop(DropTargetDropEvent e)
            { doDrop(e); }
      });
      // add controls
      GridBagLayout layout = new GridBagLayout();
      GridBagConstraints constraints = new GridBagConstraints();
      setLayout(layout);
      Label label = new Label("Rules:");
      constraints.anchor = GridBagConstraints.WEST;
      constraints.insets = new Insets(5,5,5,5);
      layout.setConstraints(label,constraints);
      add(label);
      rules = new Choice();
      constraints.anchor = GridBagConstraints.EAST;
      constraints.insets.left = 0;
      constraints.gridwidth = GridBagConstraints.REMAINDER;
      constraints.weightx = 1.0;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      layout.setConstraints(rules,constraints);
      add(rules);
      Panel inner = new Panel();
      inner.setLayout(new GridLayout(1,3));
      overwrite = new Checkbox("Overwrite",true);
      inner.add(overwrite);
      reload = new Checkbox("Reload",false);
      inner.add(reload);
      autoclose = new Checkbox("Close when done",false);
      inner.add(autoclose);
      constraints.fill = GridBagConstraints.BOTH;
      constraints.anchor = GridBagConstraints.CENTER;
      constraints.gridwidth = GridBagConstraints.REMAINDER;
      constraints.insets.top = 0;
      constraints.insets.left = 5;
      layout.setConstraints(inner,constraints);
      add(inner);
      label = new Label("Messages:");
      constraints.fill = GridBagConstraints.NONE;
      constraints.anchor = GridBagConstraints.WEST;
      layout.setConstraints(label,constraints);
      add(label);
      messages = new java.awt.List(10);
      constraints.anchor = GridBagConstraints.CENTER;
      constraints.gridwidth = GridBagConstraints.REMAINDER;
      constraints.weightx = 1.0;
      constraints.weighty = 1.0;
      constraints.insets.left = 5;
      constraints.fill = GridBagConstraints.BOTH;
      layout.setConstraints(messages,constraints);
      add(messages);
      open = new Button("Open..."); 
      open.setEnabled(false);
      open.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
            { doOpen(); }
      });
      constraints.weighty = 0.0;
      constraints.ipadx = 40;
      constraints.ipady = 4;
      constraints.fill = GridBagConstraints.NONE;
      layout.setConstraints(open,constraints);
      add(open);
      
      MenuBar bar = new MenuBar();
      Menu menu = new Menu("Editor");
      bar.add(menu);
      MenuItem item = new MenuItem("Launch...");
      menu.add(item);
      item.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            Frame frame = new Frame("Editor");
            frame.setSize(600,300);
            EditorPanel panel = new EditorPanel();
            try
            {
               SAXParserFactory factory = SAXParserFactory.newInstance();
               factory.setNamespaceAware(true);
               SAXParser parser = factory.newSAXParser();
               XMLReader reader = parser.getXMLReader();
               RulesHandler rulesHandler = new RulesHandler();
               reader.setContentHandler(new XPathHandler(rulesHandler));
               reader.parse(new InputSource("rules/e205 de.xsl"));
               Ruleset[] rulesets = rulesHandler.getRulesets();
               if(rulesets == null)
                  return;
               else
                  panel.setRulesets(rulesets);
            }
            catch(Throwable x)
            {
               display(x);
            }
            frame.add(panel);
            frame.show();
         }
      });
      setMenuBar(bar);
   }

   private void doClose()
   {
      System.exit(0);
   }

   private void doDrag(DropTargetDragEvent e)
   {
      if(e.isDataFlavorSupported(flavor) && links != null)
      {
         if((e.getDropAction() & DnDConstants.ACTION_COPY)
            != DnDConstants.ACTION_COPY)
            e.acceptDrag(DnDConstants.ACTION_COPY);
      }
      else
         e.rejectDrag();
   }

   private void doDrop(DropTargetDropEvent e)
   {
      if(e.isDataFlavorSupported(flavor) && links != null)
      {
         e.acceptDrop(DnDConstants.ACTION_COPY);
         try
         {
            Transferable transferable = e.getTransferable();
            java.util.List files =
               (java.util.List)transferable.getTransferData(flavor);
            Iterator iterator = files.iterator();
            while(iterator.hasNext())
               applyTo((File)iterator.next(),iterator.hasNext());
            e.dropComplete(true);
         }
         catch(UnsupportedFlavorException x)
         {
            e.dropComplete(false);
         }
         catch(IOException x)
         {
            e.dropComplete(false);
         }
      }
      else
         e.rejectDrop();
   }

   private void doOpen()
   {
      fileDialog.setFile("");
      fileDialog.show();
      if(fileDialog.getFile() != null)
         applyTo(new File(fileDialog.getDirectory(),
                          fileDialog.getFile()),
                 false);
   }

   private synchronized void applyTo(File source,boolean retain)
   {
      if(links == null)
         return;
      try
      {
         display("Processing {0}",new Object[] { source.getPath() });
         XSLTLink current = links[rules.getSelectedIndex()];
         if(reload.getState())
            current.reload();
         File result = current.applyTo(source,overwrite.getState());
         display("Done: {0} created",new Object[] { result.getPath() });
         if(!retain && autoclose.getState())
            doClose();
      }
      catch(Exception x)
      {
         display(x);
      }
   }
}
