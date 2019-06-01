package da.gammla;

import da.gammla.AnchoredTable.AnchoredTable;
import javazoom.jl.converter.Converter;
import javazoom.jl.decoder.JavaLayerException;
import org.jnativehook.keyboard.NativeKeyEvent;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.event.ListDataListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

public class Gui extends JFrame {
    private JPanel panel;
    public JTable sounds_table;
    private JButton play_button;
    private JComboBox mixers_combo_box;
    private JButton edit_button;
    private JButton add_button;
    private JButton stop_all_button;
    private JButton up_button;
    private JButton down_button;
    private JButton remove_button;
    private JTabbedPane tabbedPane1;
    private JComboBox skin_box;
    private DefaultTableModel table_model;

    public AnchoredTable sounds;
    public AnchoredTable settings;
    public AnchoredTable hotkeys;
    public AnchoredTable gain;

    private ArrayList<Mixer.Info> mixers;

    private String sounds_xml_path = FileSystemView.getFileSystemView().getDefaultDirectory().getPath() + "/DaGammla/Soundboard/sounds.xml";

    public ArrayList<Clip> clips = new ArrayList<Clip>();

    public Edit edit;

    Gui this_is = this;

    static int click_interval = (Integer)Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval");

    int last_selected_row = -1;
    long last_time_clicked = -1;

