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
	
	/**
	 * Constructor of the class MedicalTriage
	 * @parameter connection
	 */
	public MedicalTriage(RepositoryConnection connection)
	{
		this.connection = connection;
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
			String namespace = "http://MedOntology.project.rdfs/TEWStriage";
			JsonElement jsonElement = new JsonParser().parse(reader);
			
			// Creating an RDF model with the base URI of the ontology
			ModelBuilder builder = new ModelBuilder();
			builder.setNamespace("a", namespace);
			
			// Generating an instance of RDF value 
			ValueFactory factory = SimpleValueFactory.getInstance();

			JsonObject jsonObject = jsonElement.getAsJsonObject();
			jsonObject = jsonObject.getAsJsonObject(); //it may be needed
			// Getting the json data as a json array
			JsonArray jArray = jsonObject.getAsJsonArray("patientsData");
			
			// Accessing every json object of the json array
			for (int i = 0; i < jArray.size(); i++) 
			{
				JsonObject patient = jArray.get(i).getAsJsonObject();
			    //System.out.println(patient.toString()); //just a check
			    
			    
			    IRI patientsIRI = factory.createIRI(namespace, "P" + (i+1));
			    IRI vsIRI = factory.createIRI(namespace, "Vs" + (i+1)); 
			    IRI AVPUstateIRI = factory.createIRI(namespace, "AVPUstate" + (i+1)); 
			    IRI TEWScodeIRI = factory.createIRI(namespace, "TEWScode" + (i+1)); 
			    //System.out.println(patientsIRI); //just a check
			    //System.out.println(vsIRI); //just a check
			    
			    // Accessing the name element of every patient
				JsonElement nameElement = patient.get("patientsName");
				String name = nameElement.getAsString();
				System.out.println("Name: " + name);
				    
				// Accessing the deadPatient element of every patient
				JsonElement deadPatient = patient.get("deadPatient");
				Boolean dead = deadPatient.getAsBoolean();
				
				JsonElement sexOfPatient = patient.get("type");
				    
				if (!dead)
				{	
					JsonObject AVPUdata = patient.get("AVPUstateOfPatient").getAsJsonObject();
					JsonElement AVPUstate = AVPUdata.get("type"); // it may be unecessary
					    
					JsonObject tewsCodeData = patient.get("TEWScodeOfPatient").getAsJsonObject(); //this one should be calculated by a SPARQL query
					JsonElement tewsCode = tewsCodeData.get("type"); // it may be unecessary
					    
					JsonElement tewsScore = patient.get("TEWSscore"); //this one should be calculated by a SPARQL query
					    
					JsonObject VSdata = patient.get("VitalSignsOfPatient").getAsJsonObject();
					JsonElement VStype = VSdata.get("type"); // it may be unecessary
					JsonElement HR = VSdata.get("HR");
					JsonElement RR = VSdata.get("RR");
					JsonElement SBP = VSdata.get("SBP");
					JsonElement temp = VSdata.get("temperature");
					    
					JsonElement ableToWalk = patient.get("ableToWalk");
					    
					JsonElement age = patient.get("ageOfPatient");
					    
					JsonElement trauma = patient.get("existenceOfTrauma");
					    
					JsonElement needsHelpToWalk = patient.get("needsHelpToWalk");
					    
					JsonElement stretcher = patient.get("stretcherNeededOrImmobilePatient");
					    
					builder.subject(AVPUstateIRI).add(RDF.TYPE, "<http://MedOntology.project.rdfs/TEWStriage#"+AVPUstate.getAsString());
					builder.subject(TEWScodeIRI).add(RDF.TYPE, "<http://MedOntology.project.rdfs/TEWStriage#"+tewsCode.getAsString());
					builder.subject(vsIRI).add(RDF.TYPE, "<http://MedOntology.project.rdfs/TEWStriage#"+VStype.getAsString())
							.add("TEWStriage:HR", factory.createLiteral(HR.getAsInt()))
							.add("TEWStriage:RR", factory.createLiteral(RR.getAsInt()))
							.add("TEWStriage:SBP", factory.createLiteral(SBP.getAsInt()))
							.add("TEWStriage:temp", factory.createLiteral(temp.getAsInt()));
					 
					
					builder.subject(patientsIRI).add(RDF.TYPE, "<http://MedOntology.project.rdfs/TEWStriage#"+sexOfPatient.getAsString())
							.add("TEWStriage:deadPatient", factory.createLiteral(deadPatient.getAsBoolean()))
							.add("TEWStriage:patientsName", factory.createLiteral(nameElement.getAsString()))
							.add("TEWStriage:AVPUstateOfPatient", "TEWStriage:AVPUstate"+(i+1)) //search it more
							.add("TEWStriage:TEWScodeOfPatient", "TEWStriage:TEWScode"+(i+1)) //search it more
							.add("TEWStriage:VitalSignsOfPatient", "TEWStriage:Vs"+(i+1)) //search it more
							.add("TEWStriage:TEWSscore", factory.createLiteral(tewsScore.getAsInt()))
							.add("TEWStriage:ableToWalk", factory.createLiteral(ableToWalk.getAsBoolean()))
							.add("TEWStriage:existenceOfTrauma", factory.createLiteral(trauma.getAsBoolean()))
							.add("TEWStriage:ageOfPatient", factory.createLiteral(age.getAsInt()))
							.add("TEWStriage:needsHelpToWalk", factory.createLiteral(needsHelpToWalk.getAsBoolean()))
							.add("TEWStriage:stretcherNeededOrImmobilePatient", factory.createLiteral(stretcher.getAsBoolean()));
					
				}
				else
				{
					builder.subject(patientsIRI).add(RDF.TYPE, "<http://MedOntology.project.rdfs/TEWStriage#"+sexOfPatient.getAsString())
					.add("TEWStriage:deadPatient", factory.createLiteral(deadPatient.getAsBoolean()))
					.add("TEWStriage:patientsName", factory.createLiteral(nameElement.getAsString()));
				}
				
			}
			
			Model model = builder.build();
			this.connection.add(model);
			//model = builder.build();
		    
			//Printing statements for debugging
			//for(Statement st: model) 
			//{
				//System.out.println(st);
			//}
			
			//File file = new File("./PatientsTriplets.ttl"); 
			File file = new File("./PatientsTriplets.owl");
			FileOutputStream out = new FileOutputStream(file);
			try {
				Rio.write(model, out, RDFFormat.TURTLE);
			}
			finally 
			{
				out.close();
			}
			
			// This is needed only if you want to find the exact location of the file created
			//String currentWorkingDirectory = System.getProperty("user.dir");
			//System.out.println("Current working directory: " + currentWorkingDirectory);
		}
		catch (Exception e) 
		{
			// General exception handler
			e.printStackTrace();
		}
		
		//return model;
		
	}
	
	
	public void SPARQLnumberOfDeathsQuery()
	{
		System.out.println("Number of deaths:");
		String queryString = "PREFIX TEWStriage: <http://MedOntology.project.rdfs/TEWStriage#>";
		queryString += "SELECT (COUNT(?p) AS ?deaths)\n";
		queryString += "WHERE\n";
		queryString += "{\n";
		queryString += "?p a TEWStriage:Patient.\n";
		queryString += "?p TEWStriage:deadPatient true.\n";
		queryString += "}\n";
		
		TupleQuery query = this.connection.prepareTupleQuery(queryString);
		
		TupleQueryResult result = query.evaluate();
		while (result.hasNext()) {
			BindingSet bindingSet = result.next();
			
			IRI a = (IRI) bindingSet.getBinding("a").getValue();
			System.out.println(a);
				
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

			String queryString = "PREFIX TEWStriage: <http://MedOntology.project.rdfs/TEWStriage#>";
	    	
	    	queryString += "SELECT (COUNT(?p) AS ?"+iterator.next()+"Codes) \n";
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
				
				IRI a = (IRI) bindingSet.getBinding("a").getValue();
				System.out.println(a);
					
			}
			result.close();
	    }
	}
	
	
	public void SPARQLstretcherNeededQuery()
	{
		System.out.println("Number of deaths:");
		String queryString = "PREFIX TEWStriage: <http://MedOntology.project.rdfs/TEWStriage#>";
		queryString += "SELECT (COUNT(?p) AS ?deaths)\n";
		queryString += "WHERE\n";
		queryString += "{\n";
		queryString += "?p a TEWStriage:Patient.\n";
		queryString += "?p TEWStriage:stretcherNeededOrImmobilePatient true.\n";
		queryString += "}\n";
		
		TupleQuery query = this.connection.prepareTupleQuery(queryString);
		
		TupleQueryResult result = query.evaluate();
		while (result.hasNext()) {
			BindingSet bindingSet = result.next();
			
			IRI a = (IRI) bindingSet.getBinding("a").getValue();
			System.out.println(a);
				
		}
		result.close();
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
			//Model model = medicalTriage.loadInstances();
			//connection.add(model);
			connection.commit();
			
			// SPARQL queries
			//medicalTriage.SPARQLnumberOfDeathsQuery();
			//medicalTriage.SPARQLtewsColourCodesQuery();
			//medicalTriage.SPARQLstretcherNeededQuery();
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
