package com.github.zottaa.binarytree;

import java.util.Comparator;

public interface BinaryTree {
    public boolean add(UserType item);

    public boolean delete(int index);

    public UserType at(int index);

    public boolean isEmpty();

    public int size();

    public void balance();

    public void forEach(ElementProcessor<UserType> processor);

    public void forEachFromRoot(ElementProcessor<UserType> processor);

    public void clear();

    abstract class Abstract implements BinaryTree {
        Abstract() {
            this.root = null;
            this.size = 0;
            this.comparator = null;
        }

        Abstract(Node root) {
            this.root = root;
            this.size = 1;
            this.comparator = root.item.getTypeComparator();
        }

        public class Node {
            public Node left;
            public Node right;
            public UserType item;
            public int weight;

            Node() {
                this.item = null;
                this.left = null;
                this.right = null;
                this.weight = 0;
            }

            Node(UserType item) {
                this.item = item;
                this.left = null;
                this.right = null;
                this.weight = 1;
            }

            Node(UserType item, Node left, Node right) {
                this.item = item;
                this.left = left;
                this.right = right;
                this.weight = left.weight + right.weight + 1;
            }
        }

        protected Node root;
        private int size;

        private Comparator<Object> comparator;

        @Override
        public int size() {
            return this.size;
        }

        @Override
        public boolean add(UserType item) {
            if (root == null) {
                this.comparator = item.getTypeComparator();
                this.root = new Node(item);
                this.size++;
                return true;
            }
            return add(root, item);
        }

        @Override
        public void clear() {
            while (size != 0) {
                this.delete(0);
            }
        }

        @Override
        public boolean isEmpty() {
            return size == 0;
        }

        private boolean add(Node current, UserType item) {
            int comparisonResult = comparator.compare(current.item, item);

            if (comparisonResult == 0) {
                restoreWeights(root, item);
                return false;
            }
            current.weight += 1;

            if (comparisonResult > 0) {
                if (current.left == null) {
                    current.left = new Node(item);
                    size++;
                    return true;
                } else {
                    return add(current.left, item);
                }
            } else {
                if (current.right == null) {
                    current.right = new Node(item);
                    size++;
                    return true;
                } else {
                    return add(current.right, item);
                }
            }
        }

        private void restoreWeights(Node current, UserType item) {
            if (current == null || comparator.compare(current.item,item) == 0) {
                return;
            }

            current.weight -= 1;

            if (comparator.compare(current.item, item) > 0) {
                restoreWeights(current.left, item);
            } else {
                restoreWeights(current.right, item);
            }
        }

        @Override
        public boolean delete(int index) {
            if (this.root == null || index < 0 || index >= size)
                return false;
            return delete(this.root, index, null);
        }

        //Used only in delete
        private Node findMin(Node current, Node previous, Node deletable) {
            current.weight -= 1;
            if (current.left == null) {
                if (current.right != null && previous != deletable)
                    previous.left = current.right;
                else if (previous != deletable)
                    previous.left = null;
                return current;
            }
            return findMin(current.left, current, deletable);
        }

        private boolean delete(Node current, int index, Node previous) {
            int currentIndex = current.left != null ? current.left.weight : 0;

            current.weight -= 1;
            if (currentIndex < index) {
                return delete(current.right, index - currentIndex - 1, current);
            } else if (currentIndex > index) {
                return delete(current.left, index, current);
            } else {
                if (current.left == null || current.right == null) {
                    Node newNode = null;
                    if (current.left == null) {
                        newNode = current.right;
                    } else {
                        newNode = current.left;
                    }
                    if (previous != null) {
                        if (previous.left == current) {
                            previous.left = newNode;
                        } else {
                            previous.right = newNode;
                        }
                    } else {
                        if (newNode != null)
                            newNode.weight = (root.weight) - 1;
                        root = newNode;
                    }
                } else {
                    Node temp = findMin(current.right, current, current);
                    if (current.left != temp) {
                        temp.left = current.left;
                        if (current.right != temp)
                            temp.right = current.right;
                    } else {
                        temp.right = current.right;
                        if (current.left != temp)
                            temp.left = current.left;
                    }
                    if (previous != null) {
                        temp.weight = current.weight - 1;
                        if (previous.left == current) {
                            previous.left = temp;
                        } else {
                            previous.right = temp;
                        }
                    } else {
                        temp.weight = (root.weight) - 1;
                        root = temp;
                    }
                }
                size--;
                return true;
            }
        }

