package edu.cwru.sepia.agent.minimax;


import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.UnitTemplate.UnitTemplateView;

public class GameUnit {
	private int x, y;
	private int HP;
	private int damage;
	private int ID;
	
	GameUnit(Unit.UnitView unit) {
	
		UnitTemplateView unitTemplate = unit.getTemplateView();
		
		x = unit.getXPosition();
		y = unit.getYPosition();
		HP = unit.getHP();
		damage = unitTemplate.getBasicAttack() + unitTemplate.getPiercingAttack();
		ID = unit.getID();
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
}
