import java.util.*;
import tester.Tester;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// Extra Credit: 
// Created Toggled path
// wrong moves for both searches

// represents the edge that connects to verticies
class Edge implements Comparable<Edge> {
  Vertex start;
  Vertex end;
  int weight;
  boolean isWall;

  Edge(Vertex start, Vertex end, int weight, boolean isWall) {
    this.start = start;
    this.end = end;
    this.weight = weight;
    this.isWall = isWall;
  }

  @Override
  // compares two edges by weight
  public int compareTo(Edge o) {
    if (this.weight - o.weight == 0) {
      return this.weight - o.weight + 1;
    }
    else {
      return this.weight - o.weight;
    }
  }

}

// represents the square or position in the maze
class Vertex {
  int x;
  int y;
  Color c;
  Vertex left;
  Vertex right;
  Vertex top;
  Vertex bottom;
  
  Vertex(int x, int y, Color c) {
    this.x = x;
    this.y = y;
    this.c = c;
  }
  
  Vertex(int x, int y, Color c, Vertex left, Vertex right, Vertex top, Vertex bottom) {
    this.x = x;
    this.y = y;
    this.c = c;
  }
  
  // checks to see if the current position is blocked by a wall
  public boolean isBlocked(Vertex endVertex, Maze m) {
    for (Edge edge : m.edges) {
      if (edge.start == this && edge.end == endVertex || 
          edge.start == endVertex && edge.end == this) {
        return edge.isWall;
      }
    }
    return true;
  }
}

// represents the maze
class Maze extends World {
  Vertex curr;
  Posn size;
  ArrayList<ArrayList<Vertex>> verticies;

  ArrayList<Edge> edges;
  boolean isBFS;
  boolean isDFS;
  boolean isManual;
  boolean solved;
  boolean endGame;
  boolean isToggled = true;
  Random rand = new Random();
  Deque<Vertex> q = new LinkedList<Vertex>();
  Stack<Vertex> s = new Stack<Vertex>();
  HashMap<Vertex, Boolean> visited = new HashMap<Vertex, Boolean>();
  HashMap<Vertex, Vertex> prev = new HashMap<Vertex, Vertex>();


  Maze(Posn size) {
    this.size = size;
    this.verticies = createVerticies();
    this.edges = makeMaze();
    this.curr = this.verticies.get(0).get(0);
    
    for (ArrayList<Vertex> row : this.verticies) {
      for (Vertex curr : row) {
        this.visited.put(curr, false);
        this.prev.put(curr, null);
      }
    }
   
    
    q.add(this.verticies.get(0).get(0));   
    s.push(this.verticies.get(0).get(0));
    visited.put(this.verticies.get(0).get(0), true);
  }

  // creates a seeded maze
  Maze(Random rand, Posn size) {
    
    this.rand = rand;
    this.size = size;
    this.verticies = createVerticies();
    this.edges = makeMaze();
    this.curr = this.verticies.get(0).get(0);
    
    for (ArrayList<Vertex> row : this.verticies) {
      for (Vertex curr : row) {
        this.visited.put(curr, false);
        this.prev.put(curr, null);
      }
    }
    
    q.add(this.verticies.get(0).get(0));   
    s.push(this.verticies.get(0).get(0));
    visited.put(this.verticies.get(0).get(0), true);
  }

  // creates the walls for the maze
  ArrayList<Edge> makeMaze() {
    ArrayList<Edge> edgeList = new ArrayList<Edge>();

    for (int i = 0; i < this.size.y; i++) {
      for (int j = 0; j < this.size.x; j++) {
        
        if (j > 0) {
          this.verticies.get(i).get(j).left = this.verticies.get(i).get(j - 1);
        }

        if (j < this.size.x - 1) {
          this.verticies.get(i).get(j).right = this.verticies.get(i).get(j + 1);
        }

        if (i > 0) {
          this.verticies.get(i).get(j).top = this.verticies.get(i - 1).get(j);
        }

        if (i < this.size.y - 1) {
          this.verticies.get(i).get(j).bottom = this.verticies.get(i + 1).get(j);

        }
        
        
        if (i < this.size.y - 1) {
          edgeList.add(new Edge(verticies.get(i).get(j), verticies.get(i + 1).get(j),
              this.rand.nextInt(this.size.x * this.size.y), true));
        }

        if (j < this.size.x - 1) {
          edgeList.add(new Edge(verticies.get(i).get(j), verticies.get(i).get(j + 1),
              this.rand.nextInt(this.size.x * this.size.y), true));
        }
      }
    }
    
    this.edges = edgeList;
    this.addSortedWeight();
    this.createWalls();
    return edgeList;
  }

  // creates the verticies for the maze
  public ArrayList<ArrayList<Vertex>> createVerticies() {
    ArrayList<ArrayList<Vertex>> board = new ArrayList<ArrayList<Vertex>>();
    for (int i = 0; i < this.size.y; i++) {
      ArrayList<Vertex> row = new ArrayList<Vertex>();
      for (int j = 0; j < this.size.x; j++) {
        if (j == 0 && i == 0) {
          row.add(new Vertex(j, i, Color.GREEN));
        }
        else if (j == this.size.x - 1 && i == this.size.y - 1) {
          row.add(new Vertex(j, i, Color.MAGENTA));
        }
        else {
          row.add(new Vertex(j, i, Color.LIGHT_GRAY));
        }
            
      }
      
      board.add(row);
    }
    
    return board;
  }

  // sorts the edges by weight to perform Kruskal's algorithm on
  void addSortedWeight() {
    Collections.sort(this.edges);
  }

  // performs Kruskal's algorithm so edges become walls
  void createWalls() {
    HashMap<Vertex, Integer> trees = new HashMap<Vertex, Integer>();
    ArrayList<Edge> edgesInTree = new ArrayList<Edge>();
    for (ArrayList<Vertex> row : this.verticies) {
      for (Vertex v : row) {
        trees.put(v, v.x * 1000 + v.y);
      }
    }

    int i = 0;
    // System.out.println(this.verticies.size()*this.verticies.get(0).size());
    // System.out.println(edges.size());
    while (edgesInTree.size() < this.verticies.size() * this.verticies.get(0).size() - 1) {
      if (!(trees.get(this.edges.get(i).start).equals(trees.get(this.edges.get(i).end)))) {
        this.edges.get(i).isWall = false;
        edgesInTree.add(this.edges.get(i));
        int valueToReplace = trees.get(this.edges.get(i).end);
        int replaceWith = trees.get(this.edges.get(i).start);
        trees.forEach((k, v) -> {
          if (v == valueToReplace) {
            trees.replace(k, v, replaceWith);
          }
        });
      }
      i++;
    }
  }

  // (0,0) --> (0,0)
  // (0,1) --> (0,1)

  // draws the game
  public WorldScene makeScene() {
    int wrongMoves = 0;
    int scale = 600 / this.size.y;
    if (endGame) {
      this.endOfWorld("Game Over!");
      return lastScene("Game Over!");
    }
    else {
      WorldScene scene = new WorldScene(600 * this.size.x / this.size.y, 600);
      for (ArrayList<Vertex> row : this.verticies) {
        for (Vertex v : row) {
          if (v.c == Color.cyan && !this.isManual) {
            wrongMoves++;
          }
          scene.placeImageXY(new RectangleImage(scale, scale, OutlineMode.SOLID, v.c),
              v.x * scale + scale / 2, v.y * scale + scale / 2);
        }
      }

      for (Edge w : this.edges) {
        if (w.isWall) {
          if (w.end.y - w.start.y == 1) {
            scene.placeImageXY(new LineImage(
                new Posn((w.end.y - w.start.y) * scale, (w.end.x - w.start.x) * scale),
                Color.black), w.end.x * scale + scale / 2, w.end.y * scale);
          }
          else {
            scene.placeImageXY(
                new LineImage(
                    new Posn((w.end.y - w.start.y) * scale, (w.end.x - w.start.x) * scale),
                  Color.black),
                w.end.x * scale, w.end.y * scale + scale / 2);
          }
        }
      }
      if (solved) {
        scene.placeImageXY(new TextImage("The Maze is Solved", 45, FontStyle.BOLD, Color.RED),
            300 * this.size.x / this.size.y, 300);
      }
      scene.placeImageXY(new TextImage(String.valueOf(wrongMoves), scale, Color.white),
          5 + scale / 2,
          scale / 2);
      return scene;
    }
  }