    ActionListener play = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {

            try {
                Clip clip = AudioSystem.getClip(mixers.get(mixers_combo_box.getSelectedIndex()));
                File file = new File(sounds.getDataAt(sounds_table.getSelectedRow()));
                AudioInputStream in = AudioSystem.getAudioInputStream(file);
                clip.open(in);

                if (gain.hasData(sounds.getAnchorAt(sounds_table.getSelectedRow()))) {
                    FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    gainControl.setValue((Integer.parseInt(gain.getData(sounds.getAnchorAt(sounds_table.getSelectedRow()))) * 6.0f) / 15.0f);

                }

                clip.start();
                clips.add(clip);
            }
            catch (LineUnavailableException e1) {
                try {
                    Clip clip = AudioSystem.getClip(mixers.get(mixers_combo_box.getSelectedIndex()));
                    File file = new File(sounds.getDataAt(sounds_table.getSelectedRow()));
                    AudioInputStream in = AudioSystem.getAudioInputStream(file);
                    AudioFormat format = new AudioFormat(44100,16,1,true,true);
                    AudioInputStream convert = AudioSystem.getAudioInputStream(format,in);
                    clip.open(convert);
                    clip.start();
                    clips.add(clip);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }};

    Gui(){
        super("Da Soundboard");

        try {
            sounds = new AnchoredTable(new File(sounds_xml_path));
        } catch (Exception e) {
            sounds = new AnchoredTable("Sounds");
        }

        try {
            settings = new AnchoredTable(new File(FileSystemView.getFileSystemView().getDefaultDirectory().getPath() + "/DaGammla/Soundboard/settings.xml"));
        } catch (Exception e) {
            settings = new AnchoredTable("Settings");
        }

        try {
            hotkeys = new AnchoredTable(new File(FileSystemView.getFileSystemView().getDefaultDirectory().getPath() + "/DaGammla/Soundboard/hotkeys.xml"));
        } catch (Exception e) {
            hotkeys = new AnchoredTable("Hotkeys");
        }

        try {
            gain = new AnchoredTable(new File(FileSystemView.getFileSystemView().getDefaultDirectory().getPath() + "/DaGammla/Soundboard/gain.xml"));
        } catch (Exception e) {
            gain = new AnchoredTable("Gain");
        }

        cloneSoundsFromAnchoredTable(0);

        mixers = filterMixerDevices();
        setMixersModel();



        if (settings.hasData("Mixer")){
            for (int i = 0; i < mixers.size(); i++) {
                if (mixers.get(i).getName().equals(settings.getData("Mixer"))) {
                    mixers_combo_box.setSelectedIndex(i);
                    break;
                } else {
                    mixers_combo_box.setSelectedIndex(0);
                }
            }
        } else {
            mixers_combo_box.setSelectedIndex(0);
        }

        skin_box.addItem("Default");
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            skin_box.addItem(info.getName());
        }

        if (settings.hasData("Skin")){
            for (int i = 0; i < mixers.size(); i++) {
                if (settings.getData("Skin").equals(skin_box.getItemAt(i))) {
                    skin_box.setSelectedIndex(i);
                    break;
                } else {
                    skin_box.setSelectedIndex(0);
                }
            }
        } else {
            skin_box.setSelectedIndex(0);
        }

        add_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();

                fc.setCurrentDirectory(new File(FileSystemView.getFileSystemView().getDefaultDirectory().getPath()));
                fc.setDialogTitle("Sound selection");
                fc.setFileFilter(new FileNameExtensionFilter(".wav files (.mp3 will be converted first)", "wav", "mp3"));
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setMultiSelectionEnabled(true);
                fc.showOpenDialog(this_is);
                for (File file:fc.getSelectedFiles()) {
                    if (file.getPath().endsWith(".wav") || file.getPath().endsWith(".mp3"))
                        addSound(file.getName().substring(0, file.getName().length()-4), file.getPath());
                }
                if (fc.getSelectedFiles().length >= 1){
                    String last_filename = fc.getSelectedFiles()[fc.getSelectedFiles().length-1].getName();
                    cloneSoundsFromAnchoredTable(
                        sounds.getAnchorPosition(last_filename.substring(0, last_filename.length()-4)));
                    if (fc.getSelectedFiles().length == 1) {
                        edit = new Edit(Main.gui, sounds.getAnchorAt(sounds_table.getSelectedRow()));
                        setEnabled(false);
                    }
                }

            }
        });

        remove_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int save_selected_row = sounds_table.getSelectedRow();
                sounds.removeDataAt(sounds_table.getSelectedRow());
                cloneSoundsFromAnchoredTable(save_selected_row == sounds_table.getRowCount() - 1 ? save_selected_row - 1 : save_selected_row);
            }
        });

        edit_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                edit = new Edit(Main.gui, sounds.getAnchorAt(sounds_table.getSelectedRow()));
                setEnabled(false);
            }
        });

        play_button.addActionListener(play);

        stop_all_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                while (!clips.isEmpty()){
                    clips.get(0).stop();
                    clips.get(0).flush();
                    clips.get(0).close();
                    clips.remove(clips.get(0));
                }
            }
        });

        up_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (sounds_table.getSelectedRow() >= 1){
                    sounds.switchAnchors(sounds.getAnchorAt(sounds_table.getSelectedRow()), sounds.getAnchorAt(sounds_table.getSelectedRow() - 1));
                    cloneSoundsFromAnchoredTable(sounds_table.getSelectedRow() - 1);
                }
            }
        });

        down_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (sounds_table.getSelectedRow() <= sounds_table.getRowCount() - 2){
                    sounds.switchAnchors(sounds.getAnchorAt(sounds_table.getSelectedRow()), sounds.getAnchorAt(sounds_table.getSelectedRow() + 1));
                    cloneSoundsFromAnchoredTable(sounds_table.getSelectedRow() + 1);
                }
            }
        });

        skin_box.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setLookAndFeel();
                settings.setData("Skin", (String) skin_box.getSelectedItem());
            }
        });

        setContentPane(panel);
        pack();
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        setLocationRelativeTo(null);

        setDefaultLookAndFeelDecorated(true);
        setResizable(true);


        setVisible(true);

        pack();
    }

    private void createUIComponents() {
        table_model = new DefaultTableModel(new String[]{"Sounds", "Hotkey"}, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        sounds_table = new JTable(table_model);
        sounds_table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = sounds_table.rowAtPoint(evt.getPoint());
                if (last_selected_row == row && System.currentTimeMillis() - click_interval <= last_time_clicked){
                    play.actionPerformed(null);
                }
                last_time_clicked = System.currentTimeMillis();
                last_selected_row = row;
            }
        });

        sounds_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    Line.Info filter = new Line.Info(SourceDataLine.class);

    private ArrayList<Mixer.Info> filterMixerDevices() {
        ArrayList<Mixer.Info> result = new ArrayList<>();
        for (Mixer.Info info : AudioSystem.getMixerInfo()) {
            Mixer mixer = AudioSystem.getMixer(info);
            if (mixer.isLineSupported(filter)) {
                result.add(info);
            }
        }
        return result;
    }

    private void setMixersModel(){
        mixers_combo_box.setModel(new ComboBoxModel() {

            String select_name = "";

            @Override
            public void setSelectedItem(Object anItem) {
                select_name = (String) anItem;
                settings.setData("Mixer", select_name);
            }

            @Override
            public Object getSelectedItem() {
                return select_name;
            }

            @Override
            public int getSize() {
                return mixers.size();
            }

            @Override
            public Object getElementAt(int index) {
                return mixers.get(index).getName();
            }

            @Override
            public void addListDataListener(ListDataListener l) {

            }

            @Override
            public void removeListDataListener(ListDataListener l) {

            }
        });
    }

    private void addSound(String name, String path){
        if (path.endsWith(".mp3")){
            Converter converter = new Converter();
            String new_path = path.substring(0, path.length()-4) + ".wav";
            try {
                converter.convert(path, new_path);
            } catch (JavaLayerException e) {
                e.printStackTrace();
            }
            sounds.setData(name, new_path);

        } else sounds.setData(name, path);
    }
    public void cloneSoundsFromAnchoredTable(int selected_row){

        table_model.setRowCount(0);

        if (!sounds.isEmpty())
            for (int i = 0; i < sounds.getSize(); i++) {
                table_model.addRow(new String[]{sounds.getAnchorAt(i)});
                if (hotkeys.hasData(sounds.getAnchorAt(i)))
                    table_model.setValueAt(NativeKeyEvent.getKeyText(Integer.parseInt(hotkeys.getData(sounds.getAnchorAt(i)))), i, 1);
            }

        sounds.saveToXML(sounds_xml_path);
        hotkeys.saveToXML(FileSystemView.getFileSystemView().getDefaultDirectory().getPath() + "/DaGammla/Soundboard/hotkeys.xml");

        sounds_table.changeSelection(selected_row, 0, false, false);
    }

    public void buttonPressed(int button){
        if (isEnabled() && !hotkeys.isEmpty()) {
            for (int i = 0; i < hotkeys.getSize(); i++) {
                if (Integer.parseInt(hotkeys.getDataAt(i)) == button) {
                    sounds_table.changeSelection(sounds.getAnchorPosition(hotkeys.getAnchorAt(i)), 0, false, false);
                    play.actionPerformed(null);
                }
            }
        } else if (edit != null && edit.isVisible() && edit.isEnabled() && edit.is_key_requested) {
            edit.receiveButton(button);
        }

    }

    void setLookAndFeel(){
        if (skin_box.getSelectedIndex() == 0) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                UIManager.setLookAndFeel(UIManager.getInstalledLookAndFeels()[skin_box.getSelectedIndex() - 1].getClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        SwingUtilities.updateComponentTreeUI(this);
        pack();
    }
}
