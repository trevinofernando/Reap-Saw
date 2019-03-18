import java.util.*;
import java.io.*;

class Ideone{
	
	public static int board_size_x; //Coloums # i
	public static int board_size_y; //Rows # j
	public static int max_holes_col;
	public static int max_holes_rows; 
	public static int[] counter_for_col;
	public static int[] counter_for_rows;
	public static int[][] board_to_solve;
	public static int[][] board_scheme;
	public static final int IMPOSSIBLE = 0;
	public static final int EMPTY_SPACE = -1;
	public static final int FORCED_USED_SPACE = 1;
	public static boolean swaped = false;

	public static void main(String[] Args){
		Scanner sc = new Scanner(System.in);

		// Read in the size of the board and the number of groups
		board_size_x = sc.nextInt();
		board_size_y = sc.nextInt();
		/*
		//Swap variables to make x greater or equal to y
		if(board_size_x < board_size_y){
			board_size_x = board_size_x + board_size_y;   
			board_size_y = board_size_x - board_size_y;
			board_size_x = board_size_x - board_size_y;
			swaped = true;
		}
		*/
		
		max_holes_col = board_size_y / 2; //Num of rows/2 = holes in each col
		max_holes_rows = board_size_x / 2;//Num of col/2 = holes in each row
		
		// Initialize the board
		board_to_solve = new int[board_size_x][board_size_y];
		board_scheme = new int[board_size_x][board_size_y];
		counter_for_col = new int[board_size_x];
		counter_for_rows = new int[board_size_y];
		
		//Fill Board scheme with input, and initialze board_to_solve with 0's and place 1's where they should be.
		for (int i = 0; i < board_size_x; i++){	
			counter_for_col[i]=0; //Initialize counter for col
            for (int j = 0; j < board_size_y; j++){
				if(i==0)
					counter_for_rows[j]=0; //Initialize counter for rows
                board_scheme[i][j] = sc.nextInt();
				if(board_scheme[i][j] == FORCED_USED_SPACE){//1
					board_to_solve[i][j] = FORCED_USED_SPACE;
					//Increase counters to know # of holes until max_holes
					counter_for_col[i] += 1; 
					counter_for_rows[j] += 1;
				}
				else
					board_to_solve[i][j]= 0;
            }
        }

		// Try to back track a solution for the puzzle
		if (backTrack(0, 0)){
			//System.out.println("Succesfully found solution!");
			printBoard();
		}
		else{
			System.out.println("Failed to find solution.");
		}
	}

	// start at 0, 0 fill in the board row by row, each row is filled from left to right
	public static boolean backTrack(int col, int row){
				
		if (col == board_size_x){
			
			//Check counter of this row
			if(counter_check_row(row))//If row not filled
				return false;//Go Back
			
			//Check pattern above by rows
			if(!pattern_check_col(col-1, row, 1 , 0))//If pattern repeats somewhere before this row
				return false; //Go back
			return backTrack(0, row + 1);//If unique and counter check, Go to the next Row 
		}
		if (row == board_size_y){
			
			//Check COUNTERS for columns
			for(int i=0; i<board_size_x; i++){//Iterate through each col in this row
				if(counter_check_col(i))//Make sure the counters are filled
					return false; //If counters not full, counter_check_col = True, Therefore return false and go back.
			}
			
			//Check board for PATTERN by columns
			for(int i = 1; i <= board_size_x; i++){
				//Use board_size_x to have original size of col since it just got set to 0
				//Substract i to test each col against the ones before
				//Invert row and col values to check by columns intead of rows
				if(!pattern_check_row(board_size_x - i, row-1, 0 , 1)) //If is not unique
					return false;//Go back :(
			}
			return true; //Else solution found :D
		}
		if(!available_space(col, row)){ //If Forced used space, skip and go to the next col
			return backTrack(col + 1, row);
		}
		if(!counters_check(col, row)){ //If is not safe to add a 1 due to the counters
			if(!rule_of_3_consecutive_0s(col, row))//Then check if we can place a 0
				return false; //Stop backTracking if can't place 1 due counter and 0 due rule of 3
			board_to_solve[col][row] = 0;//Else leave as 0
			return backTrack(col + 1, row);//And go to the next iteration
		}
		if(!rule_of_3_consecutive_1s(col, row)){//If is not safe to add a 1 due to the rule of 3
			if(!rule_of_3_consecutive_0s(col, row))//Then check if we can place a 0
				return false; //Stop backTracking if can't place 1 or 0 by rule of 3
			return backTrack(col + 1, row);//Else leave as 0 and go to the next iteration
		}
		
		//If safe to add a 1
		board_to_solve[col][row] = 1; //Place 1
		counter_for_col[col]+=1;
		counter_for_rows[row]+=1;
		if (backTrack(col + 1, row)) //Go to the next col
			return true;
		//if(rule_of_3_consecutive_0s(col, row)){//If is safe to place 0
			board_to_solve[col][row] = 0; //Reverse to 0, since the 1 didn't work
			//Fix counters
			counter_for_col[col]-=1;
			counter_for_rows[row]-=1;
			if (backTrack(col + 1, row)) //Go to the next col
				return true;
		//}
		
		// Unable to find a solution returning false!
		return false;
	}

	public static boolean available_space(int col, int row){
		if(col == board_size_x)
			return false;
		if(board_scheme[col][row] != EMPTY_SPACE)
			return false;
		return true;
	}
	
