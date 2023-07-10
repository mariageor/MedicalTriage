package TriageMedOntologyPackage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.EmptyModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
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
    	
		connection.begin();
		
		// Getting the turtle file of the ontology and adding it in the connection which was created
		connection.add(MedicalTriage.class.getResourceAsStream("/TEWStriage.ttl"), "urn:base", RDFFormat.TURTLE);
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
			//jsonObject = jsonObject.getAsJsonObject(); //it may be needed
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
			    //System.out.println(dataIRI); //just a check
			    
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
					JsonElement AVPUstate = AVPUdata.get("type");
					    
					JsonObject tewsCodeData = patient.get("TEWScodeOfPatient").getAsJsonObject(); //this one should be calculated by a SPARQL query
					JsonElement tewsCode = tewsCodeData.get("type");
					    
					JsonElement tewsScore = patient.get("TEWSscore"); //this one should be calculated by a SPARQL query
					    
					JsonObject VSdata = patient.get("VitalSignsOfPatient").getAsJsonObject();
					JsonElement VStype = VSdata.get("type");
					JsonElement HR = VSdata.get("HR");
					JsonElement RR = VSdata.get("RR");
					JsonElement SBP = VSdata.get("SBP");
					JsonElement temp = VSdata.get("temperature");
					    
					JsonElement ableToWalk = patient.get("ableToWalk");
					    
					JsonElement age = patient.get("ageOfPatient");
					    
					JsonElement trauma = patient.get("existenceOfTrauma");
					    
					JsonElement needsHelpToWalk = patient.get("needsHelpToWalk");
					    
					JsonElement stretcher = patient.get("stretcherNeededOrImmobilePatient");
					    
					builder.subject(AVPUstateIRI).add(RDF.TYPE, AVPUstate);
					builder.subject(TEWScodeIRI).add(RDF.TYPE, tewsCode);
					builder.subject(vsIRI).add(RDF.TYPE, VStype).add("TEWStriage:HR", HR).add("TEWStriage:RR", RR)
							.add("TEWStriage:SBP", SBP).add("TEWStriage:temp", temp);
					 
					builder.subject(patientsIRI).add(RDF.TYPE, sexOfPatient).add("TEWStriage:deadPatient", deadPatient)
							.add("TEWStriage:patientsName", nameElement).add("TEWStriage:AVPUstateOfPatient", AVPUdata)
							.add("TEWStriage:TEWScodeOfPatient", tewsCodeData).add("TEWStriage:VitalSignsOfPatient", VSdata)
							.add("TEWStriage:TEWSscore", tewsScore).add("TEWStriage:ableToWalk", ableToWalk)
							.add("TEWStriage:existenceOfTrauma", trauma).add("TEWStriage:ageOfPatient", age)
							.add("TEWStriage:needsHelpToWalk", needsHelpToWalk)
							.add("TEWStriage:stretcherNeededOrImmobilePatient", stretcher);
					
				}
				else
				{
					builder.subject(patientsIRI).add(RDF.TYPE, sexOfPatient).add("TEWStriage:deadPatient", deadPatient)
					.add("TEWStriage:patientsName", nameElement);
				}
				
			}
			
			Model model = builder.build();
			connection.add(model);
			//model = builder.build();
		    
			//Printing statements for debugging
			//for(Statement st: model) 
			//{
				//System.out.println(st);
			//}
			
			File file = new File("./PatientsTriplets.owl"); 
			FileOutputStream out = new FileOutputStream(file);
			try {
				Rio.write(model, out, RDFFormat.TURTLE);
			}
			finally 
			{
				out.close();
			}
			
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
		queryString += "select (COUNT(?p) AS ?deaths)\n";
		queryString += "where\n";
		queryString += "{\n";
		queryString += "?p a TEWStriage:Patient.\n";
		queryString += " ?p TEWStriage:deadPatient true.\n";
		queryString += "}\n";
		
		TupleQuery query = connection.prepareTupleQuery(queryString);
		
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
			medicalTriage.SPARQLnumberOfDeathsQuery();
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
