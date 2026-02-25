package com.example;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class DocumentSimilarityReducer extends Reducer<Text, Text, Text, Text> {

    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        // Pass through the similarity value for each document pair
        for (Text val : values) {
            context.write(key, val);
        }
    }
}
