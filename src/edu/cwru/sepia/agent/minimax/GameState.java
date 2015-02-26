package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.action.DirectedAction;
import edu.cwru.sepia.action.TargetedAction;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.Direction;

import java.util.*;

/**
 * This class stores all of the information the agent
 * needs to know about the state of the game. For example this
 * might include things like footmen HP and positions.
 *
 * Add any information or methods you would like to this class,
 * but do not delete or change the signatures of the provided methods.
 */
public class GameState {
	 private static final int W_FOOTMAN_HP = 1;
	 private static final int W_FOOTMAN_DISTANCE = -1;
	 private static final int W_ARCHER_HP = -10;
	 private static final int W_FOOTMAN_ALIVE = 10;
	 private static final int W_ARCHER_ALIVE = -100;

	public int xExtent, yExtent;
	public List<UnitView> footmen, archers;
	private int footmanNum = 0;
	private int archerNum = 1;
	
    /**
     * You will implement this constructor. It will
     * extract all of the needed state information from the built in
     * SEPIA state view.
     *
     * You may find the following state methods useful:
     *
     * state.getXExtent() and state.getYExtent(): get the map dimensions
     * state.getAllResourceIDs(): returns all of the obstacles in the map
     * state.getResourceNode(Integer resourceID): Return a ResourceView for the given ID
     *
     * For a given ResourceView you can query the position using
     * resource.getXPosition() and resource.getYPosition()
     *
     * For a given unit you will need to find the attack damage, range and max HP
     * unitView.getTemplateView().getRange(): This gives you the attack range
     * unitView.getTemplateView().getBasicAttack(): The amount of damage this unit deals
     * unitView.getTemplateView().getBaseHealth(): The maximum amount of health of this unit
     *
     * @param state Current state of the episode
     */
    public GameState(State.StateView state) {
    	xExtent = state.getXExtent();
    	yExtent = state.getYExtent();
    	this.footmen = state.getUnits(footmanNum);
    	this.archers = state.getUnits(archerNum);
    	
    }
    
    public int getFootmenHealth() {
    	int totalHealth = 0;
    	for (UnitView footman : footmen) {
    		totalHealth += footman.getHP();
    	}
    	return totalHealth;
    }
    
    public int getArcherHealth() {
    	int totalHealth = 0;
    	for (UnitView archer : archers) {
    		totalHealth += archer.getHP();
    	}
    	return totalHealth;
    }

    /**
     * You will implement this function.
     *
     * You should use weighted linear combination of features.
     * The features may be primitives from the state (such as hp of a unit)
     * or they may be higher level summaries of information from the state such
     * as distance to a specific location. Come up with whatever features you think
     * are useful and weight them appropriately.
     *
     * It is recommended that you start simple until you have your algorithm working. Then watch
     * your agent play and try to add features that correct mistakes it makes. However, remember that
     * your features should be as fast as possible to compute. If the features are slow then you will be
     * able to do less plys in a turn.
     *
     * Add a good comment about what is in your utility and why you chose those features.
     *
     * @return The weighted linear combination of the features
     */
    public double getUtility() {
		int distanceFromArchers = 0;
		for (UnitView footman : footmen) {
			distanceFromArchers += minDistanceFromArcher(footman);
		}
		
		return ((W_FOOTMAN_HP * getFootmenHealth())
		+ (W_ARCHER_HP * getArcherHealth())
		+ (W_FOOTMAN_DISTANCE * distanceFromArchers)
		+ (W_FOOTMAN_ALIVE * footmen.size())
		+ (W_ARCHER_ALIVE * archers.size()));
    }
    
    private int minDistanceFromArcher(UnitView footman) {
    	int minDist = Integer.MAX_VALUE;
    	int nextDist = 0;
    	//Find the closest distance between footman and archers
    	for (UnitView archer : archers) {
    		nextDist = Math.max(
    		    		Math.abs(footman.getXPosition() - archer.getXPosition()),
    		    		Math.abs(footman.getYPosition() - archer.getYPosition()));
    		if (nextDist < minDist){
    			minDist = nextDist;
    		}
    	}
    	//When there are no archers, return 0, else return the min distance to an archer
    	return archers.isEmpty() ? 0 : minDist;
    }

    /**
     * You will implement this function.
     *
     * This will return a list of GameStateChild objects. You will generate all of the possible
     * actions in a step and then determine the resulting game state from that action. These are your GameStateChildren.
     *
     * You may find it useful to iterate over all the different directions in SEPIA.
     *
     * for(Direction direction : Directions.values())
     *
     * To get the resulting position from a move in that direction you can do the following
     * x += direction.xComponent()
     * y += direction.yComponent()
     *
     * @return All possible actions and their associated resulting game state
     */
    public List<GameStateChild> getChildren() {
        return null;
    }
}
