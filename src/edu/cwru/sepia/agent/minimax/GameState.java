package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.action.DirectedAction;
import edu.cwru.sepia.action.TargetedAction;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.StateCreator;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.Direction;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * This class stores all of the information the agent needs to know about the
 * state of the game. For example this might include things like footmen HP and
 * positions.
 *
 * Add any information or methods you would like to this class, but do not
 * delete or change the signatures of the provided methods.
 */
public class GameState {
	private static final int W_FOOTMAN_HP = 1;
	private static final int W_FOOTMAN_DISTANCE = -1;
	private static final int W_ARCHER_HP = -10;
	private static final int W_FOOTMAN_ALIVE = 10;
	private static final int W_ARCHER_ALIVE = -100;
	private static final int FOOTMAN_RANGE = 1;
	private static final int ARCHER_RANGE = 15;

	public int xExtent = 0, yExtent = 0;
	public List<GameUnit> footmen, archers;
	private int footmanNum = 0;
	private int archerNum = 1;
	private int depth = 0;
	private int weight = Integer.MIN_VALUE;
	private boolean isMax = true;
	
	private State state;

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
	 * For a given ResourceView you can query the position using
	 * resource.getXPosition() and resource.getYPosition()
	 *
	 * For a given unit you will need to find the attack damage, range and max
	 * HP unitView.getTemplateView().getRange(): This gives you the attack range
	 * unitView.getTemplateView().getBasicAttack(): The amount of damage this
	 * unit deals unitView.getTemplateView().getBaseHealth(): The maximum amount
	 * of health of this unit
	 *
	 * @param state
	 *            Current state of the episode
	 * @throws IOException 
	 */
	public GameState(State.StateView stateView){
		footmen = new ArrayList<GameUnit>();
		archers = new ArrayList<GameUnit>();
		List<Unit.UnitView> footmenUnitView = stateView.getUnits(footmanNum);
		List<Unit.UnitView> archersUnitView = stateView.getUnits(archerNum);
		
		for (Unit.UnitView footman : footmenUnitView) {
			footmen.add(new GameUnit(footman));
		}
		
		for (Unit.UnitView archer : archersUnitView) {
			archers.add(new GameUnit(archer));
		}
		
		xExtent = state.getXExtent();
		yExtent = state.getYExtent();
	}

	public GameState(State.StateView state, int depth) throws IOException {
		this(state);
		this.depth = depth;
	}

	/* Not really sure what this constructor is for so I'm commenting it out for now
	public GameState(Integer weight) {
		this.weight = weight;
		footmenView = new ArrayList<UnitView>();
		archersView = new ArrayList<UnitView>();
	}
	*/
	
	public GameState(GameState parent){
		this.footmen = parent.footmen;
		this.archers = parent.archers;
		this.xExtent = parent.getXExtent();
		this.yExtent = parent.getYExtent();
		this.depth = parent.getDepth() + 1;
	}

