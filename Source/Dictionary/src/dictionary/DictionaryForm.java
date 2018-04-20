/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dictionary;

import java.awt.Image;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.input.KeyCode;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Gokki
 */
public class DictionaryForm extends javax.swing.JFrame implements Serializable {

    // MAp dùng cho từ điển Việt - Anh
    private static TreeMap<String, String> DictionarisMapVA = null;
    private static DefaultListModel<String> dlmVA = null; // model Việt - Anh

    //Map dùng chung cho dữ liệu
    private static TreeMap<String, String> DictionarisMap = null;

    // map dùng cho từ điển Anh - Việt
    private static TreeMap<String, String> DictionarisMapAV = null;
    private static DefaultListModel<String> dlmAV = null; // model Anh - Việt

    // map dùng để lưu từ yêu thích
    private static TreeMap<String, String> LoveDictionarisMap = null;

    //map dùng để lưu từ đã tìm kiếm, dùng cho việc thống kê
    private static TreeMap<String, InforSearchedWord> SearchedDictionarisMap = null;

    //tên file chứa danh sách từ yêu thích
    private static final String filename = "Loveword.dat";
    private static final String searched_filename = "Searchedword.dat";

    public DictionaryForm() {

        initComponents();
        setLocationRelativeTo(null);

        //tạo mới 2 model cho 2 loại từ điển V-A và A-V
        dlmVA = new DefaultListModel<>();
        dlmAV = new DefaultListModel<>();

        try {
            loadContentDictionary();
            LoadLoveWordFromFile();
            LoadSearchedWord();
            DictionarisMap = new TreeMap<>(DictionarisMapAV);
            lstWord.setModel(dlmAV);
            lstWord.repaint();
            txaMeaning.setText(DictionarisMap.get("(logic học)"));
            btnSearch.requestFocus();
            lstWord.setSelectedIndex(0);
        } catch (SAXException | ParserConfigurationException | IOException ex) {
            JOptionPane.showMessageDialog(this, "Không load được dữ liệu");
        }

    }

