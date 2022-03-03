package orfanmine.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * It represents a taxonomy node. The description for a node includes the
 * following fields: <br>
 * <ul>
 * <li>node ID in GenBank taxonomy database</li>
 * <li>parent node ID in GenBank taxonomy database</li>
 * <li>rank of this node (species, genus, ...)</li>
 * </ul>
 */
public class TaxNode implements Comparable<TaxNode> {

	/**
	 * The complete taxonomy ID lineage of the tree of life: the tree of node IDs.
	 * Static field.
	 */
	private static TreeMap<Integer, TaxNode> taxIdLineage = null;

	/**
	 * Node ID in GenBank taxonomy database
	 */
	private Integer id;

	/**
	 * Parent node in the taxonomy tree lineage
	 */
	private TaxNode parent;

	/**
	 * The list of children nodes in the taxonomy tree lineage
	 */
	private TreeSet<TaxNode> children;

	/**
	 * Rank of this node (species, genus, ...)
	 */
	private TaxRank rank;

	/**
	 * Taxonomy ID node constructor
	 * 
	 * @param id the node ID in GenBank taxonomy database
	 */
	public TaxNode(Integer id) {
		this.id = id;
		this.parent = null;
		children = new TreeSet<TaxNode>();
		this.rank = null;
	}

	/**
	 * Getter: the node ID in GenBank taxonomy database
	 * 
	 * @return the node ID in GenBank taxonomy database
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Getter: the parent node in the taxonomy tree lineage
	 * 
	 * @return the parent node in the taxonomy tree lineage
	 */
	public TaxNode getParent() {
		return parent;
	}

	/**
	 * Setter of the parent node in the taxonomy tree lineage
	 * 
	 * @param parent the parent node in the taxonomy tree lineage
	 */
	public void setParent(TaxNode parent) {
		this.parent = parent;
	}

	/**
	 * Getter: the list of children nodes in the taxonomy tree lineage
	 * 
	 * @return the list of children nodes in the taxonomy tree lineage
	 */
	public TreeSet<TaxNode> getChildren() {
		return children;
	}

	/**
	 * Attach a child node to current node
	 * 
	 * @param child the child node from the taxonomy tree lineage
	 */
	public void addChild(TaxNode child) {
		children.add(child);
	}

	/**
	 * Getter: the rank of this node ({@link TaxRank#SPECIES},
	 * {@link TaxRank#GENUS}, ...)
	 * 
	 * @return the rank of this node (species, genus, ...)
	 */
	public TaxRank getRank() {
		return rank;
	}

	/**
	 * Setter
	 * 
	 * @param rank the rank of this node (species, genus, ...)
	 */
	public void setRank(TaxRank rank) {
		this.rank = rank;
	}

