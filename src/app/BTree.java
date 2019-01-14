package app;

import java.util.Stack;

public class BTree<K extends Comparable, V>
{
    public final static int     REBALANCE_FOR_LEAF_NODE         =   1;
    public final static int     REBALANCE_FOR_INTERNAL_NODE     =   2;

    private BTNode<K, V> mRoot = null;
    private long  mSize = 0L;
    private BTNode<K, V> mIntermediateInternalNode = null;
    private int mNodeIdx = 0;
    private final Stack<StackInfo> mStackTracer = new Stack<StackInfo>();

    //
    // Get the root node
    //
    public BTNode<K, V> getRootNode() {
        return mRoot;
    }

    //
    // The total number of nodes in the tree
    //
    public long size() {
        return mSize;
    }

    //
    // Clear all the entries in the tree
    //
    public void clear() {
        mSize = 0L;
        mRoot = null;
    }

    //
    // Create a node with default values
    //
    private BTNode<K, V> createNode() {
        BTNode<K, V> btNode;
        btNode = new BTNode();
        btNode.mIsLeaf = true;
        btNode.mCurrentKeyNum = 0;
        return btNode;
    }

    //
    // Search value for a specified key of the tree
    //
    public V search(K key) {
        BTNode<K, V> currentNode = mRoot;
        BTKeyValue<K, V> currentKey;
        int i, numberOfKeys;

        while (currentNode != null) {
            numberOfKeys = currentNode.mCurrentKeyNum;
            i = 0;
            currentKey = currentNode.mKeys[i];
            while ((i < numberOfKeys) && (key.compareTo(currentKey.mKey) > 0)) {
                ++i;
                if (i < numberOfKeys) {
                    currentKey = currentNode.mKeys[i];
                }
                else {
                    --i;
                    break;
                }
            }
            
            if ((i < numberOfKeys) && (key.compareTo(currentKey.mKey) == 0)) {
                return currentKey.mValue;
            }

            if (key.compareTo(currentKey.mKey) > 0) {
                currentNode = BTNode.getRightChildAtIndex(currentNode, i);
            }
            else {
                currentNode = BTNode.getLeftChildAtIndex(currentNode, i);
            }
        }

        return null;
    }
    
    //
    // Insérer la clé et sa valeur dans l'arbre
    //
    public BTree insert(K key, V value) {
        if (mRoot == null) {
            mRoot = createNode();
        }

        ++mSize;
        if (mRoot.mCurrentKeyNum == BTNode.UPPER_BOUND_KEYNUM) {
            // La racine est complèt, on va l'éclater
            BTNode<K, V> btNode = createNode();
            btNode.mIsLeaf = false;
            btNode.mChildren[0] = mRoot;
            mRoot = btNode;
            splitNode(mRoot, 0, btNode.mChildren[0]);
        }

        insertKeyAtNode(mRoot, key, value);
        return this;
    }
    
    //
    // Insérer la clé et sa valeur dans la racine spécifiér
    //
    private void insertKeyAtNode(BTNode rootNode, K key, V value) {
        int i;
        int currentKeyNum = rootNode.mCurrentKeyNum;

        if (rootNode.mIsLeaf) {
            if (rootNode.mCurrentKeyNum == 0) {
                // La racine est vide
                rootNode.mKeys[0] = new BTKeyValue<K, V>(key, value);
                ++(rootNode.mCurrentKeyNum);
                return;
            }

            // Vérifier si la clé spécifiée n'existe pas dans le noeud
            for (i = 0; i < rootNode.mCurrentKeyNum; ++i) {
                if (key.compareTo(rootNode.mKeys[i].mKey) == 0) {
                    // La clé existe, écrasser sa valeur
                    rootNode.mKeys[i].mValue = value;
                    --mSize;
                    return;
                }
            }

            i = currentKeyNum - 1;
            BTKeyValue<K, V> existingKeyVal = rootNode.mKeys[i];
            while ((i > -1) && (key.compareTo(existingKeyVal.mKey) < 0)) {
                rootNode.mKeys[i + 1] = existingKeyVal;
                --i;
                if (i > -1) {
                    existingKeyVal = rootNode.mKeys[i];
                }
            }

            i = i + 1;
            rootNode.mKeys[i] = new BTKeyValue<K, V>(key, value);

            ++(rootNode.mCurrentKeyNum);
            return;
        }

        // This is an internal node (i.e: not a leaf node)
        // So let find the child node where the key is supposed to belong
        i = 0;
        int numberOfKeys = rootNode.mCurrentKeyNum;
        BTKeyValue<K, V> currentKey = rootNode.mKeys[i];
        while ((i < numberOfKeys) && (key.compareTo(currentKey.mKey) > 0)) {
            ++i;
            if (i < numberOfKeys) {
                currentKey = rootNode.mKeys[i];
            }
            else {
                --i;
                break;
            }
        }

        if ((i < numberOfKeys) && (key.compareTo(currentKey.mKey) == 0)) {
            // The key already existed so replace its value and done with it
            currentKey.mValue = value;
            --mSize;
            return;
        }

        BTNode<K, V> btNode;
        if (key.compareTo(currentKey.mKey) > 0) {
            btNode = BTNode.getRightChildAtIndex(rootNode, i);
            i = i + 1;
        }
        else {
            if ((i - 1 >= 0) && (key.compareTo(rootNode.mKeys[i - 1].mKey) > 0)) {
                btNode = BTNode.getRightChildAtIndex(rootNode, i - 1);
            }
            else {
                btNode = BTNode.getLeftChildAtIndex(rootNode, i);
            }
        }

        if (btNode.mCurrentKeyNum == BTNode.UPPER_BOUND_KEYNUM) {
            // If the child node is a full node then handle it by splitting out
            // then insert key starting at the root node after splitting node
            splitNode(rootNode, i, btNode);
            insertKeyAtNode(rootNode, key, value);
            return;
        }

        insertKeyAtNode(btNode, key, value);
    }

