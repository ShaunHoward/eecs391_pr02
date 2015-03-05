package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.action.DirectedAction;
import edu.cwru.sepia.action.TargetedAction;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.StateCreator;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.Direction;
import edu.cwru.sepia.util.DistanceMetrics;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * This class stores all of the information the agent needs to know about the
 * state of the game. For example this might include things like footmen HP and
 * s.
 *
 * Add any information or methods you would like to this class, but do not
 * delete or change the signatures of the provided methods.
 */
public class GameState implements Comparable<GameState> {
	
	//Constants necessary for weighted utility function
	private static final int W_FOOTMAN_HP = 1;
	private static final int W_FOOTMAN_DISTANCE = -1;
	private static final int W_ARCHER_HP = -10;
	private static final int W_FOOTMAN_ALIVE = 10;
	private static final int W_ARCHER_ALIVE = -100;
	private static final int FOOTMAN_RANGE = 1;
	private static final int ARCHER_RANGE = 8;

	//Game state variables including units, depth, utility, map
	//properties and valid directions to move in.
	public int xExtent = 0, yExtent = 0;
	public List<GameUnit> footmen, archers;
	private int footmanNum = 0;
	private int archerNum = 1;
	private int depth = 0;
	private int utility;
	private boolean isMax = true;
	private List<Direction> validDirections;
	private List<ResourceView> obstacles;

	/**
	 * You will implement this constructor. It will extract all of the needed
	 * state information from the built in SEPIA state view.
	 *
	 * You may find the following state methods useful:
	 *
	 * state.getXExtent() and state.getYExtent(): get the map dimensions
	 * state.getAllResourceIDs(): returns all of the obstacles in the map
	 * state.getResourceNode(Integer resourceID): Return a ResourceView for the
	 * given ID
	 *
	 * For a given ResourceView you can query the using resource.getX() and
	 * resource.getY()
	 *
	 * For a given unit you will need to find the attack damage, range and max
	 * HP unitView.getTemplateView().getRange(): This gives you the attack range
	 * unitView.getTemplateView().getBasicAttack(): The amount of damage this
	 * unit deals unitView.getTemplateView().getBaseHealth(): The maximum amount
	 * of health of this unit
	 *
	 * @param state Current state of the episode
	 * @throws IOException 
	 */
	public GameState(State.StateView stateView){
		//Lists of the GameUnits that will be used to track the state

		footmen = new ArrayList<GameUnit>();
		archers = new ArrayList<GameUnit>();
		
		//Lists of the UnitView from which the GameUnits will be made
		List<Unit.UnitView> footmenUnitView = stateView.getUnits(footmanNum);
		List<Unit.UnitView> archersUnitView = stateView.getUnits(archerNum);
		
		//Create the footman GameUnits
		for (Unit.UnitView footman : footmenUnitView) {
			footmen.add(new GameUnit(footman));
		}
		
		//Create the archer GameUnits
		for (Unit.UnitView archer : archersUnitView) {
			archers.add(new GameUnit(archer));
		}
		
		//Determine map limits and valid move directions
		xExtent = stateView.getXExtent();
		yExtent = stateView.getYExtent();
		this.validDirections = createValidDirectionsList();
		this.obstacles = new ArrayList<>();
		for (ResourceView resource : stateView.getAllResourceNodes()){
			obstacles.add(resource);
		}
	}

	/**
	 * Creates a GameState with the assigned depth value
	 * @param state The state to created the GameState with
	 * @param depth The specified depth of the GameState
	 */
	public GameState(State.StateView state, int depth) {
		this(state);
		this.depth = depth;
	}

	/**
	 * Constructor used to initialize the A/B search
	 * 
	 * @param utility - the initial utility of this game state
	 */
	public GameState(Integer utility) {
		this.utility = utility;
		footmen = new ArrayList<GameUnit>();
		archers = new ArrayList<GameUnit>();
		this.validDirections = createValidDirectionsList();
		this.obstacles = new ArrayList<>();
	}
	
	/**
	 * Creates a child game state from a parent game state, same values but depth is one deeper
	 * @param parent The parent GameState
	 */
	public GameState(GameState parent){
		//Initializes all footmen and archers to the same as the parent
		this.footmen = new ArrayList<GameUnit>();
		for (GameUnit unit : parent.footmen) {
			this.footmen.add(new GameUnit(unit));
		}
		
		this.archers = new ArrayList<GameUnit>();
		for (GameUnit unit : parent.archers) {
			this.archers.add(new GameUnit(unit));
		}
		
		/**
		 * Sets map dimensions to same as parent, and depth to parent+1
		 */
		this.xExtent = parent.getXExtent();
		this.yExtent = parent.getYExtent();
		this.depth = parent.getDepth() + 1;
		
		//Finds the directions valid to move in
		this.validDirections = createValidDirectionsList();
		
		//Finds the obstacles on the map and recognizes them
		this.obstacles = new ArrayList<ResourceView>();
		for (ResourceView rView : parent.obstacles){
			this.obstacles.add(rView);
		}
	}

	
	/**
	 * Gets a list of all valid directions for movement in the state
	 * @return A list of valid movement directions
	 */
	public List<Direction> createValidDirectionsList(){

		List<Direction> validDirections = new ArrayList<>();
		for (Direction dir : Direction.values()) {
			if (isValidDirection(dir)) {
				validDirections.add(dir);
			}
		}
		return validDirections;
	}
	
