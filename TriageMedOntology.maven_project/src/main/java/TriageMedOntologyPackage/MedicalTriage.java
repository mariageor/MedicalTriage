package TriageMedOntologyPackage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.rio.RDFFormat; 
import org.eclipse.rdf4j.rio.RDFParseException; 
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

/**
 * This application represents the proccess of medical TEWS triage.
 * At first, it loads an ontology and the rdf statetements of an instance
 * into a GraphDB repository. Then, it reads a json file (PatientsData.json)
 * containing the data of 15 new patients/victims and converts them into rdf statements. 
 * Lastly, the reasoning is conducted through various SPARQL queries and the results
 * are also converted into rdf statements and added to the GraphDB repository.
 * @author Maria-Georgia Georgiadou
 * @version 1.0
 * @since 2023-04-10
 *
 */
public class MedicalTriage 
{
	private RepositoryConnection connection;
	private String namespace;
	
	
	/**
	 * Constructor of the class MedicalTriage
	 * @parameter connection
	 */
	public MedicalTriage(RepositoryConnection connection)
	{
		this.connection = connection;
		this.namespace = "http://MedOntology.project.rdfs/TEWStriage#";
	}
	
	
	/**
	 * This function loads the ontology from the TEWStriage.ttl file and adds
	 * it to the connection that has been made with the repository in GraphDB.
	 * @throws RDFParseException
	 * @throws RepositoryException
	 * @throws IOException
	 */
	public void loadOntology() throws RDFParseException, RepositoryException, IOException 
	{
		System.out.println("Loading the ontology...\n");
    	
		this.connection.begin();
		
		// Getting the turtle file of the ontology and adding it in the connection which was created
		this.connection.add(MedicalTriage.class.getResourceAsStream("/TEWStriage.ttl"), "urn:base", RDFFormat.TURTLE);
	}
	
	
	/**
	 * This function is loading new instances to the ontology from the PatientsData.json file.
	 * In addition, it creates a model with all of the new rdf triples, which is added in  the 
	 * connection to the GraphDB repository, conducting at the same time all the necessary checks.
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public void loadInstances() throws IOException, URISyntaxException
	{
		System.out.println("Loading new instances...\n");
		
		// Reading the json file, which includes patients data
		try(JsonReader reader = new JsonReader(new FileReader("C://PatientsData.json")))
		{
			JsonElement jsonElement = new JsonParser().parse(reader);
			
			// Creating an RDF model with the base URI of the ontology
			ModelBuilder builder = new ModelBuilder();
			builder.setNamespace("TEWStriage", this.namespace);
			
			// Generating an instance of RDF value 
			SimpleValueFactory factory = SimpleValueFactory.getInstance();
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			
			// Getting the json data as a json array
			JsonArray jArray = jsonObject.getAsJsonArray("patientsData");
			
			// Accessing every json object of the json array
			for (int i = 0; i < jArray.size(); i++) 
			{
				JsonObject patient = jArray.get(i).getAsJsonObject();
			    
			    IRI patientsIRI = factory.createIRI(namespace, "p" + (i+1));
			    
			    // Accessing the name element of every patient
				JsonElement nameElement = patient.get("patientsName");
				String name = nameElement.getAsString();
				System.out.println("Patient"+(i+1)+": " + name);
				    
				// Accessing the deadPatient element of every patient
				JsonElement deadPatient = patient.get("deadPatient");
				Boolean dead = deadPatient.getAsBoolean();
				
				JsonElement sexOfPatient = patient.get("type");
				
				JsonElement age = patient.get("ageOfPatient");
				    
				if (!dead)
				{	
					// Handling the case of an alive patient and everything that is included
					IRI vsIRI = factory.createIRI(namespace, "VSp" + (i+1)); 
				    IRI AVPUstateIRI = factory.createIRI(namespace, "AVPUstate" + (i+1));
					
					JsonObject AVPUdata = patient.get("AVPUstateOfPatient").getAsJsonObject();
					JsonElement AVPUstate = AVPUdata.get("type"); 
					
					JsonElement tewsScore = patient.get("TEWSscore"); 
					    
					JsonObject VSdata = patient.get("VitalSignsOfPatient").getAsJsonObject();
					JsonElement VStype = VSdata.get("type"); 
					JsonElement HR = VSdata.get("HR");
					JsonElement RR = VSdata.get("RR");
					JsonElement SBP = VSdata.get("SBP");
					JsonElement temp = VSdata.get("temperature");
					    
					JsonElement ableToWalk = patient.get("abilityOfMobility"); 
					    
					JsonElement trauma = patient.get("existenceOfTrauma");
					    
					JsonElement needsHelpToWalk = patient.get("needsHelpToWalk");
					    
					JsonElement stretcher = patient.get("stretcherNeededOrImmobilePatient");
					   
					// Creating and adding all the new triples to the model builder
					builder.subject(AVPUstateIRI).add(RDF.TYPE, factory.createIRI(namespace,AVPUstate.getAsString()));
					
					builder.subject(vsIRI).add(RDF.TYPE, factory.createIRI(namespace,VStype.getAsString()))
							.add("TEWStriage:HR", factory.createLiteral(HR.getAsInt()))
							.add("TEWStriage:RR", factory.createLiteral(RR.getAsInt()))
							.add("TEWStriage:SBP", factory.createLiteral(SBP.getAsInt()))
							.add("TEWStriage:temperature", factory.createLiteral(temp.getAsFloat()));
					
					builder.subject(patientsIRI).add(RDF.TYPE, factory.createIRI("TEWStriage:Patient"))
							.add(RDF.TYPE, factory.createIRI(namespace, sexOfPatient.getAsString()))
							.add("TEWStriage:deadPatient", factory.createLiteral(deadPatient.getAsBoolean()))
							.add("TEWStriage:patientsName", factory.createLiteral(nameElement.getAsString()))
							.add("TEWStriage:AVPUstateOfPatient", AVPUstateIRI)
							.add("TEWStriage:vitalSignsOfPatient", vsIRI) 
							.add("TEWStriage:TEWSscore", factory.createLiteral(tewsScore.getAsInt()))
							.add("TEWStriage:abilityOfMobility", factory.createLiteral(ableToWalk.getAsBoolean()))
							.add("TEWStriage:existenceOfTrauma", factory.createLiteral(trauma.getAsBoolean()))
							.add("TEWStriage:ageOfPatient", factory.createLiteral(age.getAsInt()))
							.add("TEWStriage:needsHelpToWalk", factory.createLiteral(needsHelpToWalk.getAsBoolean()))
							.add("TEWStriage:stretcherNeededOrImmobilePatient", factory.createLiteral(stretcher.getAsBoolean()));
				}
				else
				{
					// Handling the case of a dead patient
					builder.subject(patientsIRI).add(RDF.TYPE, factory.createIRI("TEWStriage:Patient"))
							.add(RDF.TYPE, factory.createIRI(namespace,sexOfPatient.getAsString()))
							.add("TEWStriage:deadPatient", factory.createLiteral(deadPatient.getAsBoolean()))
							.add("TEWStriage:patientsName", factory.createLiteral(nameElement.getAsString()))
							.add("TEWStriage:ageOfPatient", factory.createLiteral(age.getAsInt()));
				}
				
			}
			
			// Constructing the new model and adding it to the connection with the GraphDB repository
			Model model = builder.build();
			this.connection.add(model);
		    
			// Printing statements for debugging
			/**for(Statement st: model) 
			{
				System.out.println(st);
			}*/
			
			
			// Saving all the output in the PatientsTriplets.owl file
			File file = new File("./PatientsTriplets.owl");
			FileOutputStream out = new FileOutputStream(file);
			try {
				Rio.write(model, out, RDFFormat.RDFXML);
			}
			finally 
			{
				out.close();
			}
			
