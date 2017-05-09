call mvnw install:install-file -Dfile=lib/sigar-native-1.0.jar -DgroupId=sigar -DartifactId=sigar-native -Dversion=1.0 -Dpackaging=jar -DcreateChecksum=true
call mvnw install:install-file -Dfile=lib/grinder-3.9.1-patch.jar -DgroupId=grinder -DartifactId=grinder-patch -Dversion=3.9.1-patch -Dpackaging=jar -DcreateChecksum=true
call mvnw install:install-file -Dfile=lib/grinder-core-3.9.1.1.jar -DgroupId=net.sf.grinder -DartifactId=grinder-core -Dversion=3.9.1.1 -Dpackaging=jar -DcreateChecksum=true
call mvnw install:install-file -Dfile=lib/universal-analytics-java-1.0.jar -DgroupId=org.ngrinder -DartifactId=universal-analytics-java -Dversion=1.0 -Dpackaging=jar -DcreateChecksum=true