  // ends the program when the game is over and calls the last draw state function
  public WorldScene lastScene(String msg) {
    WorldScene scene = new WorldScene(600 * this.size.x / this.size.y, 600);
    scene.placeImageXY(
        new OverlayImage(new TextImage("Game Over!", 50, Color.blue), new RectangleImage(
            600 * this.size.x / this.size.y, 600, OutlineMode.SOLID, Color.YELLOW)),
        300 * this.size.x / this.size.y, 300);
    return scene;
  }
  
  
  // animates the maze on every tick
  public void onTick() {
    if (this.isBFS) {
      this.searchHelper("BFS");
      Vertex start = this.verticies.get(0).get(0);
      Vertex end = this.verticies.get(this.size.y - 1).get(this.size.x - 1);
      if (this.prev.get(end) != null) {
        this.isBFS = false;
        this.drawPath(prev, start, end);
      }
    }

    if (this.isDFS) {
      this.searchHelper("DFS");
      Vertex start = this.verticies.get(0).get(0);
      Vertex end = this.verticies.get(this.size.y - 1).get(this.size.x - 1);
      if (this.prev.get(end) != null) {
        this.isDFS = false;
        this.drawPath(prev, start, end);
      }
    }
  }

  // performs Breadth first and depth first search for the maze
  public void searchHelper(String search) {
    Vertex vert;
    if (search.equals("BFS")) {
      vert = this.q.pollFirst();
    }
    else {
      vert = this.s.pop();
    }
    List<Vertex> neighbors = new ArrayList<Vertex>(
        Arrays.asList(vert.left, vert.right, vert.top, vert.bottom));
    neighbors.removeIf(s -> vert.isBlocked(s, this));
    for (Vertex next : neighbors) {
      if (next != null && !this.visited.get(next)) {
        if (search.equals("BFS")) {
          this.q.add(next);
        }
        else {
          this.s.push(next);
        }
        this.visited.put(next, true);
        next.c = Color.cyan;
        this.prev.put(next, vert);
      }
    }
  }

  // draws the solution path of the maze
  public void drawPath(HashMap<Vertex, Vertex> prev, Vertex start, Vertex end) {
    for (Vertex current = end; current != null; current = prev.get(current)) {
      current.c = Color.BLUE;
    }
  }
  
  // performs actions based on key pressed:
  // press "b" for bredth first search
  // press "d" for depth first search
  // press "m" to manually play the maze
  // press "r" to create a new random maze
  // press "t" to toggle the visited paths
  // press "e" to end the game
  public void onKeyEvent(String key) {
    if (key.equals("b")) {
      this.isBFS = true;
      this.isDFS = false;
      this.isManual = false;
      this.solved = false;
      this.isToggled = true;
      this.reset();
    }
    
    if (key.equals("d")) {
      this.isDFS = true;
      this.isBFS = false;
      this.isManual = false;
      this.solved = false;
      this.isToggled = true;
      this.reset();
    }

    if (key.equals("r")) {
      this.verticies = createVerticies();
      this.edges = makeMaze();
      this.isBFS = false;
      this.isDFS = false;
      this.isManual = false;
      this.isToggled = true;
      this.solved = false;
      
      for (ArrayList<Vertex> row : this.verticies) {
        for (Vertex curr : row) {
          this.visited.put(curr, false);
          this.prev.put(curr, null);
        }
      }
      
      q.add(this.verticies.get(0).get(0));   
      s.push(this.verticies.get(0).get(0));
      visited.put(this.verticies.get(0).get(0), true);
    }

    if (key.equals("m")) {
      this.isManual = true;
      this.isBFS = false;
      this.isDFS = false;
      this.solved = false;
      this.isToggled = true;
      this.curr = this.verticies.get(0).get(0);
      this.reset();
    }
    
    if (isManual) {
      if (key.equals("left")) {
        Vertex prevCurr = this.curr;
        this.move(new Posn(-1, 0));
        if (!this.visited.get(this.curr)) {
          this.visited.put(this.curr, true);
          this.prev.put(this.curr, prevCurr);
        }
      }
      
      if (key.equals("right")) {
        Vertex prevCurr = this.curr;
        this.move(new Posn(1, 0));
        if (!this.visited.get(this.curr)) {
          this.visited.put(this.curr, true);
          this.prev.put(this.curr, prevCurr);
        }
      }
      
      if (key.equals("up")) {
        Vertex prevCurr = this.curr;
        this.move(new Posn(0, -1));
        if (!this.visited.get(this.curr)) {
          this.visited.put(this.curr, true);
          this.prev.put(this.curr, prevCurr);
        }
      }
      
      if (key.equals("down")) {
        Vertex prevCurr = this.curr;
        this.move(new Posn(0, 1));
        if (!this.visited.get(this.curr)) {
          this.visited.put(this.curr, true);
          this.prev.put(this.curr, prevCurr);
        }
      }
      
      if (this.curr.x == this.size.x - 1 && this.curr.y == this.size.y - 1) {
        this.solved = true;
        Vertex start = this.verticies.get(0).get(0);
        Vertex end = this.verticies.get(this.size.y - 1).get(this.size.x - 1);
        this.drawPath(this.prev, start, end);
        this.isManual = false;
      }
    }
    
    if (key.equals("e")) {
      this.endGame = true;
    }

    if (key.equals("t")) {
      this.isToggled = !this.isToggled;
      if (isManual) {
        this.verticies.get(0).get(0).c = Color.green;
      }
      for (Map.Entry<Vertex, Boolean> element : this.visited.entrySet()) {
        if (element.getValue() && element.getKey().c != Color.blue
            && element.getKey().c != Color.black) {
          if (isToggled) {
            element.getKey().c = Color.cyan;
          }
          else {
            element.getKey().c = Color.LIGHT_GRAY;
          }
        }
      }
    }
  }
  
  // moves the current position based on the direction the user wants
  public void move(Posn dir) {
    if (this.curr.x + dir.x >= 0 && this.curr.y + dir.y >= 0
        && this.curr.x + dir.x <= this.size.x - 1 && this.curr.y + dir.y <= this.size.y - 1) {
      if (!curr.isBlocked(this.verticies.get(this.curr.y + dir.y).get(this.curr.x + dir.x), this)) {
        if (this.isToggled) {
          this.curr.c = Color.cyan;
        }
        else {
          this.curr.c = Color.lightGray;
        }

        this.curr = this.verticies.get(this.curr.y + dir.y).get(this.curr.x + dir.x);
        this.curr.c = Color.black;
      }
    }
    this.verticies.get(0).get(0).c = Color.green;
  }

  // resets the current maze so different search or attempts can be made
  public void reset() {
    for (ArrayList<Vertex> row : this.verticies) {
      for (Vertex curr : row) {
        this.visited.put(curr, false);
        this.prev.put(curr, null);
        curr.c = Color.LIGHT_GRAY;
      }
    }
    this.verticies.get(0).get(0).c = Color.green;
    this.verticies.get(this.size.y - 1).get(this.size.x - 1).c = Color.magenta;
    
    this.q = new LinkedList<Vertex>();
    this.s = new Stack<Vertex>();

    this.q.add(this.verticies.get(0).get(0));
    this.s.push(this.verticies.get(0).get(0));
    this.visited.put(this.verticies.get(0).get(0), true);
  }

}

// represents examples of games;
class ExamplesMaze {
  ExamplesMaze() {}

  Maze maze = new Maze(new Posn(20, 18));
  // same examples as ones below but tests randoms
  Maze maze1 = new Maze(new Posn(3, 3));
  Maze maze2 = new Maze(new Posn(4, 8));
  Maze maze3 = new Maze(new Posn(5, 4));

  // same examples as ones above but seeds it
  Maze maze4 = new Maze(new Random(1), new Posn(3, 3));
  Maze maze5 = new Maze(new Random(56), new Posn(4, 8));
  Maze maze6 = new Maze(new Random(77), new Posn(5, 4));

  Maze maze7 = new Maze(new Random(1), new Posn(3, 3));
  Maze maze8 = new Maze(new Random(56), new Posn(4, 8));
  Maze maze9 = new Maze(new Random(77), new Posn(5, 4));
  Maze maze10 = new Maze(new Random(1), new Posn(3, 3));

  Edge edge1 = new Edge(new Vertex(1, 4, Color.LIGHT_GRAY), 
      new Vertex(2, 4, Color.LIGHT_GRAY), 7, false);
  Edge edge2 = new Edge(new Vertex(3, 2, Color.LIGHT_GRAY), 
      new Vertex(3, 3, Color.LIGHT_GRAY), 8, true);
  Edge edge3 = new Edge(new Vertex(5, 4, Color.LIGHT_GRAY), 
      new Vertex(6, 4, Color.LIGHT_GRAY), 7, true);

  // initializes the data
  public void initData() {
    this.maze4.reset();
    this.maze5.reset();
    this.maze6.reset();
    
  }

