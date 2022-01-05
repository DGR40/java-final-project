package submit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import game.FindState;
import game.Finder;
import game.Node;
import game.NodeStatus;
import game.ScramState;
import game.Tile;

/** Student solution for two methods. */
public class Pollack extends Finder {

    /** Get to the orb in as few steps as possible. <br>
     * Once you get there, you must return from the function in order to pick it up. <br>
     * If you continue to move after finding the orb rather than returning, it will not count.<br>
     * If you return from this function while not standing on top of the orb, it will count as <br>
     * a failure.
     *
     * There is no limit to how many steps you can take, but you will receive<br>
     * a score bonus multiplier for finding the orb in fewer steps.
     *
     * At every step, you know only your current tile's ID and the ID of all<br>
     * open neighbor tiles, as well as the distance to the orb at each of <br>
     * these tiles (ignoring walls and obstacles).
     *
     * In order to get information about the current state, use functions<br>
     * state.currentLoc(), state.neighbors(), and state.distanceToOrb() in FindState.<br>
     * You know you are standing on the orb when distanceToOrb() is 0.
     *
     * Use function state.moveTo(long id) in FindState to move to a neighboring<br>
     * tile by its ID. Doing this will change state to reflect your new position.
     *
     * A suggested first implementation that will always find the orb, but <br>
     * likely won't receive a large bonus multiplier, is a depth-first walk. <br>
     * Some modification is necessary to make the search better, in general. */

    @Override
    public void findOrb(FindState s) {
        // TODO 1: Get the orb

        // create visited array
        ArrayList visited= new ArrayList();
        dfsOpt(s, visited);

    }

    /*dfs walk of the map (not optimized) */
    private void dfs(FindState s, ArrayList visited) {

        // if on orb return
        if (s.distanceToOrb() == 0) { return; }

        // curr location
        long currTile= s.currentLoc();

        // add curr location to visited
        visited.add(currTile);

        // get collection of neighbors
        Collection<NodeStatus> neighbors= s.neighbors();

        // for each neighbor that is not visited, visit
        for (NodeStatus n : neighbors) {
            if (!visited.contains(n.getId())) {
                s.moveTo(n.getId());
                dfs(s, visited);
                if (s.distanceToOrb() == 0) { return; }
                s.moveTo(currTile);
            }
        }

    }

    /** optimized dfs walk of the map. It moves martha to the orb by moving to the
     * neighbor that is closest to the orb each time the function recurses. */
    private void dfsOpt(FindState s, ArrayList visited) {
        // if on orb return
        if (s.distanceToOrb() == 0) { return; }

        // curr location
        long currTile= s.currentLoc();

        // add curr location to visited
        visited.add(currTile);

        // get collection of neighbors
        Collection<NodeStatus> neighbors= s.neighbors();

        // convert neighbors to arraylist
        List<NodeStatus> nlist= (List) neighbors;

        // sort arraylist of neighbors, closest to orb first
        Collections.sort(nlist);

        // for each neighbor that is not visited, visit
        for (NodeStatus n : nlist) {
            if (!visited.contains(n.getId())) {
                s.moveTo(n.getId());
                dfsOpt(s, visited);
                if (s.distanceToOrb() == 0) { return; }
                s.moveTo(currTile);
            }
        }
    }

    /** Pres Pollack is standing at a node given by parameter state.<br>
     *
     * Get out of the cavern before the ceiling collapses, trying to collect as <br>
     * much gold as possible along the way. Your solution must ALWAYS get out <br>
     * before time runs out, and this should be prioritized above collecting gold.
     *
     * You now have access to the entire underlying graph, which can be accessed <br>
     * through parameter state. <br>
     * state.currentNode() and state.getExit() will return Node objects of interest, and <br>
     * state.allNodes() will return a collection of all nodes on the graph.
     *
     * The cavern will collapse in the number of steps given by <br>
     * state.stepsLeft(), and for each step this number is decremented by the <br>
     * weight of the edge taken. <br>
     * Use state.stepsLeft() to get the time still remaining, <br>
     * Use state.moveTo() to move to a destination node adjacent to your current node.<br>
     * Do not call state.grabGold(). Gold on a node is automatically picked up <br>
     * when the node is reached.<br>
     *
     * The method must return from this function while standing at the exit. <br>
     * Failing to do so before time runs out or returning from the wrong <br>
     * location will be considered a failed run.
     *
     * You will always have enough time to scram using the shortest path from the <br>
     * starting position to the exit, although this will not collect much gold. <br>
     * For this reason, using the shortest path method to calculate the shortest <br>
     * path to the exit is a good starting solution */
    @Override
    public void scram(ScramState state) {
        // TODO 2: scram//
        optScramIt(state);

    }

