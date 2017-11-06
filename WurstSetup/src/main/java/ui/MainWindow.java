package ui;

import file.GlobalWurstConfig;
import file.WurstProjectConfig;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import tablelayout.Table;
import workers.CompilerUpdateWorker;
import workers.ProjectCreateWorker;
import workers.ProjectUpdateWorker;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.awt.GridBagConstraints.NORTHWEST;
import static java.awt.GridBagConstraints.VERTICAL;

public class MainWindow extends JFrame {
    public final UI ui;
    private JFileChooser saveChooser;
    private JSystemFileChooser importChooser;
    private static final Pattern projNamePattern = Pattern.compile("(\\w|\\s)+");
    static Point point = new Point();

    /**
     * Create the frame.
     */
    public MainWindow() {
        setLayout(new BorderLayout());
        setSize(570, 360);
        setBackground(new Color(36, 36, 36));
        centerWindow();
        setUndecorated(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        initChooser();
        ui = new UI();
        add(ui, BorderLayout.CENTER);
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                point.x = e.getX();
                point.y = e.getY();
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point p = getLocation();
                setLocation(p.x + e.getX() - point.x, p.y + e.getY() - point.y);
            }
        });
        setVisible(true);
    }

    class UI extends JPanel {
        private Table contentTable;
        private JPanel topBar = new JPanel();
        private JPanel title = new JPanel();
        private JLabel windowLabel = new JLabel("    Wurst Setup");
        public JLabel lblWelcome;
        public JLabel lblNote;
        public JLabel lblVersions;
        public JProgressBar progressBar;
        public SetupButton btnCreate;
        public SetupButton btnUpdate;
        public JTextArea jTextArea = new JTextArea("Ready.\n");
        public JTextField projectRootTF;
        private JButton exit;
        private JButton minimize;
        private JTextField gamePathTF;
        private JTextField dependencyTF;
        private List<String> dependencies;
        private JTextField projectNameTF;

        private WurstProjectConfig selectedConfig = null;

        public UI() {
            // Add default stdlib
            dependencies = new ArrayList<>();
            dependencies.add("https://github.com/wurstscript/wurstStdlib2");
            initComponents();
        }

        private void initComponents() {
            setTitle("Wurst Setup");
            setBackground(new Color(36, 36, 36));

            setupTopBar();
            topBar.setBackground(new Color(64, 67, 69));
            title.setBackground(new Color(94, 97, 99));
            windowLabel.setForeground(new Color(255, 255, 255));
            contentTable = new Table();
            contentTable.setSize(570, 360);

            getContentPane().add(contentTable);

            contentTable.top();
            contentTable.row().height(26);
            Table titleTable = new Table();
            titleTable.addCell(topBar).growX().height(26);
            titleTable.addCell(title).size(100, 26);
            contentTable.addCell(titleTable).growX();

            contentTable.row();

            lblWelcome = new JLabel("Welcome to the Wurst Setup");
            lblWelcome.setHorizontalAlignment(SwingConstants.CENTER);
            lblWelcome.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
            contentTable.addCell(lblWelcome).center().pad(2);

            contentTable.row();

            lblNote = new JLabel("Checking for updates..");
            lblVersions = new JLabel("(check again)");
            lblVersions.setCursor(new Cursor(Cursor.HAND_CURSOR));
            lblVersions.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Init.print("\nchecking for updates..");
                    Init.init(true, null);
                    Init.print("done\n");
                }
            });
            Table noteTable = new Table();
            noteTable.addCell(lblNote);
            noteTable.addCell(lblVersions).padLeft(12);
            contentTable.addCell(noteTable).pad(2).growX();

            contentTable.row();

            createConfigTable();

            contentTable.row();

            jTextArea.setBackground(new Color(46, 46, 46));
            jTextArea.setForeground(new Color(255, 255, 255));
            jTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            DefaultCaret caret = (DefaultCaret) jTextArea.getCaret();
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
            caret.setSelectionVisible(true);
            jTextArea.setEditable(false);
            jTextArea.setMargin(new Insets(2, 2, 2, 2));
            JScrollPane scrollPane = new JScrollPane(jTextArea);
            contentTable.addCell(scrollPane).height(120).growX().pad(2);
            Border line = BorderFactory.createLineBorder(Color.DARK_GRAY);
            scrollPane.setBorder(line);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            contentTable.row();

            progressBar = new JProgressBar();
            contentTable.addCell(progressBar).growX().pad(2);

            contentTable.row();

            createButtonTable();
            setStyle(contentTable);


            refreshComponents();
        }

        private void setupTopBar() {
            try {
                BufferedImage exitimg = ImageIO.read(MainWindow.class.getResourceAsStream("/exitup.png"));
                BufferedImage minimg = ImageIO.read(MainWindow.class.getResourceAsStream("/minimizeup.png"));
                BufferedImage exitimgdown = ImageIO.read(MainWindow.class.getResourceAsStream("/exitdown.png"));
                BufferedImage minimgdown = ImageIO.read(MainWindow.class.getResourceAsStream("/minimizedown.png"));
                BufferedImage exithover = ImageIO.read(MainWindow.class.getResourceAsStream("/exithover.png"));
                BufferedImage minimghover = ImageIO.read(MainWindow.class.getResourceAsStream("/minimizehover.png"));

                ImageIcon exitIcon = new ImageIcon(exitimg);
                ImageIcon minIcon = new ImageIcon(minimg);
                ImageIcon exitIconDown = new ImageIcon(exitimgdown);
                ImageIcon minIconDown = new ImageIcon(minimgdown);
                ImageIcon exitIconHover = new ImageIcon(exithover);
                ImageIcon minIconHover = new ImageIcon(minimghover);
                exit = new JButton(exitIcon);
                minimize = new JButton(minIcon);

                exit.setOpaque(false);
                exit.setContentAreaFilled(false);
                exit.setFocusPainted(false);
                exit.setBorderPainted(false);
                exit.setPressedIcon(exitIconDown);
                exit.setRolloverIcon(exitIconHover);

                minimize.setOpaque(false);
                minimize.setContentAreaFilled(false);
                minimize.setFocusPainted(false);
                minimize.setBorderPainted(false);
                minimize.setPressedIcon(minIconDown);
                minimize.setRolloverIcon(minIconHover);
            } catch (IOException e) {
                e.printStackTrace();
            }
            title.setLayout(new GridLayout(1, 2));

            minimize.setSize(new Dimension(50, 26));
            exit.setSize(new Dimension(50, 26));

            title.add(minimize);
            title.add(exit);
            topBar.setLayout(new GridLayout(1, 1));
            topBar.add(windowLabel, new GridBagConstraints(0, 0, 0, 0, 0, 0, NORTHWEST, VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
            titleEvents(minimize, exit);
        }

        private void setStyle(Table table) {
            for (int i = 0; i < table.getComponents().length; i++) {
                Component component = table.getComponents()[i];
                if (component instanceof JTextField) {
                    component.setBackground(new Color(46, 46, 46));
                    component.setForeground(new Color(255, 255, 255));
                    Border line = BorderFactory.createEtchedBorder();
                    Border pad = new EmptyBorder(0, 5, 0, 0);
                    CompoundBorder compoundBorder = new CompoundBorder(line, pad);
                    ((JComponent) component).setBorder(compoundBorder);
                } else if (component instanceof Table) {
                    setStyle((Table) component);
                }
                if (component instanceof JLabel) {
                    component.setForeground(Color.WHITE);
                }
            }
        }

        private void titleEvents(JButton minimize, JButton exit) {
            minimize.addActionListener(e -> setState(ICONIFIED));
            exit.addActionListener(e -> {
                dispose();
                System.exit(0);
            });
        }

        private void createConfigTable() {
            UI that = this;
            Table configTable = new Table();
            configTable.row().height(24);
            configTable.addCell(new JLabel("Project name:")).left();

            Table projectInputTable = new Table();
            projectInputTable.row().height(24);
            projectNameTF = new JTextField("MyWurstProject");
            projectNameTF.getDocument().addDocumentListener(new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                    warn();
                }

                public void removeUpdate(DocumentEvent e) {
                    warn();
                }

                public void insertUpdate(DocumentEvent e) {
                    warn();
                }

                public void warn() {
                    if (projectNameTF.getText().length() > 0 && !projNamePattern.matcher(projectNameTF.getText()).matches()) {
                        JOptionPane.showMessageDialog(null,
                                "Error: Please enter valid project name", "Error Massage",
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        if (projectNameTF.getText().length() == 0) {
                            btnCreate.setEnabled(false);
                        } else {
                            projectRootTF.setText(saveChooser.getCurrentDirectory().getAbsolutePath() + File.separator + projectNameTF.getText());
                            btnCreate.setEnabled(true);
                        }
                    }
                }
            });
            projectInputTable.addCell(projectNameTF).growX();
            SetupButton importButton = new SetupButton("Import");
            importButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent arg0) {
                    if (importButton.isEnabled() && !progressBar.isIndeterminate()) {
                        if (importChooser.showOpenDialog(that) == JFileChooser.APPROVE_OPTION) {
                            try {
                                WurstProjectConfig config = WurstProjectConfig.loadProject(importChooser.getSelectedFile());
                                if (config != null) {
                                    projectNameTF.setText(config.getProjectName());
                                    projectRootTF.setText(config.getProjectRoot().toString());
                                    dependencies = config.dependencies;
                                    dependencyTF.setText(dependencies.stream().map(i -> i.substring(i.lastIndexOf("/") + 1)).collect(Collectors.joining(", ")));
                                    btnCreate.setText("Update Project");
                                    selectedConfig = config;
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
            projectInputTable.addCell(importButton);
            configTable.addCell(projectInputTable).growX();

            configTable.row().height(24).padTop(2);

            configTable.addCell(new JLabel("Project root:")).left();

            Table projectTF = new Table();
            projectRootTF = new JTextField(saveChooser.getCurrentDirectory().getAbsolutePath() + File.separator + projectNameTF.getText());
            projectRootTF.setEditable(false);
            projectTF.row().height(24);
            projectTF.addCell(projectRootTF).growX();
            SetupButton selectProjectRoot = new SetupButton("...");

            selectProjectRoot.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent arg0) {
                    if (saveChooser.showSaveDialog(that) == JFileChooser.APPROVE_OPTION) {
                        projectRootTF.setText(saveChooser.getSelectedFile().getAbsolutePath() + File.separator + projectNameTF.getText());
                    }
                }
            });
            projectTF.addCell(selectProjectRoot).pad(0, 2, 0, 2);

            configTable.addCell(projectTF).growX();

            configTable.row().height(24).padTop(2);

            configTable.addCell(new JLabel("Game path:")).left();

            Table gameTF = new Table();
            gamePathTF = new JTextField("Select your wc3 installation folder (optional)");
            gamePathTF.setEditable(false);
            gameTF.addCell(gamePathTF).height(24).growX();
            SetupButton selectGamePath = new SetupButton("...");
            selectGamePath.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent arg0) {
                    if (saveChooser.showSaveDialog(that) == JFileChooser.APPROVE_OPTION) {
                        gamePathTF.setText(saveChooser.getSelectedFile().getAbsolutePath());
                    }
                }
            });
            if (System.getProperty("os.name").startsWith("Windows")) {
                try {
                    String wc3Path = WinRegistry.readString(WinRegistry.HKEY_CURRENT_USER, "SOFTWARE\\Blizzard Entertainment\\Warcraft III", "InstallPath");
                    if (wc3Path != null) {
                        if (!wc3Path.endsWith(File.separator)) wc3Path = wc3Path + File.separator;
                        File gameFolder = new File(wc3Path);
                        if (gameFolder.exists()) {
                            gamePathTF.setText(wc3Path);
                        }
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            gameTF.addCell(selectGamePath).height(24).pad(0, 2, 0, 2);

            configTable.addCell(gameTF).growX();
            configTable.row().height(24).padTop(2);

            configTable.addCell(new JLabel("Dependencies:")).left();

            Table dependencyTable = new Table();
            dependencyTF = new JTextField("wurstStdlib2");
            dependencyTF.setEditable(false);
            dependencyTable.addCell(dependencyTF).height(24).growX();
            SetupButton manageDependencies = new SetupButton("Advanced");
            manageDependencies.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent arg0) {
                    String url = JOptionPane.showInputDialog("Enter git remote addres (https://github.com/user/project)");
                    if (url != null && url.length() > 0) {
                        if (dependencies.contains(url)) {
                            Init.print("This git repo is already added");
                            return;
                        }
                        Init.print("Checking git repo..");
                        try {
                            Collection<Ref> result = Git.lsRemoteRepository()
                                    .setRemote(url)
                                    .call();
                            if (!result.isEmpty()) {
                                Init.print("valid!\n");
                                dependencies.add(url);
                                dependencyTF.setText(dependencies.stream().map(i -> i.substring(i.lastIndexOf("/") + 1)).collect(Collectors.joining(", ")));
                            } else {
                                Init.print("Error: Entered invalid git repo\n");
                            }
                        } catch (Exception e) {
                            Init.print("Error: Entered invalid git repo\n");
                            e.printStackTrace();
                        }
                    }
                }
            });
            dependencyTable.addCell(manageDependencies).height(24).pad(0, 2, 0, 2);

            configTable.addCell(dependencyTable).growX();

            contentTable.addCell(configTable).growX().pad(2);
        }

        public void refreshComponents() {
            progressBar.setIndeterminate(false);
            if (GlobalWurstConfig.isFreshInstall()) {
                lblNote.setText("No WurstScript detected. Please first install WurstScript.");
                btnCreate.setEnabled(false);
                btnUpdate.setText("Install WurstScript");
                btnUpdate.setEnabled(true);
            } else if (GlobalWurstConfig.updateAvailable) {
                lblNote.setText("There is an update available!");
                lblNote.setForeground(new Color(25, 125, 125));
                btnUpdate.setText("Update Compiler");
                btnUpdate.setEnabled(true);
                btnCreate.setEnabled(true);
            } else {
                lblNote.setText("WurstScript is up to date!");
                btnUpdate.setText("Compiler up to date");
                btnUpdate.setEnabled(false);
                btnCreate.setEnabled(true);
            }
        }

        private void createButtonTable() {
            Table buttonTable = new Table();
            buttonTable.setSize(420, 90);
            btnUpdate = new SetupButton("Install WurstScript");
            if (!GlobalWurstConfig.isFreshInstall()) {
                btnUpdate.setEnabled(false);
            }
            btnUpdate.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent arg0) {
                    if (btnUpdate.isEnabled() && !progressBar.isIndeterminate()) {
                        handleWurstUpdate();
                    }
                }
            });
            buttonTable.addCell(btnUpdate);
            buttonTable.addCell().growX();
            btnCreate = new SetupButton("Create Project");
            btnCreate.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent arg0) {
                    if (btnCreate.isEnabled() && !progressBar.isIndeterminate()) {
                        progressBar.setIndeterminate(true);
                        btnUpdate.setEnabled(false);
                        if (selectedConfig == null) {
                            try {
                                selectedConfig = WurstProjectConfig.loadProject(new File(projectRootTF.getText(), "wurst.build"));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (selectedConfig != null) {
                            handleUpdateProject();
                        } else {
                            handleCreateProject();
                        }
                    }
                }
            });
            buttonTable.addCell(btnCreate);
            contentTable.addCell(buttonTable).growX().pad(2);
        }

        private void handleUpdateProject() {
            File file = new File(gamePathTF.getText());
            if (file.exists()) {
                selectedConfig.setGameRoot(file);
            }
            new ProjectUpdateWorker(selectedConfig).execute();
        }

        private void handleCreateProject() {
            btnCreate.setEnabled(false);
            progressBar.setIndeterminate(true);
            String gamePath = gamePathTF.getText();
            File projectRoot = new File(projectRootTF.getText());
            File gameRoot = (gamePath != null && gamePath.length() > 0) ? new File(gamePath) : null;
            WurstProjectConfig config = new WurstProjectConfig(projectRoot, gameRoot, projectNameTF.getText());
            for (String dep : dependencies) {
                config.addDependency(dep);
            }
            new ProjectCreateWorker(config).execute();
        }

        private void handleWurstUpdate() {
            btnUpdate.setEnabled(false);
            progressBar.setIndeterminate(true);
            new CompilerUpdateWorker().execute();
        }
    }

    private void centerWindow() {
        Rectangle screenBounds = getGraphicsConfiguration().getBounds();

        int center_x = screenBounds.x + screenBounds.width / 2;
        int center_y = screenBounds.y + screenBounds.height / 2;

        setLocation(center_x - getWidth() / 2, center_y - getHeight() / 2);
    }

    private void initChooser() {
        saveChooser = new JSystemFileChooser();
        saveChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        saveChooser.setCurrentDirectory(new java.io.File("."));
        saveChooser.setDialogTitle("Select project root");
        saveChooser.setAcceptAllFileFilterUsed(false);

        importChooser = new JSystemFileChooser();
        importChooser.setCurrentDirectory(new java.io.File("."));
        importChooser.setDialogTitle("Select wurst.build file");
        importChooser.setAcceptAllFileFilterUsed(false);
        importChooser.setFileFilter(new FileNameExtensionFilter("wurst.build files", "build"));
    }

    public static class SetupButton extends JButton {
        private Color textColor = Color.WHITE;
        private Color backgroundColor = new Color(18, 18, 18);
        private Color overColor = new Color(120, 20, 20);
        private Color pressedColor = new Color(240, 40, 40);

        SetupButton(String buttonTag) {
            super(buttonTag);
            setBackground(backgroundColor);
            setForeground(textColor);
            setContentAreaFilled(false);
            setFocusPainted(false);

            Border line = BorderFactory.createLineBorder(new Color(80, 80, 80));
            Border empty = new EmptyBorder(4, 4, 4, 4);
            CompoundBorder border = new CompoundBorder(line, empty);
            setBorder(border);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (getModel().isPressed()) {
                g.setColor(pressedColor);
            } else if (getModel().isRollover()) {
                g.setColor(overColor);
            } else {
                g.setColor(getBackground());
            }
            g.fillRect(0, 0, getWidth(), getHeight());
            super.paintComponent(g);
        }
    }
}
