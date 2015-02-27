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
	private int depth = 0;
	
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
    
    public GameState(State.StateView state, int depth){
    	this(state);
    	this.depth = depth;
    }
    
    public int getDepth(){
    	return this.depth;
    }
    
    public void setDepth(int depth){
    	this.depth = depth;
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
    
    public List<UnitView> getEntities(){
    	List<UnitView> entities = new ArrayList<>(footmen);
    	entities.addAll(archers);
    	return entities;
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
    	
    	 List<GameStateChild> states = new ArrayList<>();
    	 Map<UnitView, List<Action>> possibleActions = new HashMap<>();
    	 List<Map<Integer, Action>> possibleStates = new ArrayList<>();
    	 List<UnitView> entities = getEntities();
    	 
    	 //Add all possible actions to the action list for each footman
    	 for (UnitView footman : footmen) {
    		 possibleActions.put(footman, new ArrayList<Action>());
    		 
	    	 int footmanX = footman.getXPosition();
	    	 int footmanY = footman.getYPosition();
    		 
	    	 //Add all possible moves to the action list for this footman
        	 for (Direction direction : Direction.values()){
        		 if (possibleMove(footmanX + direction.xComponent(), footmanY + direction.yComponent(), entities)){
        			 possibleActions.get(footman).add(Action.createPrimitiveMove(footman.getID(), direction));
        		 }
        	 }
        	 
        	 //Add all possible attacks to the action list for this footman
        	 for (UnitView archer : adjacentArchers(footmanX, footmanY)){
        		 possibleActions.get(footman).add(Action.createPrimitiveAttack(footman.getID(), archer.getID()));
        	 }
    	 }
    	 
    	 Map<Integer, Action> currStateActions = new HashMap<>();
    	 
    	 //Make all the possible game states, eliminating those options where
    	 //footmen move to the same location.
    	 for (Action footmanOneAction : possibleActions.get(footmen.get(0))){
    		 if (footmen.size() > 1){
	    		 for (Action footmanTwoAction : possibleActions.get(footmen.get(1))){
	    			 //Create a new game state when footmen are not moving to the same
	    			 //location or when footmen have different types of actions.
		    		 if (footmanOneAction.getType() == ActionType.PRIMITIVEMOVE 
		    		 && footmanTwoAction.getType() == ActionType.PRIMITIVEMOVE
		    		 && !moveToSameLocation(footmanOneAction, footmanTwoAction)
		    		 || (footmanOneAction.getType() != ActionType.PRIMITIVEMOVE 
		    		 || footmanTwoAction.getType() != ActionType.PRIMITIVEMOVE)){
		    			 currStateActions.put(footmen.get(0).getID(), footmanOneAction);
		    			 currStateActions.put(footmen.get(1).getID(), footmanTwoAction);
		    			 states.add(new GameStateChild(currStateActions, )))
		    		 }
	    	 	 }
    		 } else {
    			 currStateActions.put(footmen.get(0).getID(), footmanOneAction);
    		 }
    	 }

    	 return states;

//    	for (Direction direction : Directions.values()){
//    		
//    	}
//        return null;
    }
    
    /**
     * Determines if both footman 1 and footman 2 are moving to the same location.
     * 
     * @param moveActionOne - the move action of footman 1
     * @param moveActionTwo - the move action of footman 2
     * @return whether both footman 1 and footman 2 are moving to the same location
     */
    public boolean moveToSameLocation(Action moveActionOne, Action moveActionTwo){
    	DirectedAction dActionOne = (DirectedAction)moveActionOne;
    	DirectedAction dActionTwo = (DirectedAction)moveActionTwo;
    	int xOne = footmen.get(0).getXPosition() + dActionOne.getDirection().xComponent();
    	int yOne = footmen.get(0).getYPosition() + dActionOne.getDirection().yComponent();
    	int xTwo = footmen.get(1).getXPosition() + dActionTwo.getDirection().xComponent();
    	int yTwo = footmen.get(1).getYPosition() + dActionTwo.getDirection().yComponent();
    	return (xOne == xTwo) && (yOne == yTwo);
    }
    
    /**
     * Determines if the given x and y coordinates lead to a possible move that
     * is not blocked by other entities on the game map.
     * 
     * @param x - the x coordinate of the move
     * @param y - the y coordinate of the move
     * @param entities - the entities on the game map currently
     * @return whether the given x and y coordinates lead to a possible move that
     * is not currently blocked by another entity
     */
    public boolean possibleMove(int x, int y, List<UnitView> entities){
    	boolean isPossible = true;
    	
    	//check if the location is on the map
    	if (!(0 <= x && x < xExtent) || !(0 <= y && y < yExtent)){
    		isPossible = false;
    	} else {
	    	//check if an entity is already at the desired move location
    		ENTITY_LOOP:
	    	for (UnitView entity : entities){
	    		if (entity.getXPosition() == x && entity.getYPosition() == y){
	    			isPossible = false;
	    			break ENTITY_LOOP;
	    		}
	    	}
    	}
    	return isPossible;
    }
    
    /**
    * Returns all targets adjacent to the given coordinate.
    *
    * @param x The x coordinate
    * @param y The y coordinate
    * @return A list containing all adjacent targets
    */
    private List<UnitView> adjacentArchers(int x, int y) {
	    List<UnitView> targets = new ArrayList<>();
	    
	    //get north
	    //get south
	    //get east
	    //get west
	    
	    for (int i = x - 1; i <= x + 1; i++) {
		    for (int j = y - 1; j <= y + 1; j++) {
			    for (UnitView archer : archers) {
				    if (archer.getXPosition() == i && archer.getYPosition() == j) {
				    	targets.add(archer);
				    }
			    }
		    }
	    }
	    return targets;
    }
}
