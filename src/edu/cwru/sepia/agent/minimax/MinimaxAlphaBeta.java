package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A minimax, depth-limited search agent with alpha-beta pruning that plays a
 * game between a team of footmen and a team of archers in the SEPIA game engine.
 * The search algorithm is designed such that the footmen are the max player and
 * the archers are the min player. Alpha-beta pruning is implemented by disregarding
 * any game states that would lead to worse actions than previously examined game states. 
 * Such states need not be evaluated further and thus the best actions for each player
 * are utilized based on a weighted utility function in order to find the maximum and/or
 * minimum game state child. From this the algorithm can decide the 
 * next set of actions to be carried out by SEPIA that are best for the current player.
 * 
 * @author Shaun Howard (smh150), Matt Swartwout (mws85)
 */
public class MinimaxAlphaBeta extends Agent {

	private static final long serialVersionUID = 1L;
	private final int numPlys;

	/**
	 * Constructor for the minimax alpha-beta agent that takes the 
	 * number of the player as well as the number of plys to search
	 * until as a string.
	 * 
	 * @param playernum - the number of the current player
	 * @param args - the number of plys to search until
	 */
	public MinimaxAlphaBeta(int playernum, String[] args) {
		super(playernum);

		if (args.length < 1) {
			System.err.println("You must specify the number of plys");
			System.exit(1);
		}

		numPlys = Integer.parseInt(args[0]);
	}

	/**
	 * Initial step of the game search.
	 * 
	 * @param newstate - the latest state of the game board
	 * @param statehistory - the history of the game board states
	 */
	@Override
	public Map<Integer, Action> initialStep(State.StateView newstate,
			History.HistoryView statehistory) {
		return middleStep(newstate, statehistory);
	}

	/**
	 * The middle step of the game search. This method will find
	 * the best child of the current game board state to execute.
	 * 
	 * @param newstate - the latest state of the game board
	 * @param statehistory - the history of the game board states
	 */
	@Override
	public Map<Integer, Action> middleStep(State.StateView newstate,
			History.HistoryView statehistory) {
		//the best child of the minimax search
		GameStateChild bestChild = null;
		
		/**
		 * Begin minimax alpha-beta search at depth 0 as max player.
		 * 
		 * Initially alpha is a game state child of minimum value and
		 * beta is a game state child of maximum value.
		 */
		bestChild = alphaBetaSearch(new GameStateChild(newstate), 0, true,
				new GameStateChild(new HashMap<Integer, Action>(),
						new GameState(Integer.MIN_VALUE)), new GameStateChild(
						new HashMap<Integer, Action>(), new GameState(
								Integer.MAX_VALUE)));

		return bestChild.action;
	}

	@Override
	public void terminalStep(State.StateView newstate,
			History.HistoryView statehistory) {}

	@Override
	public void savePlayerData(OutputStream os) {}

	@Override
	public void loadPlayerData(InputStream is) {}