	/**
	 * Checks if directions are valid, only allowed movement are N,E,S,W
	 * @param direction The direction whose validity is being checked
	 * @return True if the direction is valid, false otherwise
	 */
	private boolean isValidDirection(Direction direction) {
		return direction == Direction.NORTH || direction == Direction.EAST
				|| direction == Direction.WEST || direction == Direction.SOUTH;
	}

	public int getXExtent() {
		return xExtent;
	}

	public List<Direction> getValidDirections() {
		return this.validDirections;
	}

	public void setXExtent(int xExtent) {
		this.xExtent = xExtent;
	}

	public int getYExtent() {
		return yExtent;
	}

	public void setYExtent(int yExtent) {
		this.yExtent = yExtent;
	}

	public boolean isMax() {
		return this.isMax;
	}

	public void setIsMax(boolean isMax) {
		this.isMax = isMax;
	}

	public int getDepth() {
		return this.depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	/**
	 * Gets the total health of all footmen
	 * @return int Total health of all footmen
	 */
	public int getFootmenHealth() {
		int totalHealth = 0;
		for (GameUnit footman : footmen) {
			totalHealth += footman.getHP();
		}
		return totalHealth;
	}

	/**
	 * Gets the total health of all archers
	 * @return in Total health of all archers
	 */
	public int getArcherHealth() {
		int totalHealth = 0;
		for (GameUnit archer : archers) {
			totalHealth += archer.getHP();
		}
		return totalHealth;
	}

	/**
	 * Returns a list of all GameUnits in the game, bother archers and footmen
	 * @return List<GameUnit> All GameUnits in the state
	 */
	public List<GameUnit> getEntities() {
		List<GameUnit> entities = new ArrayList<>(footmen);
		entities.addAll(archers);
		return entities;
	}

	/**
	 * Returns whether or not a state is terminal
	 * @return True if the state is terminal, false otherwise
	 */
	//TODO: Needs to include the depth limit
	public boolean isTerminal() {
		return footmen.isEmpty() || archers.isEmpty();
	}

	/**
	 * Applies actions to the state
	 * @param actions The actions to be applied
	 */
	public void applyActions(Map<Integer, Action> actions) {
		Set<Integer> keySet = actions.keySet(); // Gets set of all keys
												// contained in the map
		Iterator<Integer> keySetItr = keySet.iterator();

		while (keySetItr.hasNext()) {
			Integer currentKey = keySetItr.next();
			Action currentAction = actions.get(currentKey);
			ActionType currentActionType = currentAction.getType();

			//Apply the attack action and deduct health from opponent
			if (currentActionType == ActionType.COMPOUNDATTACK) {
				TargetedAction currentTargetedAction = (TargetedAction) currentAction;
				int unitId = currentTargetedAction.getUnitId();
				int targetId = currentTargetedAction.getTargetId();

				GameUnit unit = getUnit(unitId);
				GameUnit target = getUnit(targetId);

				target.setHP(target.getHP() - unit.getDamage());
			} else if (currentActionType == ActionType.PRIMITIVEMOVE) {
				//Move the current unit in the desired direction
				DirectedAction currentDirectedAction = (DirectedAction) currentAction;
				int unitID = currentDirectedAction.getUnitId();
				GameUnit unit = getUnit(unitID);
				Direction moveDirection = currentDirectedAction.getDirection();

				unit.setX(unit.getX() + moveDirection.xComponent());
				unit.setY(unit.getY() + moveDirection.yComponent());
			}
		}
	}

	private GameUnit getUnit(int ID) {
		List<GameUnit> entities = this.getEntities();

		for (GameUnit entity : entities) {
			if (entity.getID() == ID) {
				return entity;
			}
		}

		return null;
	}

	/**
	 * You will implement this function.
	 *
	 * You should use weighted linear combination of features. The features may
	 * be primitives from the state (such as hp of a unit) or they may be higher
	 * level summaries of information from the state such as distance to a
	 * specific location. Come up with whatever features you think are useful
	 * and weight them appropriately.
	 *
	 * It is recommended that you start simple until you have your algorithm
	 * working. Then watch your agent play and try to add features that correct
	 * mistakes it makes. However, remember that your features should be as fast
	 * as possible to compute. If the features are slow then you will be able to
	 * do less plys in a turn.
	 *
	 * Add a good comment about what is in your utility and why you chose those
	 * features.
	 *
	 * @return The weighted linear combination of the features
	 */
	public int getUtility() {
		if (utility == 0) {
			
			int distance1FromArchers = minDistanceFromArcher(footmen.get(0), true);
			int distance2FromArchers = 0;
			if (footmen.size() >1){
				distance2FromArchers = minDistanceFromArcher(footmen.get(1), false);	
			}
//			for (GameUnit footman : footmen) {
//				distanceFromArchers += minDistanceFromArcher(footman);
//			}

			utility =(W_FOOTMAN_HP * getFootmenHealth())
					+ (W_ARCHER_HP * getArcherHealth())
					+ (W_FOOTMAN_DISTANCE * distance1FromArchers)
					+ (W_FOOTMAN_DISTANCE * distance2FromArchers)
					+ (W_FOOTMAN_ALIVE * footmen.size()) + (W_ARCHER_ALIVE * archers
					.size());
		}
		return utility;
	}

//	 public int getUtility(){
//	 if (utility == 0){
//		 utility = 0;
//		 GameUnit a = archers.get(0);
//		 GameUnit f1 = footmen.get(0);
//		 GameUnit f2 = footmen.get(1);
//		 int dx1 = Math.abs(a.getX() - f1.getX());
//		 int dy1 = Math.abs(a.getY() - f1.getY());
//		 int dx2 = Math.abs(a.getX() - f2.getX());
//		 int dy2 = Math.abs(a.getY() - f2.getY());
//		 utility -= dx1 * 10 + dy1 + dx2 + dy2 * 10;
//	 }
//	 return utility;
//	 }

	private int minDistanceFromArcher(GameUnit footman, boolean isFirst) {
		
		int xDiff = 0;
		int yDiff = 0;
	    double nextDist = 0;
		double minDist = Double.MAX_VALUE;
		GameUnit archer;
		if (isFirst){
			archer = archers.get(0);
			xDiff = footman.getX() - archer.getX();
			yDiff = footman.getY() - archer.getY();
			//Manhattan Distance implementation
			//nextDist = Math.abs(xDiff) + Math.abs(yDiff);
			//Euclidean Distance Implementation
			//minDist = Math.sqrt(Math.pow(Math.abs(xDiff),2)+Math.pow(Math.abs(yDiff), 2));
			//A star search method
			Stack<MapLocation> aStarPath = findPath(obstacles, footman, archer);
			if (aStarPath != null){
				minDist = aStarPath.size();
			} else {
				minDist = 50;
			}
		} else {
			if (archers.size()>1){
				archer = archers.get(1);
			} else {
				archer = archers.get(0);
			}
			xDiff = footman.getX() - archer.getX();
			yDiff = footman.getY() - archer.getY();
			//Manhattan Distance implementation
			//nextDist = Math.abs(xDiff) + Math.abs(yDiff);
			//Euclidean Distance Implementation
			//minDist = Math.sqrt(Math.pow(Math.abs(xDiff),2)+Math.pow(Math.abs(yDiff), 2));
			//A star search method
			Stack<MapLocation> aStarPath = findPath(obstacles, footman, archer);
			if (aStarPath != null){
				minDist = aStarPath.size();
			} else {
				minDist = 50;
			}
		}
//		// Find the closest distance between footman and archers
//		for (GameUnit archer : archers) {
//			xDiff = footman.getX() - archer.getX();
//			yDiff = footman.getY() - archer.getY();
//			//Manhattan Distance implementation
//			//nextDist = Math.abs(xDiff) + Math.abs(yDiff);
//			//Euclidean Distance Implementation
//			nextDist = Math.sqrt(Math.pow(Math.abs(xDiff),2)+Math.pow(Math.abs(yDiff), 2));
//			if (nextDist < minDist) {
//				minDist = nextDist;
//			}
//		}
		// When there are no archers, return 0, else return the min distance to
		// an archer
		return archers.isEmpty() ? 0 : (int)minDist;
	}

	/**
	 * You will implement this function.
	 *
	 * This will return a list of GameStateChild objects. You will generate all
	 * of the possible actions in a step and then determine the resulting game
	 * state from that action. These are your GameStateChildren.
	 *
	 * You may find it useful to iterate over all the different directions in
	 * SEPIA.
	 *
	 * for(Direction direction : Directions.values())
	 *
	 * To get the resulting from a move in that direction you can do the
	 * following x += direction.xComponent() y += direction.yComponent()
	 *
	 * @return All possible actions and their associated resulting game state
	 */
	public List<GameStateChild> getChildren() {

		List<Action> unitOneActions = new ArrayList<>();
		List<Action> unitTwoActions = new ArrayList<>();
		List<Map<Integer, Action>> action = new ArrayList<>();
		int unitOneID = 0;
		int unitTwoID = 0;
		boolean twoUnits = false;
		if (isMax) {
			unitOneID = footmen.get(0).getID();
			unitOneActions = getActions(footmen.get(0), archers);

			if (footmen.size() > 1) {
				unitTwoID = footmen.get(1).getID();
				unitTwoActions = getActions(footmen.get(1), archers);
				twoUnits = true;
			}
		} else {
			unitOneID = archers.get(0).getID();
			unitOneActions = getActions(archers.get(0), footmen);
			if (archers.size() > 1) {
				unitTwoID = archers.get(1).getID();
				unitTwoActions = getActions(archers.get(1), footmen);
				twoUnits = true;
			}
		}

		List<GameStateChild> children = new ArrayList<>();
		Map<Integer, Action> actionMap = new HashMap<>();

		// Make all the possible game states, eliminating those options where
		// player units move to the same location.
		if (twoUnits) {
			for (Action unitOneAction : unitOneActions) {
				for (Action unitTwoAction : unitTwoActions) {
					actionMap = new HashMap<>();
					actionMap.put(unitOneID, unitOneAction);
					actionMap.put(unitTwoID, unitTwoAction);
					if(!badActions(actionMap, unitOneID, unitTwoID)){
						// ** Need to apply actions to new state
						GameState newState = new GameState(this);
						newState.applyActions(actionMap);
						// ** Then add new state child to list of state children
						children.add(new GameStateChild(actionMap, newState));
					}
				}
			}
		} else {
			for (Action unitOneAction : unitOneActions) {
				actionMap = new HashMap<>();
				actionMap.put(unitOneID, unitOneAction);
				// ** Need to apply actions to new state
				GameState newState = new GameState(this);
				newState.applyActions(actionMap);
				// ** Then add new state child to list of state children
				children.add(new GameStateChild(actionMap, newState));
			}
		}
		return children;
	}

	private boolean badActions(Map<Integer, Action> actionMap, int unitOneID,
			int unitTwoID) {

		Action unitOneAction = actionMap.get(unitOneID);
		Action unitTwoAction = actionMap.get(unitTwoID);

		if (unitOneAction.getType() == ActionType.PRIMITIVEMOVE
				&& unitTwoAction.getType() == ActionType.PRIMITIVEMOVE
				&& moveToSameLocation(unitOneAction, unitOneID, unitTwoAction,
						unitTwoID)){
			return true;
		}
		for (ResourceView obstacle : obstacles){
			
			if (unitOneAction.getType() == ActionType.PRIMITIVEMOVE
				&& unitTwoAction.getType() == ActionType.PRIMITIVEMOVE){ 
				if (moveToSameLocation(obstacle, unitOneAction, unitOneID, unitTwoAction,
							unitTwoID)){
					return true;
				}	
			}
		}
		return false;
	}
	
	public boolean moveToSameLocation(ResourceView obstacle, Action moveActionOne, int unitIDOne,
			Action moveActionTwo, int unitIDTwo) {
		DirectedAction dActionOne = (DirectedAction) moveActionOne;
		DirectedAction dActionTwo = (DirectedAction) moveActionTwo;

		GameUnit unitOne = getUnit(unitIDOne);
		GameUnit unitTwo = getUnit(unitIDTwo);
		int xOne = unitOne.getX() + dActionOne.getDirection().xComponent();
		int yOne = unitOne.getY() + dActionOne.getDirection().yComponent();
		int xTwo = unitTwo.getX() + dActionTwo.getDirection().xComponent();
		int yTwo = unitTwo.getY() + dActionTwo.getDirection().yComponent();
		int xOb = obstacle.getXPosition();
		int yOb = obstacle.getYPosition();
		if ((xOne == xOb && yOne == yOb) || (xTwo == xOb && yTwo == yOb)){
			return true;
		}
		return false;
	}

	private List<Action> getActions(GameUnit player, List<GameUnit> enemies) {
		List<GameUnit> entities = getEntities();
		List<Action> actions = new ArrayList<>();

		int playerX = player.getX();
		int playerY = player.getY();

		// Add all possible moves to the action list for this player
		for (Direction direction : validDirections) {
			if (possibleMove(playerX + direction.xComponent(), playerY
					+ direction.yComponent(), entities)) {
				actions.add(Action.createPrimitiveMove(player.getID(),
						direction));
			}
		}

		// Add all possible attacks to the action list for this player
		for (GameUnit enemy : enemiesInRange(player)) {
			actions.add(Action.createCompoundAttack(player.getID(),
					enemy.getID()));
		}
		return actions;
	}

	/**
	 * Determines if both unit 1 and unit 2 are moving to the same location.
	 * These units are determined based on whether this game state is a max
	 * state.
	 * 
	 * @param moveActionOne
	 *            - the move action of unit 1
	 * @param unitIDOne
	 *            - the id of unit one
	 * @param moveActionTwo
	 *            - the move action of unit 2
	 * @param unitIDTwo
	 *            - the id of unit two
	 * @return whether both unit 1 and unit 2 are moving to the same location
	 */
	public boolean moveToSameLocation(Action moveActionOne, int unitIDOne,
			Action moveActionTwo, int unitIDTwo) {
		DirectedAction dActionOne = (DirectedAction) moveActionOne;
		DirectedAction dActionTwo = (DirectedAction) moveActionTwo;

		GameUnit unitOne = getUnit(unitIDOne);
		GameUnit unitTwo = getUnit(unitIDTwo);
		int xOne = unitOne.getX() + dActionOne.getDirection().xComponent();
		int yOne = unitOne.getY() + dActionOne.getDirection().yComponent();
		int xTwo = unitTwo.getX() + dActionTwo.getDirection().xComponent();
		int yTwo = unitTwo.getY() + dActionTwo.getDirection().yComponent();
		return (xOne == xTwo) && (yOne == yTwo);
	}

	/**
	 * Determines if the given x and y coordinates lead to a possible move that
	 * is not blocked by other entities on the game map.
	 * 
	 * @param x
	 *            - the x coordinate of the move
	 * @param y
	 *            - the y coordinate of the move
	 * @param entities
	 *            - the entities on the game map currently
	 * @return whether the given x and y coordinates lead to a possible move
	 *         that is not currently blocked by another entity
	 */
	public boolean possibleMove(int x, int y, List<GameUnit> entities) {
		boolean isPossible = true;

		// check if the location is on the map
		if (!(0 <= x && x < xExtent) || !(0 <= y && y < yExtent)) {
			isPossible = false;
		} else {
			// check if an entity is already at the desired move location
			ENTITY_LOOP: for (GameUnit entity : entities) {
				if (entity.getX() == x && entity.getY() == y) {
					isPossible = false;
					break ENTITY_LOOP;
				}
			}
		}
		return isPossible;
	}

	private List<GameUnit> enemiesInRange(GameUnit player) {
		int range = 0;
		int xDiff = 0;
		int yDiff = 0;
		List<GameUnit> enemies;
		List<GameUnit> enemiesInRange = new ArrayList<>();

		if (isMax) {
			enemies = archers;
			range = FOOTMAN_RANGE;
		} else {
			enemies = footmen;
			range = ARCHER_RANGE;
		}

		for (GameUnit enemy : enemies) {
			xDiff = Math.abs(player.getX() - enemy.getX());
			yDiff = Math.abs(player.getY() - enemy.getY());
			if (range >= (xDiff + yDiff)) {
				enemiesInRange.add(enemy);
			}
		}
		return enemiesInRange;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof GameState) {
			GameState state = (GameState) o;
			return this.footmen.equals(state.footmen)
					&& this.archers.equals(state.archers)
					&& this.depth == state.depth
					&& this.getUtility() == state.getUtility()
					&& this.isMax == state.isMax;
		}
		return false;
	}

