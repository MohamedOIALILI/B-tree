package app;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.Toolkit;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

public class BTreeRenderer extends JFrame
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final static int APP_WIDTH       =   1140;
    public final static int APP_HEIGHT      =   650;
    public final static int HEIGHT_STEP     =   80;
    public final static int NODE_HEIGHT     =   30;
    public final static int NODE_DIST       =   16;
    public final static int TREE_HEIGHT     =   32;

    private final BTreeTest mTreeTest;
    private final StringBuilder mBuf;
    private final Object []mObjLists;
    private mxGraph mGraph;
    private final JTextField mText;
    private final JButton mAddBt, mRemoveBt;
    private final JButton mAddMoreBt, mRemoveMoreBt;
    private final JButton mClearBt, mSearchKeyBt;
    private final JButton mListBt, mSaveBt;
    private final JTextArea mOutputConsole;

    public BTreeRenderer() {
        super("Arbre B");
        mTreeTest = new BTreeTest();
        mBuf = new StringBuilder();
        mObjLists = new Object[TREE_HEIGHT];
        mSearchKeyBt = new JButton("Rechercher");
        mAddBt = new JButton("+");
        mRemoveBt = new JButton("-");
        mAddMoreBt = new JButton("+>");
        mRemoveMoreBt = new JButton("<-");
        mClearBt = new JButton("Effacer");
        mListBt = new JButton("Liste");
        mSaveBt = new JButton("Enregistrer");
        mText = new JTextField("");
        mOutputConsole = new JTextArea(4, 80);

        mSearchKeyBt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { 
                searchButtonPressed();
            }
        });

        mAddBt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { 
                addButtonPressed();
            }
        });

        mRemoveBt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { 
                removeButtonPressed();
            }
        });

        mAddMoreBt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { 
                addMoreButtonPressed();
            }
        });

        mRemoveMoreBt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { 
                removeMoreButtonPressed();
            }
        });

        mListBt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { 
                listButtonPressed();
            }
        });

        mClearBt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { 
                clearButtonPressed();
            }
        });

        mSaveBt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { 
                saveButtonPressed();
            }
        });

        generateTestData();
    }


    private Integer getInputValue() {
        String strInput = mText.getText().trim();
        int nVal;

        try {
            nVal = Integer.parseInt(strInput);
        }
        catch (Exception ex) {
            return null;
        }

        mText.setFocusable(true);
        return nVal;
    }


    public void searchButtonPressed() {
        Integer in = getInputValue();
        if (in == null) {
            return;
        }

        mText.setText("");
        searchKey(in);
    }


    public void addButtonPressed() {
        Integer in = getInputValue();
        if (in == null) {
            return;
        }

        mText.setText("");
        addKey(in);
        render();
    }


    public void removeButtonPressed() {
        Integer in = getInputValue();
        if (in == null) {
            return;
        }

        mText.setText("");
        deleteKey(in);
        render();
    }


    public void addMoreButtonPressed() {
        Integer in = getInputValue();
        if (in == null) {
            return;
        }

        addKey(in);
        in += 1;
        mText.setText(in + "");
        render();
    }


    public void removeMoreButtonPressed() {
        Integer in = getInputValue();
        if (in == null) {
            return;
        }

        deleteKey(in);
        in -= 1;
        mText.setText(in + "");
        render();
    }


    public void clearButtonPressed() {
        mTreeTest.getBTree().clear();
        mOutputConsole.setText("");
        render();
    }


    public void saveButtonPressed() {
        String strSavedFile;
        String strText = mOutputConsole.getText();
        if (strText == null) {
            strText = "";
        }
        strText = strText.trim();
        if (strText.isEmpty()) {
            // Rien à enregistrer
            return;
        }
        try {
            strSavedFile = SimpleFileWriter.saveToFile(strText);
        }
        catch (IOException ioex) {
            println("Erreur: Echec d'enregistrement dans fichier, msg = " + ioex.getMessage());
            ioex.printStackTrace();
            return;
        }

        mOutputConsole.setText("Enregistrement est effectué avec succès dans le fichier = " + strSavedFile);
    }

    private BTIteratorIF mIter = null;
    public void listButtonPressed() {
        if (mIter == null) {
            mIter = new BTIteratorImpl();
        }

        mOutputConsole.setText("");
        mTreeTest.listItems(mIter);
    }

    public void render() {
        mGraph = new mxGraph();
        Object parent = mGraph.getDefaultParent();
        List<Object> pObjList = new ArrayList<Object>();
        List<Object> cObjList = new ArrayList<Object>();
        List<Object> tempObjList;

        for (int i = 0; i < TREE_HEIGHT; ++i) {
            mObjLists[i] = null;
        }

        try {
            generateGraphObject(mTreeTest.getBTree().getRootNode(), 0);
        }
        catch (BTException btex) {
            btex.printStackTrace();
            return;
        }

        Box hBox = Box.createHorizontalBox();
        hBox.add(new JLabel("   Valeur:  "));
        hBox.add(mText);
        hBox.add(mSearchKeyBt);
        hBox.add(mAddBt);
        hBox.add(mRemoveBt);
        hBox.add(mAddMoreBt);
        hBox.add(mRemoveMoreBt);
        hBox.add(mListBt);
        hBox.add(mSaveBt);
        hBox.add(mClearBt);

        mGraph.getModel().beginUpdate();
        try {
            int nStartXPos;
            int nStartYPos = 10;
            int cellWidth;
            for (int i = 0; i < mObjLists.length; ++i) {
                cObjList.clear();
                List<KeyData> objList = (List<KeyData>)mObjLists[i];
                if (objList == null) {
                    continue;
                }

                int totalWidth = 0;
                int nCount = 0;
                for (KeyData keyData : objList) {
                    totalWidth += keyData.mKeys.length() * 6;
                    if (nCount > 0) {
                        totalWidth += NODE_DIST;
                    }
                    ++nCount;
                }

                nStartXPos = (APP_WIDTH - totalWidth) / 2;
                if (nStartXPos < 0) {
                    nStartXPos = 0;
                }

                for (KeyData keyData : objList) {
                    int len = keyData.mKeys.length();
                    if (len == 1) {
                        len += 2;
                    }
                    cellWidth = len * 6;
                    Object gObj = mGraph.insertVertex(parent, null, keyData.mKeys, nStartXPos, nStartYPos, cellWidth, 24);
                    cObjList.add(gObj);
                    nStartXPos += (cellWidth + NODE_DIST);
                }

                if (i > 0) {
                    // Connect the nodes
                    List<KeyData> keyList = (List<KeyData>)mObjLists[i - 1];
                    int j = 0, k = 0;
                    for (Object pObj : pObjList) {
                        KeyData keyData = keyList.get(j);
                        for (int l = 0; l < keyData.mKeyNum + 1; ++l) {
                            mGraph.insertEdge(parent, null, "", pObj, cObjList.get(k));
                            ++k;
                        }
                        ++j;
                    }
                }

                // Échanger les deux listes d'objets pour la boucle suivante
                tempObjList = pObjList;
                pObjList = cObjList;
                cObjList = tempObjList;

                nStartYPos += HEIGHT_STEP;
            }
        }
        finally {
            mGraph.getModel().endUpdate();
        }

        mxGraphComponent graphComponent = new mxGraphComponent(mGraph);
        getContentPane().removeAll();
        getContentPane().add(hBox, BorderLayout.NORTH);
        getContentPane().add(graphComponent, BorderLayout.CENTER);
        getContentPane().add(new JScrollPane(mOutputConsole), BorderLayout.SOUTH);
        revalidate();
    }

    public static void centreWindow(JFrame frame) {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
        frame.setLocation(x, y);
    }

    private void generateGraphObject(BTNode<Integer, String> treeNode, int nLevel) throws BTException {
        if ((treeNode == null) ||
            (treeNode.mCurrentKeyNum == 0)) {
            return;
        }

        int currentKeyNum = treeNode.mCurrentKeyNum;
        BTKeyValue<Integer, String> keyVal;

        List<KeyData> keyList = (List<KeyData>)mObjLists[nLevel];
        if (keyList == null) {
            keyList = new ArrayList<KeyData>();
            mObjLists[nLevel] = keyList;
        }

        mBuf.setLength(0);
        // Affichage des clés de noeuds
        for (int i = 0; i < currentKeyNum; ++i) {
            if (i > 0) {
                mBuf.append(" | ");
            }

            keyVal = treeNode.mKeys[i];
            mBuf.append(keyVal.mKey);
        }

        keyList.add(new KeyData(mBuf.toString(), currentKeyNum));

        if (treeNode.mIsLeaf) {
            return;
        }

        ++nLevel;
        for (int i = 0; i < currentKeyNum + 1; ++i) {
            generateGraphObject(treeNode.mChildren[i], nLevel);
        }
    }

    public void println(String strText) {
        mOutputConsole.append(strText);
        mOutputConsole.append("\n");
    }


    public void searchKey(Integer key) {
        println("Recherche pour la clé = " + key);
        String strVal = mTreeTest.getBTree().search(key);
        if (strVal != null) {
            println("Clé = " + key + " | " + strVal);
        }
        else {
            println("Aucune valeur trouvée pour la clé = " + key);
        }
    }


    public void deleteKey(Integer key) {
        String strVal = mTreeTest.getBTree().delete(key);
        println("Suppression de la clé = " + key + " | " + strVal);
    }


    public void addKey(Integer key) {
        println("L'ajoute de la clé = " + key);
        mTreeTest.getBTree().insert(key, "Valeur = " + key);
    }


    public final void generateTestData() {
        for (int i = 1; i < 42; ++i) {
            mTreeTest.add(i, "Valeur = " + i);
        }
    }

    public static void main(String[] args) {
        BTreeRenderer frame = new BTreeRenderer();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(APP_WIDTH, APP_HEIGHT);
        centreWindow(frame);
        frame.render();
        frame.setVisible(true);
    }


    /**
     * classe Interne KeyData
     */
    class KeyData {
        String mKeys = null;
        int mKeyNum = 0;

        KeyData(String keys, int keyNum) {
            mKeys = keys;
            mKeyNum = keyNum;
        }
    }

    /**
     * Classe interne pour implémenter BTree iterator
     */
    class BTIteratorImpl<K extends Comparable, V> implements BTIteratorIF<K, V> {
        private StringBuilder mBuf = new StringBuilder();

        @Override
        public boolean item(K key, V value) {
            mBuf.setLength(0);
            mBuf.append("Clé "+key)
                .append(" | ")
                .append(value);
            println(mBuf.toString());
            return true;
        }
    }
}
