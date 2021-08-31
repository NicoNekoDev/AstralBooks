if [ -e BuildTools.jar ]
then
	echo "[INFO] Found BuildTools.jar"
else
	echo "[WARNING] Couldn't find BuildTools.jar! It will be downloaded for you."
	wget https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar
fi
echo "[INFO} Installing spigot to maven..."
java -jar BuildTools.jar --rev 1.17.1 --compile remapped-mojang