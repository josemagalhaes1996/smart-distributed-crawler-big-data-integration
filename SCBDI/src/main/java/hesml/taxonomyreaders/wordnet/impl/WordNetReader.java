/*
 * Copyright (C) 2016 Universidad Nacional de Educación a Distancia (UNED)
 *
 * This program is free software for non-commercial use:
 * you can redistribute it and/or modify it under the terms of the
 * Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * (CC BY-NC-SA 4.0) as published by the Creative Commons Corporation,
 * either version 4 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * section 5 of the CC BY-NC-SA 4.0 License for more details.
 *
 * You should have received a copy of the Creative Commons
 * Attribution-NonCommercial-ShareAlike 4.0 International (CC BY-NC-SA 4.0) 
 * license along with this program. If not,
 * see <http://creativecommons.org/licenses/by-nc-sa/4.0/>.
 *
 */
package hesml.taxonomyreaders.wordnet.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;
import hesml.taxonomy.*;
import hesml.taxonomyreaders.wordnet.IWordNetDB;
import hesml.taxonomyreaders.wordnet.IWordNetSynset;
import hesmlclient.HESMLclient;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * This class loads a WordNet database and creates its taxonomy
 *
 * @author Juan Lastra-Díaz
 */
public class WordNetReader {

    /**
     * This function drives the WordNet loading process.
     *
     * @param strWordnetDir: Directory containing the WordNet database file
     * @param strWordNetDBFileName: Full name of the WordNet database file.
     * @throws Exception Unexpected error
     */

    public WordNetReader() {
    }

    static IWordNetDB loadDatabase() throws Exception {
        Exception error;      // Error lanzado
        String strError;   // Mensaje de error

        WordNetDB wordnet;    // Returned value

        Scanner reader;         // Text file reader 

        WordNetSynset synset; // Synset readed

        String strEntityLine = "";  // Line with entity information

//         We create the wordnet file
        InputStream resource = WordNetReader.class.getClassLoader().getResourceAsStream("data.noun");

        // We cretae the database
        wordnet = new WordNetDB();
        // We open the file for reading

        reader = new Scanner(resource);
//        System.out.println("Loading DataBase");

        // We read the entioty lines        
        do {
            // We skip the header block

            if (strEntityLine.isEmpty()) {
                strEntityLine = skipWordnetHeader(reader);
            } else {
                strEntityLine = reader.nextLine();
            }

            // Get the next entity in the file
            synset = readEntity(wordnet, strEntityLine, true);

            // We insert the novel entity in the database
            wordnet.addSynset(synset);

        } while (reader.hasNextLine());

        // We close the database
        reader.close();

        // We sort the synsets for the creation of the graph
        wordnet.sortSynsets();

        // We return the result
        return (wordnet);
    }

    /**
     * This function drives the WordNet loading process.
     *
     * @param strWordNetDBFullpath: Full name of the WordNet database file.
     * @param loadGloss This flag indicates if the gloss of the concepts will be
     * loaded.
     * @throws Exception
     */
    static IWordNetDB loadDatabase(
            String strWordNetDBFullpath,
            boolean loadGloss) throws Exception {
        Exception error;      // Error lanzado
        String strError;   // Mensaje de error

        WordNetDB wordnet;    // Returned value

        File wordnetFile;    // WordNet file
        Scanner reader;         // Text file reader 

        WordNetSynset synset; // Synset readed

        String strEntityLine = "";  // Line with entity information

        // We create the wordnet file
        wordnetFile = new File(strWordNetDBFullpath);

        // We chechk the existence of the path
        if (!wordnetFile.exists()) {
            strError = "The WordNet database file doesn´t exist";
            error = new Exception(strError);
            throw (error);
        }

        // We create the database
        wordnet = new WordNetDB();

        // We open the file for reading
        reader = new Scanner(wordnetFile);
        System.out.println("Loading " + wordnetFile);

        // We read the entioty lines
        do {
            // We skip the header block

            if (strEntityLine.isEmpty()) {
                strEntityLine = skipWordnetHeader(reader);
            } else {
                strEntityLine = reader.nextLine();
            }

            // Get the next entity in the file
            synset = readEntity(wordnet, strEntityLine, loadGloss);

            // We insert the novel entity in the database
            wordnet.addSynset(synset);

        } while (reader.hasNextLine());

        // We close the database
        reader.close();

        // We sort the synsets for the creation of the graph
        wordnet.sortSynsets();

        // We return the result
        return (wordnet);
    }

