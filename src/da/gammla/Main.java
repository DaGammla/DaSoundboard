package da.gammla;

import da.gammla.AnchoredTable.AnchoredTable;
import org.jnativehook.GlobalScreen;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static Gui gui;
    public static boolean isKeyRequested = false;

    public static void main(String[] args){
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        try {
            GlobalScreen.registerNativeHook();
        } catch (Throwable e) {
            JOptionPane.showMessageDialog(null, e.toString(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }

        GlobalScreen.addNativeKeyListener(new GlobalKeyListener());


        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            AnchoredTable settings = new AnchoredTable(new File(FileSystemView.getFileSystemView().getDefaultDirectory().getPath() + "/DaGammla/Soundboard/settings.xml"));

            if (settings.hasData("Skin")) {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if (settings.getData("Skin").equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        gui = new Gui();
        while (true) {
            if (!gui.isVisible()) {
                gui.settings.saveToXML(FileSystemView.getFileSystemView().getDefaultDirectory().getPath() + "/DaGammla/Soundboard/settings.xml");
                gui.hotkeys.saveToXML(FileSystemView.getFileSystemView().getDefaultDirectory().getPath() + "/DaGammla/Soundboard/hotkeys.xml");
                gui.gain.saveToXML(FileSystemView.getFileSystemView().getDefaultDirectory().getPath() + "/DaGammla/Soundboard/gain.xml");
                gui.sounds.saveToXML(FileSystemView.getFileSystemView().getDefaultDirectory().getPath() + "/DaGammla/Soundboard/sounds.xml");
                System.exit(0);
            } else {
                for (int i = 0; i < gui.clips.size(); i++) {
                    if (!gui.clips.get(i).isRunning()) {
                        gui.clips.get(i).stop();
                        gui.clips.get(i).flush();
                        gui.clips.get(i).close();
                        gui.clips.remove(gui.clips.get(i));
                        i--;
                    }
                }
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
