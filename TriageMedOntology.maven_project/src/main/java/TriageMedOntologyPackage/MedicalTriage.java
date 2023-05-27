package TriageMedOntologyPackage;

import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.rio.RDFFormat; //
import org.eclipse.rdf4j.rio.RDFParseException; //
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
		
		// Reading the json file, which includes patients data
		try(JsonReader reader = new JsonReader(new FileReader("C://test2patients.json")))
		{
			JsonElement jsonElement = new JsonParser().parse(reader);
			
			// Creating an RDF model with the base URI of the ontology
			ModelBuilder builder = new ModelBuilder();
			builder.setNamespace("a", "http://MedOntology.project.rdfs/TEWStriage");
			
			// Generating an instance of RDF value 
			ValueFactory factory = SimpleValueFactory.getInstance();

			JsonObject jsonObject = jsonElement.getAsJsonObject();
			//jsonObject = jsonObject.getAsJsonObject(); //it may be needed
			JsonArray jArray = jsonObject.getAsJsonArray("patientsData");
			
			// Accessing every json object of the json array
			for (int i = 0; i < jArray.size(); i++) 
			{
			    JsonObject jsonObject2 = jArray.get(i).getAsJsonObject();
			    System.out.println(jsonObject2.toString()); //just a check
			}
			
		}
		catch (Exception e) 
		{
			// General exception handler
			e.printStackTrace();
		}
		
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
