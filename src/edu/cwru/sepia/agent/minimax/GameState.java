package edu.cwru.sepia.agent.minimax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.action.DirectedAction;
import edu.cwru.sepia.action.TargetedAction;
import edu.cwru.sepia.agent.minimax.AstarAgent.MapLocation;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.util.Direction;

/**
 * This class stores all of the information the agent needs to know about the
 * state of the game. Properties such as footmen health, archer health,
 * state utility, whether the state is a max state, and the list of obstacles
 * in this current state are stored in this class.
 * 
 * There are methods to generate the children of this game state based on 
 * what actions can be taken for the current state of the game. If there are
 * obstacles in the current game, the game state will produce directed actions
 * based on the use of a-star search. Once the search fails, all permutations
 * of directed actions are generated and evaluated for utility value.
 * When the given unit is in range of another unit, attacks will also be applied
 * to their actions list, and thus generated children are produced by directed
 * and attack actions.
 * 
 * A game state is comparable based on its estimated utility value.
 */
public class GameState implements Comparable<GameState> {

	// Constants necessary for weighted utility function
	private static final int W_FOOTMAN_HP = 1;
	private static final int W_FOOTMAN_DISTANCE = -1;
	private static final int W_ARCHER_HP = -10;
	private static final int W_FOOTMAN_ALIVE = 10;
	private static final int W_ARCHER_ALIVE = -100;
	
	//Ranges of game units
	private static final int FOOTMAN_RANGE = 1;
	private static final int ARCHER_RANGE = 8;

	// Game state variables including units, depth, utility, is max node, map
	// properties and valid directions to move in.
	public int xExtent = 0, yExtent = 0;
	public List<GameUnit> footmen, archers;
	private int footmanNum = 0;
	private int archerNum = 1;
	private int depth = 0;
	private int utility;
	private boolean isMax = true;
	private List<ResourceView> obstacles;
	private List<Direction> validDirections;
	
	//The a-star agent to use when path-finding is needed.
	private AstarAgent aStarAgent;

	/**
	 * A constructor that extracts all of the needed
	 * state information from the built-in SEPIA state view.
	 *
	 * @param state -
	 *            current state of the game
	 */
	public GameState(State.StateView stateView){
		//Lists of the GameUnits that will be used to track the state

		footmen = new ArrayList<GameUnit>();
		archers = new ArrayList<GameUnit>();

		// Lists of the UnitView from which the GameUnits will be made
		List<Unit.UnitView> footmenUnitView = stateView.getUnits(footmanNum);
		List<Unit.UnitView> archersUnitView = stateView.getUnits(archerNum);

		// Create the footman GameUnits
		for (Unit.UnitView footman : footmenUnitView) {
			footmen.add(new GameUnit(footman));
		}

		// Create the archer GameUnits
		for (Unit.UnitView archer : archersUnitView) {
			archers.add(new GameUnit(archer));
		}

		// Determine map limits and valid move directions
		xExtent = stateView.getXExtent();
		yExtent = stateView.getYExtent();
		this.validDirections = createValidDirectionsList();
		this.obstacles = new ArrayList<>();
		for (ResourceView resource : stateView.getAllResourceNodes()) {
			obstacles.add(resource);
		}
		aStarAgent = new AstarAgent(xExtent, yExtent);
	}

	/**
	 * Creates a GameState with the assigned depth value
	 * 
	 * @param state
	 *            The state to created the GameState with
	 * @param depth
	 *            The specified depth of the GameState
	 */
	public GameState(State.StateView state, int depth) {
		this(state);
		this.depth = depth;
	}

	/**
	 * Constructor used to initialize the A/B search
	 * 
	 * @param utility
	 *            - the initial utility of this game state
	 */
	public GameState(Integer utility) {
		this.utility = utility;
		footmen = new ArrayList<GameUnit>();
		archers = new ArrayList<GameUnit>();
		this.validDirections = createValidDirectionsList();
		this.obstacles = new ArrayList<>();
		aStarAgent = new AstarAgent(xExtent, yExtent);
	}

