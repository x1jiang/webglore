cd "E:\MyProgram\JavaWorkspace\WebGlore\bin"
E:
rem jar cvf ..\Procedure2Applet.jar glore\*
rem cd ..
rem keytool -genkey -alias Procedure2Applet -keystore jwcapplet
rem keytool -export -keystore jwcapplet -alias Procedure2Applet -file Procedure2Applet.cert
rem jarsigner -keystore jwcapplet Procedure2Applet.jar Procedure2Applet

jar cvf ..\ChooseFileApplet.jar glore\*
cd ..
 keytool -genkey -alias ChooseFileApplet -keystore jwcapplet
keytool -export -keystore jwcapplet -alias ChooseFileApplet -file ChooseFileApplet.cert
jarsigner -keystore jwcapplet ChooseFileApplet.jar ChooseFileApplet