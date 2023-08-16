# MedicalTriage
This application represents the proccess of medical TEWS triage.
At first, it loads an ontology and the rdf statetements of an instance
into a GraphDB repository. Then, it reads a json file (PatientsData.json)
containing the data of 15 new patients/victims and converts them into rdf statements. 
Lastly, the reasoning is conducted through various SPARQL queries and the results
are also converted into rdf statements and added to the GraphDB repository.

@author Maria-Georgia Georgiadou
@since 2023-04-10
