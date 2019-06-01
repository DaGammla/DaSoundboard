package da.gammla;

import org.jnativehook.keyboard.NativeKeyEvent;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class Edit extends JFrame {
    private JTextField name_field;
    private JTextField path_field;
    private JButton open_file_chooser_button;
    private JButton cancel_button;
    private JButton apply_button;
    private JButton hotkey_button;
    private JButton delete_sound_button;
    private JPanel panel;
    private JSlider decibel_slider;
    private JLabel decibel_text;

    Gui gui;

    Edit self = this;

    public boolean is_key_requested = false;

    boolean exit_by_button = false;

    int hotkey = -1;

    Edit(Gui gui, String anchor) {
        super("Da Soundboard");

        name_field.setText(anchor);
        path_field.setText(gui.sounds.getData(anchor));
        if (gui.hotkeys.hasData(anchor)) {
            hotkey = Integer.parseInt(gui.hotkeys.getData(anchor));
            hotkey_button.setText(NativeKeyEvent.getKeyText(hotkey));
        } else hotkey_button.setText("No hotkey");
        if (gui.gain.hasData(anchor))
            decibel_slider.setValue(Integer.parseInt(gui.gain.getData(anchor)));

        this.gui = gui;

        setContentPane(panel);

        pack();

        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        WindowListener exitListener = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                gui.setEnabled(true);
                if (!exit_by_button) {
                    int answer = JOptionPane.showOptionDialog(self, "Do you want to\nsave the changes?", "Save changes", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
                    if (answer == 0) {
                        if (gui.sounds.hasData(name_field.getText()) && !name_field.getText().equals(anchor)) {
                            JOptionPane.showMessageDialog(self, "This name is already in use!", "Name unavailable", JOptionPane.ERROR_MESSAGE);
                        } else {
                            gui.sounds.setData(anchor, path_field.getText());
                            gui.sounds.setAnchorAt(gui.sounds.getAnchorPosition(anchor), name_field.getText());
                            gui.gain.setData(anchor, "" + decibel_slider.getValue());
                            gui.hotkeys.removeData(anchor);
                            if (hotkey != -1)
                                gui.hotkeys.setData(name_field.getText(), String.valueOf(hotkey));
                            gui.cloneSoundsFromAnchoredTable(gui.sounds_table.getSelectedRow());
                        }
                    }
                }
            }
        };

        addWindowListener(exitListener);

        decibel_text.setText((((decibel_slider.getValue() * 6.0) / 15.0) >= 0.0 ? "+" : "") + Math.round(((decibel_slider.getValue() * 6.0) / 15.0) * 10.0) / 10.0 + " dBA");

        open_file_chooser_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();

                fc.setCurrentDirectory(new File(gui.sounds.getData(anchor)));
                fc.setDialogTitle("Sound selection");
                fc.setFileFilter(new FileNameExtensionFilter(".wav files", "wav"));
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.showOpenDialog(self);
                if (fc.getSelectedFile() != null)
                    path_field.setText(fc.getSelectedFile().getPath());
            }
        });

        cancel_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exit_by_button = true;
                self.dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
            }
        });

        apply_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gui.sounds.hasData(name_field.getText()) && !name_field.getText().equals(anchor)){
                    JOptionPane.showMessageDialog(self, "This name is already in use!", "Name unavailable", JOptionPane.ERROR_MESSAGE);
                } else {
                    exit_by_button = true;
                    gui.sounds.setData(anchor, path_field.getText());
                    gui.sounds.setAnchorAt(gui.sounds.getAnchorPosition(anchor), name_field.getText());
                    gui.gain.setData(anchor, "" + decibel_slider.getValue());
                    if (decibel_slider.getValue() == 0)
                        gui.gain.removeData(anchor);
                    gui.hotkeys.removeData(anchor);
                    if (hotkey != -1)
                        gui.hotkeys.setData(name_field.getText(), String.valueOf(hotkey));
                    gui.cloneSoundsFromAnchoredTable(gui.sounds_table.getSelectedRow());
                    self.dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
                }
            }
        });

        delete_sound_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exit_by_button = true;
                gui.sounds.removeData(anchor);
                gui.cloneSoundsFromAnchoredTable(gui.sounds_table.getSelectedRow());
                self.dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
            }
        });

        hotkey_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.requestFocus();
                if (!is_key_requested){
                    hotkey = -1;
                    is_key_requested = true;
                    hotkey_button.setText("Waiting for key...");
                    hotkey_button.setBackground(new Color(0x22D727));
                    hotkey_button.setForeground(new Color(0xFF0000));
                } else {
                    hotkey = -1;
                    is_key_requested = false;
                    hotkey_button.setText("No hotkey");
                    hotkey_button.setBackground(panel.getBackground());
                    hotkey_button.setForeground(new Color(0x000000));
                }
            }
        });

        decibel_slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                decibel_text.setText((((decibel_slider.getValue() * 6.0) / 15.0) >= 0.0 ? "+" : "") + Math.round(((decibel_slider.getValue() * 6.0) / 15.0) * 10.0) / 10.0 + " dBA");
            }
        });


        Point gui_mid = new Point(gui.getLocation().x + gui.getWidth() / 2, gui.getLocation().y + gui.getHeight() / 2);

        setLocation(gui_mid.x - getWidth() / 2, gui_mid.y - getHeight() / 2);

        setDefaultLookAndFeelDecorated(true);
        setResizable(true);

        setVisible(true);

        pack();
    }

    public void receiveButton(int button){
        hotkey = button;
        is_key_requested = false;
        hotkey_button.setText(NativeKeyEvent.getKeyText(button));
        hotkey_button.setBackground(panel.getBackground());
        hotkey_button.setForeground(new Color(0x000000));
    }

}
