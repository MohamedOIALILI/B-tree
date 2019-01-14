package app;


public class BTNode<K extends Comparable, V>
{
    public final static int MIN_DEGREE          =   5; // le nombre des copules
    public final static int LOWER_BOUND_KEYNUM  =   MIN_DEGREE - 1; //nombre min des couples
    public final static int UPPER_BOUND_KEYNUM  =   (MIN_DEGREE * 2) - 1; ////nombre max des couples

    protected boolean mIsLeaf; // pour tester si c'est feuille
    protected int mCurrentKeyNum; // clé courant
    protected BTKeyValue<K, V> mKeys[]; // tableau des clés/valeurs
    protected BTNode mChildren[]; // tableau pour stocker les clés/valeurs du fils du noeud courant


    public BTNode() {
        mIsLeaf = true;
        mCurrentKeyNum = 0;
        mKeys = new BTKeyValue[UPPER_BOUND_KEYNUM];
        mChildren = new BTNode[UPPER_BOUND_KEYNUM + 1];
    }


    protected static BTNode getChildNodeAtIndex(BTNode btNode, int keyIdx, int nDirection) {
        if (btNode.mIsLeaf) {
            return null;
        }

        keyIdx += nDirection;
        if ((keyIdx < 0) || (keyIdx > btNode.mCurrentKeyNum)) {
            return null;
        }

        return btNode.mChildren[keyIdx];
    }


    protected static BTNode getLeftChildAtIndex(BTNode btNode, int keyIdx) {
        return getChildNodeAtIndex(btNode, keyIdx, 0);
    }


    protected static BTNode getRightChildAtIndex(BTNode btNode, int keyIdx) {
        return getChildNodeAtIndex(btNode, keyIdx, 1);
    }


    protected static BTNode getLeftSiblingAtIndex(BTNode parentNode, int keyIdx) {
        return getChildNodeAtIndex(parentNode, keyIdx, -1);
    }


    protected static BTNode getRightSiblingAtIndex(BTNode parentNode, int keyIdx) {
        return getChildNodeAtIndex(parentNode, keyIdx, 1);
    }
}
