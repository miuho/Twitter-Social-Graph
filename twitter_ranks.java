import java.io.*;
import java.util.*;

public class task3 {
	// hash lists twitter user u to all followers vs
	private static HashMap<String, HashSet<String>> u_vs = new HashMap<String, HashSet<String>>();
	// follower sizes
	private static HashMap<String, Float> u_vs_size = new HashMap<String, Float>();
	// hash lists follower v to all followed users us
	private static HashMap<String, HashSet<String>> v_us = new HashMap<String, HashSet<String>>();
	// all unique users
	private static HashSet<String> nodes = new HashSet<String>();
	// user rank scores
	private static HashMap<String, Float> ranks = new HashMap<String, Float>();
	// user contributions
	private static HashMap<String, Float> contributions = new HashMap<String, Float>();
	// globals
	private static Float d1 = new Float(0.85);
	private static Float d2 = new Float(0.15);

	
	public static void main(String[] args) {
		// import twitter data for edges u->vs
		try {
			BufferedReader br = new BufferedReader(new FileReader("/mnt/twitter-graph.txt"));

			String inputLine;
			int i = 0;

			while((inputLine=br.readLine()) != null) {

				String[] pair = inputLine.split(" ");

				if (pair.length != 2 ||
				    pair[0].length() == 0 ||
				    pair[1].length() == 0)
					continue;

				nodes.add(pair[0]);
				nodes.add(pair[1]);

				if (u_vs.containsKey(pair[0])) {
					u_vs.get(pair[0]).add(pair[1]);
				}

				else {
					HashSet<String> tmp = new HashSet<String>();

					u_vs.put(pair[0], tmp);	
					u_vs.get(pair[0]).add(pair[1]);
				}

				i += 1;

				if (i % 10000000 == 0)
					System.out.println(i);
			}

		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}

		for (String u : u_vs.keySet()) {	
			u_vs_size.put(u, new Float(u_vs.get(u).size()));
		}

		u_vs.clear();
		u_vs = null;

		System.out.println("load u_vs done");

		// import twitter data for edges v->us
		try {
			BufferedReader br = new BufferedReader(new FileReader("/mnt/twitter-graph.txt"));

			String inputLine;
			int i = 0;

			while((inputLine=br.readLine()) != null) {

				String[] pair = inputLine.split(" ");

				if (pair.length != 2 ||
				    pair[0].length() == 0 ||
				    pair[1].length() == 0)
					continue;

				if (v_us.containsKey(pair[1])) {
					v_us.get(pair[1]).add(pair[0]);
				}

				else {
					HashSet<String> tmp = new HashSet<String>();

					v_us.put(pair[1], tmp);	
					v_us.get(pair[1]).add(pair[0]);
				}

				i += 1;

				if (i % 10000000 == 0)
					System.out.println(i);
			}

		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}


		System.out.println("load v_us done");
		
		// initialize score for each unique node
		for (String n : nodes) {
			ranks.put(n, new Float(1.0));
		}

		System.out.println("rank init done");


		int it;
		for (it = 0; it < 10; it++) {
			Float dangling_contribution = new Float(0.0);

			// compute contribution for each node
			for (String n : nodes) {

				if (u_vs_size.containsKey(n)) {
					// normal
					contributions.put(n, new Float(ranks.get(n) / u_vs_size.get(n)));
				}
				else {
					// dangling
					contributions.put(n, new Float(ranks.get(n) / nodes.size()));
					dangling_contribution += contributions.get(n);
				}
			}

			System.out.println("contribution done");

			// reuse data structure
			ranks.clear();

			// add contribution for each node
			for (String n : nodes) {
				Float sum = new Float(0.0);

				if (v_us.containsKey(n)) {
					// get followers of each node
					HashSet<String> us = v_us.get(n);

					for (String u : us) {
						if (u_vs_size.containsKey(u))
							sum += contributions.get(u);
					}			
				}

				sum += dangling_contribution;
				sum *= d1;
				sum += d2;

				// finalize score for each node
				ranks.put(n, sum);
			}
			
			System.out.println("iteration done");

			contributions.clear();
		}

		//System.out.println(ranks.toString());
		
		try {
			// write ranks to file
			File statText = new File("/mnt/output");

			FileOutputStream is = new FileOutputStream(statText);
			OutputStreamWriter osw = new OutputStreamWriter(is);    
			Writer w = new BufferedWriter(osw);

			for (String key : ranks.keySet()) {	
				w.write(key + "\t" + ranks.get(key).toString() + "\n");
			}

			w.close();
		
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}
}


