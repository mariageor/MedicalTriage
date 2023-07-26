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
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryEvaluationException;
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
 * 
 * @author Maria-Georgia Georgiadou
 * @version 1.0
 * @since 2023-05-10
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
	 * 
	 * @throws RDFParseException
	 * @throws RepositoryException
	 * @throws IOException
	 */
	public void loadOntology() throws RDFParseException, RepositoryException, IOException 
	{
		System.out.println("Loading the ontology...");
    	
		this.connection.begin();
		
		// Getting the turtle file of the ontology and adding it in the connection which was created
		this.connection.add(MedicalTriage.class.getResourceAsStream("/TEWStriage.ttl"), "urn:base", RDFFormat.TURTLE);
	}
	
	/**
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public void loadInstances() throws IOException, URISyntaxException
	{
		System.out.println("Loading new instances...");
		
		//Model model = new TreeModel();
		
		// Reading the json file, which includes patients data
		try(JsonReader reader = new JsonReader(new FileReader("C://test.json")))
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
				    
				if (!dead)
				{	
					IRI vsIRI = factory.createIRI(namespace, "VSp" + (i+1)); 
				    IRI AVPUstateIRI = factory.createIRI(namespace, "AVPUstate" + (i+1)); 
				    //IRI TEWScodeIRI = factory.createIRI(namespace, "TEWScode" + (i+1));
					
					JsonObject AVPUdata = patient.get("AVPUstateOfPatient").getAsJsonObject();
					JsonElement AVPUstate = AVPUdata.get("type"); // it may be unecessary
					    
					//JsonObject tewsCodeData = patient.get("TEWScodeOfPatient").getAsJsonObject(); //this one should be calculated by a SPARQL query
					//JsonElement tewsCode = tewsCodeData.get("type"); // it may be unecessary
					    
					JsonElement tewsScore = patient.get("TEWSscore"); //this one should will take the right value in a SPARQL query above
					//System.out.println(tewsScore.getAsInt());
					    
					JsonObject VSdata = patient.get("VitalSignsOfPatient").getAsJsonObject();
					JsonElement VStype = VSdata.get("type"); // it may be unecessary
					JsonElement HR = VSdata.get("HR");
					JsonElement RR = VSdata.get("RR");
					JsonElement SBP = VSdata.get("SBP");
					JsonElement temp = VSdata.get("temperature");
					    
					JsonElement ableToWalk = patient.get("abilityOfMobility"); 
					    
					JsonElement age = patient.get("ageOfPatient");
					    
					JsonElement trauma = patient.get("existenceOfTrauma");
					    
					JsonElement needsHelpToWalk = patient.get("needsHelpToWalk");
					    
					JsonElement stretcher = patient.get("stretcherNeededOrImmobilePatient");
					    
					builder.subject(AVPUstateIRI).add(RDF.TYPE, factory.createIRI(namespace,AVPUstate.getAsString()));
					//builder.subject(TEWScodeIRI).add(RDF.TYPE, factory.createIRI(namespace,tewsCode.getAsString()));
					
					builder.subject(vsIRI).add(RDF.TYPE, factory.createIRI(namespace,VStype.getAsString()))
							.add("TEWStriage:HR", factory.createLiteral(HR.getAsInt()))
							.add("TEWStriage:RR", factory.createLiteral(RR.getAsInt()))
							.add("TEWStriage:SBP", factory.createLiteral(SBP.getAsInt()))
							.add("TEWStriage:temp", factory.createLiteral(temp.getAsInt()));
					 
					builder.subject(patientsIRI).add(RDF.TYPE, factory.createIRI("TEWStriage:Patient"))
							.add(RDF.TYPE, factory.createIRI(namespace, sexOfPatient.getAsString()))
							.add("TEWStriage:deadPatient", factory.createLiteral(deadPatient.getAsBoolean()))
							.add("TEWStriage:patientsName", factory.createLiteral(nameElement.getAsString()))
							.add("TEWStriage:AVPUstateOfPatient", AVPUstateIRI) 
							//.add("TEWStriage:TEWScodeOfPatient", TEWScodeIRI) 
							.add("TEWStriage:VitalSignsOfPatient", vsIRI) 
							.add("TEWStriage:TEWSscore", factory.createLiteral(tewsScore.getAsInt()))
							.add("TEWStriage:abilityOfMobility", factory.createLiteral(ableToWalk.getAsBoolean()))
							.add("TEWStriage:existenceOfTrauma", factory.createLiteral(trauma.getAsBoolean()))
							.add("TEWStriage:ageOfPatient", factory.createLiteral(age.getAsInt()))
							.add("TEWStriage:needsHelpToWalk", factory.createLiteral(needsHelpToWalk.getAsBoolean()))
							.add("TEWStriage:stretcherNeededOrImmobilePatient", factory.createLiteral(stretcher.getAsBoolean()));
				}
				else
				{
					builder.subject(patientsIRI).add(RDF.TYPE, factory.createIRI("TEWStriage:Patient"))
							.add(RDF.TYPE, factory.createIRI(namespace,sexOfPatient.getAsString()))
							.add("TEWStriage:deadPatient", factory.createLiteral(deadPatient.getAsBoolean()))
							.add("TEWStriage:patientsName", factory.createLiteral(nameElement.getAsString()));
				}
				
			}
			
			Model model = builder.build();
			this.connection.add(model);
		    
			//Printing statements for debugging
			//for(Statement st: model) 
			//{
				//System.out.println(st);
			//}
			
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
			//String currentWorkingDirectory = System.getProperty("user.dir");
			//System.out.println("Current working directory: " + currentWorkingDirectory);
		}
		catch (Exception e) 
		{
			// General exception handler
			e.printStackTrace();
		}
		
	}
	
	
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
	
	
	public void SPARQLtewsColourCodesQuery()
	{
		List<String> colours = new ArrayList<String>();
		colours.add("Blue"); // it should have the same results as the number of deaths query
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
		ColourCodes(builder, factory, model, "Red", 7, 1000);
	}
	
	
	public void ColourCodes(ModelBuilder builder, SimpleValueFactory factory, Model model, String colour, Integer l1, Integer l2)
	{
		String queryString = "PREFIX TEWStriage: <http://MedOntology.project.rdfs/TEWStriage#>";
		queryString += "SELECT ?p\n";
		queryString += "WHERE\n";
		queryString += "{\n";
		queryString += "    ?p a TEWStriage:Patient .\n";
		queryString += "    ?p TEWStriage:TEWSscore ?s.\n";
		queryString += "    FILTER(?s >="+ l1 +"&& ?s <="+ l2 +")\n";
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
	
	
	public void constructionOfTEWScolourSPARQLqueries2()
	{	
		// Here, I have to construct the triples about  the TEWSscore and TEWScolourCode
		// and add them in the PatientsTriplets.owl for completeness.
		
		String queryString = "PREFIX TEWStriage: <http://MedOntology.project.rdfs/TEWStriage#>";
		queryString += "CONSTRUCT\n";
		queryString += "{\n";
		queryString += "	?p TEWStriage:TEWScodeOfPatient TEWStriage:Green\n";
		queryString += "}\n";
		queryString += "WHERE\n";
		queryString += "{\n";
		queryString += "    ?p a TEWStriage:Patient .\n";
		queryString += "    ?p TEWStriage:TEWSscore ?s.\n";
		queryString += "    FILTER(?s >= 0 && ?s <= 2)\n";
		queryString += "}";

		//this.connection.prepareGraphQuery(queryString);
		GraphQuery query = this.connection.prepareGraphQuery(queryString);

		try (GraphQueryResult result = query.evaluate()) {
		    while (result.hasNext()) 
		    {   
                Statement statement = result.next();
		        
		        String subject = statement.getSubject().stringValue();
		        String predicate = statement.getPredicate().stringValue();
		        String object = statement.getObject().stringValue();

		        System.out.println("Subject: " + subject);
		        System.out.println("Predicate: " + predicate);
		        System.out.println("Object: " + object);
		        System.out.println("-----------");
		    }
		} catch (Exception e) {
		    // Handle any exceptions that may occur during query evaluation.
		    e.printStackTrace();
		}
	}
	
	
	public void TEWSscoreCalculationSPARQLqueries()
	{
		scoreCalculationForAVPU("Verbal", 1);
		scoreCalculationForAVPU("Pain", 2);
		scoreCalculationForAVPU("Unresponsive", 3);
		scoreCalculationsForMobilityAndTrauma("stretcherNeededOrImmobilePatient", 2);
		scoreCalculationsForMobilityAndTrauma("needsHelpToWalk", 1);
		scoreCalculationsForMobilityAndTrauma("existenceOfTrauma", 1);
	}
	
	
	public void scoreCalculationForAVPU(String type, int num )
	{
		String queryString = "PREFIX TEWStriage: <http://MedOntology.project.rdfs/TEWStriage#>";
		queryString += "SELECT ?p ((?score)+"+ num +" AS ?updatedValue)\n";
		queryString += "WHERE\n";
		queryString += "{\n";
		queryString += "    ?p a TEWStriage:Patient .\n";
		queryString += "    ?p TEWStriage:TEWSscore ?s.\n";
		queryString += "    ?p TEWStriage:AVPUstateOfPatient ?avpu .\n";
		queryString += "    ?avpu a TEWStriage:"+ type +" .\n";
		queryString += "}";
		
		this.connection.prepareTupleQuery(queryString);
		
		/**TupleQuery query = this.connection.prepareTupleQuery(queryString);
		
		TupleQueryResult result = query.evaluate();
		while (result.hasNext()) 
		{
			BindingSet bindingSet = result.next();
			
			Literal updatedValueLiteral = (Literal) bindingSet.getBinding("updatedValue").getValue();
			int updatedValues = updatedValueLiteral.intValue();
			System.out.println(updatedValues);
				
		}
		
		result.close();*/
	}
	
	public void scoreCalculationsForMobilityAndTrauma(String type, int num)
	{
		String queryString = "PREFIX TEWStriage: <http://MedOntology.project.rdfs/TEWStriage#>";
		queryString += "SELECT ?p ?updatedValue\n";
		queryString += "WHERE\n";
		queryString += "{\n";
		queryString += "    ?p a TEWStriage:Patient .\n";
		queryString += "    ?p TEWStriage:TEWSscore ?s.\n";
		queryString += "    ?p TEWStriage:"+ type+" ?r .\n";
		queryString += "    FILTER(?r=true) .\n";
		queryString += "    BIND((?score+"+ num +") AS ?updatedValue)\n";
		queryString += "}";
		
		this.connection.prepareTupleQuery(queryString);
	}
	
	
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
