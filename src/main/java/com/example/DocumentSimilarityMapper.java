package com.example;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.*;

public class DocumentSimilarityMapper extends Mapper<LongWritable, Text, Text, Text> {

    // Store all documents in memory: docID -> set of words
    private Map<String, Set<String>> documents = new LinkedHashMap<>();

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString().trim();
        if (line.isEmpty()) {
            return;
        }

        // Split the line into tokens
        String[] tokens = line.split("\\s+");
        if (tokens.length < 2) {
            return;
        }

        // First token is the document ID
        String docId = tokens[0];

        // Remaining tokens are the words (lowercased, unique)
        Set<String> wordSet = new HashSet<>();
        for (int i = 1; i < tokens.length; i++) {
            wordSet.add(tokens[i].toLowerCase());
        }

        documents.put(docId, wordSet);
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        // Get all document IDs in order
        List<String> docIds = new ArrayList<>(documents.keySet());

        // Generate all pairs and compute Jaccard similarity
        for (int i = 0; i < docIds.size(); i++) {
            for (int j = i + 1; j < docIds.size(); j++) {
                String docA = docIds.get(i);
                String docB = docIds.get(j);

                Set<String> setA = documents.get(docA);
                Set<String> setB = documents.get(docB);

                // Compute intersection
                Set<String> intersection = new HashSet<>(setA);
                intersection.retainAll(setB);

                // Compute union
                Set<String> union = new HashSet<>(setA);
                union.addAll(setB);

                // Compute Jaccard similarity
                double similarity = 0.0;
                if (!union.isEmpty()) {
                    similarity = (double) intersection.size() / union.size();
                }

                // Format similarity to 2 decimal places
                String similarityStr = String.format("%.2f", similarity);

                // Emit: key = "DocA, DocB", value = "Similarity: X.XX"
                context.write(
                    new Text(docA + ", " + docB),
                    new Text("Similarity: " + similarityStr)
                );
            }
        }
    }
}