rem jar cvf ..\WebGlore.jar glore\*
rem cd ..
keytool -genkey -alias CA -keystore webglore
keytool -export -keystore webglore -alias CA -file CA.cert
jarsigner -keystore webglore WebGlore.jar WebGlore