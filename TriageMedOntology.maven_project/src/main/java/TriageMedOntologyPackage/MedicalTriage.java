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
import org.eclipse.rdf4j.model.ValueFactory;
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
		try(JsonReader reader = new JsonReader(new FileReader("C://PatientsData.json")))
		{
			JsonElement jsonElement = new JsonParser().parse(reader);
			
			// Creating an RDF model with the base URI of the ontology
			ModelBuilder builder = new ModelBuilder();
			builder.setNamespace("TEWStriage", this.namespace);
			
			// Generating an instance of RDF value 
			ValueFactory factory = SimpleValueFactory.getInstance();
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			
			// Getting the json data as a json array
			JsonArray jArray = jsonObject.getAsJsonArray("patientsData");
			
			// Accessing every json object of the json array
			for (int i = 0; i < jArray.size(); i++) 
			{
				JsonObject patient = jArray.get(i).getAsJsonObject();
			    
			    IRI patientsIRI = factory.createIRI(namespace, "p" + (i+1));
			    IRI vsIRI = factory.createIRI(namespace, "VSp" + (i+1)); 
			    IRI AVPUstateIRI = factory.createIRI(namespace, "AVPUstate" + (i+1)); 
			    IRI TEWScodeIRI = factory.createIRI(namespace, "TEWScode" + (i+1)); 
			    
			    // Accessing the name element of every patient
				JsonElement nameElement = patient.get("patientsName");
				String name = nameElement.getAsString();
				System.out.println("Patient"+i+": " + name);
				    
				// Accessing the deadPatient element of every patient
				JsonElement deadPatient = patient.get("deadPatient");
				Boolean dead = deadPatient.getAsBoolean();
				
				JsonElement sexOfPatient = patient.get("type");
				    
				if (!dead)
				{	
					// I didn't add the colour codes beacause they would be constructed in the end
					
					JsonObject AVPUdata = patient.get("AVPUstateOfPatient").getAsJsonObject();
					//IRI AVPUiri = factory.createIRI("TEWStriage:AVPUstate"+ (i+1));
					JsonElement AVPUstate = AVPUdata.get("type"); // it may be unecessary
					    
					JsonObject tewsCodeData = patient.get("TEWScodeOfPatient").getAsJsonObject(); //this one should be calculated by a SPARQL query
					//IRI tewsCodeIRI = factory.createIRI("TEWStriage:TEWScode"+(i+1));
					JsonElement tewsCode = tewsCodeData.get("type"); // it may be unecessary
					    
					JsonElement tewsScore = patient.get("TEWSscore"); //this one should be calculated by a SPARQL query
					    
					JsonObject VSdata = patient.get("VitalSignsOfPatient").getAsJsonObject();
					//IRI VsIRI = factory.createIRI("TEWStriage:Vs"+ (i+1));
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
					    
					builder.subject(AVPUstateIRI).add(RDF.TYPE, factory.createIRI("TEWStriage:"+AVPUstate.getAsString()));
					builder.subject(TEWScodeIRI).add(RDF.TYPE, factory.createIRI("TEWStriage:"+tewsCode.getAsString()));
					
					builder.subject(vsIRI).add(RDF.TYPE, factory.createIRI("TEWStriage:"+VStype.getAsString()))
							.add("TEWStriage:HR", factory.createLiteral(HR.getAsInt()))
							.add("TEWStriage:RR", factory.createLiteral(RR.getAsInt()))
							.add("TEWStriage:SBP", factory.createLiteral(SBP.getAsInt()))
							.add("TEWStriage:temp", factory.createLiteral(temp.getAsInt()));
					 
					builder.subject(patientsIRI).add(RDF.TYPE, factory.createIRI("TEWStriage:Patient"))
							.add(RDF.TYPE, factory.createIRI("TEWStriage:"+sexOfPatient.getAsString()))
							.add("TEWStriage:deadPatient", factory.createLiteral(deadPatient.getAsBoolean()))
							.add("TEWStriage:patientsName", factory.createLiteral(nameElement.getAsString()))
							.add("TEWStriage:AVPUstateOfPatient", AVPUstateIRI) //search it more
							.add("TEWStriage:TEWScodeOfPatient", TEWScodeIRI) //search it more
							.add("TEWStriage:VitalSignsOfPatient", vsIRI) //search it more
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
							.add(RDF.TYPE, factory.createIRI("TEWStriage:"+sexOfPatient.getAsString()))
							.add("TEWStriage:deadPatient", factory.createLiteral(deadPatient.getAsBoolean()))
							.add("TEWStriage:patientsName", factory.createLiteral(nameElement.getAsString()));
				}
				
			}
			
			Model model = builder.build();
			this.connection.add(model);
			//connection.commit(); //just a trial
			//model = builder.build(); //just a trial
		    
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
		
		//return model; //just a trial
		
	}
	
	
	public void SPARQLnumberOfDeathsQuery()
	{
		System.out.print("Number of deaths: ");
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
		
		
		Iterator<String> iterator = colours.iterator();
	    while(iterator.hasNext()) 
	    {
	    	String nameOfCode = iterator.next()+"Codes";
	    	System.out.print("The number of "+nameOfCode+" is: ");

			String queryString = "PREFIX TEWStriage: <http://MedOntology.project.rdfs/TEWStriage#>";
	    	
	    	queryString += "SELECT (COUNT(?p) AS ?"+nameOfCode+"Codes) \n";
	    	queryString += "WHERE\n";
	    	queryString += "{\n";
	    	queryString += "?p a TEWStriage:Patient.\n";
	    	queryString += "?p TEWStriage:TEWScodeOfPatient ?c.\n";
	    	queryString += "?c a TEWStriage:"+iterator.next()+".\n";
	    	queryString += "}\n";
	    	
	    	TupleQuery query = this.connection.prepareTupleQuery(queryString);
			
			TupleQueryResult result = query.evaluate();
			while (result.hasNext()) {
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
		System.out.print("Number of stretchers needed: ");
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
	
	
	
	public void TEWScalculation()
	{
		// Here I have to check the ranges of the values of HR, RR, SBP, abilityOfMobility and AVPU.
		// Latly, I have to construct the triplets about  the TEWSscore and TEWScolourCode
		// and add them in the PatientsTriplets.owl for completeness.
		
		System.out.println("Calculatin the results...");
		
		// Creating an RDF model with the base URI of the ontology
		ModelBuilder builder = new ModelBuilder();
		builder.setNamespace("TEWStriage", this.namespace);
					
		// Generating an instance of RDF value 
		ValueFactory factory = SimpleValueFactory.getInstance();
		
		IRI TEWScodeIRI = factory.createIRI(namespace, "TEWScode_pX"); 
		IRI TEWScolourIRI = factory.createIRI(namespace, "TEWScolourCode_pX"); 
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
			// medicalTriage.TEWScalculation();
			medicalTriage.SPARQLstretcherNeededQuery();
			medicalTriage.SPARQLnumberOfDeathsQuery();
			//medicalTriage.SPARQLtewsColourCodesQuery();
			
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
