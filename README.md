Setup
=====


// JMS
====== 
create-jmsdest --desttype queue PhysicalQueue			 // create-jmsdest --desttype queue --property User=public:Password=public PhysicalQueue
create-jms-resource --restype javax.jms.QueueConnectionFactory --description "connection factory for durable subscriptions" jms/ConnectionFactory
create-jms-resource --restype javax.jms.Queue --property Name=PhysicalQueue jms/Queue


// Java Mail Resource
=====================
Einstellungen aus javamail.jpg

// Jackrabbit
=============
create-connector-connection-pool --steadypoolsize 20 --maxpoolsize 100 --poolresize 2 --maxwait 60000 --raname jackrabbit-jca --connectiondefinition javax.jcr.Repository jcrPool --property homeDir=/home/glassfish/glassfish3/storage:bindSessionToTransaction=false

Transaction Support NoTransaction

create-connector-resource --poolname jcrPool --description "Jackrabbit Repo" jcr/rep1


// Virtuoso
===========
create-jdbc-connection-pool --datasourceclassname virtuoso.jdbc3.VirtuosoXADataSource --restype javax.sql.XADataSource jdbc/Virtuoso --property portNumber=1111:serverName=localhost:dataSourceName=triplestore:user=dba:password=dba
create-jdbc-resource --connectionpoolid jdbc/Virtuoso triplestore

Non Transactional Connections:  enabled
Guaranteed : disabled
Advanced / Wrap JDBC Objects :disabled


// start stop skripte
=====================
sudo /etc/init.d/virtuoso-opensource-6.1 start


