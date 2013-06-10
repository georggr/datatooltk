package com.dickimawbooks.datatooltk.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import javax.swing.filechooser.FileFilter;
import javax.help.*;

import com.dickimawbooks.datatooltk.*;
import com.dickimawbooks.datatooltk.io.*;

public class DatatoolGUI extends JFrame
  implements ActionListener
{
   public DatatoolGUI(DatatoolSettings settings)
   {
      super(DatatoolTk.appName);

      this.settings = settings;

      initGui();
   }

   private void initGui()
   {
      setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

      addWindowListener(new WindowAdapter()
         {
            public void windowClosing(WindowEvent evt)
            {
               quit();
            }
         }
      );

      String imgFile = "/resources/icons/logosmall.png";

      URL imageURL = DatatoolTk.class.getResource(imgFile);

      if (imageURL != null)
      {
         setIconImage(new ImageIcon(imageURL).getImage());
      }
      else
      {
         DatatoolGuiResources.error(null, 
            new FileNotFoundException("Can't find resource: '"
            +imgFile+"'"));
      }

      try
      {
         initHelp();
      }
      catch (HelpSetException e)
      {
         DatatoolGuiResources.error(null, e);
      }
      catch (FileNotFoundException e)
      {
         DatatoolGuiResources.error(null, e);
      }

      JMenuBar mbar = new JMenuBar();
      setJMenuBar(mbar);

      JMenu fileM = DatatoolGuiResources.createJMenu("file");
      mbar.add(fileM);

      fileM.add(DatatoolGuiResources.createJMenuItem(
        "file", "open", this,
         KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK)));

      fileM.add(DatatoolGuiResources.createJMenuItem(
        "file", "importcsv", this));

      fileM.add(DatatoolGuiResources.createJMenuItem(
        "file", "close", this));

      fileM.add(DatatoolGuiResources.createJMenuItem(
        "file", "quit", this,
        KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK)));

      JMenu helpM = DatatoolGuiResources.createJMenu("help");
      mbar.add(helpM);

      helpM.add(DatatoolGuiResources.createJMenuItem(
         "help", "manual", csh,
          KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0)));

      helpM.add(DatatoolGuiResources.createJMenuItem(
         "help", "about", this));

      settings.setPasswordReader(new GuiPasswordReader(this));

      // main panel

      tabbedPane = new JTabbedPane()
      {
         public String getToolTipText(MouseEvent event) 
         {
            javax.swing.plaf.TabbedPaneUI ui = getUI();

            if (ui != null)
            {
               int index = ui.tabForCoordinate(this, event.getX(), event.getY());

               if (index != -1)
               {
                  return ((DatatoolDbPanel)getComponentAt(index)).getToolTipText();
               }
            }

            return super.getToolTipText(event);
         }
      };
      getContentPane().add(tabbedPane, "Center");

      // File filters

      texFilter = new TeXFileFilter();
      dbtexFilter = new DbTeXFileFilter();
      csvtxtFilter = new CsvTxtFileFilter();
      csvFilter = new CsvFileFilter();
      txtFilter = new TxtFileFilter();

      fileChooser = new JFileChooser();

      // Set default dimensions

      Toolkit tk = Toolkit.getDefaultToolkit();

      Dimension d = tk.getScreenSize();

      int width = 3*d.width/4;
      int height = d.height/2;

      setSize(width, height);

      setLocationRelativeTo(null);
   }

   private void initHelp()
     throws HelpSetException,FileNotFoundException
   {
      if (mainHelpBroker == null)
      {
         HelpSet mainHelpSet = null;

         String resource = "datatooltk";

         String helpsetLocation = "/resources/helpsets/"+resource;

         String lang    = DatatoolTk.getLanguage();
         String country = DatatoolTk.getCountry();

         URL hsURL = getClass().getResource(helpsetLocation
          + "-" + lang + "-" + country + "/" + resource + ".hs");

         if (hsURL == null)
         {
            hsURL = getClass().getResource(helpsetLocation
              + "-"+lang + "/" + resource + ".hs");

            if (hsURL == null)
            {
               hsURL = getClass().getResource(helpsetLocation
                 + "-en-US/" + resource + ".hs");

               if (hsURL == null)
               {
                  throw new FileNotFoundException(
                    "Can't find helpset files. Tried: \n"
                   + helpsetLocation + "-" + lang + "-" 
                   + country + "/" + resource + ".hs\n"
                   + helpsetLocation + "-" + lang + "/" 
                   + resource + ".hs\n"
                   + helpsetLocation + "-en-US/" + resource + ".hs");
               }
            }
         }

         mainHelpSet = new HelpSet(null, hsURL);

         mainHelpBroker = mainHelpSet.createHelpBroker();

         csh = new CSH.DisplayHelpFromSource(mainHelpBroker);
      }
   }

   public void enableHelpOnButton(JComponent comp, String id)
   {
      if (mainHelpBroker != null)
      {
         try
         {
            mainHelpBroker.enableHelpOnButton(comp, id, 
               mainHelpBroker.getHelpSet());
         }
         catch (BadIDException e)
         {
            DatatoolGuiResources.error(null, e);
         }
      }
      else
      {
         DatatoolTk.debug("Can't enable help on button (id="+id
           +"): null help broker");
      }
   }

   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      if (action.equals("quit"))
      {
         quit();
      }
      else if (action.equals("save"))
      {
         save();
      }
      else if (action.equals("open"))
      {
         load();
      }
      else if (action.equals("importcsv"))
      {
         importCsv();
      }
      else if (action.equals("close"))
      {
         close();
      }
      else if (action.equals("about"))
      {
         JOptionPane.showMessageDialog(this, 
           DatatoolTk.getAppInfo(),
           DatatoolTk.getLabelWithValue("about.title", DatatoolTk.appName),
           JOptionPane.PLAIN_MESSAGE);
      }
   }

   public void quit()
   {
      for (int i = tabbedPane.getTabCount()-1; i >= 0; i--)
      {
         if (!close((DatatoolDbPanel)tabbedPane.getComponentAt(i)))
         {
            return;
         }
      }

      System.exit(0);
   }

   private void setTeXFileFilters()
   {
      FileFilter current = fileChooser.getFileFilter();

      if (current == dbtexFilter || current == texFilter)
      {
         return;
      }

      fileChooser.resetChoosableFileFilters();

      FileFilter all = fileChooser.getAcceptAllFileFilter();

      fileChooser.removeChoosableFileFilter(all);

      fileChooser.addChoosableFileFilter(dbtexFilter);
      fileChooser.addChoosableFileFilter(texFilter);
      fileChooser.addChoosableFileFilter(all);
   }

   private void setCsvFileFilters()
   {
      FileFilter current = fileChooser.getFileFilter();

      if (current == csvFilter || current == txtFilter 
       || current == csvtxtFilter)
      {
         return;
      }

      fileChooser.resetChoosableFileFilters();

      FileFilter all = fileChooser.getAcceptAllFileFilter();

      fileChooser.removeChoosableFileFilter(all);

      fileChooser.addChoosableFileFilter(csvtxtFilter);
      fileChooser.addChoosableFileFilter(csvFilter);
      fileChooser.addChoosableFileFilter(txtFilter);
      fileChooser.addChoosableFileFilter(all);
   }

   public boolean close()
   {
      DatatoolDbPanel panel 
         = (DatatoolDbPanel)tabbedPane.getSelectedComponent();

      return close(panel);
   }

   public boolean close(DatatoolDbPanel panel)
   {
      if (panel == null) return true;

      if (panel.isModified())
      {
         switch (JOptionPane.showConfirmDialog(this,
           DatatoolTk.getLabelWithValue("message.unsaved_data_query",
             panel.getName()),
           DatatoolTk.getLabel("message.unsaved_data"),
           JOptionPane.YES_NO_CANCEL_OPTION,
           JOptionPane.QUESTION_MESSAGE))
         {
            case JOptionPane.YES_OPTION:
               panel.save();
            break;
            case JOptionPane.NO_OPTION:
            break;
            default:
               return false;
         }
      }

      tabbedPane.remove(panel);
      panel = null;

      return true;
   }

   public void save()
   {
      DatatoolDbPanel panel 
         = (DatatoolDbPanel)tabbedPane.getSelectedComponent();

      if (panel == null)
      {
         DatatoolGuiResources.error(this, 
           DatatoolTk.getLabel("error.nopanel"));
         return;
      }

      panel.save();
   }

   public void saveAs()
   {
      setTeXFileFilters();

      if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
      {
         return;
      }

      DatatoolDbPanel panel 
         = (DatatoolDbPanel)tabbedPane.getSelectedComponent();

      if (panel == null)
      {
         DatatoolGuiResources.error(this, 
           DatatoolTk.getLabel("error.nopanel"));
         return;
      }

      panel.save(fileChooser.getSelectedFile());
   }

   public void load()
   {
      setTeXFileFilters();

      if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
      {
         load(fileChooser.getSelectedFile());
      }
   }

   public void load(String filename)
   {
      load(new File(filename));
   }

   public void load(File file)
   {
      try
      {
         DatatoolDb db = DatatoolDb.load(file);

         DatatoolDbPanel panel = new DatatoolDbPanel(this, db);

         tabbedPane.addTab(panel.getName(), panel);
         tabbedPane.setToolTipTextAt(tabbedPane.getTabCount()-1, 
           file.toString());
      }
      catch (IOException e)
      {
         DatatoolGuiResources.error(this,
           DatatoolTk.getLabelWithValues(
             "error.load.failed", file.toString(), e.getMessage()));
      }
   }

   public void importCsv()
   {
      setCsvFileFilters();

      if (fileChooser.showDialog(this, DatatoolTk.getLabel("button.import"))
       != JFileChooser.APPROVE_OPTION)
      {
         return;
      }

      DatatoolCsv imp = new DatatoolCsv(settings);

      try
      {
         DatatoolDb db = imp.importData(fileChooser.getSelectedFile());

         DatatoolDbPanel panel = new DatatoolDbPanel(this, db);

         tabbedPane.addTab(panel.getName(), panel);
      }
      catch (DatatoolImportException e)
      {
         DatatoolGuiResources.error(this, e);
      }
   }

   public void importData(DatatoolImport imp, String source)
   {
      try
      {
         DatatoolDb db = imp.importData(source);

         DatatoolDbPanel panel = new DatatoolDbPanel(this, db);

         tabbedPane.addTab(panel.getName(), panel);
      }
      catch (DatatoolImportException e)
      {
         DatatoolGuiResources.error(this, e);
      }
   }

   private DatatoolSettings settings;

   private JTabbedPane tabbedPane;

   private JFileChooser fileChooser;

   private FileFilter texFilter, dbtexFilter, csvFilter, txtFilter,
     csvtxtFilter;

   private HelpBroker mainHelpBroker;
   private CSH.DisplayHelpFromSource csh;
}