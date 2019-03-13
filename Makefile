CLASSDIR = class

OrbitViewer.jar: ${CLASSDIR}/OrbitViewer.class
	cd ${CLASSDIR}; jar cf ../OrbitViewer.jar * astro/*

${CLASSDIR}/OrbitViewer.class: OrbitViewer.java
	-mkdir ${CLASSDIR}
	javac -d ${CLASSDIR} OrbitViewer.java
#	javac -d ${CLASSDIR} -target 1.5 OrbitViewer.java
#	javac -d ${CLASSDIR} -deprecation OrbitViewer.java

clean:
	rm -rf class
	rm -f *.jar *~ astro/*~