	/**
	 * Generates the String serialization of this node
	 * 
	 * @return the String representation of this node
	 */
	public String toString() {
		String printOut = id + "\t" + parent.getId() + "\t" + rank + "\t" + children.size() + "\n";
		for (TaxNode child : children)
			printOut += "CHILD: \n" + child.toString();
		return printOut;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	/**
	 * Indicates whether some other taxonomy node is "equal to" this one
	 * 
	 * @param node the reference taxonomy node with which to compare
	 * 
	 * @return true if this object is the same as the node argument; false otherwise
	 */
	public boolean equals(TaxNode node) {
		if (this == node)
			return true;
		if (node == null)
			return false;
		if (getClass() != node.getClass())
			return false;
		return id == node.id;
	}

	/**
	 * Compares this node with the specified node for order. Returns a negative
	 * integer, zero, or a positive integer as this node is less than, equal to, or
	 * greater than the specified node.
	 * 
	 * @param node the reference taxonomy node with which to compare
	 * 
	 * @return a negative integer, zero, or a positive integer as this node is less
	 *         than, equal to, or greater than the specified node
	 * 
	 * @throws NullPointerException - if the specified node is null
	 *                              ClassCastException - if the specified node's
	 *                              type prevents it from being compared to this
	 *                              node
	 */
	public int compareTo(TaxNode node) {
		return this.id.compareTo(node.getId());
	}

	/**
	 * Returns the complete taxonomy ID lineage of the tree of life: the tree of
	 * node IDs. Static method.
	 * 
	 * @return the taxonomy ID lineage
	 */
	public static TreeMap<Integer, TaxNode> getTaxIdLineage() {
		if (taxIdLineage == null)
			return loadTreeNodes();
		return taxIdLineage;
	}

	/**
	 * Loads the complete taxonomy ID lineage of the tree of life: the tree of node
	 * IDs provided by the GenBank 'nodes.dmp' file. Static method.
	 * 
	 * @return the taxonomy ID lineage
	 */
	private static TreeMap<Integer, TaxNode> loadTreeNodes() {

		taxIdLineage = new TreeMap<Integer, TaxNode>();

		BufferedReader br = ORFanMineUtils.openReader(ORFanMineUtils.getTaxDBNodesFilePath());

		long count = 0;
		try {
			String contentLine = br.readLine();
			while (contentLine != null) {
				count++;
//				if (count % 1000000 == 0)
//					System.out.println("MILLION: " + count / 1000000);
				// [0] = tax_id
				// [1] = parent_tax_id
				// [2] = rank
				String[] strArray = contentLine.split(" ");
				Integer taxID = Integer.valueOf(strArray[0]);
				Integer parentTaxID = Integer.valueOf(strArray[1]);
				String rank = strArray[2];
				TaxNode node = null;
				if (taxIdLineage.containsKey(taxID)) {
					node = taxIdLineage.get(taxID);
				} else {
					node = new TaxNode(taxID);
					taxIdLineage.put(taxID, node);
				}
				node.setRank(TaxRank.identifyRank(rank));

				TaxNode parentNode = null;
				if (taxIdLineage.containsKey(parentTaxID)) {
					parentNode = taxIdLineage.get(parentTaxID);
				} else {
					parentNode = new TaxNode(parentTaxID);
					taxIdLineage.put(parentTaxID, parentNode);
				}
				if (node.getParent() == null)
					node.setParent(parentNode);
				if (!node.equals(parentNode))
					parentNode.addChild(node);

				contentLine = br.readLine();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			ORFanMineUtils.closeReader(br);
		}
//		System.out.println("Total Nr of Nodes: " + count + "...");
		return taxIdLineage;
	}

	/**
	 * Identifies the species taxonomy ID to which the @param taxID sequence belongs
	 * to. Static method.
	 * 
	 * @param taxID the taxonomy ID of the target sequence
	 * 
	 * @return the species taxonomy ID; 'null' if the @param taxID does not exist in
	 *         the lineage or it is a higher rank than SPECIES
	 */
	public static Integer findSpecies(Integer taxID) {
		if (taxIdLineage == null)
			loadTreeNodes();
		TaxNode node = taxIdLineage.get(taxID);
		if (node == null) {
//			System.out.println(taxID + " is unknown");
			return null;
		}
		if (node.getRank() == TaxRank.SPECIES)
			return node.getId();
		else {
			if (node.getRank().getLevel() < TaxRank.SPECIES.getLevel()) {
//				System.out.println(taxID + " is not a SPECIES taxon, but a " + node.getRank().getName() + " taxon");
				return null;
			}
			return findSpecies(node.getParent().getId());
		}
	}

	/**
	 * Identifies the species node to which the @param taxID sequence belongs to
	 * Static method.
	 * 
	 * @param taxID the taxonomy ID of the target sequence
	 * 
	 * @return the species node; 'null' if the @param taxID does not exist in the
	 *         lineage
	 */
	public static TaxNode findSpeciesNode(Integer taxID) {
		Integer taxon = findSpecies(taxID);
		if (taxon == null)
			return null;
		return taxIdLineage.get(taxon);
	}

	/**
	 * Builds the complete list of taxonomy ID lineage of the @param taxID sequence
	 * Static method.
	 * 
	 * @param taxID the taxonomy ID of the target sequence
	 * 
	 * @return the complete list of taxonomy ID lineage; 'null' if the @param taxID
	 *         does not exist in the lineage
	 */
	public static ArrayList<Integer> findSpeciesLineage(Integer taxID) {
		TaxNode node = findSpeciesNode(taxID);
		if (node == null)
			return null;
		ArrayList<Integer> lineage = new ArrayList<Integer>();
		while (node != node.getParent()) {
			node = node.getParent();
			lineage.add(node.getId());
		}
		return lineage;
	}

	/**
	 * Builds the list of taxonomy ID lineage (filtering only the rank names
	 * validated by the @orfanmine.util.TaxRank enumeration) of the @param taxID
	 * sequence Static method.
	 * 
	 * @param taxID the taxonomy ID of the target sequence
	 * 
	 * @return the filtered list of taxonomy ID lineage; 'null' if the @param taxID
	 *         does not exist in the lineage
	 */
	public static ArrayList<Integer> findSpeciesLineageByLevel(Integer taxID) {
		return findSpeciesLineageByLevel(taxID, TaxRank.ROOT);
	}

	/**
	 * Builds the partial list of taxonomy ID lineage (filtering only the rank names
	 * validated by the @orfanmine.util.TaxRank enumeration) of the @param taxID
	 * sequence limiting to the @param stopRank level. Static method.
	 * 
	 * @param taxID    the taxonomy ID of the target sequence
	 * 
	 * @param stopRank the rank level where to stop mining the lineage
	 * 
	 * @return the filtered list of taxonomy ID lineage; 'null' if the @param taxID
	 *         does not exist in the lineage
	 */
	public static ArrayList<Integer> findSpeciesLineageByLevel(Integer taxID, TaxRank stopRank) {
		TaxNode node = findSpeciesNode(taxID);
		if (node == null)
			return null;
		ArrayList<Integer> lineage = new ArrayList<Integer>();
		while (node != node.getParent()) {
			node = node.getParent();
			if (node.getRank().getLevel() <= TaxRank.GENUS.getLevel())
				lineage.add(node.getId());
			if (node.getRank() == stopRank)
				break;
		}
		return lineage;
	}

	/**
	 * Returns the parent node's taxonomy ID of the @param taxID at the
	 * lineage @param rank Static method.
	 * 
	 * @param taxID the taxonomy ID of the target sequence
	 * 
	 * @param rank  the rank target level
	 * 
	 * @return the parent node's taxonomy ID at level @param rank
	 */
	public static Integer identifyLineageSpecificRank(Integer taxID, TaxRank rank) {
		TaxNode node = findSpeciesNode(taxID);
		if (node == null)
			return null;
		if (node.getRank() == rank)
			return node.id;
		while (node != node.getParent()) {
			node = node.getParent();
			if (node.getRank() == rank)
				return node.id;
		}
		return null;
	}

	/**
	 * Returns a statistic of the number of nodes in GenBank for each rank.
	 */
	public static void checkRanks() {
		BufferedReader br = ORFanMineUtils.openReader(ORFanMineUtils.getTaxDBNodesFilePath());

		TreeMap<String, Integer> ranks = new TreeMap<String, Integer>();
		long count = 0;
		try {
			String contentLine = br.readLine();
			while (contentLine != null) {
				count++;
				if (count % 1000000 == 0)
					System.out.println("MILLION: " + count / 1000000);
				// [0] = tax_id
				// [1] = parent_tax_id
				// [2] = rank
				String[] strArray = contentLine.split(" ");
				String rank = strArray[2];
				if (ranks.containsKey(rank)) {
					Integer nrInstances = ranks.get(rank);
					nrInstances++;
					ranks.put(rank, nrInstances);
				} else
					ranks.put(rank, 1);
				contentLine = br.readLine();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			ORFanMineUtils.closeReader(br);
			Set set = ranks.entrySet();
			Iterator it = set.iterator();
			while (it.hasNext()) {
				Map.Entry me = (Map.Entry) it.next();
				System.out.println(me.getKey() + " = " + me.getValue());
			}
			System.out.println("Count: " + count);
		}
// Statistic from 11/11/2021
//		biotype = 17
//		clade = 899
//		class = 447
//		cohort = 5
//		family = 9640
//		forma = 1331
//		genotype = 20
//		genus = 100419
//		infraclass = 18
//		infraorder = 130
//		isolate = 1321
//		kingdom = 13
//		morph = 12
//		no = 227262
//		order = 1698
//		parvorder = 26
//		pathogroup = 5
//		phylum = 289
//		root = 1
//		section = 476
//		series = 9
//		serogroup = 140
//		serotype = 1240
//		species = 1927747
//		strain = 44816
//		subclass = 163
//		subcohort = 3
//		subfamily = 3130
//		subgenus = 1706
//		subkingdom = 1
//		suborder = 374
//		subphylum = 32
//		subsection = 21
//		subspecies = 25837
//		subtribe = 574
//		superclass = 6
//		superfamily = 866
//		superkingdom = 4
//		superorder = 54
//		superphylum = 1
//		tribe = 2266
//		varietas = 8805
//		Count: 2361824
	}

}
