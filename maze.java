/* 
Maze Builder and Solver

This program generates an R by C randomized maze and solves it using breadth-first search
and depth-first search to solve it, printing out the unsolved maze followed by
the partial and complete solutions for each search

Run with the command java maze.java "R" "C"

with R being the number of rows and C being the number of columns. If neither are given,
a default maze of 20x20 will be generated
By Max Silverstein
 */

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class maze {
     
    private int R;						// rows
    private int C;						// columns
    
    //Maze Parameters: Walls
    private boolean[][] North;     		// true if there's a wall to the North of cell i, j
    private boolean[][] East;			// true if there's a wall to the East of cell i, j
    private boolean[][] South;			// true if there's a wall to the South of cell i, j
    private boolean[][] West;			// true if there's a wall to the West of cell i, j
    
    //Maze Parameters: Union-Search Array
    private mycell[][] mazecells; 		// union-search array for maze construction
    
    
    //Variables used for performing search algorithms
    private boolean[][] visited;			// true if cell i, j is visited by search algorithm
    private boolean finished = false;	// true if maze solving is finished
    private int[][] path_num;			// path number
    private String[][] hash_tag_path;	// hash path
    private int counter = 0;				// counter
    String s_horizontal = "";			// horizontal string
    String s_vertical = "";				// vertical string
    
    //Maze constructor
    public maze(int r, int c) {
        R = r;	// j, y 
        C = c;	// i, x
        mazecells = cellarray(C,R);
        
        
        initialize();
        generate(C,R);

    }
    
    //Initialize the maze: set all walls to true, initialized search parameters
    public void initialize() {
        // initialize the border cells as ones that are already visited
        visited = new boolean[C + 2][R + 2];
        for (int x = 0; x < C + 2; x++) {
            visited[x][0] = visited[x][R + 1] = true;
        }
        for (int y = 0; y < R + 2; y++) {
            visited[0][y] = visited[C + 1][y] = true;
        }

        // initialize all the walls as present
        North = new boolean[C + 2][R + 2];
        East = new boolean[C + 2][R + 2];
        South = new boolean[C + 2][R + 2];
        West = new boolean[C + 2][R + 2];
        for (int x = 0; x < C + 2; x++) {
            for (int y = 0; y < R + 2; y++) {
                North[x][y] = East[x][y] = South[x][y] = West[x][y] = true;
            }
        }

        path_num = new int[C][R];
        hash_tag_path = new String[C][R];

        // set all default has_tag_path values to an empty string
        for (int i = 0; i < C; i++) {
            for (int j = 0; j < R; j++) {
                hash_tag_path[i][j] = " ";
            }
        }
    }
    
    
    
    //cell class that uses linked list and keeps track of x and y components of maze
	public class mycell{
		private int x;
		private int y;
        private int size = -1;
        private mycell parent;
        private boolean root = true;
		
		public mycell(int x, int y){
			this.x = x;
			this.y = y;
            this.parent = this;
            this.root = true;
		}
		
		public mycell(int x, int y, mycell c){
			this.x = x;
			this.y = y;
			this.setparent(c);
            this.root = false;
		}
                
        public int getx(){
        	return this.x;
        }
                
        public int gety(){
        	return this.y;
        }
        
        public void setx(int i){
        	this.x = i;
        }
        
        public void sety(int j){
        	this.y = j;
        }
                
        public mycell getparent(){
        	return this.parent;
        }
        
        public void setparent(mycell p){
        	this.parent = p;
        }
 
    
        public int getsize(){
        	return this.size;
        }
                
        public void setsize(int s){
        	this.size = s;
        }
         
        public boolean cellequals(mycell a){
        	return (this.getx() == a.getx()) && (this.gety() == a.gety());
        }
        
        public boolean isroot(){
        	return this.root;
        }
        
        public void setroot(boolean a){
        	this.root = a;
        }
        
	} 
	
    //initialize array of mycells
	public mycell[][] cellarray(int c, int r){
		mycell[][] temp = new mycell[c][r];
		for (int i = 0; i<c; i++){
			for(int j = 0; j<r; j++){
				temp[i][j] = new mycell(i,j);
                                
			}
		}
		return temp;
	}
	
    //find set method to find the root of the set
	public mycell findroot(mycell a){
		if(a.isroot()){
			return a;
		}
        
		//flattens tree to one layer of children, to simplify future searches
		else{
        	mycell result = findroot(a.getparent());
        	a.setparent(result);
        	a.setroot(false); 
        	return result;
         	}
	}

    //union method that joins two sets together
	public void cellunion(mycell a, mycell b){
		mycell x = findroot(a);
        mycell y = findroot(b);
               
        if (!x.cellequals(y)){
        	int tsize = x.getsize() + y.getsize();
        	int xsize = x.getsize();
        	int ysize = y.getsize();
        	if(y.getsize() < x.getsize()){
        		findroot(a).setsize((xsize*-1));
        		findroot(a).setparent(findroot(b));
        		findroot(a).setroot(false);
        		findroot(b).setsize(tsize);
            	}
        	else{
        		findroot(b).setsize(ysize*-1);
        		findroot(b).setparent(findroot(a));
        		findroot(b).setroot(false);
        		findroot(a).setsize(tsize);  
        	}
        }
	}
    
    //method to initialize a list with all possible adjacent room connections for a maze with C columns and R rows
    public List<int[]> populatewalls(int c, int r){
        List<int[]> temp = new ArrayList<>();
        
        //vertical walls
        for (int j = 0; j<r; j++){
        	for(int i = 0; i<(c-1); i++){
        		int[] xy = new int[4];
        		xy[0] = i;
        		xy[1] = j;
        		xy[2] = i+1;
        		xy[3] = j;       
        		temp.add(xy);        
        	}
        }
        // horizontal walls
        for (int i = 0; i<c; i++){
        	for(int j = 0; j<(r-1); j++){
        		int[] xy = new int[4];
        		xy[0] = i;
        		xy[1] = j;
        		xy[2] = i;
        		xy[3] = j+1;       
        		temp.add(xy);                
        	}
        } 
        return temp;
    }
    // Method to generate a randomized maze
    public void generate(int x, int y) { 
        Random random = new Random();
        List<int[]> candidates = populatewalls(x,y);
        
        while(!candidates.isEmpty()){ 
            int[] temp = new int[4]; 
            int index = random.nextInt(candidates.size());
            temp = candidates.remove(index);
            int x1 = temp[0];
            int y1 = temp[1];
            int x2 = temp[2];
            int y2 = temp[3]; 
            mycell a = findroot(mazecells[x1][y1]);
            mycell b = findroot(mazecells[x2][y2]);
            
            if(!a.cellequals(b)){
                cellunion((findroot(mazecells[x1][y1])), (findroot(mazecells[x2][y2])));
                visited[x1+1][y1+1] = visited[x2+1][y2+1] = true;
                if((x1 == x2) && ((y2-y1)== 1)){
                    East[x1+1][y1+1] = West[x2+1][y2+1] = false; 
                } else if((y1 == y2) && ((x2-x1)== 1)){
                    South[x1+1][y1+1] = North[x2+1][y2+1] = false;
                } else{
                    System.out.println("error - something has gone terribly wrong");
                    return;
                 }            
            }
        }     
     }
  //Method to compare to correct solution
    public int check_path(int x, int y) {

        // check to see if node goes to the border values
        if (x < 1 || y < 1 || x > C || y > R) {
            return 2;
        }

        if (x == C && y == R) {
            visited[x][y] = false;
            counter++;
            path_num[x - 1][y - 1] = counter;
            return 1;
        }
        
        if (!visited[x][y]) {
            return 2;
        }

        visited[x][y] = false;
        counter++;
        path_num[x - 1][y - 1] = counter;


        return 0;
    }

    public void display_BFS_num() {
       
    	// display bfs path numbers
        for (int k = 0, i = 1; k < 2 * C + 1; k++) {

            if (k % 2 == 0) {
                s_horizontal = "";
                for (int j = 1; j < North[0].length - 1; j++) {
                    if (North[i][j] == true) {
                    	if ((k == 0 && j == 1) || (j == North[0].length - 2 && k == 2 * C )){
                    		s_horizontal = s_horizontal + "+ ";
                    	}
                    	else{
                    		s_horizontal = s_horizontal + "+-";
                    	}

                    } 
                    else {
                        s_horizontal = s_horizontal + "+ ";
                    }
                    if (j == North[0].length - 2) {
                        s_horizontal = s_horizontal + "+";
                    }
                }

                System.out.println(s_horizontal);

            } 
            else {
                s_vertical = "";
                for (int j = 1; j < West[0].length - 1; j++) {
                    if (West[i][j] == true) {
                    	if(path_num[i-1][j-1] == 0){
                    		s_vertical = s_vertical + "|" + " ";
                    	}
                    	else{
                        s_vertical = s_vertical + "|" + (path_num[i - 1][j - 1]-1)%10;
                    	}
                    } 
                    else {
                    	if(path_num[i-1][j-1] == 0){
                    		s_vertical = s_vertical + " " + " ";
                    	}
                    	else{
                        s_vertical = s_vertical + " " + (path_num[i - 1][j - 1]-1)%10;
                    	}
                    }
                    if (j == West[0].length - 2) {
                        if (East[i][j] == true) {
                            s_vertical = s_vertical + "|";
                        } else if (East[i][j] == false) {
                            s_vertical = s_vertical + "";
                        }
                    }

                }
                System.out.println(s_vertical);
                i++;
            }

        }

    }

    // solve using bfs
    public void BFS_path() {

        counter = 0;
        // set visited to true for all the nodes
        for (int i = 1; i <= C; i++) {
            for (int j = 1; j <= R; j++) {
                visited[i][j] = true;
            }
        }

        for (int i = 0; i < C; i++) {
            for (int j = 0; j < R; j++) {
                path_num[i][j] = 0;
            }
        }

        List<Integer> l1x = new LinkedList<Integer>();
        List<Integer> l2y = new LinkedList<Integer>();

        l1x.add(1);
        l2y.add(1);
        visited[1][1] = false;

        counter++;
        path_num[1 - 1][1 - 1] = counter;

        while (!l1x.isEmpty()) {
            int x = l1x.get(0);
            int y = l2y.get(0);
            l1x.remove(0);
            l2y.remove(0);
            if(check_path(x, y)==1){
                break;
            }

            if (North[x][y] == false) {
                if (check_path(x - 1, y) == 0) {
                    l1x.add(x - 1);
                    l2y.add(y);
                } 
                else if (check_path(x - 1, y) == 1) {
                    break;
                }
            }
            if (West[x][y] == false) {
                if (check_path(x, y - 1) == 0) {
                    l1x.add(x);
                    l2y.add(y - 1);
                } 
                else if (check_path(x, y - 1) == 1) {
                    break;
                }
            }
            if (East[x][y] == false) {
                if (check_path(x, y + 1) == 0) {
                    l1x.add(x);
                    l2y.add(y + 1);
                } 
                else if (check_path(x, y + 1) == 1) {
                    break;
                }
            }
            if (South[x][y] == false) {
                if (check_path(x + 1, y) == 0) {
                    l1x.add(x + 1);
                    l2y.add(y);
                } 
                else if (check_path(x + 1, y) == 1) {
                    break;
                }
            }
        }
        path_num[C-1][R-1]--;
    }
    
    // for hash_tag_path
    public int check_path1(int x, int y) {

        // check to see if node goes to the border values
        if (x < 1 || y < 1 || x > C || y > R) {
            return 2;
        }

        if (x == C && y == R) {
            visited[x][y] = false;
            hash_tag_path[x - 1][y - 1] = "#";
            return 1;
        }
        
        if (!visited[x][y]) {
            return 2;
        }

        visited[x][y] = false;
        hash_tag_path[x - 1][y - 1] = "#";

        return 0;
    }
    
    // for hash_tag_path
    public void BFS_path1() {

        // set visited to false for all the nodes
        for (int i = 1; i <= C; i++) {
            for (int j = 1; j <= R; j++) {
                visited[i][j] = false;
            }
        }

        for (int i = 0; i < C; i++) {
            for (int j = 0; j < R; j++) {
                path_num[i][j] = 0;
            }
        }

        List<Integer> l1x = new LinkedList<Integer>();
        List<Integer> l2y = new LinkedList<Integer>();

        l1x.add(1);
        l2y.add(1);
        visited[1][1] = false;

        hash_tag_path[1 - 1][1 - 1] = "#";

        while (!l1x.isEmpty()) {
            int x = l1x.get(0);
            int y = l2y.get(0);
            l1x.remove(0);
            l2y.remove(0);
            if(check_path1(x, y)==1){
                break;
            }

            if (North[x][y] == false) {
                if (check_path1(x - 1, y) == 0) {
                    l1x.add(x - 1);
                    l2y.add(y);
                } 
                else if (check_path1(x - 1, y) == 1) {
                    break;
                }
            }
            if (West[x][y] == false) {
                if (check_path1(x, y - 1) == 0) {
                    l1x.add(x);
                    l2y.add(y - 1);
                } 
                else if (check_path1(x, y - 1) == 1) {
                    break;
                }
            }
            if (East[x][y] == false) {
                if (check_path1(x, y + 1) == 0) {
                    l1x.add(x);
                    l2y.add(y + 1);
                } 
                else if (check_path1(x, y + 1) == 1) {
                    break;
                }
            }
            if (South[x][y] == false) {
                if (check_path1(x + 1, y) == 0) {
                    l1x.add(x + 1);
                    l2y.add(y);
                } 
                else if (check_path1(x + 1, y) == 1) {
                    break;
                }
            }
        }
    }
    
    public void display_DFS_num() {
  
        // display dfs number path with walls
        for (int k = 0, i = 1; k < 2 * C + 1; k++) {

            if (k % 2 == 0) {
                s_horizontal = "";
                for (int j = 1; j < North[0].length - 1; j++) {
                    if (North[i][j] == true) {
                    	if ((k == 0 && j == 1) || (j == North[0].length - 2 && k == 2 * C )){
                    		s_horizontal = s_horizontal + "+ ";
                    	}
                    	else{
                    		s_horizontal = s_horizontal + "+-";
                    	}
                    } 
                    else {
                        s_horizontal = s_horizontal + "+ ";
                    }
                    if (j == North[0].length - 2) {
                        s_horizontal = s_horizontal + "+";
                    }
                }
                System.out.println(s_horizontal);
            } 
            else {
                s_vertical = "";
                for (int j = 1; j < West[0].length - 1; j++) {
                    if (West[i][j] == true) {
                    	if(path_num[i-1][j-1] == 0){
                    		s_vertical = s_vertical + "|" + " ";
                    	}
                    	else{
                        s_vertical = s_vertical + "|" + (path_num[i - 1][j - 1]-1)%10;
                    	}
                    } 
                    else {
                    	if(path_num[i-1][j-1] == 0){
                    		s_vertical = s_vertical + " " + " ";
                    	}
                    	else{
                        s_vertical = s_vertical + " " + (path_num[i - 1][j - 1]-1)%10;
                    	}
                    }
                    if (j == West[0].length - 2) {
                        if (East[i][j] == true) {
                            s_vertical = s_vertical + "|";
                        } 
                        else if (East[i][j] == false) {
                            s_vertical = s_vertical + "";
                        }
                    }
                }
                
                System.out.println(s_vertical);

                i++;
            }
        }
    }

    // solve using DFS
    public void solve() {
        // set visited to false for all the nodes
        for (int i = 1; i <= C; i++) {
            for (int j = 1; j <= R; j++) {
                visited[i][j] = false;
            }
        }

        // starting cell
        solve(1, 1);
    }

    public void solve(int x, int y) {

        //if node goes to border values
        if (x == 0 || y == 0 || x == C + 1 || y == R + 1) {
            return;
        }

        if (finished || visited[x][y]) {
            return;
        }

        // else
        visited[x][y] = true;
        counter++;
        path_num[x - 1][y - 1] = counter;
        hash_tag_path[x - 1][y - 1] = "#";

        // at the final room
        if (x == C && y == R) {
            finished = true;
        }

        // if not at final room
        if (!North[x][y]) {
            solve(x - 1, y);
        }
        if (!East[x][y]) {
            solve(x, y + 1);
        }
        if (!South[x][y]) {
            solve(x + 1, y);
        }
        if (!West[x][y]) {
            solve(x, y - 1);
        }

        if (finished) {
            return;
        }

        // if you cannot go forward and you are not at the final node, you need to retreat/backup 
        hash_tag_path[x - 1][y - 1] = " ";
    }

    public void display_path() {
    	
        System.out.println(System.lineSeparator());
        
        // display hash path with walls
        for (int k = 0, i = 1; k < 2 * C + 1; k++) {

            if (k % 2 == 0) {
                s_horizontal = "";
                for (int j = 1; j < North[0].length - 1; j++) {
                    if (North[i][j] == true) {
                    	if ((k == 0 && j == 1) || (j == North[0].length - 2 && k == 2 * C )){
                    		s_horizontal = s_horizontal + "+ ";
                    	}
                    	else{
                    		s_horizontal = s_horizontal + "+-";
                    	}
                    } 
                    else {
                            s_horizontal = s_horizontal + "+ ";
                    }
                    if (j == North[0].length - 2) {
                        s_horizontal = s_horizontal + "+";
                    }
                }
                
                System.out.println(s_horizontal);
            } 
            else {
                s_vertical = "";
                for (int j = 1; j < West[0].length - 1; j++) {
                    if (West[i][j] == true) {
                        s_vertical = s_vertical + "|" + hash_tag_path[i - 1][j - 1];
                    } 
                    else {
                        s_vertical = s_vertical + " " + hash_tag_path[i - 1][j - 1];
                    }
                    if (j == West[0].length - 2) {
                        if (East[i][j] == true) {
                            s_vertical = s_vertical + "|";
                        } 
                        else if (East[i][j] == false) {
                            s_vertical = s_vertical + "";
                        }
                    }

                }
                System.out.println(s_vertical);

                i++;
            }
        }
    }



    public void display_the_maze() {

        for (int k = 0, i = 1; k < 2 * C + 1; k++) {

            // show horizontal lines
            if (k % 2 == 0) {
                s_horizontal = "";
                for (int j = 1; j < North[0].length - 1; j++) {
                    if (North[i][j] == true) {
                    	if ((k == 0 && j == 1) || (j == North[0].length - 2 && k == 2 * C )){
                    		s_horizontal = s_horizontal + "+ ";
                    	}
                    	else{
                    		s_horizontal = s_horizontal + "+-";
                    	}
                    } 
                    else {
                        s_horizontal = s_horizontal + "+ ";
                    }
                    if (j == North[0].length - 2) {
                        s_horizontal = s_horizontal + "+";
                    }
                }

                System.out.println(s_horizontal);

            } 
            // show vertical lines
            else {
                s_vertical = "";
                for (int j = 1; j < West[0].length - 1; j++) {
                    if (West[i][j] == true) {
                        s_vertical = s_vertical + "| ";

                    } 
                    else {
                        s_vertical = s_vertical + "  ";
                    }
                    if (j == West[0].length - 2) {
                        if (East[i][j] == true) {
                            s_vertical = s_vertical + "|";
                        } 
                        else if (East[i][j] == false) {
                            s_vertical = s_vertical + "";
                        }
                    }
                }
                System.out.println(s_vertical);

                i++;
            }
        }
    }
    //Method to print the unsolved maze, the partial and final solutions to DFS and BFS
    public void printmaze(){
        System.out.println("<pre>");
        display_the_maze();
        solve();
        
        System.out.println(System.lineSeparator() + "DFS:"+ System.lineSeparator());
        display_DFS_num();
        display_path();
        
        BFS_path1();   // final BFS solution
        BFS_path();    // intermediate BFS solution
        System.out.println(System.lineSeparator() + "BFS:"+ System.lineSeparator());
        display_BFS_num();
        display_path();
    }
    
   
         

    public static void main(String args[]) {
        int r; 
        int c;
        if (args.length == 2){
        	try{
        		r = Integer.parseInt(args[0]);
        		c = Integer.parseInt(args[1]);
        		if (r > 0 && c > 0){
        			new maze(c,r).printmaze();
        		}
        		else{
        			System.out.println("error - must have more than 0 rows and columns");
        		}
        	}
        	catch(Exception e){
        		System.out.println("error - need integer values for rows and columns");
        	}
        }
        else if (args.length == 0){
        	c = 20;
        	r = 20;
        	System.out.println("Default parameters used: 20 x 20");
        	new maze(c,r).printmaze();
        }
        else{
        	 System.out.println("error - wrong number of arguments");
        }
    }
}