  // shows big bang
  void testBigBang(Tester t) {
    // this.maze.bigBang(600 * maze.size.x / maze.size.y, 600, .00001);
  }

  // tests the CompareTo method
  void testCompareTo(Tester t) {
    t.checkExpect(edge1.compareTo(edge2), -1);
    t.checkExpect(edge2.compareTo(edge3), 1);
    t.checkExpect(edge3.compareTo(edge1), 1);
  }

  // tests the createVerticies method
  public void testCreateVerticies(Tester t) {
    ArrayList<ArrayList<Vertex>> verticies1 = new ArrayList<ArrayList<Vertex>>(Arrays.asList(
        new ArrayList<Vertex>(Arrays.asList(new Vertex(0, 0, Color.GREEN),
            new Vertex(1, 0, Color.LIGHT_GRAY), new Vertex(2, 0, Color.LIGHT_GRAY))),
        new ArrayList<Vertex>(Arrays.asList(new Vertex(0, 1, Color.LIGHT_GRAY),
            new Vertex(1, 1, Color.LIGHT_GRAY), new Vertex(2, 1, Color.LIGHT_GRAY))),
        new ArrayList<Vertex>(Arrays.asList(new Vertex(0, 2, Color.LIGHT_GRAY),
            new Vertex(1, 2, Color.LIGHT_GRAY), new Vertex(2, 2, Color.magenta)))));

    for (int i = 0; i < this.maze1.size.y; i++) {
      for (int j = 0; j < this.maze1.size.x; j++) {

        if (j > 0) {
          verticies1.get(i).get(j).left = verticies1.get(i).get(j - 1);
        }

        if (j < this.maze1.size.x - 1) {
          verticies1.get(i).get(j).right = verticies1.get(i).get(j + 1);
        }

        if (i > 0) {
          verticies1.get(i).get(j).top = verticies1.get(i - 1).get(j);
        }

        if (i < this.maze1.size.y - 1) {
          verticies1.get(i).get(j).bottom = verticies1.get(i + 1).get(j);

        }
      }
    }
    t.checkExpect(maze1.verticies, verticies1);

    ArrayList<ArrayList<Vertex>> verticies2 = new ArrayList<ArrayList<Vertex>>(Arrays.asList(
        new ArrayList<Vertex>(
            Arrays.asList(new Vertex(0, 0, Color.GREEN), new Vertex(1, 0, Color.LIGHT_GRAY),
                new Vertex(2, 0, Color.LIGHT_GRAY), new Vertex(3, 0, Color.LIGHT_GRAY))),
        new ArrayList<Vertex>(
            Arrays.asList(new Vertex(0, 1, Color.LIGHT_GRAY), new Vertex(1, 1, Color.LIGHT_GRAY),
                new Vertex(2, 1, Color.LIGHT_GRAY), new Vertex(3, 1, Color.LIGHT_GRAY))),
        new ArrayList<Vertex>(
            Arrays.asList(new Vertex(0, 2, Color.LIGHT_GRAY), new Vertex(1, 2, Color.LIGHT_GRAY),
                new Vertex(2, 2, Color.LIGHT_GRAY), new Vertex(3, 2, Color.LIGHT_GRAY))),
        new ArrayList<Vertex>(
            Arrays.asList(new Vertex(0, 3, Color.LIGHT_GRAY), new Vertex(1, 3, Color.LIGHT_GRAY),
                new Vertex(2, 3, Color.LIGHT_GRAY), new Vertex(3, 3, Color.LIGHT_GRAY))),
        new ArrayList<Vertex>(
            Arrays.asList(new Vertex(0, 4, Color.LIGHT_GRAY), new Vertex(1, 4, Color.LIGHT_GRAY),
                new Vertex(2, 4, Color.LIGHT_GRAY), new Vertex(3, 4, Color.LIGHT_GRAY))),
        new ArrayList<Vertex>(
            Arrays.asList(new Vertex(0, 5, Color.LIGHT_GRAY), new Vertex(1, 5, Color.LIGHT_GRAY),
                new Vertex(2, 5, Color.LIGHT_GRAY), new Vertex(3, 5, Color.LIGHT_GRAY))),
        new ArrayList<Vertex>(
            Arrays.asList(new Vertex(0, 6, Color.LIGHT_GRAY), new Vertex(1, 6, Color.LIGHT_GRAY),
                new Vertex(2, 6, Color.LIGHT_GRAY), new Vertex(3, 6, Color.LIGHT_GRAY))),
        new ArrayList<Vertex>(
            Arrays.asList(new Vertex(0, 7, Color.LIGHT_GRAY), new Vertex(1, 7, Color.LIGHT_GRAY),
                new Vertex(2, 7, Color.LIGHT_GRAY), new Vertex(3, 7, Color.magenta)))));

    for (int i = 0; i < this.maze2.size.y; i++) {
      for (int j = 0; j < this.maze2.size.x; j++) {

        if (j > 0) {
          verticies2.get(i).get(j).left = verticies2.get(i).get(j - 1);
        }

        if (j < this.maze2.size.x - 1) {
          verticies2.get(i).get(j).right = verticies2.get(i).get(j + 1);
        }

        if (i > 0) {
          verticies2.get(i).get(j).top = verticies2.get(i - 1).get(j);
        }

        if (i < this.maze2.size.y - 1) {
          verticies2.get(i).get(j).bottom = verticies2.get(i + 1).get(j);

        }
      }
    }

    t.checkExpect(maze2.verticies, verticies2);

    ArrayList<ArrayList<Vertex>> verticies3 = new ArrayList<ArrayList<Vertex>>(Arrays.asList(
        new ArrayList<Vertex>(Arrays.asList(new Vertex(0, 0, Color.GREEN),
            new Vertex(1, 0, Color.LIGHT_GRAY), new Vertex(2, 0, Color.LIGHT_GRAY),
            new Vertex(3, 0, Color.LIGHT_GRAY), new Vertex(4, 0, Color.LIGHT_GRAY))),
        new ArrayList<Vertex>(Arrays.asList(new Vertex(0, 1, Color.LIGHT_GRAY),
            new Vertex(1, 1, Color.LIGHT_GRAY), new Vertex(2, 1, Color.LIGHT_GRAY),
            new Vertex(3, 1, Color.LIGHT_GRAY), new Vertex(4, 1, Color.LIGHT_GRAY))),
        new ArrayList<Vertex>(Arrays.asList(new Vertex(0, 2, Color.LIGHT_GRAY),
            new Vertex(1, 2, Color.LIGHT_GRAY), new Vertex(2, 2, Color.LIGHT_GRAY),
            new Vertex(3, 2, Color.LIGHT_GRAY), new Vertex(4, 2, Color.LIGHT_GRAY))),
        new ArrayList<Vertex>(Arrays.asList(new Vertex(0, 3, Color.LIGHT_GRAY),
            new Vertex(1, 3, Color.LIGHT_GRAY), new Vertex(2, 3, Color.LIGHT_GRAY),
            new Vertex(3, 3, Color.LIGHT_GRAY), new Vertex(4, 3, Color.magenta)))));

    for (int i = 0; i < this.maze3.size.y; i++) {
      for (int j = 0; j < this.maze3.size.x; j++) {

        if (j > 0) {
          verticies3.get(i).get(j).left = verticies3.get(i).get(j - 1);
        }

        if (j < this.maze3.size.x - 1) {
          verticies3.get(i).get(j).right = verticies3.get(i).get(j + 1);
        }

        if (i > 0) {
          verticies3.get(i).get(j).top = verticies3.get(i - 1).get(j);
        }

        if (i < this.maze3.size.y - 1) {
          verticies3.get(i).get(j).bottom = verticies3.get(i + 1).get(j);

        }
      }
    }
    t.checkExpect(maze3.verticies, verticies3);
  }

