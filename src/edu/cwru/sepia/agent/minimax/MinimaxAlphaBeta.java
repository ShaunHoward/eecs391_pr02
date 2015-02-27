package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinimaxAlphaBeta extends Agent {

    private final int numPlys;

    public MinimaxAlphaBeta(int playernum, String[] args)
    {
        super(playernum);

        if(args.length < 1)
        {
            System.err.println("You must specify the number of plys");
            System.exit(1);
        }

        numPlys = Integer.parseInt(args[0]);
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView newstate, History.HistoryView statehistory) {
        return middleStep(newstate, statehistory);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView newstate, History.HistoryView statehistory) {
        GameStateChild bestChild = alphaBetaSearch(new GameStateChild(newstate),
                0,
                true,
                new GameStateChild(new HashMap<Integer, Action>(), new GameState(Integer.MIN_VALUE)),
                new GameStateChild(new HashMap<Integer, Action>(), new GameState(Integer.MAX_VALUE)));

        return bestChild.action;
    }

    @Override
    public void terminalStep(State.StateView newstate, History.HistoryView statehistory) {

    }

    @Override
    public void savePlayerData(OutputStream os) {

    }

    @Override
    public void loadPlayerData(InputStream is) {

    }

    /**
     * You will implement this.
     *
     * This is the main entry point to the alpha beta search. Refer to the slides, assignment description
     * and book for more information.
     *
     * Try to keep the logic in this function as abstract as possible (i.e. move as much SEPIA specific
     * code into other functions and methods)
     *
     * @param node The action and state to search from
     * @param depth The remaining number of plies under this node
     * @param isMax if the search is on the max node
     * @param alpha The current best node for the maximizing node from this node to the root
     * @param beta The current best node for the minimizing node from this node to the root
     * @return The best child of this node with updated values
     */
    public GameStateChild alphaBetaSearch(GameStateChild node, int depth, boolean isMax, GameStateChild alpha, GameStateChild beta)
    {
    	if (depth == numPlys || node.state.isTerminal()){
    		return node;
    	}
    	if (isMax){
    		node.state.setIsMax(true);
    	} else {
    		node.state.setIsMax(false);
    	}
    	List<GameStateChild> children = orderChildrenWithHeuristics(node.state.getChildren());
    	
    	for (GameStateChild child : children){
    		int v = alphaBetaSearch(child, depth+1, !isMax, alpha, beta).state.getUtility();
    		if (isMax && v > alpha.state.getUtility()){
    			alpha = child;
    		} else if (!isMax && v < beta.state.getUtility()){
    			beta = child;
    		}
    		if (alpha.state.getUtility() >= beta.state.getUtility()){
    			return child;
    		}
    	}
    	
    	if (isMax){
    		return alpha;
    	}
    	return beta;
    }
    
    /**
     * You will implement this.
     *
     * Given a list of children you will order them according to heuristics you make up.
     * See the assignment description for suggestions on heuristics to use when sorting.
     *
     * Use this function inside of your alphaBetaSearch method.
     *
     * Include a good comment about what your heuristics are and why you chose them.
     *
     * @param children
     * @return The list of children sorted by your heuristic.
     */
    public List<GameStateChild> orderChildrenWithHeuristics(List<GameStateChild> children)
    {
        return children;
    }
    
//    public GameStateChild minimax(GameStateChild node, int depth, boolean maximizingPlayer){
//	    if (depth == 0 || node is a terminal node
//	        return the heuristic value of node
//	    if maximizingPlayer
//	        bestValue := -∞
//	        for each child of node
//	            val := minimax(child, depth - 1, FALSE)
//	            bestValue := max(bestValue, val)
//	        return bestValue
//	    else
//	        bestValue := +∞
//	        for each child of node
//	            val := minimax(child, depth - 1, TRUE)
//	            bestValue := min(bestValue, val)
//	        return bestValue
//    }

    
//    /**
//     * Returns the max value attainable from the given game state child, according to
//     * the given alpha, beta, and depth for the current minimax game tree.
//     * 
//     * @param child - the game state child with the game state and action map for the previous game state
//     * @param alpha - the max value found this far in the game search
//     * @param beta - the min value found this far in the game search
//     * @param currDepth - the depth of the current game tree
//     * @return the max value attainable from the given game state child
//     */
//	private GameStateChild maxValue(GameStateChild child, double alpha, double beta, int currDepth){
//	    GameState state = child.state;
//		
//	    //if state is terminal (all archers dead or depth limit reached)
//		if(state.getArcherHealth() == 0 || currDepth == 0){ 
//		    return child; //instead of state.getUtility()
//		} else {
//			//double v = Double.NEGATIVE_INFINITY;
//			List<GameStateChild> sortedChildren = orderChildrenWithHeuristics(state.getChildren());
//			for(GameStateChild sortedChild : sortedChildren){
//				vState = maxChild(vState, minValue(sortedChild, alpha, beta, currDepth - 1));
//				if (vState.state.getUtility() >= beta){ //instead of v >= beta
//					return vState;
//				}
//				alpha = Math.max(alpha, vState.state.getUtility());
//			}
//	        
//	        return vState;
//		}
//	}
	
	
	
//    /**
//     * Returns the min value attainable from the given game state child, according to
//     * the given alpha, beta, and depth for the current minimax game tree.
//     * 
//     * @param child - the game state child with the game state and action map for the previous game state
//     * @param alpha - the max value found this far in the game search
//     * @param beta - the min value found this far in the game search
//     * @param currDepth - the depth of the current game tree
//     * @return the min value attainable from the given game state child
//     */
//	private GameStateChild minValue(GameStateChild child, double alpha, double beta, int currDepth){
//	    GameState state = child.state;
//		
//	    //if state is terminal (all footmen dead or depth limit reached)
//		if(state.getFootmenHealth() == 0 || currDepth == 0){ 
//		    return child; //instead of state.getUtility()
//		} else {
//			GameStateChild vState = null;
//			//double v = Double.POSITIVE_INFINITY;
//			List<GameStateChild> sortedChildren = orderChildrenWithHeuristics(state.getChildren());
//			for(GameStateChild sortedChild : sortedChildren){
//				
//				vState = minChild(vState, maxValue(sortedChild, alpha, beta, currDepth - 1));
//				//v = min(v, maxValue(sortedChild, alpha, beta, depth));
//				if (vState.state.getUtility() <= alpha){ //instead of v <= alpha
//					return vState;
//				}
//				alpha = Math.min(alpha, vState.state.getUtility());
//			}
//	        
//	        return vState;
//		}
//	}
//	
//	/**
//	 * Returns the game state child with the maximum value of the given 
//	 * game state children.
//	 * 
//	 * @param values - the game state children to find the max of
//	 * @return the max game state child of the given game state children
//	 */
//	private GameStateChild maxChild(GameStateChild... states){
//		double max = Double.NEGATIVE_INFINITY;
//		GameStateChild maxChild = null;
//		for (GameStateChild state : states){
//			if (state.state.getWeight() > max){
//				max = state.state.getWeight();
//				maxChild = state;
//			}
//		}
//		return maxChild;
//	}
//	
//	/**
//	 * Returns the game state child with the minimum value of the given 
//	 * game state children.
//	 * 
//	 * @param values - the game state children to find the min of
//	 * @return the min game state child of the given game state children
//	 */
//	private GameStateChild minChild(GameStateChild... states){
//		double min = Double.POSITIVE_INFINITY;
//		GameStateChild minChild = null;
//		for (GameStateChild state : states){
//			if (state.state.getWeight() < min){
//				min = state.state.getWeight();
//				minChild = state;
//			}
//		}
//		return minChild;
//	}

 
}
