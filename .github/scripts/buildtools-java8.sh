if [ -e BuildTools.jar ]
then
	echo "[INFO] Found BuildTools.jar"
else
	echo "[WARNING] Couldn't find BuildTools.jar! It will be downloaded for you."
	wget https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar
fi
echo "[INFO] Installing spigot to maven..."
java -jar BuildTools.jar --rev 1.9
java -jar BuildTools.jar --rev 1.9.2
java -jar BuildTools.jar --rev 1.9.4
java -jar BuildTools.jar --rev 1.10.2
java -jar BuildTools.jar --rev 1.11.2
java -jar BuildTools.jar --rev 1.12.2
java -jar BuildTools.jar --rev 1.13
java -jar BuildTools.jar --rev 1.13.2
java -jar BuildTools.jar --rev 1.14.4
java -jar BuildTools.jar --rev 1.15.2
java -jar BuildTools.jar --rev 1.16.1
java -jar BuildTools.jar --rev 1.16.3
java -jar BuildTools.jar --rev 1.16.5