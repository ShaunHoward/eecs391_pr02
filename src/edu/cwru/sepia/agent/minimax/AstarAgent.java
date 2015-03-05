package edu.cwru.sepia.agent.minimax;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;

import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.util.DistanceMetrics;

public class AstarAgent {
	//The x and y extent of the current map
	private int xExtent, yExtent;
	
	/**
	 * A basic constructor for the astar agent.
	 * 
	 * @param xExtent - the x extent of the current map
	 * @param yExtent - the y extent of the current map
	 */
	public AstarAgent(int xExtent, int yExtent){
		this.xExtent = xExtent;
		this.yExtent = yExtent;
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
    public class MapLocation implements Comparable<MapLocation> {

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

            //Add neighbors of 4 directions to set of neighbors
            neighbors.add(getNorthNeighbor(map));
            neighbors.add(getSouthNeighbor(map));
            neighbors.add(getEastNeighbor(map));
            neighbors.add(getWestNeighbor(map));

            //Remove null if any neighbors were not found
            if (neighbors.contains(null)) {
                neighbors.remove(null);
            }
            return neighbors;
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
     * Finds an A* path from the footman start position to the town hall position if one exists.
     * If a path does not exist, null will be returned. The enemy footman location and resource locations
     * are noted to make sure the agent navigates around them since they are considered unreachable locations.
     *
     * @param state - the state of the map
     * @return the maplocations of an A* path to navigate the agent to the town hall around all resources and enemy
     */
    public Stack<MapLocation> findPath(List<ResourceView> obstacles, GameUnit player, GameUnit enemy) {

        MapLocation startLoc = new MapLocation(player.getX(), player.getY(), null, 0);

        MapLocation goalLoc = new MapLocation(enemy.getX(), enemy.getY(), null, 0);

        // get resource locations
        Set<MapLocation> resourceLocations = new HashSet<MapLocation>();
        for (ResourceView resource : obstacles) {
            resourceLocations.add(new MapLocation(resource.getXPosition(), resource.getYPosition(), null, 0));
        }

        return AstarSearch(startLoc, goalLoc, xExtent, yExtent, resourceLocations);
    }

    /**
     * Finds the A* path through a map via the given map locations. The algorithm will avoid designing a path through
     * the resource locations but will find an optimal path to a location adjacent to the goal location
     * from the footman starting location.
     * <p/>
     * It returns a Stack of locations with the top of the stack being the first space to move to
     * and the bottom of the stack being the last space to move to. If there is no path to the goal
     * then null is returned from the method and the agent will print a message.
     * <p/>
     * An example map is the following:
     * <p/>
     * F - - - -
     * x x x - x
     * G - - - -
     * <p/>
     * F is the footman
     * G is the goal
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
     * Notice how the initial footman position and the goal position are not included in the path stack
     *
     * @param start             - Starting position of the footman
     * @param goal              - MapLocation of the goal
     * @param xExtent           - Width of the map
     * @param yExtent           - Length of the map
     * @param resourceLocations - Set of positions occupied by resources
     * @return Stack of positions with top of stack being first move in plan
     */
    private Stack<MapLocation> AstarSearch(MapLocation start, MapLocation goal, int xExtent, int yExtent, Set<MapLocation> resourceLocations) {
        /* The agent map for this A* agent. It is used for each A* search. */
        AgentMap agentMap;
        
    	//initialization of map locations for the search
        MapLocation cheapestLocation;
        Set<MapLocation> possibleLocations;
        Set<MapLocation> expandedLocations = new HashSet<>();
        PriorityQueue<MapLocation> openLocations = new PriorityQueue<>();

        //initialization of agent's current map
        agentMap = new AgentMap(xExtent, yExtent, start, goal, resourceLocations);
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
    public Stack<MapLocation> AstarPath(MapLocation end) {
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

}
