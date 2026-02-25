package com.example.controller;

import com.example.DocumentSimilarityMapper;
import com.example.DocumentSimilarityReducer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class DocumentSimilarityDriver {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: DocumentSimilarityDriver <input path> <output path>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Document Similarity");

        job.setJarByClass(DocumentSimilarityDriver.class);

        // Set Mapper and Reducer classes
        job.setMapperClass(DocumentSimilarityMapper.class);
        job.setReducerClass(DocumentSimilarityReducer.class);

        // Set output key and value types
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        // Set input and output formats
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        // Set input and output paths
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // Run the job and wait for completion
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