    private void unOptScram(ScramState s) {
        Node start= s.currentNode();
        Node finish= s.getExit();
        List<Node> path= Path.shortest(start, finish);

        for (Node n : path) {
            if (n == start) {

            } else {
                s.moveTo(n);
            }
        }
        return;

    }

    /** idea... continually finds node with best gold to weight ratio and that is reachable and
     * goes there from starting point. Reachable means enough steps left to get to the node and to
     * the exit from the node. Once there are no more steps left, or no more optimal paths,
     * returns. */
    private void optScramIt(ScramState s) {
        // while there are steps left and president isn't on the exit
        while (s.stepsLeft() > 0 && s.currentNode() != s.getExit()) {
            // if path does not exist
            if (getBestNode(s) == null) {
                List<Node> emergencyExit= Path.shortest(s.currentNode(), s.getExit());
                go(s, emergencyExit);
                return;
            } else {
                go(s, Path.shortest(s.currentNode(), getBestNode(s)));
            }
        }
        return;
    }

    /** returns the node at the end of the optimal path (best ratio of gold to weight) . if path
     * does not exist, returns null */
    private Node getBestNode(ScramState s) {

        // the factor gold is multiplied by in the gToW ratio (18k avg with factor = 10)
        int factor= 10;
        // initializes the best node
        Node bestNode= null;
        // gets list of all gold nodes
        List<Node> goldNodes= getGoldList(s);
        // gets number of steps left
        int stepsToFindGold= s.stepsLeft();
        // initializes gold to weight ratio
        int gToW= 0;
        // iterates over each gold node to find optimal path
        for (Node n : goldNodes) {
            // List of nodes from start to node n
            List<Node> goldPath= Path.shortest(s.currentNode(), n);

            // List of nodes from node n to exit
            List<Node> exitPath= Path.shortest(n, s.getExit());

            // gold of this path
            int gold= goldOf(s, goldPath);
            // to avoid division by zero
            if (gold == 0) { gold= 1; }

            // avg. weight of this path and its exit path
            int weight= weightOf(s, goldPath) + weightOf(s, exitPath) / 2;
            // to avoid division by zero
            if (weight == 0) { weight= 1; }

            // make sure enough steps left. if not enough left, skip this path
            if (gold * factor / weight > gToW &&
                weightOf(s, goldPath) + weightOf(s, exitPath) <= stepsToFindGold) {
                gToW= gold * factor / weight;
                bestNode= n;
            }
        }
        if (s.currentNode() == bestNode) {
            return null;
        } else {
            return bestNode;
        }

    }

    /** return Node with most Gold */
    private Node mostGold(List<Node> l) {
        int mostGold= 0;
        Node mostNode= null;
        for (Node n : l) {
            Tile t= n.getTile();
            if (t.gold() > mostGold) {
                mostGold= t.gold();
                mostNode= n;
            }
        }
        return mostNode;
    }

    /** moves Pollack over a given lists of nodes p */
    private void go(ScramState s, List<Node> p) {
        for (Node n : p) {
            if (n != s.currentNode()) {
                s.moveTo(n);
            }
        }
    }

    /** returns total weight of a given lists of nodes p */
    private int weightOf(ScramState s, List<Node> p) {
        int weight= 0;
        for (int i= 0; i < p.size() - 1; i++ ) {
            Node first= p.get(i);
            Node second= p.get(i + 1);
            weight= weight + first.getEdge(second).length();
        }
        return weight;
    }

    /** returns total gold found on a given lists of nodes p */
    private int goldOf(ScramState s, List<Node> p) {
        int gold= 0;
        for (Node n : p) {
            if (n == s.currentNode()) {
                gold= n.getTile().gold();
            } else {
                gold= gold + n.getTile().gold();
            }
        }
        return gold;
    }

    /** returns list of only gold nodes */
    private List<Node> getGoldList(ScramState s) {
        List<Node> goldNodes= new ArrayList<>();
        for (Node n : s.allNodes()) {
            goldNodes.add(n);
        }
        return goldNodes;
    }

}
