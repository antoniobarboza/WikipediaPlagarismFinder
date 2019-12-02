{\rtf1\ansi\ansicpg1252\cocoartf1671\cocoasubrtf600
{\fonttbl\f0\fswiss\fcharset0 Helvetica;}
{\colortbl;\red255\green255\blue255;}
{\*\expandedcolortbl;;}
\margl1440\margr1440\vieww10800\viewh8400\viewkind0
\pard\tx720\tx1440\tx2160\tx2880\tx3600\tx4320\tx5040\tx5760\tx6480\tx7200\tx7920\tx8640\pardirnatural\partightenfactor0

\f0\fs24 \cf0 # Information Retreval Plagarism Project - Tony and Bobby\
\
This is a readme for the project and will include the full process for downloading \
and running the application and different aspects of it. \
\
INSTALLATION- Clone the repo - https://github.com/antoniobarboza/WikipediaPlagarismFinder.git\
\
**I created a bash script for downloading the dataset and Wordnet automatically..\
./downloadExternalFiles.sh\
\
NOTE!************************************** This only needs to be done manualy if something goes wrong with the bash \
(Skip step 1 and step 2) - if using bash script. \
1. Download the data that is needed for the project to run! \
   http://trec-car.cs.unh.edu/datareleases/v2.0-2017-08-20/all-enwiki-20170820.tar.xz\
   a. unzip the file and place it in the ./src/main/java/data directory\
\
2. Download WordNet-3.0 \
   http://wordnetcode.princeton.edu/3.0/WordNet-3.0.tar.gz\
   a. unzip the file and place it in the ./src/main/java/data directory\
*******************************************\
\
3. mvn clean compile assembly:single\
\
5. INDEXER \
   java -Xmx50g -cp target/IR_program1-0.1-jar-with-dependencies.jar lucene.Indexer\
   \
6. Application-\
   The application has two main driver functions: \
   \
   SEARCHFILES - searchfile.java ( no query expansion )\
   java -Xmx50g -cp target/IR_program1-0.1-jar-with-dependencies.jar lucene.SearchFiles\
   ~This file takes a string at the command line that is the query you are checking for plagarism. (can be a personal paper or     another wikipedia doc!)\
   ~ To run with LSH need to edit boolean on line 63 (lsh) to true\
   \
   SYNOMYMSEARCHER - performs query expansion and calculates plagarism \
   java -Xmx50g -cp target/IR_program1-0.1-jar-with-dependencies.jar lucene.SynonymSearch\
   ~This file takes a string at the command line that is the query you are checking for plagarism. (can be a personal paper or     another wikipedia doc!)\
   \
   TO RUN LHS IMPLEMENTATIONS:\
   MUST RUN POSITIONAL INDEX to run UseLSHOnPosIndex \
   java -Xmx50g -cp target/IR_program1-0.1-jar-with-dependencies.jar lucene.PositionalIndexer\
   \
   LSH-This takes in a String (document content) as an argument, if none given uses a default\
   java -Xmx50g -cp target/IR_program1-0.1-jar-with-dependencies.jar lucene.UseLSHOnPosIndex\
   \
   \
   EXAMPLE QUERIES TO GIVE TO SEARCHER AND SYNONMYM SEARCHER:\
   Really basic example 1: (reminder you can just pass these as an argument at runtime )\
   ******************* Copy Below ****************\
   The ICC Cricket World Cup is the international championship of One Day International (ODI) cricket.  The Second Den.  Non Plagarized\
   ***********************************************\
   Wikipedia Document input example: (reminder you can just pass these as an argument at runtime )\
   ******************* Copy Below ****************\
the icc cricket world cup is the international championship of one day international (odi) cricket. the event is organised by the sport's governing body, the international cricket council (icc), every four years, with preliminary qualification rounds leading up to a finals tournament. the tournament is one of the world's most viewed sporting events and is considered the "flagship event of the international cricket calendar" by the icc. the first world cup was organised in england in june 1975, with the first odi cricket match having been played only four years earlier. however, a separate women's cricket world cup had been held two years before the first men's tournament, and a tournament involving multiple international teams had been held as early as 1912, when a triangular tournament of test matches was played between australia, england and south africa. the first three world cups were held in england. from the 1987 tournament onwards, hosting has been shared between countries under an unofficial rotation system, with fourteen icc members having hosted at least one match in the tournament. the finals of the world cup are contested by the ten full members of the icc (all of which are test-playing teams) and a number of teams made up from associate and affiliate members of the icc, selected via the world cricket league and a later qualifying tournament. a total of twenty teams have competed in the eleven editions of the tournament, with fourteen competing in the latest edition in 2015. australia has won the tournament five times, with the west indies, india (twice each), pakistan and sri lanka (once each) also having won the tournament. the best performance by a non-full-member team came when kenya made the semi-finals of the 2003 tournament.\
******************************************************\
Demonstrating some query expansion on a basic example: (reminder you can just pass these as an argument at runtime ) \
Note- This is the same as the basic example 1 but I swapped the word "World" -> "universe" \
      ~ when you run SearchFiles it will not detect plagarism \
      ~ when you use the Synonym Search it should expand the query and find plagarism\
********************** copy Below *********************\
   The ICC Cricket universe Cup is the international championship of One Day International (ODI) cricket.  The Second Den.  Non Plagarized\
}