	/**
	 * Creates a child game state from a parent game state with the same properties but
	 * depth is one deeper
	 * 
	 * @param parent -
	 *            the parent GameState
	 */
	public GameState(GameState parent) {
		// Initializes all footmen and archers to the same as the parent
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

		// Finds the directions valid to move in
		this.validDirections = createValidDirectionsList();

		// Finds the obstacles on the map and recognizes them
		this.obstacles = new ArrayList<ResourceView>();
		for (ResourceView rView : parent.obstacles) {
			this.obstacles.add(rView);
		}
		aStarAgent = new AstarAgent(xExtent, yExtent);
	}

	/**
	 * Gets a list of all valid directions for movement in the state
	 * 
	 * @return a list of valid movement directions
	 */
	public List<Direction> createValidDirectionsList() {

		List<Direction> validDirections = new ArrayList<>();
		for (Direction dir : Direction.values()) {
			if (isValidDirection(dir)) {
				validDirections.add(dir);
			}
		}
		return validDirections;
	}

	/**
	 * Checks if a direction is valid. Only allowed movement is NORTH,
	 * EAST, SOUTH, and WEST.
	 * 
	 * @param direction -
	 *            the direction whose validity is being checked
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
	 * Gets the total health of all footmen.
	 * 
	 * @return the total health of all footmen
	 */
	public int getFootmenHealth() {
		int totalHealth = 0;
		for (GameUnit footman : footmen) {
			totalHealth += footman.getHP();
		}
		return totalHealth;
	}

	/**
	 * Gets the total health of all archers.
	 * 
	 * @return the total health of all archers
	 */
	public int getArcherHealth() {
		int totalHealth = 0;
		for (GameUnit archer : archers) {
			totalHealth += archer.getHP();
		}
		return totalHealth;
	}

	/**
	 * Returns a list of all GameUnits in the game, both archers and footmen.
	 * 
	 * @return all GameUnits in the state
	 */
	public List<GameUnit> getEntities() {
		List<GameUnit> entities = new ArrayList<>(footmen);
		entities.addAll(archers);
		return entities;
	}

	/**
	 * Returns whether a state is terminal based on if there
	 * are any players left on the map.
	 * 
	 * If either team is completely dead, the state is a terminal.
	 * 
	 * @return true if the state is terminal, false otherwise
	 */
	public boolean isTerminal() {
		return footmen.isEmpty() || archers.isEmpty();
	}

	/**
	 * Applies actions to the state based on the action type.
	 * 
	 * Primitve attacks denote the player to attack the target enemy.
	 * The result is that the enemy's health is deducted by the damage of the
	 * attacking game unit.
	 * 
	 * Primitive moves denote the player to move in the direction of the move.
	 * It is assumed the the moves will be checked for validity before they are
	 * added to the map of actions.
	 * 
	 * @param actions -
	 *            the actions to be applied for each game unit keyed by ID
	 */
	public void applyActions(Map<Integer, Action> actions) {
		Iterator<Integer> keySetItr = actions.keySet().iterator();

		//Iterate through all units and apply their actions.
		while (keySetItr.hasNext()) {
			Integer currentKey = keySetItr.next();
			Action currentAction = actions.get(currentKey);
			ActionType currentActionType = currentAction.getType();

			// Apply the attack action and deduct health from opponent
			if (currentActionType == ActionType.PRIMITIVEATTACK) {
				TargetedAction currentTargetedAction = (TargetedAction) currentAction;
				int unitId = currentTargetedAction.getUnitId();
				int targetId = currentTargetedAction.getTargetId();

				GameUnit unit = getUnit(unitId);
				GameUnit target = getUnit(targetId);

				target.setHP(target.getHP() - unit.getDamage());
			}
			//Move the current unit in the desired direction
			else if (currentActionType == ActionType.PRIMITIVEMOVE) {
				DirectedAction currentDirectedAction = (DirectedAction) currentAction;
				int unitID = currentDirectedAction.getUnitId();
				GameUnit unit = getUnit(unitID);
				Direction moveDirection = currentDirectedAction.getDirection();

				unit.setX(unit.getX() + moveDirection.xComponent());
				unit.setY(unit.getY() + moveDirection.yComponent());
			}
		}
	}

