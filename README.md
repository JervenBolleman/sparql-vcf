sparql-vcf
==========

This functionality is better implemented in [jvarkit](http://lindenb.github.io/jvarkit/VcfSparql.html). 

This project might be revived in the future, but remains a demonstration of what is possible and it's performance
is not what we would expect for production code.


sparql against simple vcf files without loading them into a triple store.

```
git clone https://github.com/JervenBolleman/sparql-vcf
cd sparql-vcf
mvn assembly:assembly
java -jar target/sparql-vcf-1.0-SNAPSHOT-jar-with-dependencies.jar -v src/test/resources/example.vcf -q "SELECT ?s ?p WHERE {?s <http://biohackathon.org/resource/faldo#position> ?p}"
```

Or run it as a standalone server

```
java -jar target/sparql-vcf-1.0-SNAPSHOT-jar-with-dependencies.jar -v src/test/resources/example.vcf -p 8090
```
See the issue lists for future tasks..
The only requirements are a maven2 or maven3 and java8+ installation.