    //
    // Eclater le noeud fils avec le respect du positionnement du parent
    //
    private void splitNode(BTNode parentNode, int nodeIdx, BTNode btNode) {
        int i;

        BTNode<K, V> newNode = createNode();

        newNode.mIsLeaf = btNode.mIsLeaf;

        // Puisque le noeud est complèt,
        // nouveau noeud doit être partagé LOWER_BOUND_KEYNUM clés
        newNode.mCurrentKeyNum = BTNode.LOWER_BOUND_KEYNUM;

        // Copier la moitié droite des clés du noeud courant vers le nouveau noeud
        for (i = 0; i < BTNode.LOWER_BOUND_KEYNUM; ++i) {
            newNode.mKeys[i] = btNode.mKeys[i + BTNode.MIN_DEGREE];
            btNode.mKeys[i + BTNode.MIN_DEGREE] = null;
        }

        // Si le noeud est interne (n'est pas une feuille),
        // copier les pointeurs du fils dans la moitié du droite aussi
        if (!btNode.mIsLeaf) {
            for (i = 0; i < BTNode.MIN_DEGREE; ++i) {
                newNode.mChildren[i] = btNode.mChildren[i + BTNode.MIN_DEGREE];
                btNode.mChildren[i + BTNode.MIN_DEGREE] = null;
            }
        }

        // Le noeud dans cette position doit avoir LOWER_BOUND_KEYNUM clés
        // On va déplacer sa clé droite la plus à droite vers le noeud parent après
        btNode.mCurrentKeyNum = BTNode.LOWER_BOUND_KEYNUM;

        // Faire le décalage à droite pour les pointeurs du fils du noeud parent
        // donc, ça va permette de mettre le nouveau noeud et son nouveau pointeur du fils
        for (i = parentNode.mCurrentKeyNum; i > nodeIdx; --i) {
            parentNode.mChildren[i + 1] = parentNode.mChildren[i];
            parentNode.mChildren[i] = null;
        }
        parentNode.mChildren[nodeIdx + 1] = newNode;

        // Faire le décalage droite de toutes les clés du noeud parent vers le coté droit
        // On va avoir un slot pour déplacer la clé du milieu récupérer du noeud éclaté
        for (i = parentNode.mCurrentKeyNum - 1; i >= nodeIdx; --i) {
            parentNode.mKeys[i + 1] = parentNode.mKeys[i];
            parentNode.mKeys[i] = null;
        }
        parentNode.mKeys[nodeIdx] = btNode.mKeys[BTNode.LOWER_BOUND_KEYNUM];
        btNode.mKeys[BTNode.LOWER_BOUND_KEYNUM] = null;
        ++(parentNode.mCurrentKeyNum);
    }


    //
    // Find the predecessor node for a specified node
    //
    private BTNode<K, V> findPredecessor(BTNode<K, V> btNode, int nodeIdx) {
        if (btNode.mIsLeaf) {
            return btNode;
        }

        BTNode<K, V> predecessorNode;
        if (nodeIdx > -1) {
            predecessorNode = BTNode.getLeftChildAtIndex(btNode, nodeIdx);
            if (predecessorNode != null) {
                mIntermediateInternalNode = btNode;
                mNodeIdx = nodeIdx;
                btNode = findPredecessor(predecessorNode, -1);
            }

            return btNode;
        }

        predecessorNode = BTNode.getRightChildAtIndex(btNode, btNode.mCurrentKeyNum - 1);
        if (predecessorNode != null) {
            mIntermediateInternalNode = btNode;
            mNodeIdx = btNode.mCurrentKeyNum;
            btNode = findPredecessorForNode(predecessorNode, -1);
        }

        return btNode;
    }


