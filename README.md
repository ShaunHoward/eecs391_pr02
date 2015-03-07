# eecs391_pr02
Minimax A/B search implementation for EECS 391 at CWRU, Spring '15.

Team: Shaun Howard (smh150) and Matt Swartwout (mws85)

This project represents a minimax, depth-limited search agent with alpha-beta pruning that plays a
game between a team of footmen and a team of archers in the SEPIA game engine.
The search algorithm is designed such that the footmen are the max player and
the archers are the min player. Alpha-beta pruning is implemented by disregarding
any game states that would lead to worse actions than previously examined game states. 
Such states need not be evaluated further and thus the best actions for each player
are utilized based on a weighted utility function in order to find the maximum and/or
minimum game state child. From this the algorithm can decide the 
next set of actions to be carried out by SEPIA that are best for the current player.

A game state stores all of the information the agent needs to know about the
state of the game. Properties such as footmen health, archer health,
state utility, whether the state is a max state, and the list of obstacles
in this current state are stored in this class.
 
There are methods to generate the children of this game state based on 
what actions can be taken for the current state of the game. If there are
obstacles in the current game, the game state will produce directed actions
based on the use of a-star search. Once the search fails, all permutations
of directed actions are generated and evaluated for utility value.
When the given unit is in range of another unit, attacks will also be applied
to their actions list, and thus generated children are produced by directed
and attack actions.
 
A game state is comparable based on its estimated utility value.

Each game state has an estimated utility value that is used as the heuristic.
The estimated utility is a weighted linear function designed with a heavy
positive weight on whether the footmen are alive and a heavy negative weight
on whether the archers are alive.

The heuristic is based on the following properties:
 
* Current health of combined footmen
* Current health of combined archers
* Minimum distance of footmen from specifically targeted archers
* Current number of footmen alive
* Current number of archers alive
 
The minimum distance from footmen to targeted archers when there are
no obstacles on the map is the euclidean distance between footman and archer.
  
The minimum distance from footmen to targeted archers when there are
obstacles on the map is the size of the a-star path length between footman and archer.
 
This function is used in the alphaBetaSearch method to order the children of the current
game state based on their heuristics in order to expand the best-valued children first.
 
We chose to use these properties as heuristics because the survival of the footmen is vital to
the game. If we weight the survival of archers as negative and the survival of footmen as positive,
then the max node will play to kill the archers and this is the behavior we desire. We also know
that the archers have a greater range than the footmen, and thus they can attack footmen from far away.
Hence, we weight the distance of footmen from archers negatively so that the footmen will move toward
the archers and try to attack and kill them. Lastly, we aim to take actions that will dwindle the number
of archers alive, hence max nodes will take actions that aim to kill archers and the min nodes will aim
to minimize their own death (the utility of the max).

A game unit is a unit in the SEPIA game.
This class stores the values for each unit, including x,y position,
health, damage, ID, and a path to the targeted enemy, if one has
been calculated.

A game state child is a class that links an action map to the game state
it produces.

An a-star agent is used in the minimax alpha-beta search to find a decent path
to the enemy around obstacles that exist in the current game map. Movement actions
are based on the direction of the next map location in the calculated path. If no obstacles
exist, the permutations of possible moves are generated for the current player
units that are alive on the map.

For any more notes, please see the comments in the included java files. They are descriptive and better
explain what our code does to find the best children.

We hope you enjoy and have a great Spring Break!
 
 
