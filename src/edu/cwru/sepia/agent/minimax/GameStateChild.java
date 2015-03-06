package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.environment.model.state.State;

import java.util.Map;

/**
 * Do not change this class.
 */
public class GameStateChild {
    /**
     * The set of unit actions that produced the game state.
     * The integer is the unit id and the action is the action that unit took
	 * in this state.
     */
    public Map<Integer, Action> action;
    
    //The game state resulting from the specified set of actions
    public GameState state;

    public GameStateChild(State.StateView state)
    {
        action = null;
        this.state = new GameState(state);
    }

    public GameStateChild(Map<Integer, Action> action, GameState state)
    {
        this.action = action;
        this.state = state;
    }
}