	@Override
	public int compareTo(GameState state) {
		// Compare utilities of states to order them
		return new Integer(this.getUtility()).compareTo(state.getUtility());
	}

	public int hashcode() {
		int hashcode = footmen.size() + archers.size();
		hashcode *= 31;
		hashcode += depth * 31;
		hashcode += getUtility() * 31;
		if (isMax) {
			hashcode += 3 * (53 + 31);
		}
		return hashcode;
	}
	
    /**
     * Finds an A* path from the footman start position to the town hall position if one exists.
     * If a path does not exist, null will be returned. The enemy footman location and resource locations
     * are noted to make sure the agent navigates around them since they are considered unreachable locations.
     *
     * @param state - the state of the map
     * @return the maplocations of an A* path to navigate the agent to the town hall around all resources and enemy
     */
    private Stack<MapLocation> findPath(List<ResourceView> obstacles, GameUnit player, GameUnit enemy) {

        MapLocation startLoc = new MapLocation(player.getX(), player.getY(), null, 0);

        MapLocation goalLoc = new MapLocation(enemy.getX(), enemy.getY(), null, 0);

        // get resource locations
        Set<MapLocation> resourceLocations = new HashSet<MapLocation>();
        for (ResourceView resource : obstacles) {
            resourceLocations.add(new MapLocation(resource.getXPosition(), resource.getYPosition(), null, 0));
        }

        return AstarSearch(startLoc, goalLoc, xExtent, yExtent, null, resourceLocations);
    }