			// This is needed only if you want to find the exact location of the file created above
			/**String currentWorkingDirectory = System.getProperty("user.dir");
			System.out.println("Current working directory: " + currentWorkingDirectory);*/
		}
		catch (Exception e) 
		{
			// General exception handler
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * This function provides the number of the dead 
	 * patients/victims through the appropriate SPARQL query.
	 */
	public void SPARQLnumberOfDeathsQuery()
	{
		System.out.print("\nNumber of deaths: ");
		String queryString = "PREFIX TEWStriage: <http://MedOntology.project.rdfs/TEWStriage#>";
		queryString += "SELECT (COUNT(?p) AS ?deaths)\n";
		queryString += "WHERE\n";
		queryString += "{\n";
		queryString += "?p a TEWStriage:Patient.\n";
		queryString += "?p TEWStriage:deadPatient true.\n";
		queryString += "}\n";
		
		TupleQuery query = this.connection.prepareTupleQuery(queryString);
		
		TupleQueryResult result = query.evaluate();
		while (result.hasNext()) 
		{
			BindingSet bindingSet = result.next();
			
			Literal deathsLiteral = (Literal) bindingSet.getBinding("deaths").getValue();
			int deathsCount = deathsLiteral.intValue();
			System.out.println(deathsCount);
				
		}
		result.close();
		
	}
	
	
	/**
	 * This function is using the appropriate SPARQL query to provide and print out 
	 * the total number of patients, that belong in each TEWS colour code. 
	 */
	public void SPARQLtewsColourCodesQuery()
	{
		List<String> colours = new ArrayList<String>();
		colours.add("Blue");
		colours.add("Green");
		colours.add("Yellow");
		colours.add("Orange");
		colours.add("Red");
		
		System.out.println("\nThe number of each colour code is:");
		
		Iterator<String> iterator = colours.iterator();
	    while(iterator.hasNext()) 
	    {
	    	String nameOfCode = iterator.next();
	    	System.out.print("Number of "+nameOfCode+" codes: ");

			String queryString = "PREFIX TEWStriage: <http://MedOntology.project.rdfs/TEWStriage#>";
	    	
	    	queryString += "SELECT (COUNT(?p) AS ?"+nameOfCode+") \n";
	    	queryString += "WHERE\n";
	    	queryString += "{\n";
	    	queryString += "	?p a TEWStriage:Patient.\n";
	    	queryString += "	?p TEWStriage:TEWScodeOfPatient ?c.\n";
	    	queryString += "	?c a TEWStriage:"+nameOfCode+".\n";
	    	queryString += "}\n";
	    	
	    	TupleQuery query = this.connection.prepareTupleQuery(queryString);
			
			TupleQueryResult result = query.evaluate();
			while (result.hasNext()) 
			{
				BindingSet bindingSet = result.next();
				
				Literal colourLiteral = (Literal) bindingSet.getBinding(nameOfCode).getValue();
				int colourCount = colourLiteral.intValue();
				System.out.println(colourCount);
					
			}
			result.close();
	    }
	}
	
	
	/**
	 * This function provides the number of the stretchers that are needed 
	 * in the field of the mass disaster, in order to help the immobile patients,
	 * through the appropriate SPARQL query.
	 */
	public void SPARQLstretcherNeededQuery()
	{
		System.out.print("\nThe number of strechers needed is: ");
		String queryString = "PREFIX TEWStriage: <http://MedOntology.project.rdfs/TEWStriage#>";
		queryString += "SELECT (COUNT(?p) AS ?stretchers)\n";
		queryString += "WHERE\n";
		queryString += "{\n";
		queryString += "?p a TEWStriage:Patient.\n";
		queryString += "?p TEWStriage:stretcherNeededOrImmobilePatient true.\n";
		queryString += "}\n";
		
		TupleQuery query = this.connection.prepareTupleQuery(queryString);
		
		TupleQueryResult result = query.evaluate();
		while (result.hasNext()) 
		{
			BindingSet bindingSet = result.next();
			
			Literal stretchersLiteral = (Literal) bindingSet.getBinding("stretchers").getValue();
			int stretchersCount = stretchersLiteral.intValue();
			System.out.println(stretchersCount);
				
		}
		result.close();
		
	}
	
	
	/**
	 * In this function are constructed all the rdf triples about the TEWS codes 
	 * of the patinets, through a SPARQL query and all the other necessary actions. 
	 */
	public void constructionOfTEWScolourSPARQLqueries()
	{
		System.out.println("\nReasoning...");
		
		// Creating an RDF model with the base URI of the ontology
		ModelBuilder builder = new ModelBuilder();
		builder.setNamespace("TEWStriage", this.namespace);
					
		// Generating an instance of RDF value 
		SimpleValueFactory factory = SimpleValueFactory.getInstance();
		
		String queryString = "PREFIX TEWStriage: <http://MedOntology.project.rdfs/TEWStriage#>";
		queryString += "SELECT ?p\n";
		queryString += "WHERE\n";
		queryString += "{\n";
		queryString += "    ?p a TEWStriage:Patient .\n";
		queryString += "	?p TEWStriage:deadPatient true.\n";
		queryString += "}";
		
		TupleQuery query = this.connection.prepareTupleQuery(queryString);
		
		TupleQueryResult result = query.evaluate();
		while (result.hasNext()) 
		{
			BindingSet bindingSet = result.next();
			
			IRI patientIRI = (IRI) bindingSet.getBinding("p").getValue();
			String patientID = patientIRI.stringValue().substring(43);

			
			IRI TEWScodeIRI = factory.createIRI(namespace, "Blue" + patientID);
			
			builder.subject(TEWScodeIRI).add(RDF.TYPE, factory.createIRI(namespace, "Blue"));
			builder.subject(patientIRI).add("TEWStriage:TEWScodeOfPatient", TEWScodeIRI);
				
		}
		
		Model model = builder.build();
		this.connection.add(model);
		
		result.close();
		
		ColourCodes(builder, factory, model, "Green", 0, 2);
		ColourCodes(builder, factory, model, "Yellow", 3, 4);
		ColourCodes(builder, factory, model, "Orange", 5, 6);
		ColourCodes(builder, factory, model, "Red", 7, 100);
	}
	
	
	/**
	 * This function is used to check in which TEWS colour 
	 * code every patient belongs, based to it's TEWS score.
	 * @param builder the model builder 
	 * @param factory the SimpleValueFactory object that creates the IRIs
	 * @param model the model object
	 * @param colour the string that indicates the TEWS colour code 
	 * @param l1 the first limit of the mathematical expression that is used
	 * @param l2 the second limit of the mathematical expression that is used
	 */
	public void ColourCodes(ModelBuilder builder, SimpleValueFactory factory, Model model, String colour, Integer l1, Integer l2)
	{
		String queryString = "PREFIX TEWStriage: <http://MedOntology.project.rdfs/TEWStriage#>\n";
		queryString += "SELECT ?p\n";
		queryString += "WHERE\n";
		queryString += "{\n";
		queryString += "    ?p a TEWStriage:Patient .\n";
		queryString += "    ?p TEWStriage:TEWSscore ?s.\n";
		queryString += "    FILTER(?s>="+ l1 +"&& ?s<="+ l2 +")\n";
		queryString += "}";
		
		TupleQuery query = this.connection.prepareTupleQuery(queryString);
		
		TupleQueryResult result = query.evaluate();
		while (result.hasNext()) 
		{
			BindingSet bindingSet = result.next();
			
			IRI patientIRI = (IRI) bindingSet.getBinding("p").getValue();
			String patientID = patientIRI.stringValue().substring(43);

			IRI TEWScodeIRI = factory.createIRI(namespace, colour + patientID);
			builder.subject(TEWScodeIRI).add(RDF.TYPE, factory.createIRI(namespace, colour));
			builder.subject(patientIRI).add("TEWStriage:TEWScodeOfPatient", TEWScodeIRI);			
		}
		
		model = builder.build();
		this.connection.add(model);
		
		result.close();
	}
	
	
	/**
	 * This function calls all the other necessary functions to calculate the total 
	 * TEWS score of every patient through successive SPARQL queries.
	 */
	public void TEWSscoreCalculationSPARQLqueries()
	{
		// Creating an RDF model with the base URI of the ontology
		ModelBuilder builder = new ModelBuilder();
		builder.setNamespace("TEWStriage", this.namespace);
							
		// Generating an instance of RDF value 
		SimpleValueFactory factory = SimpleValueFactory.getInstance();
		
		// SPARQL queries about the AVPU state of patient
		scoreCalculationForAVPU(builder, factory, "Verbal", 1);
		scoreCalculationForAVPU(builder, factory, "Pain", 2);
		scoreCalculationForAVPU(builder, factory, "Unresponsive", 3);
		
		// SPARQL queries about the existence of trauma or the ability of mobility of each patient
		scoreCalculationsForMobilityAndTrauma(builder, factory, "stretcherNeededOrImmobilePatient", 2);
		scoreCalculationsForMobilityAndTrauma(builder, factory, "needsHelpToWalk", 1);
		scoreCalculationsForMobilityAndTrauma(builder, factory, "existenceOfTrauma", 1);
		
		//SPARQL queries for the value of every vital sign of each patient
		scoreCalculationForVS(builder, factory, "SBP", 1, "?x>=81 && ?x<=100");
		scoreCalculationForVS(builder, factory, "SBP", 2, "?x>=71 && ?x<=80");
		scoreCalculationForVS(builder, factory, "SBP", 2, "?x>199");
		scoreCalculationForVS(builder, factory, "SBP", 3, "?x<71");
		
		scoreCalculationForVS(builder, factory, "temperature", 2, "?x<35");
		scoreCalculationForVS(builder, factory, "temperature", 2, "?x>=38.5");

		scoreCalculationForVS(builder, factory, "HR", 1, "?x>=41 && ?x<=50");
		scoreCalculationForVS(builder, factory, "HR", 1, "?x>=101 && ?x<=110");
		scoreCalculationForVS(builder, factory, "HR", 2, "?x<41");
		scoreCalculationForVS(builder, factory, "HR", 2,"?x>=111 && ?x<=129");
		scoreCalculationForVS(builder, factory, "HR", 3, "?x>129");
		
		scoreCalculationForVS(builder, factory, "RR", 1, "?x>=15 && ?x<=20");
		scoreCalculationForVS(builder, factory, "RR", 2, "?x<9");
		scoreCalculationForVS(builder, factory, "RR", 2, "?x>=21 && ?x<=29");
		scoreCalculationForVS(builder, factory, "RR", 3, "?x>29");
	}
	
	
	/**
	 * This function is adding the proper number to the TEWS score of every patient, 
	 * based on the values of his vital signs. 
	 * @param builder the model builder
	 * @param factory the SimpleValueFactory object that creates the IRIs
	 * @param type the string that indicates the specific vital sign of the query
	 * @param num the integer that should be added to the score, if the condition is met
	 * @param condition the string with the mathematical expression that must be met 
	 */
	public void scoreCalculationForVS(ModelBuilder builder, SimpleValueFactory factory, String type, int num, String condition )
	{
		String queryString = "PREFIX TEWStriage: <http://MedOntology.project.rdfs/TEWStriage#>\n";
		queryString += "SELECT ?p ?s\n";
		queryString += "WHERE\n";
		queryString += "{\n";
		queryString += "    ?p a TEWStriage:Patient .\n";
		queryString += "    ?p TEWStriage:TEWSscore ?s.\n";
		queryString += "	?p TEWStriage:vitalSignsOfPatient ?vs .";
		queryString += "    ?vs TEWStriage:"+ type +" ?x .\n";
		queryString += "    FILTER("+ condition +") \n";
		queryString += "}";
		
		TupleQuery query = this.connection.prepareTupleQuery(queryString);
		TupleQueryResult result = query.evaluate();
		
		while (result.hasNext()) 
		{
			BindingSet bindingSet = result.next();
			
			IRI patientIRI = (IRI) bindingSet.getBinding("p").getValue();
			Literal scoreLiteral = (Literal) bindingSet.getBinding("s").getValue();
			int previousScore = scoreLiteral.intValue();
			
			this.connection.remove(patientIRI, factory.createIRI("http://MedOntology.project.rdfs/TEWStriage#TEWSscore"), scoreLiteral);
			this.connection
				.add(patientIRI, factory.createIRI("http://MedOntology.project.rdfs/TEWStriage#TEWSscore"), factory.createLiteral(previousScore+num));
		}
		
		result.close();
	}
	
	
	/**
	 * This function is adding the proper number to the TEWS score of every patient, 
	 * based on the values of his AVPU (Awake, Verbal, Pain, Unresponsive) state. 
	 * @param builder the model builder
	 * @param factory the SimpleValueFactory object that creates the IRIs
	 * @param type the string that indicates the specific AVPU state of the query
	 * @param num the integer that should be added to the score, if the condition is met
	 */ 
	void scoreCalculationForAVPU(ModelBuilder builder, SimpleValueFactory factory, String type, int num )
	{
		String queryString = "PREFIX TEWStriage: <http://MedOntology.project.rdfs/TEWStriage#>\n";
		queryString += "SELECT ?p ?s\n";
		queryString += "WHERE\n";
		queryString += "{\n";
		queryString += "    ?p a TEWStriage:Patient .\n";
		queryString += "    ?p TEWStriage:TEWSscore ?s.\n";
		queryString += "    ?p TEWStriage:AVPUstateOfPatient ?avpu .\n";
		queryString += "    ?avpu a TEWStriage:"+ type +" .\n";
		queryString += "}";
		
		TupleQuery query = this.connection.prepareTupleQuery(queryString);
		TupleQueryResult result = query.evaluate();
		
		while (result.hasNext()) 
		{
			BindingSet bindingSet = result.next();
			
			IRI patientIRI = (IRI) bindingSet.getBinding("p").getValue();
			Literal scoreLiteral = (Literal) bindingSet.getBinding("s").getValue();
			int previousScore = scoreLiteral.intValue();
			
			this.connection.remove(patientIRI, factory.createIRI("http://MedOntology.project.rdfs/TEWStriage#TEWSscore"), scoreLiteral);
			this.connection
				.add(patientIRI, factory.createIRI("http://MedOntology.project.rdfs/TEWStriage#TEWSscore"), factory.createLiteral(previousScore+num));
		}
		
		result.close();
	}
	
	
	/**
	 * This function is adding the proper number to the TEWS score of every patient, 
	 * based on his state of mobility and the existence of trauma or not. 
	 * @param builder the model builder
	 * @param factory the SimpleValueFactory object that creates the IRIs
	 * @param type the string that indicates the specific mobility state of the patient or the existence of trauma
	 * @param num the integer that should be added to the score, if the condition is met
	 */
	public void scoreCalculationsForMobilityAndTrauma(ModelBuilder builder, SimpleValueFactory factory, String type, int num)
	{
		String queryString = "PREFIX TEWStriage: <http://MedOntology.project.rdfs/TEWStriage#>\n";
		queryString += "SELECT ?p ?s\n";
		queryString += "WHERE\n";
		queryString += "{\n";
		queryString += "    ?p a TEWStriage:Patient .\n";
		queryString += "    ?p TEWStriage:TEWSscore ?s.\n";
		queryString += "    ?p TEWStriage:"+ type +" ?r .\n";
		queryString += "    FILTER(?r=true) \n";
		queryString += "}";
		
		TupleQuery query  = this.connection.prepareTupleQuery(queryString);
		TupleQueryResult result = query.evaluate();
		
		while (result.hasNext()) 
		{
			BindingSet bindingSet = result.next();
			
			IRI patientIRI = (IRI) bindingSet.getBinding("p").getValue();
			Literal scoreLiteral = (Literal) bindingSet.getBinding("s").getValue();
			int previousScore = scoreLiteral.intValue();
			
			this.connection.remove(patientIRI, factory.createIRI("http://MedOntology.project.rdfs/TEWStriage#TEWSscore"), scoreLiteral);
			this.connection
				.add(patientIRI, factory.createIRI("http://MedOntology.project.rdfs/TEWStriage#TEWSscore"), factory.createLiteral(previousScore+num));
		}
		
		result.close();
	}
	
	
	/**
	 * The main function of the programm that calls all the other necessary functions in the proper order, 
	 * to recreate the proccess of the TEWS triage. More specifically, this includes the TEWS score calculation
	 * and the seperation of the patients into the TEWS colour code classes that they belong.
	 * @throws RDFParseException
	 * @throws UnsupportedRDFormatException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static void main(String[] args) throws RDFParseException, UnsupportedRDFormatException, IOException, URISyntaxException
	{
		// Access to a remote repository accessible over HTTP
		HTTPRepository repository = new HTTPRepository("http://localhost:7200/repositories/TewsTriageDataBase");

		// Separate connection to a repository
		RepositoryConnection connection = repository.getConnection();

		// Clear the repository before we start
		connection.clear();
		
		MedicalTriage medicalTriage = new MedicalTriage(connection);
		
		try
		{
			// Loading of the ontology and the instances
			medicalTriage.loadOntology();
			medicalTriage.loadInstances();
			connection.commit();
			
			// SPARQL queries
			medicalTriage.SPARQLstretcherNeededQuery();
			medicalTriage.SPARQLnumberOfDeathsQuery();
			medicalTriage.TEWSscoreCalculationSPARQLqueries();
			medicalTriage.constructionOfTEWScolourSPARQLqueries(); 
			medicalTriage.SPARQLtewsColourCodesQuery();
			
		}
		catch (Exception e)
		{
			// General exception handler 
			e.printStackTrace();
		}
		finally
		{
			connection.close();
		}
	}
}
