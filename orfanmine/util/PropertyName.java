package orfanmine.util;

/**
 * The set of properties used in configuring the behaviour of the ORFanMine
 * application See the ORFanMine home folder /util/properties file for the
 * actual data
 */
public enum PropertyName {

	BaseDir("BaseDir"), WorkSpaceDir("WorkSpaceDir"), DatabaseDir("DatabaseDir"), ProtDB("ProtDB"),
	ProtDBHeaders("ProtDBHeaders"), ProtDBConfiguration("ProtDBConfiguration"), NuclDB("NuclDB"),
	NuclDBHeaders("NuclDBHeaders"), NuclDBConfiguration("NuclDBConfiguration"), UnclassifiedDB("UnclassifiedDB"),
	ClassifiedDB("ClassifiedDB"), TaxIDExtension("taxidmap"), ProtMinNrSeqsPartitionDB("ProtMinNrSeqsPartitionDB"),
	NuclMinNrSeqsPartitionDB("NuclMinNrSeqsPartitionDB"), MakeBlastDBScript("makeBlastDBScript"),
	QueryOperational("QueryOperational"), HeadersOperational("HeadersOperational"),
	DatabaseOperational("DatabaseOperational"), ResultsOperational("ResultsOperational"),
	ScriptOperational("ScriptOperational"), DBCurationKeyWords("DBCurationKeyWords"),
	NTAccession2TaxID("NTAccession2TaxID"), NRAccession2TaxID("NRAccession2TaxID"), TaxDBNodes("TaxDBNodes"),
	TaxDBNames("TaxDBNames"), TaxDBLineage("TaxDBLineage"), Gene2RefSeq("Gene2RefSeq"), ErrorLogFile("ErrorLogFile"),
	IdentityPercentage("IdentityPercentage"), BLAST_CMD("BLAST_CMD");

	/**
	 * The String representation of an ORFanMine property
	 */
	private final String name;

	/**
	 * Constructor
	 * 
	 * @param name the String representation of the new ORFanMine property
	 */
	PropertyName(String name) {
		this.name = name;
	}

	/**
	 * Getter: the String representation of this ORFanMine property
	 * 
	 * @return the String representation of this ORFanMine property
	 */
	public String getName() {
		return this.name;
	}

}
