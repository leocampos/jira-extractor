# jira-extractor
Extracts Issues (Stories) from Jira and finds dates for each Kanban stage, creating a CSV to build CFDs, Lead Time Scatterplots etc

Requires java 8

Steps
=====
1. Download jira-extractor.jar
https://github.com/leocampos/jira-extractor/blob/master/jira-extractor.jar?raw=true

2. Write a config.properties in the same directory as the jar, WRITE IT IN UTF-8

jira_url=https://your.jira.url

jql=type = Story AND project = YOUR_PROJECT

status_list=Backlog,Ready For Dev,In Progress,QA,Done

date_format=dd/MM/yyyy HH:mm

output_path=./cfd.csv

csv_separator=;


Now, in a terminal, run the following command
java -jar jira-extractor.jar

SSL problem
===========
If you run the above command and gets the following exception:

javax.net.ssl.SSLHandshakeException

The problem can be solved (not so easily, though). I visited Jira using a Firefox browser, in Settings >> Advanced >> Certificates, there is a "Certificate button". Click it.

When you find the appropriate certificate, download it.

From now on, I only tested on a Mac...

No, you're not done yet, now you need to change and run the following command:
sudo keytool -import -file PATH_TO_DOWNLOADED_FILE -alias MEANINGFULL_ALIAS -keystore $JAVA_HOME/jre/lib/security/cacerts

** This command will ask you first your password because of your sudo, but it will also ask another one. This second one is most probably "changeit". **

It should work now
