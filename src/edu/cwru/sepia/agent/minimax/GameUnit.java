package edu.cwru.sepia.agent.minimax;

import java.util.Stack;

import edu.cwru.sepia.agent.minimax.AstarAgent.MapLocation;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.UnitTemplate.UnitTemplateView;

public class GameUnit {
	private int x, y;
	private int HP;
	private int damage;
	private int ID;
	private Stack<MapLocation> pathToEnemy = new Stack<>();

	/**
	 * Creates a GameUnit with the same characteristics as the given
	 * Unit.UnitView
	 * 
	 * @param unit
	 *            The Unit.UnitView that the GameUnit should be based on
	 */
	public GameUnit(Unit.UnitView unit) {

		UnitTemplateView unitTemplate = unit.getTemplateView();

		x = unit.getXPosition();
		y = unit.getYPosition();
		HP = unit.getHP();
		damage = unitTemplate.getBasicAttack()
				+ unitTemplate.getPiercingAttack();
		ID = unit.getID();
	}

	/**
	 * Creates a GameUnit with the same characteristics as the given GameUnit
	 * 
	 * @param gUnit
	 *            The GameUnit that the new GameUnit should be based
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