    //
    // Find predecessor node of a specified node
    //
    private BTNode<K, V> findPredecessorForNode(BTNode<K, V> btNode, int keyIdx) {
        BTNode<K, V> predecessorNode;
        BTNode<K, V> originalNode = btNode;
        if (keyIdx > -1) {
            predecessorNode = BTNode.getLeftChildAtIndex(btNode, keyIdx);
            if (predecessorNode != null) {
                btNode = findPredecessorForNode(predecessorNode, -1);
                rebalanceTreeAtNode(originalNode, predecessorNode, keyIdx, REBALANCE_FOR_LEAF_NODE);
            }

            return btNode;
        }

        predecessorNode = BTNode.getRightChildAtIndex(btNode, btNode.mCurrentKeyNum - 1);
        if (predecessorNode != null) {
            btNode = findPredecessorForNode(predecessorNode, -1);
            rebalanceTreeAtNode(originalNode, predecessorNode, keyIdx, REBALANCE_FOR_LEAF_NODE);
        }

        return btNode;
    }


    //
    // Do the left rotation
    //
    private void performLeftRotation(BTNode<K, V> btNode, int nodeIdx, BTNode<K, V> parentNode, BTNode<K, V> rightSiblingNode) {
        int parentKeyIdx = nodeIdx;

        /*
        if (nodeIdx >= parentNode.mCurrentKeyNum) {
            // This shouldn't happen
            parentKeyIdx = nodeIdx - 1;
        }
        */

        // Move the parent key and relevant child to the deficient node
        btNode.mKeys[btNode.mCurrentKeyNum] = parentNode.mKeys[parentKeyIdx];
        btNode.mChildren[btNode.mCurrentKeyNum + 1] = rightSiblingNode.mChildren[0];
        ++(btNode.mCurrentKeyNum);

        // Move the leftmost key of the right sibling and relevant child pointer to the parent node
        parentNode.mKeys[parentKeyIdx] = rightSiblingNode.mKeys[0];
        --(rightSiblingNode.mCurrentKeyNum);
        // Shift all keys and children of the right sibling to its left
        for (int i = 0; i < rightSiblingNode.mCurrentKeyNum; ++i) {
            rightSiblingNode.mKeys[i] = rightSiblingNode.mKeys[i + 1];
            rightSiblingNode.mChildren[i] = rightSiblingNode.mChildren[i + 1];
        }
        rightSiblingNode.mChildren[rightSiblingNode.mCurrentKeyNum] = rightSiblingNode.mChildren[rightSiblingNode.mCurrentKeyNum + 1];
        rightSiblingNode.mChildren[rightSiblingNode.mCurrentKeyNum + 1] = null;
    }


    //
    // Do the right rotation
    //
    private void performRightRotation(BTNode<K, V> btNode, int nodeIdx, BTNode<K, V> parentNode, BTNode<K, V> leftSiblingNode) {
        int parentKeyIdx = nodeIdx;
        if (nodeIdx >= parentNode.mCurrentKeyNum) {
            // This shouldn't happen
            parentKeyIdx = nodeIdx - 1;
        }

        // Shift all keys and children of the deficient node to the right
        // So that there will be available left slot for insertion
        btNode.mChildren[btNode.mCurrentKeyNum + 1] = btNode.mChildren[btNode.mCurrentKeyNum];
        for (int i = btNode.mCurrentKeyNum - 1; i >= 0; --i) {
            btNode.mKeys[i + 1] = btNode.mKeys[i];
            btNode.mChildren[i + 1] = btNode.mChildren[i];
        }

        // Move the parent key and relevant child to the deficient node
        btNode.mKeys[0] = parentNode.mKeys[parentKeyIdx];
        btNode.mChildren[0] = leftSiblingNode.mChildren[leftSiblingNode.mCurrentKeyNum];
        ++(btNode.mCurrentKeyNum);

        // Move the leftmost key of the right sibling and relevant child pointer to the parent node
        parentNode.mKeys[parentKeyIdx] = leftSiblingNode.mKeys[leftSiblingNode.mCurrentKeyNum - 1];
        leftSiblingNode.mChildren[leftSiblingNode.mCurrentKeyNum] = null;
        --(leftSiblingNode.mCurrentKeyNum);
    }