	public int getXExtent() {
		return xExtent;
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

	public int getFootmenHealth() {
		int totalHealth = 0;
		for (GameUnit footman : footmen) {
			totalHealth += footman.getHP();
		}
		return totalHealth;
	}

	public int getArcherHealth() {
		int totalHealth = 0;
		for (GameUnit archer : archers) {
			totalHealth += archer.getHP();
		}
		return totalHealth;
	}

	public List<GameUnit> getEntities() {
		List<GameUnit> entities = new ArrayList<>(footmen);
		entities.addAll(archers);
		return entities;
	}

	public boolean isTerminal() {
		return footmen.isEmpty() || archers.isEmpty();
	}
	
	/**
	 * Applies actions to the state
	 * 
	 * @param actions The actions to be applied
	 */
	public void applyActions(Map<Integer, Action> actions){
		Set<Integer> keySet = actions.keySet(); //Gets set of all keys contained in the map
		Iterator<Integer> keySetItr = keySet.iterator();
		
		while (keySetItr.hasNext()){
			Integer currentKey = keySetItr.next();
			Action currentAction = actions.get(currentKey);
			ActionType currentActionType = currentAction.getType();
			
			if (currentActionType == ActionType.PRIMITIVEATTACK) {
				TargetedAction currentTargetedAction = (TargetedAction) currentAction; //There might be a better way to do this
				int unitId = currentTargetedAction.getUnitId();
				int targetId = currentTargetedAction.getTargetId();
				
				GameUnit unit = getUnit(unitId);
				GameUnit target = getUnit(targetId);
				
				target.setHP(target.getHP() - unit.getDamage());
			}
			
			else if (currentActionType == ActionType.PRIMITIVEMOVE) {
				DirectedAction currentDirectedAction = (DirectedAction) currentAction;
				int unitID = currentDirectedAction.getUnitId();
				GameUnit unit = getUnit(unitID);
				Direction moveDirection = currentDirectedAction.getDirection();
				
				//Specifically says in the JavaDoc not to move units in this way
				//but since we're not actually returning this in the middlestep I don't see any other way to do it currently.
				if (moveDirection == Direction.NORTH)
					unit.setYPosition(unit.getYPosition() + 1);
				if (moveDirection == Direction.SOUTH)
					unit.setYPosition(unit.getYPosition() - 1);
				if (moveDirection == Direction.EAST)
					unit.setXPosition(unit.getXPosition() + 1);
				if (moveDirection == Direction.WEST)
					unit.setXPosition(unit.getXPosition() - 1);
				if (moveDirection == Direction.NORTHEAST){
					unit.setYPosition(unit.getYPosition() + 1);
					unit.setXPosition(unit.getXPosition() + 1);
				}
				if (moveDirection == Direction.SOUTHEAST){
					unit.setYPosition(unit.getYPosition() - 1);
					unit.setXPosition(unit.getXPosition() + 1);
				}
				if (moveDirection == Direction.NORTHWEST){
					unit.setYPosition(unit.getYPosition() + 1);
					unit.setXPosition(unit.getXPosition() - 1);
				}
				if (moveDirection == Direction.SOUTHWEST){
					unit.setYPosition(unit.getYPosition() - 1);
					unit.setXPosition(unit.getXPosition() - 1);
				}
			}
		}
		
		//refreshViews();
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
	
	/* I don't think we need this anymore
	/**
	 * Refreshes the UnitViews based on the current units. Clears the lists, then adds back in any unit that has health > 0
	 
	private void refreshViews() {
		
		if (footmenView != null) {
			footmenView.clear();
			System.out.println("footmenView cleared");
		}
		else {
			footmenView = new ArrayList<UnitView>();
			System.out.println("created footmenView arrayList");
		}
		if (archersView != null)
			archersView.clear();
		else
			archersView = new ArrayList<UnitView>();
		
		Iterator<Integer> footmenItr = footmen.keySet().iterator();
		Iterator<Integer> archersItr = archers.keySet().iterator();
		
		while (footmenItr.hasNext()) {
			Integer currentKey = footmenItr.next();
			if (footmen.get(currentKey).getCurrentHealth() > 0)
				footmenView.add(footmen.get(currentKey).getView());
		}
		
		while (archersItr.hasNext()) {
			Integer currentKey = archersItr.next();
			if (archers.get(currentKey).getCurrentHealth() > 0)
				archersView.add(archers.get(currentKey).getView());
		}
	}
	*/
	
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
		int distanceFromArchers = 0;
		for (GameUnit footman : footmen) {
			distanceFromArchers += minDistanceFromArcher(footman);
		}

		return ((W_FOOTMAN_HP * getFootmenHealth())
				+ (W_ARCHER_HP * getArcherHealth())
				+ (W_FOOTMAN_DISTANCE * distanceFromArchers)
				+ (W_FOOTMAN_ALIVE * footmen.size()) + (W_ARCHER_ALIVE * archers
				.size()));
	}

	// public int getWeight(){
	// if (weight == Integer.MIN_VALUE){
	// weight = 0;
	// UnitView a = archers.get(0);
	// UnitView f1 = footmen.get(0);
	// UnitView f2 = footmen.get(1);
	// int dx1 = Math.abs(a.getXPosition() - f1.getXPosition());
	// int dy1 = Math.abs(a.getYPosition() - f1.getYPosition());
	// int dx2 = Math.abs(a.getXPosition() - f2.getXPosition());
	// int dy2 = Math.abs(a.getYPosition() - f2.getYPosition());
	// weight -= dx1 * 10 + dy1 + dx2 + dy2 * 10;
	// }
	// return weight;
	// }

	private int minDistanceFromArcher(GameUnit footman) {
		int minDist = Integer.MAX_VALUE;
		int nextDist = 0;
		// Find the closest distance between footman and archers
		for (GameUnit archer : archers) {
			nextDist = Math.max(
					Math.abs(footman.getXPosition() - archer.getXPosition()),
					Math.abs(footman.getYPosition() - archer.getYPosition()));
			if (nextDist < minDist) {
				minDist = nextDist;
			}
		}
		// When there are no archers, return 0, else return the min distance to
		// an archer
		return archers.isEmpty() ? 0 : minDist;
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
	 * To get the resulting position from a move in that direction you can do
	 * the following x += direction.xComponent() y += direction.yComponent()
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

		// Now we have a list of action maps for each game state
		
		for (Action unitOneAction : unitOneActions) {
			Map<Integer, Action> u1ActionMap = new HashMap<>();
			u1ActionMap.put(unitOneID, unitOneAction);
			action.add(u1ActionMap);
		}

		// Make all the possible game states, eliminating those options where
		// player units move to the same location.
		if (twoUnits) {
			for (Action unitTwoAction : unitTwoActions) {
				for (Map<Integer, Action> actionMap : action) {
					actionMap.put(unitTwoID, unitTwoAction);
				}
			}
			Map<Integer, Action> actionMap = new HashMap<>();
			Iterator<Map<Integer, Action>> actionItr = action.iterator();

			while (actionItr.hasNext()) {
				actionMap = actionItr.next();
				Action unitOneAction = actionMap.get(unitOneID);
				Action unitTwoAction = actionMap.get(unitTwoID);
				if (unitOneAction.getType() == ActionType.PRIMITIVEMOVE
						&& unitTwoAction.getType() == ActionType.PRIMITIVEMOVE
						&& moveToSameLocation(unitOneAction, unitTwoAction)) {
					actionItr.remove();
				}
			}
		}
		
		List<GameStateChild> children = new ArrayList<>();
		for (Map<Integer, Action> actions : action) {
			//** Need to apply actions to new state
			GameState newState = new GameState(this);
			newState.applyActions(actions);
			//** Then add new state child to list of state children
			children.add(new GameStateChild(actions, newState));
		}

		return children;
	}

	private List<Action> getActions(UnitView player, List<UnitView> enemies) {
		List<GameUnit> entities = getEntities();
		List<Action> actions = new ArrayList<>();

		int playerX = player.getXPosition();
		int playerY = player.getYPosition();

		// Add all possible moves to the action list for this player
		for (Direction direction : Direction.values()) {
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
	 * Determines if both unit 1 and unit 2 are moving to the same
	 * location. These units are determined based on whether this
	 * game state is a max state.
	 * 
	 * @param moveActionOne
	 *            - the move action of unit 1
	 * @param moveActionTwo
	 *            - the move action of unit 2
	 * @return whether both unit 1 and unit 2 are moving to the same
	 *         location
	 */
	public boolean moveToSameLocation(Action moveActionOne, Action moveActionTwo) {
		DirectedAction dActionOne = (DirectedAction) moveActionOne;
		DirectedAction dActionTwo = (DirectedAction) moveActionTwo;
		List<GameUnit> units;
		if (isMax){
			units = footmen;
		} else {
			units = archers;
		}
		
		int xOne = units.get(0).getXPosition()
				+ dActionOne.getDirection().xComponent();
		int yOne = units.get(0).getYPosition()
				+ dActionOne.getDirection().yComponent();
		int xTwo = units.get(1).getXPosition()
				+ dActionTwo.getDirection().xComponent();
		int yTwo = units.get(1).getYPosition()
				+ dActionTwo.getDirection().yComponent();
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
				if (entity.getXPosition() == x && entity.getYPosition() == y) {
					isPossible = false;
					break ENTITY_LOOP;
				}
			}
		}
		return isPossible;
	}

	private List<GameUnit> enemiesInRange(UnitView player) {
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
			xDiff = Math.abs(player.getXPosition() - enemy.getXPosition());
			yDiff = Math.abs(player.getYPosition() - enemy.getYPosition());
			if (range >= (xDiff + yDiff)) {
				enemiesInRange.add(enemy);
			}
		}
		return enemiesInRange;
	}
}