    /**
     * Finds the A* path through a map via the given map locations. The algorithm will avoid designing a path through
     * the enemy and resource locations but will find an optimal path to a location adjacent to the townhall location (goal)
     * from the footman starting location.
     * <p/>
     * It returns a Stack of locations with the top of the stack being the first space to move to
     * and the bottom of the stack being the last space to move to. If there is no path to the townhall
     * then null is returned from the method and the agent will print a message via middlestep() and exit with exit code 0.
     * <p/>
     * An example map is the following:
     * <p/>
     * F - - - -
     * x x x - x
     * H - - - -
     * <p/>
     * F is the footman
     * H is the townhall
     * x's are occupied spaces
     * <p/>
     * xExtent would be 5 for this map with valid X coordinates in the range of [0, 4]
     * x=0 is the left most column and x=4 is the right most column
     * <p/>
     * yExtent would be 3 for this map with valid Y coordinates in the range of [0, 2]
     * y=0 is the top most row and y=2 is the bottom most row
     * <p/>
     * resourceLocations would be {(0,1), (1,1), (2,1), (4,1)}
     * <p/>
     * The path would be
     * <p/>
     * (1,0)
     * (2,0)
     * (3,1)
     * (2,2)
     * (1,2)
     * <p/>
     * Notice how the initial footman position and the townhall position are not included in the path stack
     *
     * @param start             - Starting position of the footman
     * @param goal              - MapLocation of the townhall
     * @param xExtent           - Width of the map
     * @param yExtent           - Length of the map
     * @param resourceLocations - Set of positions occupied by resources
     * @return Stack of positions with top of stack being first move in plan
     */
    private Stack<MapLocation> AstarSearch(MapLocation start, MapLocation goal, int xExtent, int yExtent, MapLocation enemyFootmanLoc, Set<MapLocation> resourceLocations) {
        //initialization of map locations for the search
        MapLocation cheapestLocation;
        Set<MapLocation> possibleLocations;
        Set<MapLocation> expandedLocations = new HashSet<>();
        PriorityQueue<MapLocation> openLocations = new PriorityQueue<>();

        //initialization of agent's current map
        agentMap = new AgentMap(xExtent, yExtent, start, goal, resourceLocations);
        agentMap.setEnemyLocation(enemyFootmanLoc);
        agentMap.setEnd(goal);
        initializeSearch(agentMap, openLocations);

        //run A* to find optimal path through map
        while (!openLocations.isEmpty()) {
            cheapestLocation = openLocations.poll();
            if (cheapestLocation.equals(goal)) {
                return AstarPath(cheapestLocation);
            }
            expandedLocations.add(cheapestLocation);
            possibleLocations = cheapestLocation.getReachableNeighbors(agentMap);

            for (MapLocation location : possibleLocations) {
                if (!expandedLocations.contains(location) &&
                        !openLocations.contains(location)) {
                    location.setDistanceFromBeginning(cheapestLocation.getDistanceFromBeginning() + 1);
                    location.setCost(location.getDistanceFromBeginning() + distanceBetweenLocations(location, goal));
                    openLocations.add(location);
                }
            }
        }

        System.err.println("No available path.");
        return null;
    }

