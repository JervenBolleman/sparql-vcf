sparql-vcf
==========

sparql against simple vcf files without loading them into a triple store.

```
git clone https://github.com/JervenBolleman/sparql-vcf
cd sparql-vcf
mvn assembly:assembly
java -jar target/sparql-vcf-1.0-SNAPSHOT-jar-with-dependencies.jar -v src/test/resources/example.vcf -q "SELECT ?s ?p WHERE {?s <http://biohackathon.org/resource/faldo#position> ?p}"
```

See the issue lists for future tasks..
The only requirements are a maven3 and java8+ installation.
