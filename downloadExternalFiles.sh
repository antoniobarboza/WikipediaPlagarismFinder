#!/bin/bash

echo "This file is going to download the external file automaticly and unzip and place them in the propper project directories"
echo "Begining Download for the DataSet... It is very large so be patient" 
cd ./src/main/java/data
if [ -d "all-enwiki-20170820" ]
then 
	echo "Data exists" 
else
	wget http://trec-car.cs.unh.edu/datareleases/v2.0-2017-08-20/all-enwiki-20170820.tar.xz
	echo "The file zip was downloaded! Beginning to unzip the files..." 
	tar xf all-enwiki-20170820.tar.xz
	echo "Done un-taring the file"
fi

if [ -d "WordNet-3.0" ]
then
	echo "WordNet Dependency installed already!"
else 
	echo "Downloading the WordNet dependency" 
	echo "WordNet is an open source Thesaurus that is used in query expansion!" 
	wget http://wordnetcode.princeton.edu/3.0/WordNet-3.0.tar.gz
	echo "The file zip was downloaded! Beginning to unzip the files..." 
	tar xf WordNet-3.0.tar.gz
fi

echo "Done installing extra packages, Refer to Readme for run instructions!"
