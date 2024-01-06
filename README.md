# MedicalTriage
Technical Summary

This application represents the proccess of medical TEWS triage.
At first, it loads an ontology and the rdf statetements of an instance
into a GraphDB repository. Then, it reads a json file (PatientsData.json)
containing the data of 15 new patients/victims and converts them into rdf statements. 
Lastly, the reasoning is conducted through various SPARQL queries and the results
are also converted into rdf statements and added to the GraphDB repository.

Abstract

The aim of this thesis is to develop a decision support tool for mass disaster 
management. The purpose of this tool is to improve the situation awareness of the 
medical/paramedical staff in the field of the disaster, so that they can make faster and 
more effective decisions or even prevent possible mistakes. Semantic Web and 
Artificial Intelligence technologies were used to achieve this result. More specifically, 
semantic web was used to represent the knowledge in an ontology, which was entirely 
designed to represent an already existing triage protocol called TEWS (Triage Early 
Warning Score). In addition, artificial intelligence and data analytics were used in the 
creation and pre-processing stage of the artificial data of the patients and also in the 
connection of them to the ontology, which was made. Consequently, after loading the 
ontology with the new and appropriately processed data, a reasoning framework was 
implemented, which aims to categorize the patients by triage colour, and to extract other 
relevant medical information, such as the number of dead and the number of stretchers 
needed at the disaster site. In conclusion, through the process described, it becomes 
possible to apply reasoning, to draw conclusions and statistics on the patient data set, 
which help the crisis management personnel, to make more effective decisions.

@author Maria-Georgia Georgiadou
@since 2023-04-10