	/**
	 * The minimax, depth-limited, alpha-beta pruning search algorithm. 
	 * This search algorithm will seek the best move for a player based
	 * on whether they are trying to maximize the utility of their state
	 * or minimize the utility of their state. Thus, the utility function of 
	 * each game state is extremely important in determining which actions 
	 * the current player will take with their team on the game map.
	 * 
	 * Alpha-beta pruning is used to minimize the number of game states that are explored
	 * by determining if each state is less valuable than others that were previously
	 * explored by disregarding any game states that would lead to worse actions than
	 * those previously examined game states.
	 * 
	 * The search will return a best game state child result when the number of plys
	 * of this minimax search agent is reached according to the depth that is tracked
	 * throughout the recursive calls of the algorithm to itself. 
	 *
	 * @param node
	 *            The action and state to search from
	 * @param depth
	 *            The remaining number of plies under this node
	 * @param isMax
	 *            if the search is on the max node
	 * @param alpha
	 *            The current best node for the maximizing node from this node
	 *            to the root
	 * @param beta
	 *            The current best node for the minimizing node from this node
	 *            to the root
	 * @return The best child of this node with updated values
	 */
	public GameStateChild alphaBetaSearch(GameStateChild node, int depth,
			boolean isMax, GameStateChild alpha, GameStateChild beta) {
		//track the current depth of this node's game state
		node.state.setDepth(depth);
		
		//Return the current game state child when it is 
		//a terminal or the max depth has been reached
		if (depth == numPlys || node.state.isTerminal()) {
			return node;
		}
		
		//Set whether this node is a max node or min node
		if (isMax) {
			node.state.setIsMax(true);
		} else {
			node.state.setIsMax(false);
		}
		
		//Get the children of this node and order them based on best to worst heuristic
		//values.
		List<GameStateChild> children = orderChildrenWithHeuristics(node.state
				.getChildren());

		//Tun the alpha-beta pruning search on the children of this node,
		//disregarding the children worse than previously explored children.
		for (GameStateChild child : children) {
			
			//Run search on this child of opposite team and evaluate the utility
			int v = alphaBetaSearch(child, depth + 1, !isMax, alpha, beta).state
					.getUtility();
			
			//Use the current child if it is the max node
			if (isMax && v > alpha.state.getUtility()) {
				alpha = child;
			} else if (!isMax && v < beta.state.getUtility()) {
				//Use the current child if it is the min node
				beta = child;
			}
			
			//Return the current child if alpha's utility has overcome beta's utility
			if (alpha.state.getUtility() >= beta.state.getUtility()) {
				return child;
			}
		}

		//Return alpha child when max player
		if (isMax) {
			return alpha;
		}
		
		//Return beta child when min player
		return beta;
	}

	/**
	 * This method orders the given list of game state children based on their
	 * game state's heuristic values. The game state children are sorted from
	 * best heuristic to worst heuristic.
	 * 
	 * Each game state has an estimated utility value that is used as the heuristic.
	 * The estimated utility is a weighted linear function designed with a heavy
	 * positive weight on whether the footmen are alive and a heavy negative weight
	 * on whether the archers are alive.
	 * 
	 * The heuristic is based on the following properties:
	 * 
	 * Current health of combined footmen
	 * Current health of combined archers
	 * Minimum distance of footmen from specifically targeted archers
	 * Current number of footmen alive
	 * Current number of archers alive
	 * 
	 * The minimum distance from footmen to targeted archers when there are
	 * no obstacles on the map is the euclidean distance between footman and archer.
	 *  
	 * The minimum distance from footmen to targeted archers when there are
	 * obstacles on the map is the size of the a-star path length between footman and archer.
	 * 
	 * This function is used in the alphaBetaSearch method to order the children of the current
	 * game state based on their heuristics in order to expand the best-valued children first.
	 * 
	 * We chose to use these properties as heuristics because the survival of the footmen is vital to
	 * the game. If we weight the survival of archers as negative and the survival of footmen as positive,
	 * then the max node will play to kill the archers and this is the behavior we desire. We also know
	 * that the archers have a greater range than the footmen, and thus they can attack footmen from far away.
	 * Hence, we weight the distance of footmen from archers negatively so that the footmen will move toward
	 * the archers and try to attack and kill them. Lastly, we aim to take actions that will dwindle the number
	 * of archers alive, hence max nodes will take actions that aim to kill archers and the min nodes will aim
	 * to minimize their own death (the utility of the max).
	 *
	 * @param children - a list of game state children to sort based on heuristic value
	 * @return The list of children sorted by their heuristic value
	 */
	public List<GameStateChild> orderChildrenWithHeuristics(
			List<GameStateChild> children) {
		Map<GameState, Map<Integer, Action>> stateChildMap = new HashMap<>();
		List<GameState> stateList = new ArrayList<>();
		List<GameStateChild> childList = new ArrayList<>();
		
		//Map all game state children to their action list
		for (GameStateChild child : children) {
			stateChildMap.put(child.state, child.action);
			stateList.add(child.state);
		}
		
		//Sort game states based on their heuristic values
		Collections.sort(stateList);
		
		//Make new game state children of the sorted game states and their actions.
		//Then add these to the fresly sorted game state child list to return.
		for (GameState state : stateList) {
			childList.add(new GameStateChild(stateChildMap.get(state), state));
		}
		
		return childList;
	}
}
