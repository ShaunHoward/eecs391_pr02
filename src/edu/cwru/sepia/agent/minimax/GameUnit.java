package edu.cwru.sepia.agent.minimax;

import java.util.Stack;
import edu.cwru.sepia.agent.minimax.AstarAgent.MapLocation;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.UnitTemplate.UnitTemplateView;

/**
 * A game unit is a unit in the SEPIA game.
 * This class stores the values for each unit, including x,y position,
 * health, damage, ID, and a path to the targeted enemy, if one has
 * been calculated.
 * 
 * @author Shaun Howard(smh150), Matt Swartwout(mws85)
 *
 */
public class GameUnit {
	
	//GameUnit variables: position, health, damage, ID, pathToEnemy
	private int x, y;
	private int HP;
	private int damage;
	private int ID;
	private Stack<MapLocation> pathToEnemy = new Stack<>();

	/**
	 * Creates a GameUnit with the same characteristics as the given
	 * Unit.UnitView.
	 * 
	 * @param unit -
	 *            the Unit.UnitView that the GameUnit should be based on
	 */
	public GameUnit(Unit.UnitView unit) {

		UnitTemplateView unitTemplate = unit.getTemplateView();

		//Set all necessary values of this game unit from the given unit view
		x = unit.getXPosition();
		y = unit.getYPosition();
		HP = unit.getHP();
		damage = unitTemplate.getBasicAttack()
				+ unitTemplate.getPiercingAttack();
		ID = unit.getID();
	}

	/**
	 * Creates a GameUnit with the same characteristics as the given GameUnit.
	 * 
	 * @param gUnit -
	 *            the GameUnit that the new GameUnit should be based on
	 */
	public GameUnit(GameUnit gUnit) {
		this.x = gUnit.getX();
		this.y = gUnit.getY();
		this.HP = gUnit.getHP();
		this.damage = gUnit.getDamage();
		this.ID = gUnit.getID();
	}
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getHP() {
		return HP;
	}

	public void setHP(int hp) {
		HP = hp;
	}

	public int getDamage() {
		return damage;
	}

	public int getID() {
		return ID;
	}

	public void setPathToEnemy(Stack<MapLocation> path) {
		this.pathToEnemy = path;
	}

	public Stack<MapLocation> getPathToEnemy() {
		return this.pathToEnemy;
	}
}
