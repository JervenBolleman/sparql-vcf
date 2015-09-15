sparql-vcf
==========

sparql against simple vcf files without loading them into a triple store.

```
git clone https://github.com/JervenBolleman/sparql-vcf
cd sparql-vcf
mvn assembly:assembly
./sparql-vcf.sh src/test/resources/example.vcf "SELECT ?s ?p WHERE {?s <http://biohackathon.org/resource/faldo#position> ?p}"
```

See the issue lists for future tasks..
The only requirements are a maven3 and java8+ installation.
