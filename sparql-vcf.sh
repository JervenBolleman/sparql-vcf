#!/bin/bash
vcffile=$1
sparql=$2
java -jar target/sparql-vcf-1.0-SNAPSHOT-jar-with-dependencies.jar $vcffile "$sparql"
