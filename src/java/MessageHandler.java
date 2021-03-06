/*
    Copyright (C) 2017 Nicola L.C. Talbot
    www.dickimaw-books.com

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package com.dickimawbooks.datatooltk;

import java.io.File;
import java.util.logging.ErrorManager;
import java.awt.Component;
import javax.swing.JOptionPane;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import com.dickimawbooks.texparserlib.*;
import com.dickimawbooks.datatooltk.gui.DatatoolGuiResources;

/**
 * Handler for dealing with messages.
 */
public class MessageHandler extends ErrorManager 
  implements ErrorHandler
{
   public MessageHandler(DatatoolTk datatooltk)
   {
      this(true, datatooltk);
   }

   public MessageHandler(boolean isBatchMode, DatatoolTk datatooltk)
   {
      super();
      this.datatooltk = datatooltk;
      init(isBatchMode);
   }

   private void init(boolean isBatchMode)
   {
      this.isBatchMode = isBatchMode;

      texApp = new DatatoolTeXApp(this);
   }

   public TeXApp getTeXApp()
   {
      return texApp;
   }

   public static String codePointToString(int codePoint)
   {
      return new String(Character.toChars(codePoint));
   }

   public void progress(int percentage)
   {
      if (isBatchMode) return;

      guiResources.progress(percentage);
   }

   public void progress(String msg)
   {
      if (isBatchMode)
      {
         message(msg);
      }
      else
      {
         guiResources.progress(msg);
      }
   }

   public void startBuffering()
   {
      if (isBatchMode) return;

      errorBuffer = new StringBuffer();
      warningBuffer = new StringBuffer();
   }

   public void stopBuffering()
   {
      if (isBatchMode) return;

      if (errorBuffer.length() > 0)
      {
         guiResources.error(bufferingComponent, errorBuffer.toString());
      }

      if (warningBuffer.length() > 0)
      {
         guiResources.warning(bufferingComponent, warningBuffer.toString());
      }

      errorBuffer = null;
      warningBuffer = null;
   }

   public boolean buffering()
   {
      return errorBuffer != null;
   }

   public void setBatchMode(boolean isBatchMode)
   {
      this.isBatchMode = isBatchMode;
   }

   public boolean isBatchMode()
   {
      return isBatchMode;
   }

   public void setDebugMode(boolean enabled)
   {
      debugMode = enabled;
   }

   public boolean isDebugMode()
   {
      return debugMode;
   }

   public void message(String msg)
   {
      if (isBatchMode)
      {
         System.out.println(msg);
      }
   }

   public String getMessage(Throwable throwable)
   {
      if (throwable instanceof TeXSyntaxException)
      {
         return ((TeXSyntaxException)throwable).getMessage(texApp);
      }
      else
      {
         return throwable.getMessage();
      }
   }

   public void warning(String message)
   {
      warning((Component)null, message);
   }

   public void warning(TeXParser parser, String message)
   {
      File file = null;
      int lineNumber = -1;

      if (parser != null)
      {
         file = parser.getCurrentFile();
         TeXReader reader = parser.getReader();

         if (reader != null)
         {
            lineNumber = reader.getLineNumber();
         }
      }

      if (file != null)
      {
         if (lineNumber > 0)
         {
            message = String.format("%s:%d: %s", file,
             lineNumber, message);
         }
         else
         {
            message = String.format("%s: %s", file, message);
         }
      }

      warning((Component)null, message);
   }

   public void warning(Component parent, String message)
   {
      if (isBatchMode)
      {
         System.err.println(String.format("%s: %s", 
           DatatoolTk.APP_NAME, message));
      }
      else
      {
         if (buffering())
         {
            bufferingComponent = parent;
            warningBuffer.append(String.format("%s%n", message));
         }
         else
         {
            guiResources.warning(parent, message);
         }
      }
   }

   public void warning(Exception e)
   {
      warning(null, e);
   }

   public void warning(Component parent, Exception e)
   {
      warning(parent, getMessage(e));

      if (debugMode)
      {
         e.printStackTrace();
      }
   }

   public void debug(String message)
   {
      if (debugMode)
      {
         System.err.println(String.format("%s: %s", 
           DatatoolTk.APP_NAME, message));
      }
   }

   public void debug(String message, Exception e)
   {
      if (debugMode)
      {
         System.err.println(String.format("%s: %s", 
           DatatoolTk.APP_NAME, message));
         e.printStackTrace();
      }
   }

   public void debug(Exception e)
   {
      debug(getMessage(e), e);
   }

   public void error(Throwable throwable)
   {
      if (throwable instanceof Exception)
      {
         error(null, null, (Exception)throwable, GENERIC_FAILURE);
      }
      else
      {
         error(getMessage(throwable), null, RUNTIME_FAILURE);

         if (debugMode)
         {
            throwable.printStackTrace();
         }
      }
   }

   public void error(Exception exception, int code)
   {
      error(null, null, exception, code);
   }

   public void error(String msg)
   {
      error(msg, null, GENERIC_FAILURE);
   }

   public void error(String msg, int code)
   {
      error(msg, null, code);
   }

   public void error(Component parent, String msg)
   {
      error(parent, msg, null, GENERIC_FAILURE);
   }

   public void error(Component parent, String msg, int code)
   {
      error(parent, msg, null, code);
   }

   public void error(Component parent, Exception e)
   {
      error(parent, null, e, GENERIC_FAILURE);
   }

   public void error(Component parent, String message, Exception e)
   {
      error(parent, message, e, GENERIC_FAILURE);
   }

   public void error(Component parent, Exception e, int code)
   {
      error(parent, null, e, code);
   }

   public void error(String msg, Exception exception, int code)
   {
      error(null, msg, exception, code);
   }

   public void error(Component parent, String msg, 
     Exception exception, int code)
   {
      if (isBatchMode)
      {
         if (msg == null)
         {
            System.err.println(String.format("%s: %s", 
              DatatoolTk.APP_NAME, getMessage(exception)));
         }
         else
         {
            System.err.println(String.format("%s: %s", 
              DatatoolTk.APP_NAME, msg));
         }

         if (exception != null)
         {
            Throwable cause = exception.getCause();

            if (cause != null)
            {
               System.err.println(getMessage(cause));
            }
         }
      }
      else
      {
         if (buffering())
         {
            bufferingComponent = parent;

            if (msg == null)
            {
               errorBuffer.append(getMessage(exception));
            }
            else
            {
               errorBuffer.append(msg);
            }
         }
         else
         {
            // guiResources may be null if there's an error
            // in the command line syntax

            if (msg != null && exception != null)
            {
               if (guiResources == null)
               {
                  JOptionPane.showMessageDialog(null, msg, 
                    getLabel("error.title"),
                    JOptionPane.ERROR_MESSAGE);
                  System.err.println(msg);

                  debug(exception);
               }
               else
               {
                  guiResources.error(parent, msg, exception);
               }
            }
            else if (exception == null)
            {
               if (guiResources == null)
               {
                  JOptionPane.showMessageDialog(null, msg, 
                    getLabel("error.title"),
                    JOptionPane.ERROR_MESSAGE);
                  System.err.println(msg);
               }
               else
               {
                  guiResources.error(parent, msg);
               }
            }
            else
            {
               if (guiResources == null)
               {
                  JOptionPane.showMessageDialog(null, 
                    exception.getMessage(), 
                    getLabel("error.title"),
                    JOptionPane.ERROR_MESSAGE);

                  System.err.println(exception.getMessage());
                  debug(exception);
               }
               else
               {
                  guiResources.error(parent, exception);
               }
            }
         }
      }

      if (exception != null && debugMode)
      {
         exception.printStackTrace();
      }
   }

   public void error(SAXParseException exception)
   {
      error(getMessage(exception), exception, FORMAT_FAILURE);
   }

   public void warning(SAXParseException exception)
   {
      warning(exception);
   }

   public void fatalError(SAXParseException exception)
   {
      error(getMessage(exception), exception, GENERIC_FAILURE);
      exception.printStackTrace();
   }

   public String getLabelWithAlt(String label, String alt)
   {
      return datatooltk.getLabelWithAlt(label, alt);
   }

   public String getLabelRemoveArgs(String parent, String label)
   {
      return datatooltk.getLabelRemoveArgs(parent, label);
   }

   public String getLabel(String label)
   {
      return datatooltk.getLabel(label);
   }

   public String getLabel(String parent, String label)
   {
      return datatooltk.getLabel(parent, label);
   }

   public String getLabelWithValues(String label, Object... values)
   {
      return datatooltk.getLabelWithValues(label, values);
   }

   public String getLabelWithValues(int lineNumber, String label, Object... values)
   {
      String msg = datatooltk.getLabelWithValues(label, values);

      return datatooltk.getLabelWithValues("error.line",
       lineNumber, msg);
   }

   public String getToolTip(String label)
   {
      return datatooltk.getToolTip(label);
   }

   public String getToolTip(String parent, String label)
   {
      return datatooltk.getToolTip(parent, label);
   }

   public char getMnemonic(String label)
   {
      return datatooltk.getMnemonic(label);
   }

   public char getMnemonic(String parent, String label)
   {
      return datatooltk.getMnemonic(parent, label);
   }

   public int getMnemonicInt(String label)
   {
      return datatooltk.getMnemonicInt(label);
   }

   public int getMnemonicInt(String parent, String label)
   {
      return datatooltk.getMnemonicInt(parent, label);
   }

   public DatatoolTk getDatatoolTk()
   {
      return datatooltk;
   }

   public DatatoolGuiResources getDatatoolGuiResources()
   {
      return guiResources;
   }

   public void setDatatoolGuiResources(DatatoolGuiResources guiResources)
   {
      this.guiResources = guiResources;
   }

   public DatatoolSettings getSettings()
   {
      return datatooltk.getSettings();
   }

   private boolean isBatchMode, debugMode=false;
   private DatatoolTk datatooltk;
   private DatatoolGuiResources guiResources;

   private TeXApp texApp;

   private StringBuffer errorBuffer, warningBuffer;

   private Component bufferingComponent=null;

   public static final int SYNTAX_FAILURE=7;
   public static final int RUNTIME_FAILURE=8;
}