    //
    // Do a left sibling merge
    // Return true if it should continue further
    // Return false if it is done
    //
    private boolean performMergeWithLeftSibling(BTNode<K, V> btNode, int nodeIdx, BTNode<K, V> parentNode, BTNode<K, V> leftSiblingNode) {
        if (nodeIdx == parentNode.mCurrentKeyNum) {
            // For the case that the node index can be the right most
            nodeIdx = nodeIdx - 1;
        }

        // Here we need to determine the parent node's index based on child node's index (nodeIdx)
        if (nodeIdx > 0) {
            if (leftSiblingNode.mKeys[leftSiblingNode.mCurrentKeyNum - 1].mKey.compareTo(parentNode.mKeys[nodeIdx - 1].mKey) < 0) {
                nodeIdx = nodeIdx - 1;
            }
        }

        // Copy the parent key to the node (on the left)
        leftSiblingNode.mKeys[leftSiblingNode.mCurrentKeyNum] = parentNode.mKeys[nodeIdx];
        ++(leftSiblingNode.mCurrentKeyNum);

        // Copy keys and children of the node to the left sibling node
        for (int i = 0; i < btNode.mCurrentKeyNum; ++i) {
            leftSiblingNode.mKeys[leftSiblingNode.mCurrentKeyNum + i] = btNode.mKeys[i];
            leftSiblingNode.mChildren[leftSiblingNode.mCurrentKeyNum + i] = btNode.mChildren[i];
            btNode.mKeys[i] = null;
        }
        leftSiblingNode.mCurrentKeyNum += btNode.mCurrentKeyNum;
        leftSiblingNode.mChildren[leftSiblingNode.mCurrentKeyNum] = btNode.mChildren[btNode.mCurrentKeyNum];
        btNode.mCurrentKeyNum = 0;  // Abandon the node

        // Shift all relevant keys and children of the parent node to the left
        // since it lost one of its keys and children (by moving it to the child node)
        int i;
        for (i = nodeIdx; i < parentNode.mCurrentKeyNum - 1; ++i) {
            parentNode.mKeys[i] = parentNode.mKeys[i + 1];
            parentNode.mChildren[i + 1] = parentNode.mChildren[i + 2];
        }
        parentNode.mKeys[i] = null;
        parentNode.mChildren[parentNode.mCurrentKeyNum] = null;
        --(parentNode.mCurrentKeyNum);

        // Make sure the parent point to the correct child after the merge
        parentNode.mChildren[nodeIdx] = leftSiblingNode;

        if ((parentNode == mRoot) && (parentNode.mCurrentKeyNum == 0)) {
            // Root node is updated.  It should be done
            mRoot = leftSiblingNode;
            return false;
        }

        return true;
    }


    //
    // Do the right sibling merge
    // Return true if it should continue further
    // Return false if it is done
    //
    private boolean performMergeWithRightSibling(BTNode<K, V> btNode, int nodeIdx, BTNode<K, V> parentNode, BTNode<K, V> rightSiblingNode) {
        // Copy the parent key to right-most slot of the node
        btNode.mKeys[btNode.mCurrentKeyNum] = parentNode.mKeys[nodeIdx];
        ++(btNode.mCurrentKeyNum);

        // Copy keys and children of the right sibling to the node
        for (int i = 0; i < rightSiblingNode.mCurrentKeyNum; ++i) {
            btNode.mKeys[btNode.mCurrentKeyNum + i] = rightSiblingNode.mKeys[i];
            btNode.mChildren[btNode.mCurrentKeyNum + i] = rightSiblingNode.mChildren[i];
        }
        btNode.mCurrentKeyNum += rightSiblingNode.mCurrentKeyNum;
        btNode.mChildren[btNode.mCurrentKeyNum] = rightSiblingNode.mChildren[rightSiblingNode.mCurrentKeyNum];
        rightSiblingNode.mCurrentKeyNum = 0;  // Abandon the sibling node

        // Shift all relevant keys and children of the parent node to the left
        // since it lost one of its keys and children (by moving it to the child node)
        int i;
        for (i = nodeIdx; i < parentNode.mCurrentKeyNum - 1; ++i) {
            parentNode.mKeys[i] = parentNode.mKeys[i + 1];
            parentNode.mChildren[i + 1] = parentNode.mChildren[i + 2];
        }
        parentNode.mKeys[i] = null;
        parentNode.mChildren[parentNode.mCurrentKeyNum] = null;
        --(parentNode.mCurrentKeyNum);

        // Make sure the parent point to the correct child after the merge
        parentNode.mChildren[nodeIdx] = btNode;

        if ((parentNode == mRoot) && (parentNode.mCurrentKeyNum == 0)) {
            // Root node is updated.  It should be done
            mRoot = btNode;
            return false;
        }

        return true;
    }


