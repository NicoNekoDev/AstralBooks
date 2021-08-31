if [ -e BuildTools.jar ]
then
	echo "[INFO] Found BuildTools.jar"
else
	echo "[WARNING] Couldn't find BuildTools.jar! It will be downloaded for you."
	wget https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar
fi
echo "[INFO} Installing spigot to maven..."
java -jar BuildTools.jar --rev 1.8 --compile craftbukkit
java -jar BuildTools.jar --rev 1.8.3 --compile craftbukkit
java -jar BuildTools.jar --rev 1.8.8 --compile craftbukkit
java -jar BuildTools.jar --rev 1.9 --compile craftbukkit
java -jar BuildTools.jar --rev 1.9.2 --compile craftbukkit
java -jar BuildTools.jar --rev 1.9.4 --compile craftbukkit
java -jar BuildTools.jar --rev 1.10.2 --compile craftbukkit
java -jar BuildTools.jar --rev 1.11.2 --compile craftbukkit
java -jar BuildTools.jar --rev 1.12.2 --compile craftbukkit
java -jar BuildTools.jar --rev 1.13 --compile craftbukkit
java -jar BuildTools.jar --rev 1.13.2 --compile craftbukkit
java -jar BuildTools.jar --rev 1.14.4 --compile craftbukkit
java -jar BuildTools.jar --rev 1.15.2 --compile craftbukkit
java -jar BuildTools.jar --rev 1.16.1 --compile craftbukkit
java -jar BuildTools.jar --rev 1.16.3 --compile craftbukkit
java -jar BuildTools.jar --rev 1.16.5 --compile craftbukkit