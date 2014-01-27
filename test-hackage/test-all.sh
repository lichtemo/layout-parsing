#!/bin/sh

JSGLR_ORIG=../../jsglr/org.spoofax.jsglr/bin/
JSGLR_LAYOUT=../../jsglr/org.spoofax.jsglr/bin/
NORMALIZE=../haskell-parsetree-normalize/bin
PLUGINS=junit.jar:../org.spoofax.terms_1.0.0.201212201448.jar:strategoxt.jar:../jsglr-layout/javassist.jar

java -Xss16m -Xmx512m -cp bin:$JSGLR_ORIG:$JSGLR_LAYOUT:$NORMALIZE:$PLUGINS org.spoofax.jsglr.tests.haskell.TestAllPackages > testoutput.txt
wget http://moritzsoftware.bplaced.net/haskell_ready.php
