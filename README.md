# jira-extractor
Extracts Issues (Stories) from Jira and finds dates for each Kanban stage, creating a CSV to build CFDs, Lead Time Scatterplots etc

Requires java 8

Steps
=====
1. Download jira-extractor.jar
https://github.com/leocampos/jira-extractor/blob/master/jira-extractor.jar?raw=true

2. Write a config.properties in the same directory as the jar

jira_url=https://your.jira.url

jql=type = Story AND project = YOUR_PROJECT

status_list=Backlog,Ready For Dev,In Progress,QA,Done

date_format=dd/MM/yyyy HH:mm

output_path=./cfd.csv

csv_separator=;


Now, in a terminal, run the following command
java -jar jira-extractor.jar