  // tests to see if the edges are turned into walls correctly using Kruskals's
  // algorithm
  public boolean testWalls(Tester t) {
    initData();
    // checks to see if there are the correct number of edges
    t.checkExpect(maze1.edges.size(), 12);
    t.checkExpect(maze2.edges.size(), 52);
    t.checkExpect(maze3.edges.size(), 31);

    // theses next tests check to make sure that the number of walls are the amount
    // of edges
    // minus the number of edges in the minimal spanning tree found using Kruskal's
    // algorithm
    // walls = # of edges - minimal spanning tree or( # of verticies - 1)
    int walls1 = 0;
    int walls2 = 0;
    int walls3 = 0;

    for (Edge e : maze1.edges) {
      if (e.isWall) {
        walls1++;
      }
    }
    t.checkExpect(walls1, 4);

    for (Edge e : maze2.edges) {
      if (e.isWall) {
        walls2++;
      }
    }
    t.checkExpect(walls2, 21);

    for (Edge e : maze3.edges) {
      if (e.isWall) {
        walls3++;
      }
    }
    t.checkExpect(walls3, 12);

    // checks to make sure all the edges weights are randomized so the mazes are
    // created with
    // random walls every times using Kruskal's algorithm
    for (Edge e : maze1.edges) {
      t.checkOneOf(e.weight, 0, 1, 2, 3, 4, 5, 6, 7, 8);
    }

    for (Edge e : maze2.edges) {
      t.checkOneOf(e.weight, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
          20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31);
    }

    for (Edge e : maze3.edges) {
      t.checkOneOf(e.weight, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
    }

    // tests belows check to see if walls were created right and are of the right
    // number
    ArrayList<Edge> connectors1 = new ArrayList<Edge>();
    ArrayList<Edge> connectors2 = new ArrayList<Edge>();
    ArrayList<Edge> connectors3 = new ArrayList<Edge>();

    for (Edge e : this.maze4.edges) {
      if (e.isWall) {
        connectors1.add(e);
      }
    }

    t.checkExpect(connectors1, new ArrayList<Edge>(Arrays
        .asList(new Edge(maze4.verticies.get(0).get(0), maze4.verticies.get(1).get(0), 6, true),
            new Edge(maze4.verticies.get(2).get(0), maze4.verticies.get(2).get(1), 7, true),
            new Edge(maze4.verticies.get(2).get(1), maze4.verticies.get(2).get(2), 7, true),
            new Edge(maze4.verticies.get(0).get(2), maze4.verticies.get(1).get(2), 8, true))));

    for (Edge e : this.maze5.edges) {
      if (e.isWall) {
        connectors2.add(e);
      }
    }

    t.checkExpect(connectors2,
        new ArrayList<Edge>(Arrays.asList(
            new Edge(maze5.verticies.get(5).get(1), maze5.verticies.get(5).get(2), 15, true),
            new Edge(maze5.verticies.get(3).get(2), maze5.verticies.get(4).get(2), 20, true),
            new Edge(maze5.verticies.get(7).get(0), maze5.verticies.get(7).get(1), 20, true),
            new Edge(maze5.verticies.get(3).get(1), maze5.verticies.get(3).get(2), 21, true),
            new Edge(maze5.verticies.get(2).get(0), maze5.verticies.get(2).get(1), 22, true),
            new Edge(maze5.verticies.get(3).get(3), maze5.verticies.get(4).get(3), 22, true),
            new Edge(maze5.verticies.get(5).get(0), maze5.verticies.get(6).get(0), 22, true),
            new Edge(maze5.verticies.get(6).get(2), maze5.verticies.get(7).get(2), 22, true),
            new Edge(maze5.verticies.get(0).get(0), maze5.verticies.get(1).get(0), 23, true),
            new Edge(maze5.verticies.get(1).get(3), maze5.verticies.get(2).get(3), 23, true),
            new Edge(maze5.verticies.get(3).get(0), maze5.verticies.get(4).get(0), 23, true),
            new Edge(maze5.verticies.get(4).get(0), maze5.verticies.get(4).get(1), 23, true),
            new Edge(maze5.verticies.get(0).get(2), maze5.verticies.get(0).get(3), 24, true),
            new Edge(maze5.verticies.get(0).get(1), maze5.verticies.get(0).get(2), 26, true),
            new Edge(maze5.verticies.get(2).get(2), maze5.verticies.get(3).get(2), 26, true),
            new Edge(maze5.verticies.get(1).get(2), maze5.verticies.get(2).get(2), 27, true),
            new Edge(maze5.verticies.get(5).get(2), maze5.verticies.get(5).get(3), 28, true),
            new Edge(maze5.verticies.get(6).get(1), maze5.verticies.get(6).get(2), 28, true),
            new Edge(maze5.verticies.get(2).get(0), maze5.verticies.get(3).get(0), 28, true),
            new Edge(maze5.verticies.get(6).get(1), maze5.verticies.get(7).get(1), 30, true),
            new Edge(maze5.verticies.get(5).get(3), maze5.verticies.get(6).get(3), 31, true))));

    for (Edge e : this.maze6.edges) {
      if (e.isWall) {
        connectors3.add(e);
      }
    }

    t.checkExpect(connectors3,
        new ArrayList<Edge>(Arrays.asList(
            new Edge(maze6.verticies.get(1).get(2), maze6.verticies.get(2).get(2), 9, true),
            new Edge(maze6.verticies.get(1).get(2), maze6.verticies.get(1).get(3), 11, true),
            new Edge(maze6.verticies.get(2).get(0), maze6.verticies.get(2).get(1), 11, true),
            new Edge(maze6.verticies.get(1).get(0), maze6.verticies.get(2).get(0), 13, true),
            new Edge(maze6.verticies.get(3).get(3), maze6.verticies.get(3).get(4), 13, true),
            new Edge(maze6.verticies.get(0).get(1), maze6.verticies.get(0).get(2), 14, true),
            new Edge(maze6.verticies.get(1).get(4), maze6.verticies.get(2).get(4), 14, true),
            new Edge(maze6.verticies.get(2).get(2), maze6.verticies.get(3).get(2), 15, true),
            new Edge(maze6.verticies.get(3).get(1), maze6.verticies.get(3).get(2), 15, true),
            new Edge(maze6.verticies.get(1).get(3), maze6.verticies.get(2).get(3), 17, true),
            new Edge(maze6.verticies.get(1).get(3), maze6.verticies.get(1).get(4), 17, true),
            new Edge(maze6.verticies.get(0).get(1), maze6.verticies.get(1).get(1), 18, true))));

    return true;
  }

  // tests the Depth First Search
  public void testDepthFirstSearch(Tester t) {
    this.maze7 = new Maze(new Random(1), new Posn(3, 3));
    this.maze8.reset();
    this.maze9.reset();

    this.maze7.onKeyEvent("d");
    t.checkExpect(this.maze7.isDFS, true);
    this.maze7.onTick();
    this.maze7.onTick();
    this.maze7.onTick();
    this.maze7.onTick();
    this.maze7.onTick();
    t.checkExpect(this.maze7.isDFS, false);
    t.checkExpect(this.maze7.verticies.get(0).get(0).c, Color.blue);
    t.checkExpect(this.maze7.verticies.get(0).get(1).c, Color.blue);
    t.checkExpect(this.maze7.verticies.get(0).get(2).c, Color.cyan);
    t.checkExpect(this.maze7.verticies.get(1).get(0).c, Color.cyan);
    t.checkExpect(this.maze7.verticies.get(1).get(1).c, Color.blue);
    t.checkExpect(this.maze7.verticies.get(1).get(2).c, Color.blue);
    t.checkExpect(this.maze7.verticies.get(2).get(1).c, Color.cyan);
    t.checkExpect(this.maze7.verticies.get(2).get(2).c, Color.blue);

    this.maze8.onKeyEvent("d");
    t.checkExpect(this.maze8.isDFS, true);
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();

    t.checkExpect(this.maze8.isDFS, false);
    t.checkExpect(this.maze8.verticies.get(0).get(0).c, Color.blue);
    t.checkExpect(this.maze8.verticies.get(0).get(1).c, Color.blue);
    t.checkExpect(this.maze8.verticies.get(1).get(0).c, Color.cyan);
    t.checkExpect(this.maze8.verticies.get(1).get(1).c, Color.blue);
    t.checkExpect(this.maze8.verticies.get(1).get(2).c, Color.cyan);
    t.checkExpect(this.maze8.verticies.get(2).get(1).c, Color.blue);
    t.checkExpect(this.maze8.verticies.get(2).get(2).c, Color.cyan);
    t.checkExpect(this.maze8.verticies.get(3).get(0).c, Color.cyan);
    t.checkExpect(this.maze8.verticies.get(3).get(1).c, Color.blue);
    t.checkExpect(this.maze8.verticies.get(4).get(0).c, Color.cyan);
    t.checkExpect(this.maze8.verticies.get(4).get(1).c, Color.blue);
    t.checkExpect(this.maze8.verticies.get(4).get(2).c, Color.blue);
    t.checkExpect(this.maze8.verticies.get(4).get(3).c, Color.cyan);
    t.checkExpect(this.maze8.verticies.get(5).get(0).c, Color.cyan);
    t.checkExpect(this.maze8.verticies.get(5).get(1).c, Color.cyan);
    t.checkExpect(this.maze8.verticies.get(5).get(2).c, Color.blue);
    t.checkExpect(this.maze8.verticies.get(6).get(0).c, Color.cyan);
    t.checkExpect(this.maze8.verticies.get(6).get(1).c, Color.cyan);
    t.checkExpect(this.maze8.verticies.get(6).get(2).c, Color.blue);
    t.checkExpect(this.maze8.verticies.get(6).get(3).c, Color.blue);
    t.checkExpect(this.maze8.verticies.get(7).get(0).c, Color.cyan);
    t.checkExpect(this.maze8.verticies.get(7).get(3).c, Color.blue);

    this.maze9.onKeyEvent("d");
    t.checkExpect(this.maze9.isDFS, true);
    this.maze9.onTick();
    this.maze9.onTick();
    this.maze9.onTick();
    this.maze9.onTick();
    this.maze9.onTick();
    this.maze9.onTick();
    this.maze9.onTick();
    this.maze9.onTick();
    this.maze9.onTick();
    this.maze9.onTick();
    this.maze9.onTick();
    this.maze9.onTick();

    t.checkExpect(this.maze9.isDFS, false);
    t.checkExpect(this.maze9.verticies.get(0).get(0).c, Color.blue);
    t.checkExpect(this.maze9.verticies.get(0).get(1).c, Color.cyan);
    t.checkExpect(this.maze9.verticies.get(1).get(0).c, Color.blue);
    t.checkExpect(this.maze9.verticies.get(1).get(1).c, Color.blue);
    t.checkExpect(this.maze9.verticies.get(1).get(2).c, Color.cyan);
    t.checkExpect(this.maze9.verticies.get(2).get(0).c, Color.cyan);
    t.checkExpect(this.maze9.verticies.get(2).get(1).c, Color.blue);
    t.checkExpect(this.maze9.verticies.get(2).get(2).c, Color.blue);
    t.checkExpect(this.maze9.verticies.get(2).get(3).c, Color.blue);
    t.checkExpect(this.maze9.verticies.get(2).get(4).c, Color.blue);
    t.checkExpect(this.maze9.verticies.get(3).get(0).c, Color.cyan);
    t.checkExpect(this.maze9.verticies.get(3).get(1).c, Color.cyan);
    t.checkExpect(this.maze9.verticies.get(3).get(2).c, Color.cyan);
    t.checkExpect(this.maze9.verticies.get(3).get(3).c, Color.cyan);
    t.checkExpect(this.maze9.verticies.get(3).get(4).c, Color.blue);
  }

  // tests the Bredth First Search
  public void testBreadthFirstSearch(Tester t) {
    this.maze7.reset();
    this.maze8.reset();
    this.maze9.reset();

    this.maze7.onKeyEvent("b");
    t.checkExpect(this.maze7.isBFS, true);
    this.maze7.onTick();
    this.maze7.onTick();
    this.maze7.onTick();
    this.maze7.onTick();
    this.maze7.onTick();
    this.maze7.onTick();
    t.checkExpect(this.maze7.isBFS, false);
    t.checkExpect(this.maze7.verticies.get(0).get(0).c, Color.blue);
    t.checkExpect(this.maze7.verticies.get(0).get(1).c, Color.blue);
    t.checkExpect(this.maze7.verticies.get(0).get(2).c, Color.cyan);
    t.checkExpect(this.maze7.verticies.get(1).get(0).c, Color.cyan);
    t.checkExpect(this.maze7.verticies.get(1).get(1).c, Color.blue);
    t.checkExpect(this.maze7.verticies.get(1).get(2).c, Color.blue);
    t.checkExpect(this.maze7.verticies.get(2).get(0).c, Color.cyan);
    t.checkExpect(this.maze7.verticies.get(2).get(1).c, Color.cyan);
    t.checkExpect(this.maze7.verticies.get(2).get(2).c, Color.blue);


    this.maze8.onKeyEvent("b");
    t.checkExpect(this.maze8.isBFS, true);
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();

    t.checkExpect(this.maze8.isBFS, false);
    t.checkExpect(this.maze8.verticies.get(0).get(0).c, Color.blue);
    t.checkExpect(this.maze8.verticies.get(0).get(1).c, Color.blue);
    t.checkExpect(this.maze8.verticies.get(0).get(2).c, Color.cyan);
    t.checkExpect(this.maze8.verticies.get(0).get(3).c, Color.cyan);
    t.checkExpect(this.maze8.verticies.get(1).get(0).c, Color.cyan);
    t.checkExpect(this.maze8.verticies.get(1).get(1).c, Color.blue);
    t.checkExpect(this.maze8.verticies.get(1).get(2).c, Color.cyan);
    t.checkExpect(this.maze8.verticies.get(1).get(3).c, Color.cyan);
    t.checkExpect(this.maze8.verticies.get(2).get(0).c, Color.cyan);
    t.checkExpect(this.maze8.verticies.get(2).get(1).c, Color.blue);
    t.checkExpect(this.maze8.verticies.get(2).get(2).c, Color.cyan);
    t.checkExpect(this.maze8.verticies.get(2).get(3).c, Color.cyan);
    t.checkExpect(this.maze8.verticies.get(3).get(0).c, Color.cyan);
    t.checkExpect(this.maze8.verticies.get(3).get(1).c, Color.blue);
    t.checkExpect(this.maze8.verticies.get(3).get(2).c, Color.cyan);
    t.checkExpect(this.maze8.verticies.get(3).get(3).c, Color.cyan);
    t.checkExpect(this.maze8.verticies.get(4).get(0).c, Color.cyan);
    t.checkExpect(this.maze8.verticies.get(4).get(1).c, Color.blue);
    t.checkExpect(this.maze8.verticies.get(4).get(2).c, Color.blue);
    t.checkExpect(this.maze8.verticies.get(4).get(3).c, Color.cyan);
    t.checkExpect(this.maze8.verticies.get(5).get(0).c, Color.cyan);
    t.checkExpect(this.maze8.verticies.get(5).get(1).c, Color.cyan);
    t.checkExpect(this.maze8.verticies.get(5).get(2).c, Color.blue);
    t.checkExpect(this.maze8.verticies.get(5).get(3).c, Color.cyan);
    t.checkExpect(this.maze8.verticies.get(6).get(0).c, Color.cyan);
    t.checkExpect(this.maze8.verticies.get(6).get(1).c, Color.cyan);
    t.checkExpect(this.maze8.verticies.get(6).get(2).c, Color.blue);
    t.checkExpect(this.maze8.verticies.get(6).get(3).c, Color.blue);
    t.checkExpect(this.maze8.verticies.get(7).get(0).c, Color.cyan);
    t.checkExpect(this.maze8.verticies.get(7).get(3).c, Color.blue);

    this.maze9.onKeyEvent("b");
    t.checkExpect(this.maze9.isBFS, true);
    this.maze9.onTick();
    this.maze9.onTick();
    this.maze9.onTick();
    this.maze9.onTick();
    this.maze9.onTick();
    this.maze9.onTick();
    this.maze9.onTick();
    this.maze9.onTick();
    this.maze9.onTick();
    this.maze9.onTick();
    this.maze9.onTick();
    this.maze9.onTick();
    this.maze9.onTick();
    this.maze9.onTick();
    this.maze9.onTick();
    this.maze9.onTick();

    t.checkExpect(this.maze9.isBFS, false);
    t.checkExpect(this.maze9.verticies.get(0).get(0).c, Color.blue);
    t.checkExpect(this.maze9.verticies.get(0).get(1).c, Color.cyan);
    t.checkExpect(this.maze9.verticies.get(0).get(2).c, Color.cyan);
    t.checkExpect(this.maze9.verticies.get(0).get(3).c, Color.cyan);
    t.checkExpect(this.maze9.verticies.get(0).get(4).c, Color.cyan);
    t.checkExpect(this.maze9.verticies.get(1).get(0).c, Color.blue);
    t.checkExpect(this.maze9.verticies.get(1).get(1).c, Color.blue);
    t.checkExpect(this.maze9.verticies.get(1).get(2).c, Color.cyan);
    t.checkExpect(this.maze9.verticies.get(1).get(3).c, Color.cyan);
    t.checkExpect(this.maze9.verticies.get(1).get(4).c, Color.cyan);
    t.checkExpect(this.maze9.verticies.get(2).get(0).c, Color.cyan);
    t.checkExpect(this.maze9.verticies.get(2).get(1).c, Color.blue);
    t.checkExpect(this.maze9.verticies.get(2).get(2).c, Color.blue);
    t.checkExpect(this.maze9.verticies.get(2).get(3).c, Color.blue);
    t.checkExpect(this.maze9.verticies.get(2).get(4).c, Color.blue);
    t.checkExpect(this.maze9.verticies.get(3).get(0).c, Color.cyan);
    t.checkExpect(this.maze9.verticies.get(3).get(1).c, Color.cyan);
    t.checkExpect(this.maze9.verticies.get(3).get(3).c, Color.cyan);
    t.checkExpect(this.maze9.verticies.get(3).get(4).c, Color.blue);
  }

  // tests manual play
  public void testManual(Tester t) {
    initData();
    this.maze4.onKeyEvent("m");
    t.checkExpect(this.maze4.isManual, true);
    this.maze4.onKeyEvent("left");

    t.checkExpect(this.maze4.verticies.get(0).get(0).c, Color.green);
    this.maze4.onKeyEvent("right");
    t.checkExpect(this.maze4.verticies.get(0).get(1).c, Color.black);
    this.maze4.onKeyEvent("down");
    t.checkExpect(this.maze4.verticies.get(0).get(1).c, Color.cyan);
    t.checkExpect(this.maze4.verticies.get(1).get(1).c, Color.black);
    this.maze4.onKeyEvent("right");
    t.checkExpect(this.maze4.verticies.get(1).get(1).c, Color.cyan);
    t.checkExpect(this.maze4.verticies.get(1).get(2).c, Color.black);
    this.maze4.onKeyEvent("down");
    t.checkExpect(this.maze4.verticies.get(0).get(0).c, Color.blue);
    t.checkExpect(this.maze4.verticies.get(0).get(1).c, Color.blue);
    t.checkExpect(this.maze4.verticies.get(1).get(1).c, Color.blue);
    t.checkExpect(this.maze4.verticies.get(1).get(2).c, Color.blue);
    t.checkExpect(this.maze4.verticies.get(2).get(2).c, Color.blue);
    t.checkExpect(this.maze4.solved, true);
    this.maze4.onKeyEvent("up");
    t.checkExpect(this.maze4.verticies.get(1).get(2).c, Color.blue);
    this.maze4.reset();
  }

  // tests the move method
  public void testMove(Tester t) {
    initData();
    this.maze4.onKeyEvent("m");
    this.maze4.onKeyEvent("left");

    t.checkExpect(this.maze4.verticies.get(0).get(0).c, Color.green);
    this.maze4.onKeyEvent("right");
    t.checkExpect(this.maze4.verticies.get(0).get(1).c, Color.black);
    this.maze4.onKeyEvent("down");
    t.checkExpect(this.maze4.verticies.get(0).get(1).c, Color.cyan);
    t.checkExpect(this.maze4.verticies.get(1).get(1).c, Color.black);
    this.maze4.onKeyEvent("t");
    t.checkExpect(this.maze4.verticies.get(0).get(1).c, Color.lightGray);

    this.maze4.reset();
  }

  // tests onKeyEvent
  public void testOnKeyEvent(Tester t) {
    initData();

    this.maze5.onKeyEvent("b");
    t.checkExpect(this.maze5.isBFS, true);
    t.checkExpect(this.maze5.isDFS, false);
    t.checkExpect(this.maze5.isManual, false);
    t.checkExpect(this.maze5.solved, false);
    t.checkExpect(this.maze5.isToggled, true);

    this.maze5.onKeyEvent("d");
    t.checkExpect(this.maze5.isBFS, false);
    t.checkExpect(this.maze5.isDFS, true);
    t.checkExpect(this.maze5.isManual, false);
    t.checkExpect(this.maze5.solved, false);
    t.checkExpect(this.maze5.isToggled, true);

    this.maze5.onKeyEvent("m");
    t.checkExpect(this.maze5.isBFS, false);
    t.checkExpect(this.maze5.isDFS, false);
    t.checkExpect(this.maze5.isManual, true);
    t.checkExpect(this.maze5.solved, false);
    t.checkExpect(this.maze5.isToggled, true);

    this.maze7.onKeyEvent("r");
    t.checkExpect(this.maze7.isBFS, false);
    t.checkExpect(this.maze7.isDFS, false);
    t.checkExpect(this.maze7.isManual, false);
    t.checkExpect(this.maze7.solved, false);
    t.checkExpect(this.maze5.isToggled, true);

    for (ArrayList<Vertex> row : this.maze7.verticies) {
      for (Vertex curr : row) {
        if (curr.x == 0 && curr.y == 0) {
          t.checkExpect(this.maze7.visited.get(curr), true);
          t.checkExpect(curr.c, Color.green);
        }
        else if (curr.x == this.maze7.size.x - 1 && curr.y == this.maze7.size.y - 1) {
          t.checkExpect(curr.c, Color.magenta);
        }
        else {
          t.checkExpect(this.maze7.visited.get(curr), false);
          t.checkExpect(curr.c, Color.LIGHT_GRAY);
        }
        t.checkExpect(this.maze7.prev.get(curr), null);
      }
    }
    this.maze5.onKeyEvent("t");
    t.checkExpect(this.maze5.isToggled, false);

    this.maze5.onKeyEvent("e");
    t.checkExpect(this.maze5.endGame, true);
  }

  // tests the searchHelper method
  public void testSearchHelper(Tester t) {
    this.maze7 = new Maze(new Random(1), new Posn(3, 3));

    this.maze7.onKeyEvent("b");
    this.maze7.searchHelper("BFS");

    t.checkExpect(this.maze7.verticies.get(0).get(1).c, Color.cyan);
    this.maze7.searchHelper("BFS");
    t.checkExpect(this.maze7.verticies.get(1).get(1).c, Color.cyan);
    this.maze7.searchHelper("BFS");
    this.maze7.searchHelper("BFS");
    t.checkExpect(this.maze7.verticies.get(1).get(0).c, Color.cyan);
  }

  // tests the drawPath method
  public void testDrawPath(Tester t) {
    initData();
    this.maze8.reset();

    this.maze10.onKeyEvent("d");
    this.maze10.onTick();
    this.maze10.onTick();
    this.maze10.onTick();
    this.maze10.onTick();
    this.maze10.onTick();
    this.maze10.drawPath(this.maze10.prev, this.maze10.verticies.get(0).get(0),
        this.maze10.verticies.get(this.maze10.size.y - 1).get(this.maze10.size.x - 1));

    t.checkExpect(this.maze10.verticies.get(0).get(0).c, Color.blue);
    t.checkExpect(this.maze10.verticies.get(0).get(1).c, Color.blue);
    t.checkExpect(this.maze10.verticies.get(1).get(1).c, Color.blue);
    t.checkExpect(this.maze10.verticies.get(1).get(2).c, Color.blue);
    t.checkExpect(this.maze10.verticies.get(2).get(2).c, Color.blue);
    this.maze10.reset();

    this.maze8.onKeyEvent("b");
    t.checkExpect(this.maze8.isBFS, true);
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.onTick();
    this.maze8.drawPath(this.maze8.prev, this.maze8.verticies.get(0).get(0),
        this.maze8.verticies.get(this.maze8.size.y - 1).get(this.maze8.size.x - 1));

    t.checkExpect(this.maze8.verticies.get(0).get(0).c, Color.blue);
    t.checkExpect(this.maze8.verticies.get(0).get(1).c, Color.blue);
    t.checkExpect(this.maze8.verticies.get(1).get(1).c, Color.blue);
    t.checkExpect(this.maze8.verticies.get(2).get(1).c, Color.blue);
    t.checkExpect(this.maze8.verticies.get(3).get(1).c, Color.blue);
    t.checkExpect(this.maze8.verticies.get(4).get(1).c, Color.blue);
    t.checkExpect(this.maze8.verticies.get(4).get(2).c, Color.blue);
    t.checkExpect(this.maze8.verticies.get(5).get(2).c, Color.blue);
    t.checkExpect(this.maze8.verticies.get(6).get(2).c, Color.blue);
    t.checkExpect(this.maze8.verticies.get(6).get(3).c, Color.blue);
    t.checkExpect(this.maze8.verticies.get(7).get(3).c, Color.blue);

  }

  // tests the reset method
  public void testReset(Tester t) {
    this.maze1.onKeyEvent("d");
    this.maze1.reset();

    for (ArrayList<Vertex> row : this.maze1.verticies) {
      for (Vertex curr : row) {
        t.checkExpect(this.maze1.prev.get(curr), null);
        if (curr.x == 0 && curr.y == 0) {
          t.checkExpect(curr.c, Color.green);
          t.checkExpect(this.maze1.visited.get(curr), true);
        }
        else if (curr.x == this.maze1.size.x - 1 && curr.y == this.maze1.size.y - 1) {
          t.checkExpect(curr.c, Color.magenta);
        }
        else {
          t.checkExpect(curr.c, Color.LIGHT_GRAY);
          t.checkExpect(this.maze1.visited.get(curr), false);
        }
      }
    }
    t.checkExpect(this.maze1.q.contains(this.maze1.verticies.get(0).get(0)), true);
    t.checkExpect(this.maze1.s.contains(this.maze1.verticies.get(0).get(0)), true);

    this.maze2.onKeyEvent("b");
    this.maze2.reset();

    for (ArrayList<Vertex> row : this.maze2.verticies) {
      for (Vertex curr : row) {
        t.checkExpect(this.maze2.prev.get(curr), null);
        if (curr.x == 0 && curr.y == 0) {
          t.checkExpect(curr.c, Color.green);
          t.checkExpect(this.maze2.visited.get(curr), true);
        }
        else if (curr.x == this.maze2.size.x - 1 && curr.y == this.maze2.size.y - 1) {
          t.checkExpect(curr.c, Color.magenta);
        }
        else {
          t.checkExpect(curr.c, Color.LIGHT_GRAY);
          t.checkExpect(this.maze2.visited.get(curr), false);
        }
      }
    }
    t.checkExpect(this.maze2.q.contains(this.maze2.verticies.get(0).get(0)), true);
    t.checkExpect(this.maze2.s.contains(this.maze2.verticies.get(0).get(0)), true);

    this.maze3.onKeyEvent("m");
    this.maze3.reset();

    for (ArrayList<Vertex> row : this.maze3.verticies) {
      for (Vertex curr : row) {
        t.checkExpect(this.maze3.prev.get(curr), null);
        if (curr.x == 0 && curr.y == 0) {
          t.checkExpect(curr.c, Color.green);
          t.checkExpect(this.maze3.visited.get(curr), true);
        }
        else if (curr.x == this.maze3.size.x - 1 && curr.y == this.maze3.size.y - 1) {
          t.checkExpect(curr.c, Color.magenta);
        }
        else {
          t.checkExpect(curr.c, Color.LIGHT_GRAY);
          t.checkExpect(this.maze3.visited.get(curr), false);
        }
      }
    }
    t.checkExpect(this.maze3.q.contains(this.maze3.verticies.get(0).get(0)), true);
    t.checkExpect(this.maze3.s.contains(this.maze3.verticies.get(0).get(0)), true);

  }

  // created different methods for make scene tests so it easier to handle
  public boolean testMakeScene1(Tester t) {
    this.maze4.reset();
    this.maze4.onKeyEvent("b");
    this.maze4.onTick();
    this.maze4.onTick();
    this.maze4.onTick();
    this.maze4.onTick();
    this.maze4.onTick();
    this.maze4.onTick();
    WorldScene world = new WorldScene(600, 600);
    RectangleImage rect1 = new RectangleImage(600, 600, OutlineMode.OUTLINE, Color.black);
    RectangleImage rect2 = new RectangleImage(200, 200, OutlineMode.SOLID, Color.blue);
    RectangleImage rect3 = new RectangleImage(200, 200, OutlineMode.SOLID, Color.blue);
    RectangleImage rect4 = new RectangleImage(200, 200, OutlineMode.SOLID, Color.cyan);
    RectangleImage rect5 = new RectangleImage(200, 200, OutlineMode.SOLID, Color.cyan);
    RectangleImage rect6 = new RectangleImage(200, 200, OutlineMode.SOLID, Color.blue);
    RectangleImage rect7 = new RectangleImage(200, 200, OutlineMode.SOLID, Color.blue);
    RectangleImage rect8 = new RectangleImage(200, 200, OutlineMode.SOLID, Color.cyan);
    RectangleImage rect9 = new RectangleImage(200, 200, OutlineMode.SOLID, Color.cyan);
    RectangleImage rect10 = new RectangleImage(200, 200, OutlineMode.SOLID, Color.blue);

    LineImage line1 = new LineImage(new Posn(200, 0), Color.black);
    LineImage line2 = new LineImage(new Posn(0, 200), Color.black);
    LineImage line3 = new LineImage(new Posn(0, 200), Color.black);
    LineImage line4 = new LineImage(new Posn(200, 0), Color.black);

    TextImage text1 = new TextImage("4", 200, Color.white);

    world.placeImageXY(rect1, 300, 300);
    world.placeImageXY(rect2, 100, 100);
    world.placeImageXY(rect3, 300, 100);
    world.placeImageXY(rect4, 500, 100);
    world.placeImageXY(rect5, 100, 300);
    world.placeImageXY(rect6, 300, 300);
    world.placeImageXY(rect7, 500, 300);
    world.placeImageXY(rect8, 100, 500);
    world.placeImageXY(rect9, 300, 500);
    world.placeImageXY(rect10, 500, 500);
    world.placeImageXY(line1, 100, 200);
    world.placeImageXY(line2, 200, 500);
    world.placeImageXY(line3, 400, 500);
    world.placeImageXY(line4, 500, 200);
    world.placeImageXY(text1, 105, 100);

    return t.checkExpect(maze4.makeScene(), world);
  }

  public boolean testMakeScene2(Tester t) {
    initData();
    this.maze5 = new Maze(new Random(56), new Posn(4, 8));
    this.maze5.onKeyEvent("d");
    this.maze5.onTick();
    this.maze5.onTick();
    this.maze5.onTick();
    this.maze5.onTick();
    this.maze5.onTick();
    this.maze5.onTick();
    this.maze5.onTick();
    this.maze5.onTick();
    this.maze5.onTick();
    this.maze5.onTick();
    this.maze5.onTick();
    this.maze5.onTick();
    this.maze5.onTick();
    this.maze5.onTick();
    this.maze5.onTick();
    this.maze5.onTick();

    WorldScene world = new WorldScene(300, 600);
    RectangleImage rect2 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.blue);
    RectangleImage rect3 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.blue);
    RectangleImage rect4 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect5 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect6 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.cyan);
    RectangleImage rect7 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.blue);
    RectangleImage rect8 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.cyan);
    RectangleImage rect9 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect10 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect11 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.blue);
    RectangleImage rect12 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.cyan);
    RectangleImage rect13 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect14 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.cyan);
    RectangleImage rect15 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.blue);
    RectangleImage rect16 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect17 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect18 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.cyan);
    RectangleImage rect19 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.blue);
    RectangleImage rect20 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.blue);
    RectangleImage rect21 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.cyan);
    RectangleImage rect22 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.cyan);
    RectangleImage rect23 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.cyan);
    RectangleImage rect24 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.blue);
    RectangleImage rect25 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect26 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.cyan);
    RectangleImage rect27 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.cyan);
    RectangleImage rect28 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.blue);
    RectangleImage rect29 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.blue);
    RectangleImage rect30 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.cyan);
    RectangleImage rect31 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect32 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect33 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.blue);

    LineImage line1 = new LineImage(new Posn(0, 75), Color.black);
    LineImage line2 = new LineImage(new Posn(75, 0), Color.black);
    LineImage line3 = new LineImage(new Posn(0, 75), Color.black);
    LineImage line4 = new LineImage(new Posn(0, 75), Color.black);
    LineImage line5 = new LineImage(new Posn(0, 75), Color.black);
    LineImage line6 = new LineImage(new Posn(75, 0), Color.black);
    LineImage line7 = new LineImage(new Posn(75, 0), Color.black);
    LineImage line8 = new LineImage(new Posn(75, 0), Color.black);
    LineImage line9 = new LineImage(new Posn(75, 0), Color.black);
    LineImage line10 = new LineImage(new Posn(75, 0), Color.black);
    LineImage line11 = new LineImage(new Posn(75, 0), Color.black);
    LineImage line12 = new LineImage(new Posn(0, 75), Color.black);
    LineImage line13 = new LineImage(new Posn(0, 75), Color.black);
    LineImage line14 = new LineImage(new Posn(0, 75), Color.black);
    LineImage line15 = new LineImage(new Posn(75, 0), Color.black);
    LineImage line16 = new LineImage(new Posn(75, 0), Color.black);
    LineImage line17 = new LineImage(new Posn(0, 75), Color.black);
    LineImage line18 = new LineImage(new Posn(0, 75), Color.black);
    LineImage line19 = new LineImage(new Posn(75, 0), Color.black);
    LineImage line20 = new LineImage(new Posn(75, 0), Color.black);
    LineImage line21 = new LineImage(new Posn(75, 0), Color.black);

    TextImage text1 = new TextImage("11", 75, Color.white);

    world.placeImageXY(rect2, 37, 37);
    world.placeImageXY(rect3, 112, 37);
    world.placeImageXY(rect4, 187, 37);
    world.placeImageXY(rect5, 262, 37);
    world.placeImageXY(rect6, 37, 112);
    world.placeImageXY(rect7, 112, 112);
    world.placeImageXY(rect8, 187, 112);
    world.placeImageXY(rect9, 262, 112);
    world.placeImageXY(rect10, 37, 187);
    world.placeImageXY(rect11, 112, 187);
    world.placeImageXY(rect12, 187, 187);
    world.placeImageXY(rect13, 262, 187);
    world.placeImageXY(rect14, 37, 262);
    world.placeImageXY(rect15, 112, 262);
    world.placeImageXY(rect16, 187, 262);
    world.placeImageXY(rect17, 262, 262);
    world.placeImageXY(rect18, 37, 337);
    world.placeImageXY(rect19, 112, 337);
    world.placeImageXY(rect20, 187, 337);
    world.placeImageXY(rect21, 262, 337);
    world.placeImageXY(rect22, 37, 412);
    world.placeImageXY(rect23, 112, 412);
    world.placeImageXY(rect24, 187, 412);
    world.placeImageXY(rect25, 262, 412);
    world.placeImageXY(rect26, 37, 487);
    world.placeImageXY(rect27, 112, 487);
    world.placeImageXY(rect28, 187, 487);
    world.placeImageXY(rect29, 262, 487);
    world.placeImageXY(rect30, 37, 562);
    world.placeImageXY(rect31, 112, 562);
    world.placeImageXY(rect32, 187, 562);
    world.placeImageXY(rect33, 262, 562);

    world.placeImageXY(line1, 150, 412);
    world.placeImageXY(line2, 187, 300);
    world.placeImageXY(line3, 75, 562);
    world.placeImageXY(line4, 150, 262);
    world.placeImageXY(line5, 75, 187);
    world.placeImageXY(line6, 262, 300);
    world.placeImageXY(line7, 37, 450);
    world.placeImageXY(line8, 187, 525);
    world.placeImageXY(line9, 37, 75);
    world.placeImageXY(line10, 262, 150);
    world.placeImageXY(line11, 37, 300);
    world.placeImageXY(line12, 75, 337);
    world.placeImageXY(line13, 225, 37);
    world.placeImageXY(line14, 150, 37);
    world.placeImageXY(line15, 187, 225);
    world.placeImageXY(line16, 187, 150);
    world.placeImageXY(line17, 225, 412);
    world.placeImageXY(line18, 150, 487);
    world.placeImageXY(line19, 37, 225);
    world.placeImageXY(line20, 112, 525);
    world.placeImageXY(line21, 262, 450);
    world.placeImageXY(text1, 42, 37);

    return t.checkExpect(maze5.makeScene(), world);
  }

  public boolean testMakeScene3(Tester t) {
    this.maze6.reset();
    this.maze6.onKeyEvent("m");
    this.maze6.onKeyEvent("down");
    this.maze6.onKeyEvent("right");
    this.maze6.onKeyEvent("down");
    this.maze6.onKeyEvent("right");
    this.maze6.onKeyEvent("right");
    this.maze6.onKeyEvent("right");
    this.maze6.onKeyEvent("down");
    WorldScene world = new WorldScene(750, 600);
    RectangleImage rect2 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.blue);
    RectangleImage rect3 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect4 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect5 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect6 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect7 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.blue);
    RectangleImage rect8 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.blue);
    RectangleImage rect9 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect10 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect11 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect12 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect13 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.blue);
    RectangleImage rect14 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.blue);
    RectangleImage rect15 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.blue);
    RectangleImage rect16 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.blue);
    RectangleImage rect17 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect18 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect19 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect20 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect21 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.blue);

    LineImage line1 = new LineImage(new Posn(150, 0), Color.black);
    LineImage line2 = new LineImage(new Posn(0, 150), Color.black);
    LineImage line3 = new LineImage(new Posn(0, 150), Color.black);
    LineImage line4 = new LineImage(new Posn(150, 0), Color.black);
    LineImage line5 = new LineImage(new Posn(0, 150), Color.black);
    LineImage line6 = new LineImage(new Posn(0, 150), Color.black);
    LineImage line7 = new LineImage(new Posn(150, 0), Color.black);
    LineImage line8 = new LineImage(new Posn(150, 0), Color.black);
    LineImage line9 = new LineImage(new Posn(0, 150), Color.black);
    LineImage line10 = new LineImage(new Posn(150, 0), Color.black);
    LineImage line11 = new LineImage(new Posn(0, 150), Color.black);
    LineImage line12 = new LineImage(new Posn(150, 0), Color.black);

    TextImage text1 = new TextImage("The Maze is Solved", 45, FontStyle.BOLD, Color.red);
    TextImage text2 = new TextImage("0", 150, Color.white);

    world.placeImageXY(rect2, 75, 75);
    world.placeImageXY(rect3, 225, 75);
    world.placeImageXY(rect4, 375, 75);
    world.placeImageXY(rect5, 525, 75);
    world.placeImageXY(rect6, 675, 75);
    world.placeImageXY(rect7, 75, 225);
    world.placeImageXY(rect8, 225, 225);
    world.placeImageXY(rect9, 375, 225);
    world.placeImageXY(rect10, 525, 225);
    world.placeImageXY(rect11, 675, 225);
    world.placeImageXY(rect12, 75, 375);
    world.placeImageXY(rect13, 225, 375);
    world.placeImageXY(rect14, 375, 375);
    world.placeImageXY(rect15, 525, 375);
    world.placeImageXY(rect16, 675, 375);
    world.placeImageXY(rect17, 75, 525);
    world.placeImageXY(rect18, 225, 525);
    world.placeImageXY(rect19, 375, 525);
    world.placeImageXY(rect20, 525, 525);
    world.placeImageXY(rect21, 675, 525);

    world.placeImageXY(line1, 375, 300);
    world.placeImageXY(line2, 450, 225);
    world.placeImageXY(line3, 150, 375);
    world.placeImageXY(line4, 75, 300);
    world.placeImageXY(line5, 600, 525);
    world.placeImageXY(line6, 300, 75);
    world.placeImageXY(line7, 675, 300);
    world.placeImageXY(line8, 375, 450);
    world.placeImageXY(line9, 300, 525);
    world.placeImageXY(line10, 525, 300);
    world.placeImageXY(line11, 600, 225);
    world.placeImageXY(line12, 225, 150);

    world.placeImageXY(text1, 375, 300);
    world.placeImageXY(text2, 80, 75);

    return t.checkExpect(maze6.makeScene(), world);
  }

  public boolean testMakeScene4(Tester t) {
    initData();
    this.maze1.onKeyEvent("e");

    WorldScene world = new WorldScene(600, 600);
    RectangleImage rect1 = new RectangleImage(600, 600, OutlineMode.OUTLINE, Color.black);
    RectangleImage rect2 = new RectangleImage(600, 600, OutlineMode.SOLID, Color.yellow);
    TextImage text1 = new TextImage("Game Over!", 50, Color.blue);
    OverlayImage over1 = new OverlayImage(text1, rect2);

    world.placeImageXY(rect1, 300, 300);
    world.placeImageXY(over1, 300, 300);

    return t.checkExpect(this.maze1.makeScene(), world);
  }

  // tests the lastScene method
  public boolean testLastScene(Tester t) {
    this.maze1.onKeyEvent("e");
    this.maze2.onKeyEvent("e");
    this.maze3.onKeyEvent("e");

    WorldScene world = new WorldScene(600, 600);
    RectangleImage rect1 = new RectangleImage(600, 600, OutlineMode.OUTLINE, Color.black);
    RectangleImage rect2 = new RectangleImage(600, 600, OutlineMode.SOLID, Color.yellow);
    TextImage text1 = new TextImage("Game Over!", 50, Color.blue);
    OverlayImage over1 = new OverlayImage(text1, rect2);

    world.placeImageXY(rect1, 300, 300);
    world.placeImageXY(over1, 300, 300);

    WorldScene world2 = new WorldScene(300, 600);
    RectangleImage rect3 = new RectangleImage(300, 600, OutlineMode.OUTLINE, Color.black);
    RectangleImage rect4 = new RectangleImage(300, 600, OutlineMode.SOLID, Color.yellow);
    OverlayImage over2 = new OverlayImage(text1, rect4);

    world2.placeImageXY(rect3, 150, 300);
    world2.placeImageXY(over2, 150, 300);

    WorldScene world3 = new WorldScene(750, 600);
    RectangleImage rect5 = new RectangleImage(750, 600, OutlineMode.OUTLINE, Color.black);
    RectangleImage rect6 = new RectangleImage(750, 600, OutlineMode.SOLID, Color.yellow);
    OverlayImage over3 = new OverlayImage(text1, rect6);

    world3.placeImageXY(rect5, 375, 300);
    world3.placeImageXY(over3, 375, 300);

    return t.checkExpect(this.maze1.makeScene(), world)
        && t.checkExpect(this.maze2.makeScene(), world2)
        && t.checkExpect(this.maze3.makeScene(), world3);

  }
}


