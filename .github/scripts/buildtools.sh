#
#     CitizensBooks
#     Copyright (c) 2023 @ Drăghiciu 'NicoNekoDev' Nicolae
#
#     Licensed under the Apache License, Version 2.0 (the "License");
#     you may not use this file except in compliance with the License.
#     You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
#     Unless required by applicable law or agreed to in writing, software
#     distributed under the License is distributed on an "AS IS" BASIS,
#     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#     See the License for the specific language governing permissions and
#     limitations under the License.
#

if [ -e BuildTools.jar ]
then
  echo "[INFO] Found BuildTools.jar"
else
	echo "[WARNING] Couldn't find BuildTools.jar! It will be downloaded for you."
	wget https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar
fi
echo "[INFO] Installing spigot to maven..."
java -jar BuildTools.jar --rev 1.17.1 --remapped
java -jar BuildTools.jar --rev 1.18.1 --remapped
java -jar BuildTools.jar --rev 1.18.2 --remapped
java -jar BuildTools.jar --rev 1.19.2 --remapped
java -jar BuildTools.jar --rev 1.19.3 --remapped
java -jar BuildTools.jar --rev 1.19.4 --remapped
java -jar BuildTools.jar --rev 1.20.1 --remapped
java -jar BuildTools.jar --rev 1.20.2 --remapped
java -jar BuildTools.jar --rev 1.20.4 --remapped