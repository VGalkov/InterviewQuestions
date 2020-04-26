#!usr/bin/snv bash

echo "Preparing"
mvn clean package

echo "Copyng"
scp -i /home/galkov/.ssh/rsa_key_IQserver \
	 where/InterviewQuestions-1.0-SNAPSHOT.jar \
 	 galkov@192.168.0.20:/home/deplovPlace
	
echo "Restarting with new data"
ssh -i /home/galkov/.ssh/rsa_key_IQserver galkov@192.168.0.20 << EOF

pgrep java | xargs kill -9
nohup java -jar InterviewQuestions-1.0-SNAPSHOT.jar >> /var/log/IQserver.log &

EOF

echo "New JAR Deployed"