    /**
     * Initializes the A* search algorithm by getting the beginning location of the map
     * and initializing its values. It is added to the open queue so it can still be included
     * in the path.
     *
     * @param map               - the map to begin the search on
     * @param openLocations     - the priority queue of new locations
     */
    public void initializeSearch(AgentMap map, PriorityQueue<MapLocation> openLocations) {
        if (map.getBegin() != null) {
            map.getBegin().setDistanceFromBeginning(0);
            map.getBegin().setCost(map.getBegin().getDistanceFromBeginning() + distanceBetweenLocations(map.getBegin(), map.getEnd()));
            openLocations.add(map.getBegin());
        }
    }

    /**
     * Returns the Chebyshev distance between the given beginning and end map locations.
     * This is the heuristic used for A* search.
     *
     * @return the distance Chebyshev distance between beginning and end map locations
     */
    private float distanceBetweenLocations(MapLocation beginning, MapLocation end) {
        if (beginning != null && end != null) {
            return DistanceMetrics.chebyshevDistance(beginning.x, beginning.y, end.x, end.y);
        	//return (float) Math.sqrt(Math.pow(Math.abs(beginning.x - end.x),2)+Math.pow(Math.abs(beginning.y - end.y), 2));
        }
        return Float.MAX_VALUE;
    }