    //
    // Search the specified key within a node
    // Return index of the keys if it finds
    // Return -1 otherwise
    //
    private int searchKey(BTNode<K, V> btNode, K key) {
        for (int i = 0; i < btNode.mCurrentKeyNum; ++i) {
            if (key.compareTo(btNode.mKeys[i].mKey) == 0) {
                return i;
            }
            else if (key.compareTo(btNode.mKeys[i].mKey) < 0) {
                return -1;
            }
        }

        return -1;
    }


    //
    // List all the items in the tree
    //
    public void list(BTIteratorIF<K, V> iterImpl) {
        if (mSize < 1) {
            return;
        }

        if (iterImpl == null) {
            return;
        }

        listEntriesInOrder(mRoot, iterImpl);
    }


    //
    // Recursively loop to the tree and list out the keys and their values
    // Return true if it should continues listing out futher
    // Return false if it is done
    //
    private boolean listEntriesInOrder(BTNode<K, V> treeNode, BTIteratorIF<K, V> iterImpl) {
        if ((treeNode == null) ||
            (treeNode.mCurrentKeyNum == 0)) {
            return false;
        }

        boolean bStatus;
        BTKeyValue<K, V> keyVal;
        int currentKeyNum = treeNode.mCurrentKeyNum;
        for (int i = 0; i < currentKeyNum; ++i) {
            listEntriesInOrder(BTNode.getLeftChildAtIndex(treeNode, i), iterImpl);

            keyVal = treeNode.mKeys[i];
            bStatus = iterImpl.item(keyVal.mKey, keyVal.mValue);
            if (!bStatus) {
                return false;
            }

            if (i == currentKeyNum - 1) {
                listEntriesInOrder(BTNode.getRightChildAtIndex(treeNode, i), iterImpl);
            }
        }

        return true;
    }


    //
    // Supprimer une clé dans l'arbre
    // Retourner la valeur si il existe et le supprimer
    // Retourner null si la clé n'existe pas
    //
    public V delete(K key) {
        mIntermediateInternalNode = null;
        BTKeyValue<K, V> keyVal = deleteKey(null, mRoot, key, 0);
        if (keyVal == null) {
            return null;
        }
        --mSize;
        return keyVal.mValue;
    }


