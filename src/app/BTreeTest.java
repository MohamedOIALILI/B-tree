package app;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class BTreeTest
{
    private final BTree<Integer, String> mBTree;
    private final Map<Integer, String> mMap;
    private BTTestIteratorImpl<Integer, String> mIter;

    public BTreeTest() {
        mBTree = new BTree<Integer, String>();
        mMap = new TreeMap<Integer, String>();
        mIter = new BTTestIteratorImpl<Integer, String>();
    }

    public BTree<Integer, String> getBTree() {
        return mBTree;
    }

    public BTNode<Integer, String> getRootNode() {
        return mBTree.getRootNode();
    }

    protected void add(Integer key, String value) {
        mMap.put(key, value);
        mBTree.insert(key, value);
    }

    protected void delete(Integer key) throws BTException {
        System.out.println("Suppression de la clé = " + key);
        String strVal1 = mMap.remove(key);
        String strVal2 = mBTree.delete(key);
        if (!isEqual(strVal1, strVal2)) {
            throw new BTException("La clé supprimée = " + key + " a plusieurs valeurs: " + strVal1 + " | " + strVal2);
        }
    }

    public void listItems(BTIteratorIF<Integer, String> iterImpl) {
        mBTree.list(iterImpl);
    }
    
    public void addRandomKeys() {
        int minNum = 10;
        int maxNum = 3000;
        int itemNum = 3000;
        int nVal;
        System.out.println("L'ajoute des clés aléatoires entre " + minNum + " et " + maxNum);
        for (int i = 0; i < itemNum; ++i) {
            nVal = randInt(minNum, maxNum);
            add(nVal, "random-" + nVal);
        }
    }

    private boolean isEqual(String strVal1, String strVal2) {
        if ((strVal1 == null) && (strVal2 == null)) {
            return true;
        }
        if ((strVal1 == null) && (strVal2 != null)) {
            return false;
        }
        if ((strVal1 != null) && (strVal2 == null)) {
            return false;
        }
        if (!strVal1.equals(strVal2)) {
            return false;
        }
        return true;
    }

    public void validateSize() throws BTException {
        System.out.println("Validation de la taille ...");
        if (mMap.size() != mBTree.size()) {
            throw new BTException("Erreur dans validateSize(): Echec de comparaison de la taille:  " + mMap.size() + " <> " + mBTree.size());
        }
    }

    public void validateSearch(Integer key) throws BTException {
        System.out.println("Validatin de la recherche pour la clé = " + key + " ...");
        String strVal1 = mMap.get(key);
        String strVal2 = mBTree.search(key);
        if (!isEqual(strVal1, strVal2)) {
            throw new BTException("Erreur dans validateSearch(): Echec de comparaison de la valeur du clé = " + key);
        }
    }

    public void validateData() throws BTException {
        System.out.println("Validation des données ...");
        for (Map.Entry<Integer, String> entry : mMap.entrySet()) {
            try {
                String strVal = mBTree.search(entry.getKey());
                if (!isEqual(entry.getValue(), strVal)) {
                    throw new BTException("Erreur dans validateData(): Echec de comparasion de la valeur du clé = " + entry.getKey());
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
                throw new BTException("Erreur Runtime dans validateData(): Echec de comparaison de la valeur du clé = " + entry.getKey() + " msg = " + ex.getMessage());
            }
        }
    }

    public void validateOrder() throws BTException {
        System.out.println("Validation de l'order des clés ...");
        mIter.reset();
        mBTree.list(mIter);
        if (!mIter.getStatus()) {
            throw new BTException("Erreur dans validateData(): Echec de comparasion de la valeur du clé = " + mIter.getCurrentKey());
        }
    }

    public void validateAll() throws BTException {
        validateData();
        validateSize();
        validateOrder();
    }

    public void addKey(int i) {
        add(i, "Valeur = " + i);
    }

    // 
    // Génération des nombres entiers dans le range spécifié
    //
    public static int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }
    /**
     * classe interne pour implémenter BTree iterator
     */
    class BTTestIteratorImpl<K extends Comparable, V> implements BTIteratorIF<K, V> {
        private K mCurrentKey;
        private K mPreviousKey;
        private boolean mStatus;

        public BTTestIteratorImpl() {
            reset();
        }

        @Override
        public boolean item(K key, V value) {
            mCurrentKey = key;
            if ((mPreviousKey != null) && (mPreviousKey.compareTo(key) > 0)) {
                mStatus = false;
                return false;
            }
            mPreviousKey = key;
            return true;
        }

        public boolean getStatus() {
            return mStatus;
        }

        public K getCurrentKey() {
            return mCurrentKey;
        }

        public final void reset() {
            mPreviousKey = null;
            mStatus = true;
        }
    }
}
