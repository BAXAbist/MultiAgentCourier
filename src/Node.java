import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class Node implements Comparable<Node> {
  // Id for readability of result purposes
  private static int idCounter = 0;
  public int id;

  // Parent in the path
  public Node parent = null;

  public List<Node> neighbors;

  // Evaluation functions
  public int f = 0;
  public int g = 0;
  // Hardcoded heuristic
  public int h = 0;
  public int Ox;
  public int Oy;
  public boolean isReachable = true;

  Node(int Oy, int Ox) {
    this.Ox = Ox;
    this.Oy = Oy;
    this.id = idCounter++;
    this.neighbors = new ArrayList<>();
  }

  @Override
  public int compareTo(Node n) {
    return Integer.compare(this.f, n.f);
  }

    /*public static class Edge {
        Edge(int weight, Node node){
            this.weight = weight;
            this.node = node;
        }

        public int weight;
        public Node node;
    }*/

    /*public void addBranch(int weight, Node node){
        Edge newEdge = new Edge(weight, node);
        neighbors.add(newEdge);
    }*/

  public int calculateHeuristic(Node target) {
    return Math.abs(this.Ox - target.Ox) + Math.abs(this.Oy - target.Oy);
  }

  public static Node aStar(Node start, Node target) {
    PriorityQueue<Node> closedList = new PriorityQueue<>();
    PriorityQueue<Node> openList = new PriorityQueue<>();
    start.g = 0;
    start.f = start.g + start.calculateHeuristic(target);
    openList.add(start);

    while (!openList.isEmpty()) {
      Node n = openList.peek();
      if (n == target) {
        return n;
      }
      int totalWeight = n.g + 1;

      for (Node m : n.neighbors) {
        if (m.isReachable) {
          if (!openList.contains(m) && !closedList.contains(m)) {
            m.parent = n;
            m.g = totalWeight;
            m.f = m.g + m.calculateHeuristic(target);
            openList.add(m);
          } else {
            if (totalWeight < m.g) {
              m.parent = n;
              m.g = totalWeight;
              m.f = m.g + m.calculateHeuristic(target);

              if (closedList.contains(m)) {
                closedList.remove(m);
                openList.add(m);
              }
            }
          }
        }
      }

      openList.remove(n);
      closedList.add(n);
    }
    return null;
  }

  public static void printPath(Node target) {
    Node n = target;

    if (n == null)
      return;

    List<Integer> ids = new ArrayList<>();

    while (n.parent != null) {
      System.out.print("[" + n.Ox + ":" + n.Oy + "] ");
      n = n.parent;
    }
        /*ids.add(n.id);
        Collections.reverse(ids);

        for(int id : ids){

        }*/
    System.out.println("");
  }
}
