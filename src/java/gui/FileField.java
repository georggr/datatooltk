package com.dickimawbooks.datatooltk.gui;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.dickimawbooks.datatooltk.DatatoolTk;

public class FileField extends Box
  implements ActionListener
{
   public FileField(Container parent, JFileChooser fileChooser)
   {
      this(parent, null, fileChooser, JFileChooser.FILES_ONLY);
   }

   public FileField(Container parent, JFileChooser fileChooser, int mode)
   {
      this(parent, null, fileChooser, mode);
   }

   public FileField(Container parent, String fileName, JFileChooser fileChooser)
   {
      this(parent, fileName, fileChooser, JFileChooser.FILES_ONLY);
   }

   public FileField(Container parent, String fileName, JFileChooser fileChooser, int mode)
   {
      super(BoxLayout.Y_AXIS);

      this.fileChooser = fileChooser;
      this.parent = parent;
      this.mode = mode;

      add(Box.createVerticalGlue());

      Box box = Box.createHorizontalBox();
      add(box);

      textField = new JTextField(fileName == null ? "" : fileName, 20);

      Dimension dim = textField.getPreferredSize();
      dim.width = (int)textField.getMaximumSize().getWidth();

      textField.setMaximumSize(dim);

      box.add(textField);

      button = new JButton("...");

      button.setActionCommand("choose");
      button.addActionListener(this);

      box.add(button);

      add(Box.createVerticalGlue());

      setAlignmentY(Component.CENTER_ALIGNMENT);
      setAlignmentX(Component.LEFT_ALIGNMENT);
   }

   public void setAlignmentY(float align)
   {
      super.setAlignmentY(align);
      textField.setAlignmentY(align);
      button.setAlignmentY(align);
   }

   public void setAlignmentX(float align)
   {
      super.setAlignmentX(align);
      textField.setAlignmentX(align);
      button.setAlignmentX(align);
   }

   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      if (action.equals("choose"))
      {
         fileChooser.setFileSelectionMode(mode);

         File file = getFile();

         if (file != null)
         {
            fileChooser.setCurrentDirectory(file.getParentFile());

            fileChooser.setSelectedFile(file);
         }

         fileChooser.setApproveButtonMnemonic(DatatoolTk.getMnemonic("button.select"));

         if (fileChooser.showDialog(parent,
            DatatoolTk.getLabel("button.select"))
            == JFileChooser.APPROVE_OPTION)
         {
            textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
         }
      }
   }

   public boolean requestFocusInWindow()
   {
      return textField.requestFocusInWindow();
   }

   public JTextField getTextField()
   {
      return textField;
   }

   public File getFile()
   {
      String fileName = getFileName();

      if (fileName == null || fileName.equals("")) return null;

      return fileName.contains(File.separator) 
         ? new File(fileName)
         : new File(fileChooser.getCurrentDirectory(), fileName);
   }

   public String getFileName()
   {
      return textField.getText();
   }

   public void setFileName(String name)
   {
      textField.setText(name);
   }

   public void setCurrentDirectory(String dirPath)
   {
      setCurrentDirectory(new File(dirPath));
   }

   public void setCurrentDirectory(File dir)
   {
      fileChooser.setCurrentDirectory(dir);
   }

   public void setFile(File file)
   {
      setCurrentDirectory(file.getParentFile());
      setFileName(file.getName());
   }

   public void setEnabled(boolean flag)
   {
      super.setEnabled(flag);

      textField.setEnabled(flag);
      button.setEnabled(flag);
   }

   private JTextField textField;

   private JButton button;

   private JFileChooser fileChooser;

   private Container parent;

   private int mode;
}