    /**
     * Returns the A* path to the given end location
     * from the beginning location of the map.
     *
     * @param end - the location to get the A* path to
     *            from the beginning location
     * @return the stack of locations from the beginning of the
     * map (top of stack) to the end of the map (bottom of stack)
     */
    public static Stack<MapLocation> AstarPath(MapLocation end) {
        Stack<MapLocation> astarPath = new Stack<>();
        MapLocation curr = null;

        //Do not add goal to path
        if (end != null) {
            curr = end.getPrevious();
        }

        //Build path from end to beginning but disregard starting node
        while (curr != null && curr.getPrevious() != null) {
            astarPath.push(curr);
            curr = curr.getPrevious();
        }

        return astarPath;
    }
    
    /* The agent map for this A* agent. It is used for each A* search. */
    AgentMap agentMap;

    /**
     * A map location class for each location on the current map of the A* agent.
     * Each map location represents a node in the A* search algorithm.
     * Each map location has an x and y coordinate, a previous map location,
     * a cost, distance from beginning, and the ability to find neighbor map locations.
     * The heuristic is not tracked to maximize efficiency.
     * </p>
     * It implements the comparable interface so that it can be compared
     * within a priority queue in order to achieve fast performance in comparison
     * with other nodes during A* search. The map locations are compared by
     * cost with compareTo() and are considered equivalent if they have the same
     * x and y coordinates.
     */
    class MapLocation implements Comparable<MapLocation> {

        /* The x and y coordinates of this location. */
        int x = 0, y = 0;

        /* The previously visited map location. */
        MapLocation previous = null;

        /* The cost to reach this location. */
        float cost = 0;

        /* The distance of this location from the initial location.*/
        float distanceFromBeginning = 0;

        /**
         * Constructor for a map location based on x,y coordinates,
         * a previously visited map location, and the cost to reach
         * this location.
         *
         * @param x        - the x coordinate on the map of this location
         * @param y        - the y coordinate on the map of this location
         * @param previous - the previously visited location
         * @param cost     - - the cost to reach this location
         */
        public MapLocation(int x, int y, MapLocation previous, float cost) {
            this.x = x;
            this.y = y;
            this.previous = previous;
            this.cost = cost;
        }

        /**
         * Gets the previously visited location.
         *
         * @return the previously visited location
         */
        public MapLocation getPrevious() {
            return previous;
        }

        /**
         * Sets the distance from the beginning location.
         *
         * @param distanceFromBeginning - the distance from the beginning location
         */
        public void setDistanceFromBeginning(float distanceFromBeginning) {
            this.distanceFromBeginning = distanceFromBeginning;
        }

        /**
         * Gets the distance from the beginning location.
         *
         * @return the distance from the beginning location
         */
        public float getDistanceFromBeginning() {
            return distanceFromBeginning;
        }

        /**
         * Gets a set of neighbor locations that are currently reachable from this
         * current location, i.e. they cannot be enemy or resource locations.
         *
         * @return reachable neighbor locations of this location
         */
        public Set<MapLocation> getReachableNeighbors(AgentMap map) {
            //Get neighbors and initialize iterator for neighbors
            Set<MapLocation> locations = getNeighbors(map);
            Iterator<MapLocation> locationItr = locations.iterator();
            MapLocation curr = null;

            //Remove any neighbors not reachable from this location.
            while (locationItr.hasNext()) {
                curr = locationItr.next();
                if (map.getResourceLocations().contains(curr)) {
                    locationItr.remove();
                }
            }
            return locations;
        }