        @Override
        public UserType at(int index) {
            if (index < 0 || index >= size || root == null) {
                return null;
            }

            int currentIndex = this.root.left != null ? this.root.left.weight : 0;
            if (currentIndex == index)
                return this.root.item;


            return currentIndex < index ?
                    at(this.root.right, index - currentIndex - 1) :
                    at(this.root.left, index);
        }

        private UserType at(Node current, int index) {
            int currentIndex = current.left != null ? current.left.weight : 0;
            if (currentIndex == index)
                return current.item;

            return currentIndex < index ?
                    at(current.right, index - currentIndex - 1) :
                    at(current.left, index);
        }

        @Override
        public void balance() {
            Node dummy = new Node();
            dummy.right = this.root;
            treeToVine(dummy);
            vineToTree(dummy, size);
            this.root = dummy.right;
            recalculateWeights();
        }

        private void treeToVine(Node root) {
            Node tail = root;
            Node rest = tail.right;
            while (rest != null) {
                if (rest.left == null) {
                    tail = rest;
                    rest = rest.right;
                } else {
                    Node temp = rest.left;
                    rest.left = temp.right;
                    temp.right = rest;
                    rest = temp;
                    tail.right = temp;
                }
            }
        }

        private void vineToTree(Node root, int size) {
            int leaves = (int) (size + 1 - Math.pow(2, Math.log(size + 1) / Math.log(2)));
            compress(root, leaves);
            size = size - leaves;
            while (size > 1) {
                compress(root, (int) size / 2);
                size = (int) size / 2;
            }
        }

        private void compress(Node root, int count) {
            Node scanner = root;
            for (int i = 0; i < count; i++) {
                Node child = scanner.right;
                scanner.right = child.right;
                scanner = scanner.right;
                child.right = scanner.left;
                scanner.left = child;
            }
        }

        private void recalculateWeights() {
            recalculateWeights(root);
        }

        private int recalculateWeights(Node node) {
            if (node == null) {
                return 0;
            }

            int leftWeight = recalculateWeights(node.left);
            int rightWeight = recalculateWeights(node.right);

            node.weight = leftWeight + rightWeight + 1;

            return node.weight;
        }

        @Override
        public void forEach(ElementProcessor<UserType> processor) {
            inOrderTraversal(root, processor);
        }

        private void inOrderTraversal(Node node, ElementProcessor<UserType> processor) {
            if (node != null) {
                inOrderTraversal(node.left, processor);
                processor.toDo(node.item);
                inOrderTraversal(node.right, processor);
            }
        }

        @Override
        public void forEachFromRoot(ElementProcessor<UserType> processor) {
            fromRootOrder(this.root, processor);
        }

        private void fromRootOrder(Node node, ElementProcessor<UserType> processor) {
            if (node != null) {
                processor.toDo(node.item);
                fromRootOrder(node.left, processor);
                fromRootOrder(node.right, processor);
            }
        }

        @Override
        public String toString() {
            StringBuilder treeString = new StringBuilder();
            buildTreeString(root, 0, treeString);
            return treeString.toString();
        }

        private void buildTreeString(Node node, int level, StringBuilder treeString) {
            if (node != null) {
                buildTreeString(node.right, level + 1, treeString);
                for (int i = 0; i < level; i++) {
                    treeString.append("         ");
                }
                treeString.append(node.item).append("\n");
                buildTreeString(node.left, level + 1, treeString);
            }
        }
    }

    class Base extends Abstract {
        public Base() {
            super();
        }
    }
}
