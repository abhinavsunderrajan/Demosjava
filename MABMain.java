package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 
 * @author abhinav.sunderrajan
 *
 */
public class MABMain {

    private static final double BANDIT_PROB[] = { 0.10, 0.50, 0.60, 0.80, 0.10, 0.25, 0.60, 0.45, 0.75, 0.65 };
    private static final int N_experiments = 10;
    private static final int N_episodes = 10000;
    private static final double epsilon = 0.1;
    private static final Random RANDOM = new Random(42);

    private static int getReward(int action) {
	int reward = RANDOM.nextDouble() <= BANDIT_PROB[action] ? 1 : 0;
	return reward;
    }

    private class Agent {

	// the Q value of an action
	double[] Qval = new double[BANDIT_PROB.length];
	// number of times an action was taken
	int k[] = new int[BANDIT_PROB.length];

	public int get_action() {
	    double rand = RANDOM.nextDouble();
	    if (rand < epsilon) {
		int action_explore = RANDOM.nextInt(BANDIT_PROB.length);
		return action_explore;
	    } else {
		int action_greedy = getMaxQValAction();
		return action_greedy;
	    }
	}

	private int getMaxQValAction() {
	    List<Integer> indices = new ArrayList<>();
	    double max = Arrays.stream(Qval).max().getAsDouble();
	    for (int index = 0; index < Qval.length; index++) {
		if (Qval[index] == max)
		    indices.add(index);
	    }

	    return indices.get(RANDOM.nextInt(indices.size()));
	}

	// Update Q action-value using:
	// Q(a) <- Q(a) + 1/(k+1) * (r(a) - Q(a))
	public void update_Q(int action, double reward) {
	    this.k[action] = this.k[action] + 1;
	    this.Qval[action] += (1.0 / this.k[action]) * (reward - this.Qval[action]);
	}

    }

    public static void main(String[] args) {
	MABMain mab = new MABMain();
	for (int exp = 0; exp < N_experiments; exp++) {
	    Agent agent = mab.new Agent();
	    int[] action_history = new int[N_episodes];
	    int[] reward_history = new int[N_episodes];
	    for (int episode = 0; episode < N_episodes; episode++) {
		// Choose action from agent (from current Q estimate)
		int action = agent.get_action();
		// Pick up reward from bandit for chosen action
		int reward = getReward(action);
		// Update Q action-value estimates
		agent.update_Q(action, reward);
		// Append to history
		action_history[episode] = action;
		reward_history[episode] = reward;
	    }

	    System.out.println("________________ Experiment " + exp + " --------------------");
	    System.out.println("episode\tbandit\tpercentage_times_chosen");

	    double[] action_counts = new double[BANDIT_PROB.length];
	    int episode = 0;
	    for (int action : action_history) {
		action_counts[action] = action_counts[action] + 1;
		episode++;
		if (episode % 1000 == 0) {
		    for (int bandit = 0; bandit < action_counts.length; bandit++)
			System.out.println(episode + "\t" + (bandit + 1) + "\t" + action_counts[bandit] / episode);

		}
	    }

	}
    }

}
