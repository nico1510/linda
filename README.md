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
create-connector-connection-pool --steadypoolsize 20 --maxpoolsize 100 --poolresize 2 --maxwait 60000 --raname jackrabbit-jca --connectiondefinition javax.jcr.Repository jcrPool --property homeDir=/data/storage:bindSessionToTransaction=false

Transaction Support NoTransaction

create-connector-resource --poolname jcrPool --description "Jackrabbit Repo" jcr/rep1


// Virtuoso
===========
create-jdbc-connection-pool --datasourceclassname virtuoso.jdbc3.VirtuosoXADataSource --restype javax.sql.XADataSource jdbc/Virtuoso --property portNumber=1111:serverName=localhost:dataSourceName=triplestore:user=dba:password=dba
create-jdbc-resource --connectionpoolid jdbc/Virtuoso triplestore

Non Transactional Connections:  enabled
Guaranteed : disabled
Advanced / Wrap JDBC Objects :disabled

/data/storage in config file unter dirsAllowed listen

/usr/bin/isql-vt

db location : /var/lib/virtuoso-opensource-6.1/db

// start stop skripte
=====================
sudo /etc/init.d/virtuoso-opensource-6.1 start

// deployment
=============

EJB : restart 
commons-compress-1.4.1.jar,jackrabbit-jcr-rmi-2.4.5.jar,commons-io-2.4.jar,jsoup-1.7.1.jar,Exec.jar,openrdf-sesame-2.7.0-beta1-onejar.jar,jackrabbit-jcr-commons-2.4.5.jar,gson-2.2.4.jar
