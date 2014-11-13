package taojava.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A randomized implementation of sorted lists.  
 * 
 * @author Samuel A. Rebelsky
 * @author Ezra Edgerton
 * 
 * I looked at Noah Schlager's github repo for this problem to solve some class and
 * field declaration decisions I was making.
 * https://github.com/Schlager/skip-lists-assignment/blob/master/src/taojava/util/SkipList.java
 * The methods were outlined in pseudocode in this essay:
 * http://delivery.acm.org/10.1145/80000/78977/p668-pugh.pdf?ip=132.161.247.168&id=7
 * 8977&acc=ACTIVE%20SERVICE&key=B63ACEF81C6334F5%2EE00B5EE484F84B68%2E4D4702B0C3E38
 * B35%2E4D4702B0C3E38B35&CFID=596042719&CFTOKEN=71125015&__acm__=1415384583_386c3ab
 * 4891ac74920c72d8bc681ddae
 */

public class SkipList<T extends Comparable<T>>
    implements SortedList<T>
{
  
  // +--------+----------------------------------------------------------
  // | Fields |
  // +--------+

  Node<T> head;
  int size;
  int maxLevel;
  double prob;

  // +------------------+------------------------------------------------
  // | Internal Classes |
  // +------------------+

  /**
   * Nodes for skip lists.
   */
  public class Node<T>
  {
    // +--------+--------------------------------------------------------
    // | Fields |
    // +--------+

    /**
     * The value stored in the node.
     */
    T val;
    /**
     * Node array with all the levels
     */
    private Node<T>[] next;

    int level;
    
    int nodesPast;

    //Constructor
    public Node()
    {
      this.level = maxLevel;
      next = new Node[maxLevel];
      this.nodesPast = 0;
      for (int lvlInit = 0; lvlInit < this.level; lvlInit++)
        {
          next[lvlInit] = null;
        }//for
    }//constructor

    public Node(T val, int lvl)
    {
      this.level = lvl;
      this.val = val;
      next = new Node[lvl];
      this.nodesPast = 0;
      for (int lvlInit = 0; lvlInit < this.level; lvlInit++)
        {
          next[lvlInit] = null;
        }//for
    }//constructor Node(val, lvl)

  } // class Node

  // +--------------+----------------------------------------------------
  // | Constructors |
  // +--------------+
  public SkipList()
  {
    this.maxLevel = 20;
    this.prob = .5;
    int size = 0;
    this.head = new Node<T>();
  }

  public SkipList(double probability)
  {
    this.maxLevel = 20;
    this.prob = probability;
    int size = 0;
    this.head = new Node<T>();
  }

  // +-------------------------+-----------------------------------------
  // | Internal Helper Methods |
  // +-------------------------+

  //returns the height of the SkipNode
  public int heightMaker()
  {
    int height = 1;
    double calc = Math.random();
    while (this.prob > calc)
      {
        height++;
        calc = Math.random();
      }
    return Math.min(height, this.maxLevel);
  }//heightMaker

  // +-----------------------+-------------------------------------------
  // | Methods from Iterable |
  // +-----------------------+

  /**
   * Return a read-only iterator
   *  that iterates the values of the list from smallest to
   * largest.
   */
  public Iterator<T> iterator()
  {
    return new Iterator<T>()
      {
        Node<T> worker = SkipList.this.head;

        //returns true if iterator has a next.
        public boolean hasNext()
        {

          return this.worker.next[0] != null;
        }

        //returns the next value in the skiplist at the lowest level
        public T next()
        {
          if (!this.hasNext())
            {
              throw new NoSuchElementException();
            }
          this.worker = this.worker.next[0];
          return this.worker.val;
        }//next

      
      //taken from Noah Schlager's  
        public void remove()
        {
         SkipList.this.remove(this.worker.val);
        }
        //remove
      };

  } // iterator()

  // +------------------------+------------------------------------------
  // | Methods from SimpleSet |
  // +------------------------+

  /**
   * Add a value to the set.
   *
   * @post contains(val)
   * @post For all lav != val, if contains(lav) held before the call
   *   to add, contains(lav) continues to hold.
   */
  public void add(T val)
  {
    Node<T> worker = this.head;
    Node<T>[] update = new Node[maxLevel];
    
    //prevents incorrect indexing(inefficiently)
   /* if(this.contains(val)){
      return;
    }*/

    //works through levels
    for (int i = worker.level - 1; i >= 0; i--)
      {
        //checks nodes in levels
        while (worker.next[i] != null && worker.next[i].val.compareTo(val) < 0)
          {
            //move through
            worker.nodesPast++;
            worker = worker.next[i];
          }//while
        update[i] = worker;
      }//for
    worker = worker.next[0];
    if (worker != null && worker.val.compareTo(val) == 0)
      {
        return;
      }//if
    else
      {
        //creates new node level
        int newLevel = heightMaker();
        
        worker = new Node<T>(val, newLevel);

        for (int i = 0; i < newLevel; i++)
          {
            worker.next[i] = update[i].next[i];
            update[i].next[i] = worker;
          }//for
      }//while
    this.size++;
  } // add(T val)

  /**
   * Determine if the set contains a particular value.
   */
  public boolean contains(T val)
  {
    Node<T> worker = this.head;
    for (int i = worker.level - 1; i >= 0; i--)
      {
        while (worker.next[i] != null && worker.next[i].val.compareTo(val) < 0)
          {
            worker = worker.next[i];
          }//while 
      }//for

    worker = worker.next[0];
    if (worker != null && worker.val.equals(val))
      {
        return true;
      }
    else
      {
        return false;
      }
  } // contains(T)

  /**
   * Remove an element from the set.
   *
   * @post !contains(val)
   * @post For all lav != val, if contains(lav) held before the call
   *   to remove, contains(lav) continues to hold.
   */
  public void remove(T val)
  {
    Node<T>[] update = new Node[maxLevel];
    Node<T> worker = this.head;
    //prevents incorrect indexing (very inefficient)
    /*
    if(!this.contains(val)){
      return;
    }*/

    for (int i = worker.level - 1; i <= 0; i--)
      {
        while (worker.next[i] != null && worker.next[i].val.compareTo(val) < 0)
          {
            worker.nodesPast--;
            worker = worker.next[i];
          }
        update[i] = worker;
      }
    worker = worker.next[0];
    if (worker != null && worker.val.equals(val))
      {
        for (int i = 0; i < worker.level; i++)
          {
            if (update[i].next[i] != worker)
              {
                break;
              }
            update[i].next[i] = worker.next[i];
          }//for
      }//if  
    this.size--;
  } // remove(T)

  // +--------------------------+----------------------------------------
  // | Methods from SemiIndexed |
  // +--------------------------+
  
  /*
   * Invariants for indexed :
   * -this.size == number of elements in skiplist
   * -after add is called, this.size++ && the index of the inserted element is 
   * where it is in the ordered skiplist.
   * -after remove is called this.size--
   * -after remove is called the values following it have an index of --index.
   * 
   * o
   * o-----o
   * o--o--o
   * o--o--o--o
   * h  3  5  7 --values
   *    1  2  3 --indexes
   * 
   * 
   */

  /**
   * Get the element a
   * t index index.
   *
   * @throws IndexOutOfBoundsException
   *   if the index is out of range (index < 0 || index >= length)
   */
  public T get(int index)
  {
    //my attempt
/*    if(index < 0 || index >= this.size){
      throw new IndexOutOfBoundsException();
    }
    Node<T>worker = this.head;
    for (int i = worker.level - 1; i >= 0; i--)
      {
        while (worker.next[0] != null && (size - worker.nodesPast) < index)
          {
            worker = worker.next[i];
          }//while 
      }//for

    worker = worker.next[0];
        return worker.val;*/
    return null;
  } // get(int)

  /**
   * Determine the number of elements in the collection.
   */
  public int length()
  {
    return this.size;
  }//length()

} // class SkipList<T>

/*
 *Part 4:
 *Although I got remove working some of the time, I could not fix it to work the rest of the time.
 *so I've got no analysis to give unfortunately. I would assume that it would be more efficient on reversed lists.
 *
 * 
 * 
 * Part 5:
 * Working with the same objects over an entire (string, ints) test seems to simplify things.
 *  I could see using this in many future tests.
 *  
 *  This wasn't really part of the code reading, but translating the pseudocode in the SkipList reading
 *  into real code was a valuable thing to practice.
 *  
 *  How the verbose sorted works well to see how we are doing specific actions in our code. It prints out everything
 *  that we do, and is a good example of that thing you talked about in class. Where you have a class that implements
 *  another class that is a third class in a triangle.
 *  I would use this on other programs I write when I want to see how and where things could be going wrong.
 * 
 */
