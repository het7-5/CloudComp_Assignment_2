# Assignment 2: Document Similarity using MapReduce

**Name:** 

**Student ID:** 

## Approach and Implementation

### Mapper Design
The `DocumentSimilarityMapper` extends `Mapper<LongWritable, Text, Text, Text>`. Its input key-value pair is `(byte offset, line of text)` from the input file. Each line is parsed as `DocID word1 word2 ...` — the first token becomes the document identifier and the remaining tokens form the document's word set (lowercased and deduplicated using a `HashSet`).

All documents are stored in an in-memory `LinkedHashMap<String, Set<String>>` during the `map()` phase. In the `cleanup()` method, the mapper generates all unique document pairs and computes the **Jaccard Similarity** for each pair:

**Jaccard Similarity = |A ∩ B| / |A ∪ B|**

The mapper emits `(Text key, Text value)` pairs where the key is `"DocA, DocB"` and the value is `"Similarity: X.XX"`.

### Reducer Design
The `DocumentSimilarityReducer` extends `Reducer<Text, Text, Text, Text>`. It acts as a pass-through reducer — its input key-value pair is `(document pair, similarity score)` from the mapper, and it simply writes each pair to the output. Since the mapper computes the final similarity, no aggregation is needed in the reducer.

### Overall Data Flow
1. **Input**: Each line of the input file represents one document: `DocID word1 word2 ...`
2. **Mapper (map phase)**: Parses each line and stores `docID → word set` in memory
3. **Mapper (cleanup phase)**: Generates all document pairs, computes Jaccard similarity using set intersection and union, emits results
4. **Shuffle/Sort**: Hadoop sorts the key-value pairs by the document pair key
5. **Reducer**: Passes through the similarity scores to the output
6. **Output**: Tab-separated `DocA, DocB\tSimilarity: X.XX` for each pair

---

## Setup and Execution

### ` Note: The below commands are the ones used for the Hands-on. You need to edit these commands appropriately towards your Assignment to avoid errors. `

### 1. **Start the Hadoop Cluster**

Run the following command to start the Hadoop cluster:

```bash
docker compose up -d
```

### 2. **Build the Code**

Build the code using Maven:

```bash
mvn clean package
```

### 4. **Copy JAR to Docker Container**

Copy the JAR file to the Hadoop ResourceManager container:

```bash
docker cp target/DocumentSimilarity-0.0.1-SNAPSHOT.jar resourcemanager:/opt/hadoop-3.2.1/share/hadoop/mapreduce/
```

### 5. **Move Dataset to Docker Container**

Copy the dataset to the Hadoop ResourceManager container:

```bash
docker cp shared-folder/input/data/input.txt resourcemanager:/opt/hadoop-3.2.1/share/hadoop/mapreduce/
```

### 6. **Connect to Docker Container**

Access the Hadoop ResourceManager container:

```bash
docker exec -it resourcemanager /bin/bash
```

Navigate to the Hadoop directory:

```bash
cd /opt/hadoop-3.2.1/share/hadoop/mapreduce/
```

### 7. **Set Up HDFS**

Create a folder in HDFS for the input dataset:

```bash
hadoop fs -mkdir -p /input/data
```

Copy the input dataset to the HDFS folder:

```bash
hadoop fs -put ./input.txt /input/data
```

### 8. **Execute the MapReduce Job**

Run your MapReduce job using the following command:

```bash
hadoop jar /opt/hadoop-3.2.1/share/hadoop/mapreduce/DocumentSimilarity-0.0.1-SNAPSHOT.jar com.example.controller.DocumentSimilarityDriver /input/data/input.txt /output1
```

### 9. **View the Output**

To view the output of your MapReduce job, use:

```bash
hadoop fs -cat /output1/*
```

### 10. **Copy Output from HDFS to Local OS**

To copy the output from HDFS to your local machine:

1. Use the following command to copy from HDFS:
    ```bash
    hdfs dfs -get /output1 /opt/hadoop-3.2.1/share/hadoop/mapreduce/
    ```

2. use Docker to copy from the container to your local machine:
   ```bash
   exit 
   ```
    ```bash
    docker cp resourcemanager:/opt/hadoop-3.2.1/share/hadoop/mapreduce/output1/ shared-folder/output/
    ```
3. Commit and push to your repo so that we can able to see your output


---

## Challenges and Solutions

1. **Algorithm Design — Pairwise Comparison**: Computing similarity between all document pairs in a distributed MapReduce framework was the primary challenge. Since MapReduce processes records independently, comparing all pairs required a different strategy. I solved this by using Hadoop's `cleanup()` method in the Mapper to store all documents in memory during the `map()` phase and then generating all pairs and computing Jaccard similarity after all records are processed.

2. **Data Structure Choice**: Using `HashSet<String>` for word storage ensured automatic deduplication and efficient set operations (intersection and union) needed for Jaccard similarity computation. Using `LinkedHashMap` for document storage preserved insertion order, ensuring consistent pair ordering in the output.

3. **Output Format**: Formatting the Jaccard similarity score to exactly 2 decimal places using `String.format("%.2f", similarity)` to match the expected output format.

---
## Sample Input

**Input from `small_dataset.txt`**
```
Document1 This is a sample document containing words
Document2 Another document that also has words
Document3 Sample text with different words
```
## Sample Output

**Output from `small_dataset.txt`**
```
Document1, Document2	Similarity: 0.18
Document1, Document3	Similarity: 0.20
Document2, Document3	Similarity: 0.10
```
## Obtained Output:
```
Document1, Document2	Similarity: 0.18
Document1, Document3	Similarity: 0.20
Document2, Document3	Similarity: 0.10
```