    //
    // Supprimer une clé dans l'arbre d'un noeud
    //
    private BTKeyValue<K, V> deleteKey(BTNode<K, V> parentNode, BTNode<K, V> btNode, K key, int nodeIdx) {
        int i;
        int nIdx;
        BTKeyValue<K, V> retVal;

        if (btNode == null) {
            // L'arbre est vide
            return null;
        }

        if (btNode.mIsLeaf) {
            nIdx = searchKey(btNode, key);
            if (nIdx < 0) {
                // Impossible de trouver la clé spécifiée
                return null;
            }

            retVal = btNode.mKeys[nIdx];

            if ((btNode.mCurrentKeyNum > BTNode.LOWER_BOUND_KEYNUM) || (parentNode == null)) {
                // Retirer la clé à partir du noeud courant
                for (i = nIdx; i < btNode.mCurrentKeyNum - 1; ++i) {
                    btNode.mKeys[i] = btNode.mKeys[i + 1];
                }
                btNode.mKeys[i] = null;
                --(btNode.mCurrentKeyNum);

                if (btNode.mCurrentKeyNum == 0) {
                    // btNode est maintenant le noeud racine
                    mRoot = null;
                }

                return retVal;
            }

            // Trouver le frère gauche
            BTNode<K, V> rightSibling;
            BTNode<K, V> leftSibling = BTNode.getLeftSiblingAtIndex(parentNode, nodeIdx);
            if ((leftSibling != null) && (leftSibling.mCurrentKeyNum > BTNode.LOWER_BOUND_KEYNUM)) {
                // Retirer la clé et emprunter une clé à partir du frère gauche
                moveLeftLeafSiblingKeyWithKeyRemoval(btNode, nodeIdx, nIdx, parentNode, leftSibling);
            }
            else {
                rightSibling = BTNode.getRightSiblingAtIndex(parentNode, nodeIdx);
                if ((rightSibling != null) && (rightSibling.mCurrentKeyNum > BTNode.LOWER_BOUND_KEYNUM)) {
                //Retirer la clé et emprunter une clé à partir du frère droit
                moveRightLeafSiblingKeyWithKeyRemoval(btNode, nodeIdx, nIdx, parentNode, rightSibling);
                }
                else {
                    // Fusionner
                    boolean isRebalanceNeeded = false;
                    boolean bStatus;
                    if (leftSibling != null) {
                        // Fusionner avec le frère gauche
                        bStatus = doLeafSiblingMergeWithKeyRemoval(btNode, nodeIdx, nIdx, parentNode, leftSibling, false);
                        if (!bStatus) {
                            isRebalanceNeeded = false;
                        }
                        else if (parentNode.mCurrentKeyNum < BTNode.LOWER_BOUND_KEYNUM) {
                            // On doit Rééquilibrer l'arbre
                            isRebalanceNeeded = true;
                        }
                    }
                    else {
                        // Fusionner avec le frère droit
                        bStatus = doLeafSiblingMergeWithKeyRemoval(btNode, nodeIdx, nIdx, parentNode, rightSibling, true);
                        if (!bStatus) {
                            isRebalanceNeeded = false;
                        }
                        else if (parentNode.mCurrentKeyNum < BTNode.LOWER_BOUND_KEYNUM) {
                            // On doit Rééquilibrer l'arbre
                            isRebalanceNeeded = true;
                        }
                    }

                    if (isRebalanceNeeded && (mRoot != null)) {
                        rebalanceTree(mRoot, parentNode, parentNode.mKeys[0].mKey);
                    }
                }
            }

            return retVal;
        }
        
        // A cette position le noeud est interne
        nIdx = searchKey(btNode, key);
        if (nIdx >= 0) {
            // On a trouvé la clé du noeud interne

            // Triuver son prédécesseur
            mIntermediateInternalNode = btNode;
            mNodeIdx = nIdx;
            BTNode<K, V> predecessorNode =  findPredecessor(btNode, nIdx);
            BTKeyValue<K, V> predecessorKey = predecessorNode.mKeys[predecessorNode.mCurrentKeyNum - 1];

            // Echanger les données du clé supprimée avec son prédécesseur
            BTKeyValue<K, V> deletedKey = btNode.mKeys[nIdx];
            btNode.mKeys[nIdx] = predecessorKey;
            predecessorNode.mKeys[predecessorNode.mCurrentKeyNum - 1] = deletedKey;

            return deleteKey(mIntermediateInternalNode, predecessorNode, deletedKey.mKey, mNodeIdx);
        }

        // Trouver sous-arbre du fils qui contient la clé
        i = 0;
        BTKeyValue<K, V> currentKey = btNode.mKeys[0];
        while ((i < btNode.mCurrentKeyNum) && (key.compareTo(currentKey.mKey) > 0)) {
            ++i;
            if (i < btNode.mCurrentKeyNum) {
                currentKey = btNode.mKeys[i];
            }
            else {
                --i;
                break;
            }
        }

        BTNode<K, V> childNode;
        if (key.compareTo(currentKey.mKey) > 0) {
            childNode = BTNode.getRightChildAtIndex(btNode, i);
            if (childNode.mKeys[0].mKey.compareTo(btNode.mKeys[btNode.mCurrentKeyNum - 1].mKey) > 0) {
                // Le coté le plus à droit du noeud
                i = i + 1;
            }
        }
        else {
            childNode = BTNode.getLeftChildAtIndex(btNode, i);
        }

        return deleteKey(btNode, childNode, key, i);
    }


    //
    // Remove the specified key and move a key from the right leaf sibling to the node
    // Note: The node and its sibling must be leaves
    //
    private void moveRightLeafSiblingKeyWithKeyRemoval(BTNode<K, V> btNode,
                                                       int nodeIdx,
                                                       int keyIdx,
                                                       BTNode<K, V> parentNode,
                                                       BTNode<K, V> rightSiblingNode) {
        // Shift to the right where the key is deleted
        for (int i = keyIdx; i < btNode.mCurrentKeyNum - 1; ++i) {
            btNode.mKeys[i] = btNode.mKeys[i + 1];
        }

        btNode.mKeys[btNode.mCurrentKeyNum - 1] = parentNode.mKeys[nodeIdx];
        parentNode.mKeys[nodeIdx] = rightSiblingNode.mKeys[0];

        for (int i = 0; i < rightSiblingNode.mCurrentKeyNum - 1; ++i) {
            rightSiblingNode.mKeys[i] = rightSiblingNode.mKeys[i + 1];
        }

        --(rightSiblingNode.mCurrentKeyNum);
    }


    //
    // Remove the specified key and move a key from the left leaf sibling to the node
    // Note: The node and its sibling must be leaves
    //
    private void moveLeftLeafSiblingKeyWithKeyRemoval(BTNode<K, V> btNode,
                                                      int nodeIdx,
                                                      int keyIdx,
                                                      BTNode<K, V> parentNode,
                                                      BTNode<K, V> leftSiblingNode) {
        // Use the parent key on the left side of the node
        nodeIdx = nodeIdx - 1;

        // Shift to the right to where the key will be deleted 
        for (int i = keyIdx; i > 0; --i) {
            btNode.mKeys[i] = btNode.mKeys[i - 1];
        }

        btNode.mKeys[0] = parentNode.mKeys[nodeIdx];
        parentNode.mKeys[nodeIdx] = leftSiblingNode.mKeys[leftSiblingNode.mCurrentKeyNum - 1];
        --(leftSiblingNode.mCurrentKeyNum);
    }