        /**
         * Gets the neighbors map locations of this map location as long as they
         * exist on the given agent map.
         *
         * @param map - the agent map to search for neighbor locations of this location in
         * @return the set of map locations neighboring this map location
         */
        public Set<MapLocation> getNeighbors(AgentMap map) {
            Set<MapLocation> neighbors = new HashSet<>();

            //Add neighbors of 8 directions to set of neighbors
            neighbors.add(getNorthNeighbor(map));
            neighbors.add(getSouthNeighbor(map));
            neighbors.add(getEastNeighbor(map));
            neighbors.add(getWestNeighbor(map));
            //neighbors.add(getNorthEastNeighbor(map));
            //neighbors.add(getNorthWestNeighbor(map));
            //neighbors.add(getSouthEastNeighbor(map));
            //neighbors.add(getSouthWestNeighbor(map));

            //Remove null if any neighbors were not found
            if (neighbors.contains(null)) {
                neighbors.remove(null);
            }
            return neighbors;
        }

        /**
         * Returns the map location southwest of this map location
         * or null if the location is off of the map.
         *
         * @param map - the map to find the southwest neighbor in
         * @return the southwest neighbor of this location
         */
        private MapLocation getSouthWestNeighbor(AgentMap map) {
            //x - 1 & y + 1
            int neighborX = this.x - 1;
            int neighborY = this.y + 1;
            return neighborWithinBounds(map, neighborX, neighborY);
        }

        /**
         * Returns the map location southeast of this map location
         * or null if the location is off of the map.
         *
         * @param map - the map to find the southeast neighbor in
         * @return the southeast neighbor of this location
         */
        private MapLocation getSouthEastNeighbor(AgentMap map) {
            //x + 1 & y + 1
            int neighborX = this.x + 1;
            int neighborY = this.y + 1;
            return neighborWithinBounds(map, neighborX, neighborY);
        }

        /**
         * Returns the map location northwest of this map location
         * or null if the location is off of the map.
         *
         * @param map - the map to find the northwest neighbor in
         * @return the northwest neighbor of this location
         */
        private MapLocation getNorthWestNeighbor(AgentMap map) {
            //x - 1 & y - 1
            int neighborX = this.x - 1;
            int neighborY = this.y - 1;
            return neighborWithinBounds(map, neighborX, neighborY);
        }

        /**
         * Returns the map location northeast of this map location
         * or null if the location is off of the map.
         *
         * @param map - the map to find the northeast neighbor in
         * @return the northeast neighbor of this location
         */
        private MapLocation getNorthEastNeighbor(AgentMap map) {
            //x + 1 & y - 1
            int neighborX = this.x + 1;
            int neighborY = this.y - 1;
            return neighborWithinBounds(map, neighborX, neighborY);
        }

        /**
         * Returns the map location west of this map location
         * or null if the location is off of the map.
         *
         * @param map - the map to find the west neighbor in
         * @return the west neighbor of this location
         */
        private MapLocation getWestNeighbor(AgentMap map) {
            //x - 1 & y
            int neighborX = this.x - 1;
            return neighborWithinBound(map, neighborX, this.y, true);
        }

        /**
         * Returns the map location east of this map location
         * or null if the location is off of the map.
         *
         * @param map - the map to find the east neighbor in
         * @return the east neighbor of this location
         */
        private MapLocation getEastNeighbor(AgentMap map) {
            //x + 1 & y
            int neighborX = this.x + 1;
            return neighborWithinBound(map, neighborX, this.y, true);
        }

        /**
         * Returns the map location south of this map location
         * or null if the location is off of the map.
         *
         * @param map - the map to find the south neighbor in
         * @return the south neighbor of this location
         */
        private MapLocation getSouthNeighbor(AgentMap map) {
            //x & y + 1
            int neighborY = this.y + 1;
            return neighborWithinBound(map, this.x, neighborY, false);
        }

        /**
         * Returns the map location north of this map location
         * or null if the location is off of the map.
         *
         * @param map - the map to find the north neighbor in
         * @return the north neighbor of this location
         */
        private MapLocation getNorthNeighbor(AgentMap map) {
            //x & y - 1
            int neighborY = this.y - 1;
            return neighborWithinBound(map, this.x, neighborY, false);
        }

        /**
         * Determines if the provided coordinates are within the x and y bounds of the map.
         * When the given coordinates are within the agent map, a new neighbor map location
         * will be generated from the provided coordinates. Otherwise, null is returned because
         * the neighbor is not within the bounds of the map.
         *
         * @param map       - the map to check the bounds of
         * @param neighborX - the x coordinate of the new neighbor
         * @param neighborY - the y coordinate of the new neighbor
         * @return a new neighbor within the bounds of the map or null if the coordinates are out
         * of the map bounds
         */
        private MapLocation neighborWithinBounds(AgentMap map, int neighborX, int neighborY) {
            //Check if the neighbor is within the x extent of the map.
            if (map.getXExtent() > neighborX && neighborX >= 0) {
                //Check if the neighbor is within the y extent of the map
                if (map.getYExtent() > neighborY && neighborY >= 0) {
                    return new MapLocation(neighborX, neighborY, this, 0);
                }
            }
            return null;
        }

