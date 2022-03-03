package orfanmine.util;

/*
 * The set of GenBank's database molecule types: 'nucl' (nucleotide) && 'prot' (protein)
 */
public enum DBType {

	NUCL("nucl"), PROT("prot");

	/*
	 * The String representation of a molecule type
	 */
	private final String name;

	/*
	 * Constructor
	 * 
	 * @param name The String representation of the molecule type
	 */
	DBType(String name) {
		this.name = name;
	}

	/*
	 * Getter
	 * 
	 * @return The String representation of this molecule type
	 */
	public String getName() {
		return this.name;
	}

	/*
	 * Provides the proper BLAST program to use for this molecule type:
	 * 
	 * @NUCL molecule type: BLASTN when comparing one or more nucleotide query
	 * sequences to a subject nucleotide sequence or a database of nucleotide
	 * sequences
	 * 
	 * @PROT molecule type: BLASTP when comparing one or more protein query
	 * sequences to a subject protein sequence or a database of protein sequences
	 */
	public String blastCmd() {
		if (name == DBType.NUCL.getName())
			return "blastn";
		else
			return "blastp";
	}

	/*
	 * Identifies the database enumeration type with the name @param name
	 * 
	 * @param name the name of the target molecule type
	 * 
	 * @return the database enumeration type: DBType.NUCL || DBType.PROT; 'null' if
	 * the name is not recognised
	 */
	public static DBType getDBTypeArg(String name) {
		if (name.equals(DBType.NUCL.getName()))
			return DBType.NUCL;
		if (name.equals(DBType.PROT.getName()))
			return DBType.PROT;
		return null;
	}
}