    //
    // Do the leaf sibling merge
    // Return true if we need to perform futher re-balancing action
    // Return false if we reach and update the root hence we don't need to go futher for re-balancing the tree
    //
    private boolean doLeafSiblingMergeWithKeyRemoval(BTNode<K, V> btNode,
                                                     int nodeIdx,
                                                     int keyIdx,
                                                     BTNode<K, V> parentNode,
                                                     BTNode<K, V> siblingNode,
                                                     boolean isRightSibling) {
        int i;

        if (nodeIdx == parentNode.mCurrentKeyNum) {
            // Case node index can be the right most
            nodeIdx = nodeIdx - 1;
        }

        if (isRightSibling) {
            // Shift the remained keys of the node to the left to remove the key
            for (i = keyIdx; i < btNode.mCurrentKeyNum - 1; ++i) {
                btNode.mKeys[i] = btNode.mKeys[i + 1];
            }
            btNode.mKeys[i] = parentNode.mKeys[nodeIdx];
        }
        else {
            // Here we need to determine the parent node id based on child node id (nodeIdx)
            if (nodeIdx > 0) {
                if (siblingNode.mKeys[siblingNode.mCurrentKeyNum - 1].mKey.compareTo(parentNode.mKeys[nodeIdx - 1].mKey) < 0) {
                    nodeIdx = nodeIdx - 1;
                }
            }

            siblingNode.mKeys[siblingNode.mCurrentKeyNum] = parentNode.mKeys[nodeIdx];
            // siblingNode.mKeys[siblingNode.mCurrentKeyNum] = parentNode.mKeys[0];
            ++(siblingNode.mCurrentKeyNum);

            // Shift the remained keys of the node to the left to remove the key
            for (i = keyIdx; i < btNode.mCurrentKeyNum - 1; ++i) {
                btNode.mKeys[i] = btNode.mKeys[i + 1];
            }
            btNode.mKeys[i] = null;
            --(btNode.mCurrentKeyNum);
        }

        if (isRightSibling) {
            for (i = 0; i < siblingNode.mCurrentKeyNum; ++i) {
                btNode.mKeys[btNode.mCurrentKeyNum + i] = siblingNode.mKeys[i];
                siblingNode.mKeys[i] = null;
            }
            btNode.mCurrentKeyNum += siblingNode.mCurrentKeyNum;
        }
        else {
            for (i = 0; i < btNode.mCurrentKeyNum; ++i) {
                siblingNode.mKeys[siblingNode.mCurrentKeyNum + i] = btNode.mKeys[i];
                btNode.mKeys[i] = null;
            }
            siblingNode.mCurrentKeyNum += btNode.mCurrentKeyNum;
            btNode.mKeys[btNode.mCurrentKeyNum] = null;
        }

        // Shift the parent keys accordingly after the merge of child nodes
        for (i = nodeIdx; i < parentNode.mCurrentKeyNum - 1; ++i) {
            parentNode.mKeys[i] = parentNode.mKeys[i + 1];
            parentNode.mChildren[i + 1] = parentNode.mChildren[i + 2];
        }
        parentNode.mKeys[i] = null;
        parentNode.mChildren[parentNode.mCurrentKeyNum] = null;
        --(parentNode.mCurrentKeyNum);

        if (isRightSibling) {
            parentNode.mChildren[nodeIdx] = btNode;
        }
        else {
            parentNode.mChildren[nodeIdx] = siblingNode;
        }

        if ((mRoot == parentNode) && (mRoot.mCurrentKeyNum == 0)) {
            // Only root left
            mRoot = parentNode.mChildren[nodeIdx];
            mRoot.mIsLeaf = true;
            return false;  // Root has been changed, we don't need to go futher
        }

        return true;
    }