	public static boolean counters_check(int col, int row){ //True = safe to add 1? || False = Counter filled
		if(!counter_check_col(col))
			return false;
		if(!counter_check_row(row))
			return false;
		return true;
	}
	
	public static boolean counter_check_col(int col){ //True = safe to add 1? || False = Counter filled
		if(counter_for_col[col] == max_holes_col)
			return false;
		return true;
	}
	
	public static boolean counter_check_row(int row){ //True = safe to add 1? || False = Counter filled
		if(counter_for_rows[row] == max_holes_rows)
			return false;
		return true;
	}
	
	public static boolean rule_of_3_consecutive_0s(int col, int row){ //Can_Place_Zero? False = Have to place 1
		//Check "2 Prior 0s Horizontally" CASE
		if(col > 1){
			if((board_to_solve[col-2][row] + board_to_solve[col-1][row]) == 0)//If both are 0, then can't place a 0 here
				return false;
		}
		//Check "2 Prior 0s Vertically" CASE
		if(row > 1){
			if((board_to_solve[col][row-2] + board_to_solve[col][row-1]) == 0)//If both are 0, then can't place a 0 here
				return false;
		}
		//Check "1 Vertically ahead - 1 Vertically prior" CASE
		//Check "2 Vertically ahead 0s" CASE
		if(row < board_size_y -1){
			if(board_scheme[col][row+1]==IMPOSSIBLE){//board_scheme have future forced values
				if(row > 0){//Check "1 Vertically ahead, 1 Vertically prior" CASE
					if(board_to_solve[col][row-1]==0)//board_to_solve have past used values
						return false;
				}
				if(row < board_size_y - 2){//Check "two spaces ahead" CASE
					if(board_scheme[col][row+2]==IMPOSSIBLE)//board_scheme have future forced values
						return false;
				}
			}
		}
		return true;
	}
	
	public static boolean rule_of_3_consecutive_1s(int col, int row){ //Can_Place_One? False = Have to place 0
		//Check col first
		if(col > 0){
			if(board_to_solve[col-1][row]==1){//Check one prior space
				if(col > 1){
					if(board_to_solve[col-2][row]==1)//Check "2 prior spaces Horizontally" CASE
						return false;
				}
				if(col < board_size_x - 1){//Check one space ahead for "one prior one ahead Horizontally" CASE
					if(board_scheme[col+1][row]==FORCED_USED_SPACE)
							return false;
				}
			}
			
		}
		if(col < board_size_x - 2){
			if(board_scheme[col+2][row]==FORCED_USED_SPACE){//Check two spaces ahead
				if(board_scheme[col+1][row]==FORCED_USED_SPACE)//Check "two spaces ahead Vertically" CASE
					return false;
			}
		}
		//Check rows now
		if(row > 0){ 
			if(board_to_solve[col][row-1]==1){ //Check one prior space
				if(row > 1){
					if(board_to_solve[col][row-2]==1)//Check "2 prior spaces Vertically" CASE
						return false;
				}
				if(row < board_size_y - 1){//Check one space ahead for "one prior one ahead Vertically" CASE
					if(board_scheme[col][row+1]==FORCED_USED_SPACE)
							return false;
				}
			}
			
		}
		if(row < board_size_y - 2){
			if(board_scheme[col][row+2]==FORCED_USED_SPACE){//Check two spaces ahead
				if(board_scheme[col][row+1]==FORCED_USED_SPACE)//Check "two spaces ahead" CASE
					return false;
			}
		}		
		return true;
	}
	
	public static boolean pattern_check_col(int col, int row, int dif_between_rows, int dif_between_col){
		if(row - dif_between_rows < 0) //No more prior rows to check
			return true;//Therefore pattern is UNIQUE
		if(col < 0) //Finish entire row with same values
			return false; //Pattern repeats 
		if(board_to_solve[col][row]==board_to_solve[col][row-dif_between_rows])//Compare Current place with the one above
			 return pattern_check_col(col - 1, row, dif_between_rows, dif_between_col + 1);//If eqaul Check the rest of that row  
		 else
			 return pattern_check_col(col + dif_between_col, row, dif_between_rows + 1, 0 );//If not, move to the next row, and reset col
	}
	
	public static boolean pattern_check_row(int col, int row, int dif_between_rows, int dif_between_col){
		if(col - dif_between_col < 0) //No more prior col to check
			return true;//Therefore pattern is UNIQUE
		if(row < 0) //Finish entire col with same values
			return false; //Pattern repeats 
		if(board_to_solve[col][row]==board_to_solve[col-dif_between_col][row])//Compare Current place with the one above
			 return pattern_check_row(col, row -1, dif_between_rows+1, dif_between_col);//If eqaul Check the rest of that col  
		 else
			 return pattern_check_row(col, row + dif_between_rows, 0, dif_between_col + 1 );//If not, move to the next col, and reset row
	}
	
	public static void printBoard(){
		System.out.println();
		// Print each row
		for (int i = 0; i < board_size_x; i++){
			// Print each element in the current row
			for (int j = 0; j < board_size_y; j++){

				// Do some spacing for 1 digit values
				if (board_to_solve[i][j] < 10)
					System.out.print(" ");

				// Print the value for the board
				System.out.print(board_to_solve[i][j] + " ");
			}
			System.out.println();
		}
	}
}
