package com.genai.tmgenai.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentLoader;
import dev.langchain4j.data.document.DocumentSegment;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.SentenceSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.PineconeEmbeddingStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static dev.langchain4j.data.document.DocumentType.PDF;
import static dev.langchain4j.model.openai.OpenAiModelName.TEXT_EMBEDDING_ADA_002;
import static java.time.Duration.ofSeconds;

@Service
public class FileEmbeddingService {

    public void embedFile(MultipartFile multipartFile,String fileId) throws IOException {

        File file = new File("/Users/amankumar/Downloads"  + fileId + ".pdf");
        multipartFile.transferTo(file);
        DocumentLoader documentLoader = DocumentLoader.from(Paths.get(file.getPath()), PDF);
        Document document = documentLoader.load();


        // Split document into segments (one paragraph per segment)

        DocumentSplitter splitter = new SentenceSplitter();
        List<DocumentSegment> documentSegments = splitter.split(document);


        // Embed segments (convert them into semantic vectors)

        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY")) // https://platform.openai.com/account/api-keys
                .modelName(TEXT_EMBEDDING_ADA_002)
                .timeout(ofSeconds(15))
                .build();

        List<Embedding> embeddings = embeddingModel.embedAll(documentSegments).get();



        // Store embeddings into embedding store for further search / retrieval

        PineconeEmbeddingStore pinecone = PineconeEmbeddingStore.builder()
                .apiKey("1d0899b3-7abf-40be-a267-ac208d572ed3") // https://app.pinecone.io/organizations/xxx/projects/yyy:zzz/keys
                .environment("asia-southeast1-gcp-free")
                .projectName("bca6a53")
                .index("documents") // make sure the dimensions of the Pinecone index match the dimensions of the embedding model (1536 for text-embedding-ada-002)
                .build();

        pinecone.addAll(embeddings, documentSegments);
    }
}