    //
    // Re-balance the tree at a specified node
    // Params:
    // parentNode = the parent node of the node needs to be re-balanced
    // btNode = the node needs to be re-balanced
    // nodeIdx = the index of the parent node's child array where the node belongs
    // balanceType = either REBALANCE_FOR_LEAF_NODE or REBALANCE_FOR_INTERNAL_NODE
    //   REBALANCE_FOR_LEAF_NODE: the node is a leaf
    //   REBALANCE_FOR_INTERNAL_NODE: the node is an internal node
    // Return:
    // true if it needs to continue rebalancing further
    // false if further rebalancing is no longer needed
    //
    private boolean rebalanceTreeAtNode(BTNode<K, V> parentNode, BTNode<K, V> btNode, int nodeIdx, int balanceType) {
        if (balanceType == REBALANCE_FOR_LEAF_NODE) {
            if ((btNode == null) || (btNode == mRoot)) {
                return false;
            }
        }
        else if (balanceType == REBALANCE_FOR_INTERNAL_NODE) {
            if (parentNode == null) {
                // Root node
                return false;
            }
        }

        if (btNode.mCurrentKeyNum >= BTNode.LOWER_BOUND_KEYNUM) {
            // The node doesn't need to rebalance
            return false;
        }

        BTNode<K, V> rightSiblingNode;
        BTNode<K, V> leftSiblingNode = BTNode.getLeftSiblingAtIndex(parentNode, nodeIdx);
        if ((leftSiblingNode != null) && (leftSiblingNode.mCurrentKeyNum > BTNode.LOWER_BOUND_KEYNUM)) {
            // Do right rotate
            performRightRotation(btNode, nodeIdx, parentNode, leftSiblingNode);
        }
        else {
            rightSiblingNode = BTNode.getRightSiblingAtIndex(parentNode, nodeIdx);
            if ((rightSiblingNode != null) && (rightSiblingNode.mCurrentKeyNum > BTNode.LOWER_BOUND_KEYNUM)) {
                // Do left rotate
                performLeftRotation(btNode, nodeIdx, parentNode, rightSiblingNode);
            }
            else {
                // Merge the node with one of the siblings
                boolean bStatus;
                if (leftSiblingNode != null) {
                    bStatus = performMergeWithLeftSibling(btNode, nodeIdx, parentNode, leftSiblingNode);
                }
                else {
                    bStatus = performMergeWithRightSibling(btNode, nodeIdx, parentNode, rightSiblingNode);
                }

                if (!bStatus) {
                    return false;
                }
            }
        }

        return true;
    }


    //
    // Re-balance the tree upward from the lower node to the upper node
    //
    private void rebalanceTree(BTNode<K, V> upperNode, BTNode<K, V> lowerNode, K key) {
        mStackTracer.clear();
        mStackTracer.add(new StackInfo(null, upperNode, 0));

        //
        // Find the child subtree (node) that contains the key
        //
        BTNode<K, V> parentNode, childNode;
        BTKeyValue<K, V> currentKey;
        int i;
        parentNode = upperNode;
        while ((parentNode != lowerNode) && !parentNode.mIsLeaf) {
            currentKey = parentNode.mKeys[0];
            i = 0;
            while ((i < parentNode.mCurrentKeyNum) && (key.compareTo(currentKey.mKey) > 0)) {
                ++i;
                if (i < parentNode.mCurrentKeyNum) {
                    currentKey = parentNode.mKeys[i];
                }
                else {
                    --i;
                    break;
                }
            }

            if (key.compareTo(currentKey.mKey) > 0) {
                childNode = BTNode.getRightChildAtIndex(parentNode, i);
                if (childNode.mKeys[0].mKey.compareTo(parentNode.mKeys[parentNode.mCurrentKeyNum - 1].mKey) > 0) {
                    // The right-most side of the node
                    i = i + 1;
                }
            }
            else {
                childNode = BTNode.getLeftChildAtIndex(parentNode, i);
            }

            if (childNode == null) {
                break;
            }

            if (key.compareTo(currentKey.mKey) == 0) {
                break;
            }

            mStackTracer.add(new StackInfo(parentNode, childNode, i));
            parentNode = childNode;
        }

        boolean bStatus;
        StackInfo stackInfo;
        while (!mStackTracer.isEmpty()) {
            stackInfo = mStackTracer.pop();
            if ((stackInfo != null) && !stackInfo.mNode.mIsLeaf) {
                bStatus = rebalanceTreeAtNode(stackInfo.mParent,
                                              stackInfo.mNode,
                                              stackInfo.mNodeIdx,
                                              REBALANCE_FOR_INTERNAL_NODE);
                if (!bStatus) {
                    break;
                }
            }
        }
    }


    /**
     * Inner class StackInfo for tracing-back purpose
     * Structure contains parent node and node index
     */
    public class StackInfo {
        public BTNode<K, V> mParent = null;
        public BTNode<K, V> mNode = null;
        public int mNodeIdx = -1;

        public StackInfo(BTNode<K, V> parent, BTNode<K, V> node, int nodeIdx) {
            mParent = parent;
            mNode = node;
            mNodeIdx = nodeIdx;
        }
    }
}