    public void LoadSearchedWord() {
        ObjectInputStream ois = null;

        try {
            ois = new ObjectInputStream(new FileInputStream(searched_filename));
            SearchedDictionarisMap = new TreeMap<>();
            if (ois != null) {
                try {
                    SearchedDictionarisMap = (TreeMap<String, InforSearchedWord>) ois.readObject();
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(DictionaryForm.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
            Logger.getLogger(DictionaryForm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void LoadLoveWordFromFile() {
        ObjectInputStream ois = null;
        LoveDictionarisMap = new TreeMap<>();
        try {
            ois = new ObjectInputStream(new FileInputStream(new File(filename)));
            if (ois != null) {
                try {
                    LoveDictionarisMap = (TreeMap<String, String>) ois.readObject();
                    DefaultListModel<String> model = new DefaultListModel<>();

                    Set<String> keySet = LoveDictionarisMap.keySet();
                    keySet.forEach((key) -> {
                        model.addElement(key);
                    });
                    lstLoveWord.setModel(model);
                    lstLoveWord.repaint();

                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(DictionaryForm.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
            Logger.getLogger(DictionaryForm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void AddToSearchMap(String temp) {
        if (SearchedDictionarisMap == null) { // nếu map lưu từ đã tìm kiếm đang rỗng
            SearchedDictionarisMap = new TreeMap<>();
            InforSearchedWord x = new InforSearchedWord((Date) spnFrom.getValue(), 1); // tạo một thông tin mới cho dữ liệu
            SearchedDictionarisMap.put(temp, x); // thêm vào map lưu trữ
        } else { // nếu đã có thông tin
            Set<String> keySet = SearchedDictionarisMap.keySet(); // duyệt trong map lưu từ đã tìm kiếm
            for (String key : keySet) {
                if (key.compareTo(temp) == 0) { // nếu đã có trong map
                    SearchedDictionarisMap.get(temp).setNum(SearchedDictionarisMap.get(temp).getNum() + 1); // tăng số lần tìm kiếm lên 1
                    return; // kết thúc
                }
            }
            // nếu là từ mới
            InforSearchedWord x = new InforSearchedWord((Date) spnFrom.getValue(), 1); // tạo một thông tin mới cho dữ liệu
            SearchedDictionarisMap.put(temp, x); // thêm vào map lưu trữ
        }
    }

    public static void loadContentDictionary() throws SAXException, ParserConfigurationException, IOException {
        // load dữ liệu cho từ điển Việt - Anh
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();

        File myXml = new File("Viet_Anh.xml");

        Document d = db.parse(myXml);

        Element root = d.getDocumentElement();

        NodeList listRecord = root.getElementsByTagName("record");

        DictionarisMapVA = new TreeMap<>();

        int n = listRecord.getLength();
        for (int i = 0; i < n; i++) {
            Element temp = (Element) listRecord.item(i);
            String t1 = temp.getElementsByTagName("word").item(0).getTextContent();
            dlmVA.addElement(t1);
            String t2 = temp.getElementsByTagName("meaning").item(0).getTextContent();
            DictionarisMapVA.put(t1, t2);
        }

        // load dữ liệu cho từ điển Anh - Việt
        myXml = new File("Anh_Viet.xml");
        d = db.parse(myXml);
        root = d.getDocumentElement();
        listRecord = root.getElementsByTagName("record");
        DictionarisMapAV = new TreeMap<>();

        n = listRecord.getLength();
        for (int i = 0; i < n; i++) {
            Element temp = (Element) listRecord.item(i);
            String t1 = temp.getElementsByTagName("word").item(0).getTextContent();
            dlmAV.addElement(t1);
            String t2 = temp.getElementsByTagName("meaning").item(0).getTextContent();
            DictionarisMapAV.put(t1, t2);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        cbxType = new javax.swing.JComboBox<>();
        txfInput = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstWord = new javax.swing.JList<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        txaMeaning = new javax.swing.JTextArea();
        btnSearch = new javax.swing.JButton();
        chbxLove = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        lstLoveWord = new javax.swing.JList<>();
        jScrollPane4 = new javax.swing.JScrollPane();
        txaLoveMeaning = new javax.swing.JTextArea();
        jButton2 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tb1 = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        spnFrom = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        spnTo = new javax.swing.JSpinner();
        jButton3 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("My Simple Dictionary");
        setResizable(false);
        setSize(new java.awt.Dimension(0, 0));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });

        jLabel1.setBackground(new java.awt.Color(51, 255, 204));
        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        jLabel1.setText("MY SIMPLE DICTIONARY");

        cbxType.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        cbxType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Anh - Viet", "Viet - Anh" }));
        cbxType.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbxTypeItemStateChanged(evt);
            }
        });
        cbxType.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cbxTypeMouseClicked(evt);
            }
        });

        txfInput.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        txfInput.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
                txfInputCaretPositionChanged(evt);
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                txfInputInputMethodTextChanged(evt);
            }
        });
        txfInput.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                txfInputPropertyChange(evt);
            }
        });
        txfInput.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txfInputKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txfInputKeyTyped(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel2.setText("Input ");

        lstWord.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lstWordMouseClicked(evt);
            }
        });
        lstWord.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstWordValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(lstWord);

        txaMeaning.setColumns(20);
        txaMeaning.setRows(5);
        jScrollPane2.setViewportView(txaMeaning);

        btnSearch.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        btnSearch.setText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        chbxLove.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        chbxLove.setText("Love Word");
        chbxLove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chbxLoveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 623, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(chbxLove)
                        .addGap(40, 40, 40)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txfInput, javax.swing.GroupLayout.PREFERRED_SIZE, 439, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(cbxType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(101, 101, 101))))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(272, 272, 272)
                .addComponent(jLabel1)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addComponent(jLabel1)
                .addGap(35, 35, 35)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txfInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnSearch)
                    .addComponent(cbxType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chbxLove))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 553, Short.MAX_VALUE)
                    .addComponent(jScrollPane2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Find word", jPanel1);

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));

        lstLoveWord.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lstLoveWordMouseClicked(evt);
            }
        });
        lstLoveWord.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstLoveWordValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(lstLoveWord);

        txaLoveMeaning.setColumns(20);
        txaLoveMeaning.setRows(5);
        jScrollPane4.setViewportView(txaLoveMeaning);

        jButton2.setText("Refresh");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 623, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane4)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 667, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Love words", jPanel2);

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));

        tb1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Words", "Frequency"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane5.setViewportView(tb1);

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel3.setText("Input");

        spnFrom.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        spnFrom.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.DAY_OF_WEEK));

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel4.setText("From");

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel5.setText("To");

        spnTo.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        spnTo.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.DAY_OF_WEEK));

        jButton3.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton3.setText("Load");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(101, 101, 101)
                        .addComponent(jLabel3)
                        .addGap(55, 55, 55)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spnFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(61, 61, 61)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(spnTo, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(105, 105, 105)
                        .addComponent(jButton3))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(249, 249, 249)
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(218, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(27, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(spnFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel4)
                    .addComponent(spnTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 649, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("statistic", jPanel3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        String temp = txfInput.getText();
        temp = temp.trim();
        temp = temp.toLowerCase();
        if (temp.isEmpty()) {
            JOptionPane.showMessageDialog(this, "chưa nhập từ cần tìm.");
            return;
        }
        String kq = DictionarisMap.get(temp);
        if (kq == null) {
            DefaultListModel<String> model = new DefaultListModel<>();

            Set<String> keySet = DictionarisMap.keySet();
            for (String key : keySet) {
                if (key.indexOf(temp) == 0) {
                    model.addElement(key);
                }
            }
            lstWord.setModel(model);
            lstWord.repaint();
            if (model.size() == 0) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy từ yêu cầu");
            }

        } else {
            txaMeaning.setText(kq); // set text cho textarea hiển thị ý nghĩa
            AddToSearchMap(temp);

        }
    }//GEN-LAST:event_btnSearchActionPerformed

    private void lstWordValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstWordValueChanged
        chbxLove.setSelected(false);
    }//GEN-LAST:event_lstWordValueChanged

    private void txfInputCaretPositionChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_txfInputCaretPositionChanged

    }//GEN-LAST:event_txfInputCaretPositionChanged

    private void txfInputPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_txfInputPropertyChange
    }//GEN-LAST:event_txfInputPropertyChange

    private void lstWordMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lstWordMouseClicked
        String temp = lstWord.getSelectedValue();
        String kq = DictionarisMap.get(temp);
        txaMeaning.setText(kq);
    }//GEN-LAST:event_lstWordMouseClicked

    private void cbxTypeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbxTypeItemStateChanged
        String type = cbxType.getItemAt(cbxType.getSelectedIndex());
        if (type.compareTo("Anh - Viet") == 0) {
            DictionarisMap = new TreeMap<>(DictionarisMapAV);
            lstWord.setModel(dlmAV);
            lstWord.repaint();
            txaMeaning.setText(DictionarisMap.get("(logic học)"));
            lstWord.setSelectedIndex(0);
        } else {
            DictionarisMap = new TreeMap<>(DictionarisMapVA);
            lstWord.setModel(dlmVA);
            lstWord.repaint();
            txaMeaning.setText(DictionarisMap.get("a"));
            lstWord.setSelectedIndex(0);
        }
    }//GEN-LAST:event_cbxTypeItemStateChanged

    private void cbxTypeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cbxTypeMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_cbxTypeMouseClicked

    private void lstLoveWordMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lstLoveWordMouseClicked
        String temp = lstLoveWord.getSelectedValue();
        String kq = LoveDictionarisMap.get(temp);
        txaLoveMeaning.setText(kq);
    }//GEN-LAST:event_lstLoveWordMouseClicked

    private void lstLoveWordValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstLoveWordValueChanged
    }//GEN-LAST:event_lstLoveWordValueChanged

    private void chbxLoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chbxLoveActionPerformed
        if (chbxLove.isSelected()) {
            String temp = lstWord.getSelectedValue(); // lấy từ đang được chọn
            if (temp == null) {
                JOptionPane.showMessageDialog(spnTo, "select a love word!");
                return;
            }
            String kq = LoveDictionarisMap.get(temp); // nếu đã có trong ds yêu thích thì thôi
            if (kq != null) {
                return;
            } else { // nếu chưa có
                kq = DictionarisMapAV.get(temp); // thử tìm trong từ điển Anh - Việt
                if (kq != null) // nếu có 
                {
                    LoveDictionarisMap.put(temp, kq); //thêm vào từ điển yếu thích
                } else { // nếu không có thì tìm trong từ điển Việt - Anh
                    kq = DictionarisMapVA.get(temp);
                    if (kq != null) // nếu có 
                    {
                        LoveDictionarisMap.put(temp, kq); //thêm vào từ điển yếu thích
                    }
                }
            }

        }
    }//GEN-LAST:event_chbxLoveActionPerformed

    public void SaveSearchedToFile() {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(new File(searched_filename)));
            if (oos != null) {
                oos.writeObject(SearchedDictionarisMap);
            }
        } catch (IOException e) {

        }
    }

    public void SaveLoveToFile() {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(new File(filename)));
            if (oos != null) {
                oos.writeObject(LoveDictionarisMap);
            }
        } catch (IOException e) {

        }
    }

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        try {

            DefaultListModel<String> model = new DefaultListModel<>();

            Set<String> keySet = LoveDictionarisMap.keySet();
            keySet.forEach((key) -> {
                model.addElement(key);
            });
            lstLoveWord.setModel(model);
            lstLoveWord.repaint();

            // lưu vào file dữ liệu
            SaveLoveToFile();
        } catch (Exception ee) {
            return;
        }

    }//GEN-LAST:event_jButton2ActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        SaveSearchedToFile();
    }//GEN-LAST:event_formWindowClosing

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        tb1.removeAll();
        tb1.repaint();
        Date from = (Date) spnFrom.getValue();
        int dayf = from.getDate();
        int monthf = from.getMonth();
        int yearf = from.getYear();

        Date to = (Date) spnTo.getValue();
        int dayt = to.getDate();
        int montht = to.getMonth();
        int yeart = to.getYear();

        DefaultTableModel dtm = (DefaultTableModel) tb1.getModel();
        dtm.setNumRows(0);
        if (SearchedDictionarisMap == null) {
            return;
        }
        Set<String> keySet = SearchedDictionarisMap.keySet();
        for (String key : keySet) {

            InforSearchedWord temp = SearchedDictionarisMap.get(key);
            int day = temp.getSearchDay().getDate();
            int month = temp.getSearchDay().getMonth();
            int year = temp.getSearchDay().getYear();

            if ((year >= yearf && year <= yeart)
                    && (month >= monthf && month <= montht)
                    && (day >= dayf && day <= dayt)) {
                dtm.addRow(new Object[]{
                    key, temp.getNum()
                });

            }

        }
        tb1.removeAll();
        tb1.setModel(dtm);
        tb1.repaint();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed

    }//GEN-LAST:event_formKeyPressed

    private void txfInputKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txfInputKeyTyped
        
    }//GEN-LAST:event_txfInputKeyTyped

    private void txfInputKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txfInputKeyReleased
        String temp = txfInput.getText();
        temp = temp.trim();
        temp = temp.toLowerCase();

        if (temp.isEmpty()) {
            String type = (String) cbxType.getSelectedItem();
            if (type != null) {
                if (type.compareTo("Anh - Viet") == 0) {
                    lstWord.setModel(dlmAV);
                } else {
                    lstWord.setModel(dlmVA);
                }
            }
            lstWord.repaint();
            return;
        }

        DefaultListModel<String> model = new DefaultListModel<>();

        Set<String> keySet = DictionarisMap.keySet();
        for (String key : keySet) {
            if (key.indexOf(temp) == 0) {
                model.addElement(key);
            }
        }
        lstWord.setModel(model);
        lstWord.repaint();
        lstWord.setSelectedIndex(0);
    }//GEN-LAST:event_txfInputKeyReleased

    private void txfInputInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_txfInputInputMethodTextChanged

    }//GEN-LAST:event_txfInputInputMethodTextChanged

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DictionaryForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DictionaryForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DictionaryForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DictionaryForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new DictionaryForm().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSearch;
    private javax.swing.JComboBox<String> cbxType;
    private javax.swing.JCheckBox chbxLove;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JList<String> lstLoveWord;
    private javax.swing.JList<String> lstWord;
    private javax.swing.JSpinner spnFrom;
    private javax.swing.JSpinner spnTo;
    private javax.swing.JTable tb1;
    private javax.swing.JTextArea txaLoveMeaning;
    private javax.swing.JTextArea txaMeaning;
    private javax.swing.JTextField txfInput;
    // End of variables declaration//GEN-END:variables
}
