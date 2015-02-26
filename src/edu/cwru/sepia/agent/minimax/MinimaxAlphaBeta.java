package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;

import java.io.InputStream;
import java.io.OutputStream;
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
                numPlys,
                Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY);

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
     * @param depth The remaining number of plys under this node
     * @param alpha The current best value for the maximizing node from this node to the root
     * @param beta The current best value for the minimizing node from this node to the root
     * @return The best child of this node with updated values
     */
    public GameStateChild alphaBetaSearch(GameStateChild node, int depth, double alpha, double beta)
    {
    	
        return node;
    }
    
    /**
     * Returns the max value attainable from the given game state child, according to
     * the given alpha, beta, and depth for the current minimax game tree.
     * 
     * @param child - the game state child with the game state and action map for the previous game state
     * @param alpha - the max value found this far in the game search
     * @param beta - the min value found this far in the game search
     * @param depth - the depth of the current game tree
     * @return the max value attainable from the given game state child
     */
	private double maxValue(GameStateChild child, double alpha, double beta, int depth){
	    GameState state = child.state;
		
	    //if state is terminal (all archers dead or depth limit reached)
		if(state.getChildren().isEmpty()){ 
		    return state.getUtility();
		} else {
			double v = Double.NEGATIVE_INFINITY;
			List<GameStateChild> sortedChildren = orderChildrenWithHeuristics(state.getChildren());
			for(GameStateChild sortedChild : sortedChildren){
				v = max(v, minValue(sortedChild), alpha, beta);
				if (v >= beta){
					return v;
				}
				alpha = max(alpha, v);
			}
	        
	        return v;
		}
	}
	
	/**
	 * Returns the maximum value of the given input values.
	 * 
	 * @param values - the double value(s) to find the max of
	 * @return the max value of the given values
	 */
	private double max(double... values){
		double max = Double.NEGATIVE_INFINITY;
		for (double value : values){
			if (value > max){
				max = value;
			}
		}
		return max;
	}
	
	/**
	 * Returns the minimum value of the given input values.
	 * 
	 * @param values - the double value(s) to find the min of
	 * @return the min value of the given values
	 */
	private double min(double... values){
		double min = Double.POSITIVE_INFINITY;
		for (double value : values){
			if (value < min){
				min = value;
			}
		}
		return min;
	}

function minValue(GameStateChild child, double a, double b, int depth)
	state <- child.state
	//input is a state and a depth, alpha and beta
	if state is a terminal
	    return a utility value
	else
		v = +inf
		sortedChildren <- orderChildrenWithHeuristics(state.getChildren())
		for each child in sortedChildren
			v <- min(v, maxValue(child), alpha, beta))
			if v <= alpha
				return v
			alpha <- min(alpha, v)
		end for
		return v

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
}