        /**
         * Determines if the provided coordinates are within either the x or y bounds of the map
         * depending on the boolean provided for checking x extent.
         * When the designated coordinate is within the agent map, a new neighbor map location
         * will be generated from the given coordinates. Otherwise, null is returned because the neighbor is not within
         * the bounds of the map.
         *
         * @param map       - the map to check the bounds of
         * @param neighborX - the x coordinate of the new neighbor
         * @param neighborY - the y coordinate of the new neighbor
         * @return a new neighbor within the bounds of the map or null if the coordinates are out
         * of the map bounds
         */
        private MapLocation neighborWithinBound(AgentMap map, int neighborX, int neighborY, boolean checkX) {
            MapLocation neighbor = null;

            //Check if the neighbor is within the x extent of the map.
            if (checkX && map.getXExtent() > neighborX && neighborX >= 0) {
                neighbor = new MapLocation(neighborX, this.y, this, 0);
            } else if (map.getYExtent() > neighborY && neighborY >= 0) {
                //Check if the neighbor is within the y extent of the map.
                neighbor = new MapLocation(this.x, neighborY, this, 0);
            }
            return neighbor;
        }

        /**
         * Sets the cost of this location.
         *
         * @param cost - the cost of this location
         */
        public void setCost(float cost) {
            this.cost = cost;
        }

        /**
         * Gets the cost of this location.
         *
         * @return the cost of this location
         */
        public float getCost() {
            return cost;
        }

        /**
         * Compares locations by their cost. Utilizes
         * the Java compareTo(Object) for Floats.
         *
         * @param location - the location to compare to this location
         * @return the comparison of the locations by cost
         */
        @Override
        final public int compareTo(MapLocation location) {
            Float thisCost = new Float(this.getCost());
            Float locationCost = new Float(location.getCost());
            return thisCost.compareTo(locationCost);
        }

        /**
         * Gets the coordinates of this location as a string.
         *
         * @return - the coordinates <x,y> of this location as a string
         */
        public String getCoordinateString() {
            return "<" + this.x + ", " + this.y + ">";
        }

        /**
         * Determines whether two locations are the same based on their costs,
         * x, and y coordinates.
         *
         * @param location - the location to compare with this location
         * @return whether the the locations are the same
         */
        public boolean sameLocation(MapLocation location) {
            if (this.x == location.x && this.y == location.y) {
                return true;
            }
            return false;
        }

        /**
         * Determines whether two locations are equal based on their costs,
         * x, and y coordinates.
         *
         * @param o - the object to compare with this location
         * @return whether the input object is equivalent to this location
         */
        @Override
        public boolean equals(Object o) {
            if (o != null && o instanceof MapLocation) {
                MapLocation l = (MapLocation) o;
                if (sameLocation(l)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Hash code for this location. Generated based on
         * x, y coordinates and cost of this location.
         *
         * @return the hash code of this location
         */
        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            return 53 * result;
        }

    }

    /**
     * A map for the current AstarAgent. This helps describe the environment of the current
     * map to the agent for search. This class holds the start and goal locations desired for
     * the agent as well as the enemy location(s) and the resource locations so the
     * agent can navigate around them during search. Any properties of the map necessary
     * to navigate are stored in the AgentMap, i.e. x and y extents.
     */
    public class AgentMap {

        /* the length of the map by rows. */
        int xExtent = 0;

        /* the width of the map by columns. */
        int yExtent = 0;

        /* the beginning location of the map. */
        MapLocation begin = null;

        /* the ending location of the map. */
        MapLocation end = null;

        /* the location of the enemy footman on this map. */
        MapLocation enemyFootmanLoc;

        /* the locations of resources on this map. */
        Set<MapLocation> resourceLocations;

        public AgentMap(int xExtent, int yExtent, MapLocation agentStart, MapLocation agentStop, Set<MapLocation> resourceLocations) {
            this.xExtent = xExtent;
            this.yExtent = yExtent;
            this.begin = agentStart;
            this.end = agentStop;
            this.resourceLocations = resourceLocations;
        }

        /**
         * Gets the beginning location of this map.
         *
         * @return the beginning location of this map
         */
        public MapLocation getBegin() {
            return begin;
        }

        /**
         * Gets the x extent of the map.
         *
         * @return the x extent of the map
         */
        public int getXExtent() {
            return this.xExtent;
        }

        /**
         * Gets the y extent of the map.
         *
         * @return the y extent of the map
         */
        public int getYExtent() {
            return this.yExtent;
        }

        /**
         * Sets the enemy footman location on the map.
         *
         * @param enemyFootmanLoc - the enemy footman location on the map
         */
        public void setEnemyLocation(MapLocation enemyFootmanLoc) {
            this.enemyFootmanLoc = enemyFootmanLoc;
        }

        /**
         * Sets the enemy footman location on the map.
         *
         * @return the enemy footman location on the map
         */
        public MapLocation getEnemyLocation() {
            return this.enemyFootmanLoc;
        }

        /**
         * Sets the end location of the current map.
         *
         * @param end - the end of the current map for the current agent
         */
        public void setEnd(MapLocation end) {
            this.end = end;
        }

        /**
         * Sets the end location of the current map.
         *
         * @return the end of the current map for the current agent
         */
        public MapLocation getEnd() {
            return this.end;
        }

        /**
         * Gets the resource locations of the current map.
         *
         * @return the resource locations of the current map for the current agent
         */
        public Set<MapLocation> getResourceLocations() {
            return resourceLocations;
        }
    }

    //The current A* path through the map/maze.
    Stack<MapLocation> path;

    //Serial IDs for game components
    int footmanID, townhallID, enemyFootmanID;

    //The next location of the footman to travel along the path through the map
    MapLocation nextLoc;

}
