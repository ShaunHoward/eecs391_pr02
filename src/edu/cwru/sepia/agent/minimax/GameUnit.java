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
	
	public int getXPosition() {
		return x;
	}

	public void setXPosition(int x) {
		this.x = x;
	}

	public int getYPosition() {
		return y;
	}

	public void setYPosition(int y) {
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