	/**
	 * Finds the GameUnit with the matching ID
	 * 
	 * @param ID - The ID to be searched for
	 * @return the GameUnit with the matching ID, null if no GameUnit has that ID
	 */
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
			int distanceFromArchers = minDistanceFromArcher(footmen.get(0));
			if (footmen.size() >1){
				distanceFromArchers += minDistanceFromArcher(footmen.get(1));	
			}

			//Utility function weighs the footmen's health, archers, health, distance between the footmen and archers, and the number of GameUnits alive
			utility =(W_FOOTMAN_HP * getFootmenHealth())
					+ (W_ARCHER_HP * getArcherHealth())
					+ (W_FOOTMAN_DISTANCE * distanceFromArchers)
					+ (W_FOOTMAN_ALIVE * footmen.size())
					+ (W_ARCHER_ALIVE * archers.size());
		}
		return utility;
	}

	/**
	 * Gets the distance from the footman to the closest archer.
	 * 
	 * @param footman The base unit
	 * @return The distance from the footman to the closest archer
	 */
	private int minDistanceFromArcher(GameUnit footman) {	
		int xDiff = 0;
		int yDiff = 0;
		double minDist = Double.MAX_VALUE;
		GameUnit archer = getClosestEnemy(footman, archers);
		
		xDiff = footman.getX() - archer.getX();
		yDiff = footman.getY() - archer.getY();
	
		//If there are obstacles, use the A* path
		if (obstacles.size() > 0) {
			Stack<MapLocation> aStarPath = aStarAgent.findPath(obstacles, footman, archer);
			if (aStarPath != null){
				minDist = aStarPath.size();
			} 
			else {
				minDist = 50;
			}
		}
		
		//If no obstacles, use Euclidean distance
		else {
			minDist = Math.sqrt(Math.pow(Math.abs(xDiff),2)+Math.pow(Math.abs(yDiff), 2));
		}

		// When there are no archers, return 0, else return the min distance to an archer
		return archers.isEmpty() ? 0 : (int) minDist;
	}

	/**
	 * This method returns a list of GameStateChild objects. We generate all
	 * of the possible actions in a step and then determine the resulting game
	 * state from that action. These are the game state children.
	 *
	 * @return all possible actions and their associated resulting game state
	 */
	public List<GameStateChild> getChildren() {
		//Lists of actions that are available to each unit
		List<Action> unitOneActions = new ArrayList<>();
		List<Action> unitTwoActions = new ArrayList<>();

		int unitOneID = 0;
		int unitTwoID = 0;
		boolean twoUnits = false;
		
		//Get the footmen and their actions
		if (isMax) {
			unitOneID = footmen.get(0).getID();
			unitOneActions = getActions(footmen.get(0), archers);

			if (footmen.size() > 1) {
				unitTwoID = footmen.get(1).getID();
				unitTwoActions = getActions(footmen.get(1), archers);
				twoUnits = true;
			}
		}
		else {
			//Get the archers and their actions
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
			//Apply actions for two units and generate states
			for (Action unitOneAction : unitOneActions) {
				for (Action unitTwoAction : unitTwoActions) {
					actionMap = new HashMap<>();
					actionMap.put(unitOneID, unitOneAction);
					actionMap.put(unitTwoID, unitTwoAction);
					//Make a new game state from actions if they are not bad actions
					if (!badActions(actionMap, unitOneID, unitTwoID)) {
						// ** Need to apply actions to new state
						GameState newState = new GameState(this);
						newState.applyActions(actionMap);
						// ** Then add new state child to list of state children
						children.add(new GameStateChild(actionMap, newState));
					}
				}
			}
		} else {
			//Apply actions for one unit and generate states
			for (Action unitOneAction : unitOneActions) {
				actionMap = new HashMap<>();
				actionMap.put(unitOneID, unitOneAction);
				//Apply actions to new state
				GameState newState = new GameState(this);
				newState.applyActions(actionMap);
				//Then add new state child to list of state children
				children.add(new GameStateChild(actionMap, newState));
			}
		}
		return children;
	}

	/**
	 * Checks to see if an action is bad, either because the two units are moving to the same location, or because they're moving into an obstacle.
	 * 
	 * @param actionMap - The action map contain the two actions for the characters
	 * @param unitOneID - The Unit1 ID
	 * @param unitTwoID - The Unit2 ID
	 * @return True if the action is bad, false otherwise
	 */
	private boolean badActions(Map<Integer, Action> actionMap, int unitOneID,
			int unitTwoID) {

		Action unitOneAction = actionMap.get(unitOneID);
		Action unitTwoAction = actionMap.get(unitTwoID);

		//Check if the two units are attempting to move to the same location
		if (unitOneAction.getType() == ActionType.PRIMITIVEMOVE
				&& unitTwoAction.getType() == ActionType.PRIMITIVEMOVE
				&& moveToSameLocation(null, unitOneAction, unitOneID, unitTwoAction,
						unitTwoID)) {
			return true;
		}
		
		//Check if either of the units is attempting to move into the same location as an obstacle
		for (ResourceView obstacle : obstacles) {

			if (unitOneAction.getType() == ActionType.PRIMITIVEMOVE
					&& unitTwoAction.getType() == ActionType.PRIMITIVEMOVE) {
				//Make sure units don't move to same locations.
				if (moveToSameLocation(obstacle, unitOneAction, unitOneID,
						unitTwoAction, unitTwoID)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks if two units are moving to the same location, or into an obstacle
	 * @param obstacle - The possible obstacle
	 * @param moveActionOne - The action of the first unit
	 * @param unitIDOne - The ID of the first unit
	 * @param moveActionTwo - The action of the second unit
	 * @param unitIDTwo - The ID of the second unit
	 * @return True if the units are moving to the same location, or into the obstacle, false otherwise
	 */
	public boolean moveToSameLocation(ResourceView obstacle,
			Action moveActionOne, int unitIDOne, Action moveActionTwo,
			int unitIDTwo) {
		DirectedAction dActionOne = (DirectedAction) moveActionOne;
		DirectedAction dActionTwo = (DirectedAction) moveActionTwo;

		GameUnit unitOne = getUnit(unitIDOne);
		GameUnit unitTwo = getUnit(unitIDTwo);
		
		//Get x's and y's of both units
		int xOne = unitOne.getX() + dActionOne.getDirection().xComponent();
		int yOne = unitOne.getY() + dActionOne.getDirection().yComponent();
		int xTwo = unitTwo.getX() + dActionTwo.getDirection().xComponent();
		int yTwo = unitTwo.getY() + dActionTwo.getDirection().yComponent();
		int xOb, yOb;
		
		//Check if obstacle or not
		if (obstacle != null) {
			xOb = obstacle.getXPosition();
			yOb = obstacle.getYPosition();
		}
		else {
			xOb = -1;
			yOb = -1;
		}
		
		//check if going to same location and return true if so
		if ((xOne == xOb && yOne == yOb) || (xTwo == xOb && yTwo == yOb)) {
			return true;
		}
		return false;
	}

	/**
	 * Gets all possible actions the agent can take. 
	 * A-star search is used to find a path to the targeted enemy if there are
	 * obstacles on the map. This assures that the enemy will find 
	 * 
	 * @param player - The agent whose actions are being retrieved
	 * @param enemies - All enemies of the player that are on the map
	 * @return A List<Action> of all possible actions for the player
	 */
	private List<Action> getActions(GameUnit player, List<GameUnit> enemies) {
		List<GameUnit> entities = getEntities();
		List<Action> actions = new ArrayList<>();
		int playerX = player.getX();
		int playerY = player.getY();
		
		// Uses A* search to determine moves if there are obstacles on the map
		if (obstacles.size() > 0) {
			Stack<MapLocation> aStarPath = aStarAgent.findPath(obstacles,
					player, getClosestEnemy(player, enemies));
			if (aStarPath.size() > 0) {
				MapLocation nextLoc = aStarPath.pop();
				actions.add(Action.createPrimitiveMove(player.getID(),
						getMoveDirection(player, nextLoc)));
			}
		}
		// Otherwise just uses all valid moves since we can take straight path
		// to enemies
		else {
			// Add all possible moves to the action list for this player
			for (Direction direction : validDirections) {
				if (possibleMove(playerX + direction.xComponent(), playerY
						+ direction.yComponent(), entities)) {
					actions.add(Action.createPrimitiveMove(player.getID(),
							direction));
				}
			}
		}
		
		// Add all possible attacks to the action list for this player
		for (GameUnit enemy : enemiesInRange(player)) {
			actions.add(Action.createPrimitiveAttack(player.getID(),
					enemy.getID()));
		}
		return actions;
	}

	/**
	 * Gets the direction of a move based on a GameUnit and its next location
	 * 
	 * @param player - The GameUnit that is moving
	 * @param nextLoc - The location the GameUnit will be moving to
	 * @return The Direction of the move
	 */
	private Direction getMoveDirection(GameUnit player, MapLocation nextLoc) {
		int playerX = player.getX();
		int playerY = player.getY();
		int nextLocX = nextLoc.x;
		int nextLocY = nextLoc.y;

		if (nextLocX == playerX) { // If this is true we are moving either north
									// or south
			if (playerY - nextLocY == 1)
				return Direction.NORTH;
			else if (playerY - nextLocY == -1)
				return Direction.SOUTH;
		} else if (nextLocY == playerY) { // If this is true we are moving
											// either east or west
			if (playerX - nextLocX == -1)
				return Direction.EAST;
			else if (playerX - nextLocX == 1)
				return Direction.WEST;
		}

		return null;
	}

	/**
	 * Finds the closest enemy to a given player.
	 * 
	 * @param player The GameUnit whose closest enemy is being found
	 * @param enemies The list of all enemies of the GameUnit in the state
	 * @return The GameUnit object for the closest enemy
	 */
	private GameUnit getClosestEnemy(GameUnit player, List<GameUnit> enemies) {
		int minDist = Integer.MAX_VALUE;
		int nextDist = 0;
		GameUnit closestEnemy = null;
		GameUnit archer;
		//Find the closest enemy
		for (GameUnit enemy : enemies) {
			archer = enemy;
			int xDiff = player.getX() - archer.getX();
			int yDiff = player.getY() - archer.getY();
			// Manhattan Distance implementation
			nextDist = Math.abs(xDiff) + Math.abs(yDiff);
			if (nextDist < minDist) {
				minDist = nextDist;
				closestEnemy = enemy;
			}
		}

		return closestEnemy;
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

	/**
	 * Gets a list of all enemies that can be attacked by the player
	 * @param player The attacking player
	 * @return List<GameUnit> of all enemies that can be attacked
	 */
	private List<GameUnit> enemiesInRange(GameUnit player) {
		int range = 0;
		int xDiff = 0;
		int yDiff = 0;
		List<GameUnit> enemies;
		List<GameUnit> enemiesInRange = new ArrayList<>();

		//When max, enemies are archers
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
}