    /**
     * This function checks the right connectivity of the taxonomy according to
     * the WordNet DB.
     *
     * @param wordnet
     * @param taxonomy
     */
    private static void checkWordnetTaxonomy(
            IWordNetDB wordnet,
            ITaxonomy taxonomy) throws Exception {
        Exception error;      // Error thrown
        String strError;   // Error message

        IWordNetSynset vertexSynset;   // Synset associated to the vertex
        IWordNetSynset childSynset;    //Child synset

        // Debugging message
        System.out.println("Checking the topological consistency ...");

        // We check all the children and parents of the concept
        for (IVertex vertex : taxonomy.getVertexes()) {
            // We get the vertex synset

            System.out.println("Vertex = " + vertex.getID());

            vertexSynset = wordnet.getSynset(vertex.getID());

            // We check all the children
            for (IVertex child : vertex.getChildren()) {
                // We get the child synset in WordNet

                childSynset = wordnet.getSynset(child.getID());

                // We check that the vertexSynset is parent
                if (!childSynset.isParent(vertexSynset.getID())) {
                    strError = "The taxonomy is wrong";
                    error = new Exception(strError);
                    throw (error);
                }
            }
        }
    }

    /**
     * This function reads the header block of the WordNet file.
     *
     * @param reader
     */
    public static String skipWordnetHeader(
            Scanner reader) {
        String strLine = "";    // Readed line

        StringTokenizer tokenizer;  // Tokenizer

        // We go forward until we find an entity line starting with a
        // 8-digit integer identifier
        while (reader.hasNextLine()) {
            // We get the next line

            strLine = reader.nextLine();

            // We segmentate the line
            tokenizer = new StringTokenizer(strLine);

            if (tokenizer.nextToken().length() == 8) {
                break;
            }
        }

        // We return the result
        return (strLine);
    }

    /**
     * This function reads one line with the Entity information and creates one
     * synset for it.
     */
    public static WordNetSynset readEntity(
            WordNetDB wordnet,
            String strEntityLine,
            boolean loadGloss) {
        WordNetSynset synset; // Returned value

        StringTokenizer tokenizer;  // Tokenizer to get the fields

        String[] strParts;       // Tokens and gloss
        String strGloss = "";  // Gloss describing the synset
        Integer[] parentsId;      // Id of the parents

        ArrayList<String> strTokens;      // List of tokens
        ArrayList<Integer> parentsIdtemp;  // Id of the parents

        Integer synsetId;       // Synset id
        Integer wordCount;      // Number of words

        int iWord, iToken;  // Counters

        String[] strWords;   // Words associated to the synset

        // We parse the entity line with the tokenizer
        tokenizer = new StringTokenizer(strEntityLine);

        // We create the temporary list of tokems
        strTokens = new ArrayList<>();

        // Now we parse and store the tokens
        while (tokenizer.hasMoreTokens()) {
            strTokens.add(tokenizer.nextToken());
        }

        // Obtengo el id sel synset
        synsetId = Integer.parseUnsignedInt(strTokens.get(0));

        // We get the number of words associated to this synset
        wordCount = Integer.parseUnsignedInt(strTokens.get(3), 16);

        // We create the vector of words for the synset
        strWords = new String[wordCount];

        // We read the words for the synset
        for (iWord = 0, iToken = 4; iWord < wordCount; iWord++, iToken += 2) {
            strWords[iWord] = strTokens.get(iToken);
        }

        // We get the number of parents for the synset
        for (iToken++, parentsIdtemp = new ArrayList<>();
                iToken < strTokens.size();
                iToken += 2) {
            if (strTokens.get(iToken).startsWith("@")) {
                parentsIdtemp.add(Integer.parseUnsignedInt(strTokens.get(iToken + 1)));
            }
        }

        // We copy the parents Id and clear the temporary list
        parentsId = new Integer[parentsIdtemp.size()];

        parentsIdtemp.toArray(parentsId);

        // We read the gloss
        if (loadGloss) {
            strGloss = readGloss(strTokens);
        }

        // We create the synset
        synset = new WordNetSynset(wordnet, synsetId,
                strTokens.get(2).charAt(0),
                strWords, parentsId, strGloss);

        // We release the resources
        parentsIdtemp.clear();
        strTokens.clear();

        // We return the result
        return (synset);
    }

    /**
     * THis function extracts the gloss associated to the synset.
     *
     * @param strTokens
     * @return Synset gloss
     */
    private static String readGloss(
            ArrayList<String> strTokens) {
        String strGloss = "";   // Returned value

        List<String> strGlossTokens; // Gloss tokens

        int indexOfGlossSep;    // Position of the gloss separator char '|' 

        // We find the position of the gloss separator
        indexOfGlossSep = strTokens.indexOf("|");

        // We remove all the tokens until the separator
        strGlossTokens = strTokens.subList(indexOfGlossSep + 1, strTokens.size() - 1);

        // We copy the gloss tokens
        for (String strToken : strGlossTokens) {
            strGloss += (strToken + " ");
        }

        // Liberamos la lista de tokens
        strGlossTokens.clear();

        // We return the result
        return (strGloss);
    }
}
