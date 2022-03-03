package orfanmine.util;

/**
 * The set of main taxonomic ranks/levels used in defining the lineage of a
 * taxon.<br>
 * These ranks are used to get/set the specific functionality represented by the
 * rank.
 */
public enum TaxRank {

	SPECIES("species"), GENUS("genus"), FAMILY("family"), ORDER("order"), CLASS("class"), PHYLUM("phylum"),
	KINGDOM("kingdom"), SUPERKINGDOM("superkingdom"), ROOT("root"), NO_RANK("no rank");

	/**
	 * The String representation of a taxonomic rank
	 */
	private final String name;

	/**
	 * Constructor
	 * 
	 * @param name The String representation of the new taxonomic rank
	 */
	TaxRank(String name) {
		this.name = name;
	}

	/**
	 * Getter
	 * 
	 * @return The String representation of this taxonomic rank
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns an Integer representation of this taxonomic rank. It is useful in
	 * comparing the level of the different ranks i.e., (GENUS << SPECIES) because
	 * in the tree of life the GENUS level is closer to the ROOT level than the
	 * SPECIES level
	 * 
	 * @return an Integer value representing the distance to the ROOT level in the
	 *         tree of life
	 */
	public int getLevel() {
		switch (this) {
		case ROOT:
			return 0;
		case SUPERKINGDOM:
			return 1;
		case KINGDOM:
			return 2;
		case PHYLUM:
			return 3;
		case CLASS:
			return 4;
		case ORDER:
			return 5;
		case FAMILY:
			return 6;
		case GENUS:
			return 7;
		case SPECIES:
			return 8;
		}
		return 9;// NO_RANK
	}

	/**
	 * Returns the Enumeration constant with the given name. Static method.
	 * 
	 * @param name The String representation of this taxonomic rank
	 * 
	 * @return the TaxRank constant with the given name. 'NO_RANK' by default: if
	 *         the @name is not identified.
	 */
	public static TaxRank identifyRank(String name) {
		switch (name) {
		case "species":
			return TaxRank.SPECIES;
		case "genus":
			return TaxRank.GENUS;
		case "family":
			return TaxRank.FAMILY;
		case "order":
			return TaxRank.ORDER;
		case "class":
			return TaxRank.CLASS;
		case "phylum":
			return TaxRank.PHYLUM;
		case "kingdom":
			return TaxRank.KINGDOM;
		case "superkingdom":
			return TaxRank.SUPERKINGDOM;
		case "root":
			return TaxRank.ROOT;
		default:
			return TaxRank.NO_RANK;
		}
	}

}
